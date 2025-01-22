package org.mehmetcc

import zio._
import zio.config.magnolia.deriveConfig
import zio.config.typesafe.TypesafeConfigProvider

case class Configuration(port: Int)

object Configuration {
  private val config: Config[Configuration] = deriveConfig[Configuration].nested("application")

  val live: Layer[Config.Error, Configuration] =
    ZLayer.fromZIO(
      ZIO
        .config[Configuration](config)
        .withConfigProvider(TypesafeConfigProvider.fromResourcePath())
        .tap { config =>
          ZIO.logInfo(s"""
                         |k-tail server configuration:
                         |port: ${config.port}
                         |""".stripMargin)
        }
    )
}
