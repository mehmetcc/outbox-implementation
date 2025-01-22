val TapirVersion      = "1.11.13"
val ZioLoggingVersion = "2.4.0"
val ZioTestVersion    = "2.1.14"
val ZioConfigVersion  = "4.0.3"

lazy val rootProject = (project in file(".")).settings(
  Seq(
    name         := "inventory",
    version      := "0.1.0-SNAPSHOT",
    organization := "org.mehmetcc",
    scalaVersion := "2.13.14",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir"   %% "tapir-zio-http-server"    % TapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-prometheus-metrics" % TapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui-bundle"  % TapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-json-zio"           % TapirVersion,
      "ch.qos.logback"                 % "logback-classic"          % "1.5.16",
      "dev.zio"                       %% "zio-logging"              % ZioLoggingVersion,
      "dev.zio"                       %% "zio-logging-slf4j"        % ZioLoggingVersion,
      "dev.zio"                       %% "zio-config"               % ZioConfigVersion,
      "dev.zio"                       %% "zio-config-typesafe"      % ZioConfigVersion,
      "dev.zio"                       %% "zio-config-magnolia"      % ZioConfigVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-sttp-stub-server"   % TapirVersion   % Test,
      "dev.zio"                       %% "zio-test"                 % ZioTestVersion % Test,
      "dev.zio"                       %% "zio-test-sbt"             % ZioTestVersion % Test,
      "com.softwaremill.sttp.client3" %% "zio-json"                 % "3.10.2"       % Test
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
)
