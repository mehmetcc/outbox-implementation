package org.mehmetcc.infrastructure

import zio._
import zio.config.magnolia.deriveConfig
import zio.config.typesafe.TypesafeConfigProvider

case class Configuration(application: ApplicationConfiguration, kafka: KafkaConfiguration)

case class ApplicationConfiguration(port: Int)

case class KafkaConfiguration(bootstrapServers: List[String], groupId: String, topics: List[String])

object Configuration {
  private val applicationConfig: Config[ApplicationConfiguration] =
    deriveConfig[ApplicationConfiguration].nested("application-configuration")

  private val kafkaConfig: Config[KafkaConfiguration] =
    deriveConfig[KafkaConfiguration].nested("kafka-configuration")

  private val combinedConfig: Config[Configuration] =
    (applicationConfig ++ kafkaConfig).map { case (app, kafka) =>
      Configuration(app, kafka)
    }

  val live: Layer[Config.Error, Configuration] =
    ZLayer.fromZIO(
      ZIO
        .config[Configuration](combinedConfig)
        .withConfigProvider(TypesafeConfigProvider.fromResourcePath())
        .tap { config =>
          ZIO.logInfo(s"""
                         |Combined configuration:
                         |Application Configuration:
                         |  port: ${config.application.port}
                         |Kafka Configuration:
                         |  bootstrapServers: ${config.kafka.bootstrapServers.mkString(", ")}
                         |  groupId: ${config.kafka.groupId}
                         |  topics: ${config.kafka.topics.mkString(", ")}
                         |""".stripMargin)
        }
    )
}
