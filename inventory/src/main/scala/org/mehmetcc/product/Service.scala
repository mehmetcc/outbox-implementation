package org.mehmetcc.product

import zio._

import java.sql.SQLException

trait Service {
  def create(product: Product): IO[Throwable, String]

  def get(sku: String): IO[SQLException, Option[Product]]

  def update(sku: String, updatedProduct: Product): IO[SQLException, Int]

  def delete(sku: String): IO[SQLException, Int]

  def isAvailable(sku: String): IO[SQLException, Boolean]
}

object Service {
  def create(product: Product): ZIO[Service, Throwable, String] =
    ZIO.serviceWithZIO[Service](_.create(product))

  def get(sku: String): ZIO[Service, SQLException, Option[Product]] =
    ZIO.serviceWithZIO[Service](_.get(sku))

  def update(sku: String, updatedProduct: Product): ZIO[Service, SQLException, Int] =
    ZIO.serviceWithZIO[Service](_.update(sku, updatedProduct))

  def delete(sku: String): ZIO[Service, SQLException, Int] =
    ZIO.serviceWithZIO[Service](_.delete(sku))

  def isAvailable(sku: String): ZIO[Service, SQLException, Boolean] =
    ZIO.serviceWithZIO[Service](_.isAvailable(sku))
}

final case class ServiceImpl(repository: Repository) extends Service {
  override def create(product: Product): IO[Throwable, String] =
    repository.create(product)

  override def get(sku: String): IO[SQLException, Option[Product]] =
    repository.read(sku)

  override def update(sku: String, updatedProduct: Product): IO[SQLException, Int] =
    repository.update(sku, updatedProduct)

  override def delete(sku: String): IO[SQLException, Int] =
    repository.delete(sku)

  override def isAvailable(sku: String): IO[SQLException, Boolean] =
    repository.isAvailable(sku)
}

object ServiceImpl {
  val live: ZLayer[Repository, Nothing, Service] = ZLayer.fromFunction(ServiceImpl(_))
}
