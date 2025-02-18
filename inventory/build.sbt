val TapirVersion          = "1.11.13"
val ZioLoggingVersion     = "2.4.0"
val ZioTestVersion        = "2.1.14"
val ZioConfigVersion      = "4.0.3"
val QuillVersion          = "4.8.5"
val TestContainersVersion = "0.41.8"
val ZioKafkaVersion       = "2.10.0"

lazy val rootProject = (project in file(".")).settings(
  Seq(
    name         := "inventory",
    version      := "0.1.0-SNAPSHOT",
    organization := "org.mehmetcc",
    scalaVersion := "2.13.14",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir"   %% "tapir-zio-http-server"           % TapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-prometheus-metrics"        % TapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui-bundle"         % TapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-json-zio"                  % TapirVersion,
      "ch.qos.logback"                 % "logback-classic"                 % "1.5.16",
      "dev.zio"                       %% "zio-logging"                     % ZioLoggingVersion,
      "dev.zio"                       %% "zio-logging-slf4j"               % ZioLoggingVersion,
      "dev.zio"                       %% "zio-config"                      % ZioConfigVersion,
      "dev.zio"                       %% "zio-config-typesafe"             % ZioConfigVersion,
      "dev.zio"                       %% "zio-config-magnolia"             % ZioConfigVersion,
      "dev.zio"                       %% "zio-kafka"                       % ZioKafkaVersion,
      "io.getquill"                   %% "quill-zio"                       % QuillVersion,
      "io.getquill"                   %% "quill-jdbc-zio"                  % QuillVersion,
      "org.postgresql"                 % "postgresql"                      % "42.7.5",
      "com.softwaremill.sttp.tapir"   %% "tapir-sttp-stub-server"          % TapirVersion          % Test,
      "dev.zio"                       %% "zio-kafka-testkit"               % ZioKafkaVersion       % Test,
      "dev.zio"                       %% "zio-test"                        % ZioTestVersion        % Test,
      "dev.zio"                       %% "zio-test-sbt"                    % ZioTestVersion        % Test,
      "com.softwaremill.sttp.client3" %% "zio-json"                        % "3.10.2"              % Test,
      "com.dimafeng"                  %% "testcontainers-scala-postgresql" % TestContainersVersion % Test,
      "com.dimafeng"                  %% "testcontainers-scala-scalatest"  % TestContainersVersion % Test
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
)

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

dockerBaseImage := "eclipse-temurin:19-jdk" // Base Docker image