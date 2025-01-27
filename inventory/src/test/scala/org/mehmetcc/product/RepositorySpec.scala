package org.mehmetcc.product

import com.zaxxer.hikari.HikariDataSource
import io.getquill.jdbczio.Quill
import io.getquill.{SnakeCase, Update}
import org.testcontainers.containers.PostgreSQLContainer
import zio._
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._

import java.time.LocalDateTime
import java.util.UUID
import javax.sql.DataSource

object RepositorySpec extends ZIOSpecDefault {
  val postgresContainer: ZLayer[Any, Nothing, PostgreSQLContainer[Nothing]] = ZLayer.scoped {
    ZIO.acquireRelease {
      ZIO.attempt {
        val container = new PostgreSQLContainer("postgres:13")
        container.start()
        container
      }.orDie
    } { container =>
      ZIO.attempt(container.stop()).orDie
    }
  }

  val createTable: ZIO[Quill.Postgres[SnakeCase], Throwable, Long] =
    ZIO.serviceWithZIO[Quill.Postgres[SnakeCase]] { ctx =>
      import ctx._
      ctx.run(
        sql"""
        CREATE TABLE IF NOT EXISTS product (
          sku TEXT PRIMARY KEY,
          initial_stock INT NOT NULL,
          current_stock INT NOT NULL,
          price DOUBLE PRECISION NOT NULL,
          is_available BOOLEAN NOT NULL,
          created_at TIMESTAMP NOT NULL,
          updated_at TIMESTAMP NOT NULL
        )
      """.as[Update[Long]]
      )
    }

  val dataSourceLayer: ZLayer[PostgreSQLContainer[Nothing], Throwable, DataSource] = ZLayer.fromZIO {
    for {
      container <- ZIO.service[PostgreSQLContainer[Nothing]]
      dataSource = {
        val ds = new HikariDataSource()
        ds.setJdbcUrl(container.getJdbcUrl)
        ds.setUsername(container.getUsername)
        ds.setPassword(container.getPassword)
        ds
      }
    } yield dataSource
  }

  val quillLayer: ZLayer[DataSource, Nothing, Quill.Postgres[SnakeCase]] =
    Quill.Postgres.fromNamingStrategy(SnakeCase)

  val quillContextLayer: ZLayer[PostgreSQLContainer[Nothing], Throwable, Quill.Postgres[SnakeCase]] =
    dataSourceLayer >>> quillLayer

  val repositoryLayer: ZLayer[Any, Throwable, Repository] =
    postgresContainer >>> quillContextLayer >>> RepositoryImpl.live

  def createTestProduct(
    initialStock: Int = 100,
    currentStock: Int = 100,
    price: Double = 49.99,
    isAvailable: Boolean = true
  ): Product = Product(
    sku = UUID.randomUUID().toString,
    initialStock = initialStock,
    currentStock = currentStock,
    price = price,
    isAvailable = isAvailable,
    createdAt = LocalDateTime.now(),
    updatedAt = LocalDateTime.now()
  )

  override def spec = suite("RepositorySpec")(
    test("create a product") {
      val testProduct = createTestProduct()
      for {
        _       <- createTable
        _       <- Repository.create(testProduct)
        product <- Repository.read(testProduct.sku)
      } yield assertTrue(product.get == testProduct)
    },
    test("read a product") {
      val testProduct = createTestProduct()
      for {
        _       <- Repository.create(testProduct)
        product <- Repository.read(testProduct.sku)
      } yield assertTrue(product.get == testProduct)
    },
    test("update a product") {
      val testProduct = createTestProduct()
      val updatedProduct = testProduct.copy(
        currentStock = 50,
        price = 39.99,
        isAvailable = false
      )
      for {
        _       <- Repository.create(testProduct)
        _       <- Repository.update(testProduct.sku, updatedProduct)
        product <- Repository.read(testProduct.sku)
      } yield assertTrue(
        product.get.sku == updatedProduct.sku,
        product.get.currentStock == updatedProduct.currentStock,
        product.get.price == updatedProduct.price,
        product.get.isAvailable == updatedProduct.isAvailable
      )
    },
    test("delete a product") {
      val testProduct = createTestProduct()
      for {
        _       <- Repository.create(testProduct)
        _       <- Repository.delete(testProduct.sku)
        product <- Repository.read(testProduct.sku)
      } yield assert(product)(isNone)
    },
    test("read a non-existent product") {
      val nonExistentSku = UUID.randomUUID().toString
      for {
        product <- Repository.read(nonExistentSku)
      } yield assert(product)(isNone)
    },
    test("update a non-existent product") {
      val nonExistentSku = UUID.randomUUID().toString
      val testProduct    = createTestProduct()
      for {
        result <- Repository.update(nonExistentSku, testProduct)
      } yield assertTrue(result == 0) // Expect 0 rows to be updated
    },
    test("delete a non-existent product") {
      val nonExistentSku = UUID.randomUUID().toString
      for {
        result <- Repository.delete(nonExistentSku)
      } yield assertTrue(result == 0) // Expect 0 rows to be deleted
    }
  )
    .provideShared(postgresContainer, dataSourceLayer, quillLayer, RepositoryImpl.live) @@ sequential
}
