package org.mehmetcc

import org.mehmetcc.infrastructure.Configuration
import org.mehmetcc.infrastructure.QuillContext.QuillContext
import org.mehmetcc.product.{RepositoryImpl, Server, ServerImpl}
import zio._
import zio.logging.LogFormat
import zio.logging.backend.SLF4J

object Main extends ZIOAppDefault {
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = SLF4J.slf4j(LogFormat.default)

  override def run: Task[ExitCode] = Server.serve.provide(
    Configuration.live,
    ServerImpl.live,
    RepositoryImpl.live,
    QuillContext
  )
}
