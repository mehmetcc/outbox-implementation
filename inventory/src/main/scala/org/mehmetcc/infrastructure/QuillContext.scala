package org.mehmetcc.infrastructure

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.ZLayer

import javax.sql.DataSource

object QuillContext {
  private val PostgresLayer: ZLayer[DataSource, Nothing, Quill.Postgres[SnakeCase.type]] =
    Quill.Postgres.fromNamingStrategy(SnakeCase)

  private val DataSourceLayer: ZLayer[Any, Throwable, DataSource] =
    Quill.DataSource.fromPrefix("database-configuration")

  val QuillContext: ZLayer[Any, Throwable, Quill.Postgres[SnakeCase.type] with DataSource] =
    ZLayer.make[Quill.Postgres[SnakeCase.type] with DataSource](PostgresLayer, DataSourceLayer)
}
