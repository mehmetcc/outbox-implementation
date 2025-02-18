package org.mehmetcc.product

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio._

import java.sql.SQLException
import java.time.LocalDateTime

trait Repository {
  def create(product: Product): IO[Throwable, String]

  def read(sku: String): IO[SQLException, Option[Product]]

  def update(sku: String, updatedProduct: Product): IO[SQLException, Int]

  def delete(sku: String): IO[SQLException, Int]

  def isAvailable(sku: String): IO[SQLException, Boolean]

  def isInStock(sku: String, amount: Int): IO[SQLException, Boolean]
}

object Repository {
  def create(product: Product): ZIO[Repository, Throwable, String] =
    ZIO.serviceWithZIO[Repository](_.create(product))

  def read(sku: String): ZIO[Repository, SQLException, Option[Product]] =
    ZIO.serviceWithZIO[Repository](_.read(sku))

  def update(sku: String, updatedProduct: Product): ZIO[Repository, SQLException, Int] =
    ZIO.serviceWithZIO[Repository](_.update(sku, updatedProduct))

  def delete(sku: String): ZIO[Repository, SQLException, Int] =
    ZIO.serviceWithZIO[Repository](_.delete(sku))

  def isAvailable(sku: String): ZIO[Repository, SQLException, Boolean] =
    ZIO.serviceWithZIO[Repository](_.isAvailable(sku))

  def isAvailable(sku: String, amount: Int): ZIO[Repository, SQLException, Boolean] =
    ZIO.serviceWithZIO[Repository](_.isInStock(sku, amount))
}

final case class RepositoryImpl(source: Quill.Postgres[SnakeCase]) extends Repository {
  import source._

  override def create(product: Product): IO[SQLException, String] = run {
    quote {
      query[Product].insertValue(lift(product))
    }
  }.as(product.sku)

  override def read(sku: String): IO[SQLException, Option[Product]] = run {
    quote {
      query[Product].filter(_.sku == lift(sku))
    }
  }.map(_.headOption)

  override def update(sku: String, updatedProduct: Product): IO[SQLException, Int] = run {
    quote {
      query[Product]
        .filter(_.sku == lift(sku))
        .updateValue(lift(updatedProduct.copy(updatedAt = LocalDateTime.now())))
    }
  }.map(_.toInt)

  override def delete(sku: String): IO[SQLException, Int] = run {
    quote {
      query[Product].filter(_.sku == lift(sku)).delete
    }
  }.map(_.toInt)

  override def isAvailable(sku: String): IO[SQLException, Boolean] = run {
    query[Product]
      .filter(_.sku == lift(sku))
      .map(_.isAvailable)
  }.map(_.headOption.getOrElse(false))

  override def isInStock(sku: String, amount: Index): IO[SQLException, Boolean] = run {
    query[Product]
      .filter(_.sku == lift(sku))
      .filter(_.currentStock >= lift(amount))
      .nonEmpty
  }
}

object RepositoryImpl {
  val live: URLayer[Quill.Postgres[SnakeCase], RepositoryImpl] = ZLayer.fromFunction(RepositoryImpl(_))
}
