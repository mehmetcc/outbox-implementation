package org.mehmetcc.product

import org.mehmetcc.infrastructure.Configuration
import sttp.tapir.generic.auto._
import sttp.tapir.json.zio.jsonBody
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
  val live: URLayer[Repository with Configuration, ServerImpl] = ZLayer {
    for {
      configuration <- ZIO.service[Configuration]
      port           = configuration.application.port
      repository    <- ZIO.service[Repository]
      endpoints      = Endpoints(repository)
    } yield ServerImpl(port, endpoints)
  }
}

final case class Endpoints(repository: Repository) {
  import Product._
  // Health endpoint
  val health: ZServerEndpoint[Any, Any] =
    endpoint.get.in("health").serverLogicSuccess(_ => ZIO.unit)

  // Create endpoint
  val create: ZServerEndpoint[Any, Any] =
    endpoint.post
      .in("products")
      .in(jsonBody[Product])
      .out(stringBody)
      .errorOut(stringBody)
      .zServerLogic { product =>
        repository.create(product).mapError(_.getMessage)
      }

  // Read endpoint
  val read: ZServerEndpoint[Any, Any] =
    endpoint.get
      .in("products" / path[String]("sku"))
      .out(jsonBody[Option[Product]])
      .errorOut(stringBody)
      .zServerLogic { sku =>
        repository.read(sku).mapError(_.getMessage)
      }

  // Update endpoint
  val update: ZServerEndpoint[Any, Any] =
    endpoint.put
      .in("products" / path[String]("sku"))
      .in(jsonBody[Product])
      .out(jsonBody[Int])
      .errorOut(stringBody)
      .zServerLogic { case (sku, updatedProduct) =>
        repository.update(sku, updatedProduct).mapError(_.getMessage)
      }

  // Delete endpoint
  val delete: ZServerEndpoint[Any, Any] =
    endpoint.delete
      .in("products" / path[String]("sku"))
      .out(jsonBody[Int])
      .errorOut(stringBody)
      .zServerLogic { sku =>
        repository.delete(sku).mapError(_.getMessage)
      }

  val api: List[ZServerEndpoint[Any, Any]] = List(health, create, read, update, delete)

  // Swagger
  val documentation: List[ZServerEndpoint[Any, Any]] =
    SwaggerInterpreter()
      .fromServerEndpoints[Task](api, "Inventory API", "1.0.0")

  // Prometheus
  val prometheus: PrometheusMetrics[Task] = PrometheusMetrics.default[Task]()
  val metrics: ZServerEndpoint[Any, Any]  = prometheus.metricsEndpoint

  val all: List[ZServerEndpoint[Any, Any]] = api ++ documentation ++ List(metrics)
}
