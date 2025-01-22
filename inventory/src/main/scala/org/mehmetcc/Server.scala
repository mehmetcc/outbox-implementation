package org.mehmetcc

import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir._
import zio._
import zio.http.{Server => ZioHttpServer}

trait Server {
  val serve: Task[ExitCode]
}

object Server {
  val serve: RIO[Server, ExitCode] = ZIO.serviceWithZIO[Server](_.serve)
}

final case class ServerImpl(port: Int, endpoints: Endpoints) extends Server {
  override val serve: Task[ExitCode] =
    ZioHttpServer
      .serve(ZioHttpInterpreter(serverOptions).toHttp(endpoints.all))
      .provide(ZioHttpServer.defaultWithPort(port))
      .exitCode

  private def serverOptions: ZioHttpServerOptions[Any] = ZioHttpServerOptions.customiseInterceptors
    .metricsInterceptor(endpoints.prometheus.metricsInterceptor())
    .options
}

object ServerImpl {
  val live: URLayer[Configuration, Server] = ZLayer {
    for {
      configuration <- ZIO.service[Configuration]
      endpoints      = Endpoints()
    } yield ServerImpl(configuration.port, endpoints)
  }
}

sealed case class Endpoints() {
  val health: ZServerEndpoint[Any, Any] = endpoint.get.in("health").serverLogicSuccess(_ => ZIO.unit)

  val api: List[ZServerEndpoint[Any, Any]] = List(health)

  val documentation: List[ZServerEndpoint[Any, Any]] = SwaggerInterpreter()
    .fromServerEndpoints[Task](api, "inventory", "1.0.0")

  val prometheus: PrometheusMetrics[Task] = PrometheusMetrics.default[Task]()

  val metrics: ZServerEndpoint[Any, Any] = prometheus.metricsEndpoint

  val all: List[ZServerEndpoint[Any, Any]] = api ++ documentation ++ List(metrics)
}
