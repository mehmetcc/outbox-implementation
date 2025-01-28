package org.mehmetcc.infrastructure

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.{TaskLayer, URLayer, ZLayer}

import javax.sql.DataSource

object QuillContext {
  private val PostgresLayer: URLayer[DataSource, Quill.Postgres[SnakeCase.type]] =
    Quill.Postgres.fromNamingStrategy(SnakeCase)

  private val DataSourceLayer: TaskLayer[DataSource] =
    Quill.DataSource.fromPrefix("database-configuration")

  val QuillContext: ZLayer[Any, Throwable, Quill.Postgres[SnakeCase.type] with DataSource] =
    ZLayer.make[Quill.Postgres[SnakeCase.type] with DataSource](PostgresLayer, DataSourceLayer)
}
