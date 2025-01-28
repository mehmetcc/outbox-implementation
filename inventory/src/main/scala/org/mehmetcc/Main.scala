package org.mehmetcc

import org.mehmetcc.event.{Consumer, ConsumerImpl}
import org.mehmetcc.infrastructure.Configuration
import org.mehmetcc.infrastructure.QuillContext.QuillContext
import org.mehmetcc.product.{RepositoryImpl, Server, ServerImpl}
import zio._
import zio.logging.LogFormat
import zio.logging.backend.SLF4J

object Main extends ZIOAppDefault {
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = SLF4J.slf4j(LogFormat.default)

  private val program = for {
    // TODO add sink in the future
    _    <- Consumer.consume.forkDaemon
    code <- Server.serve
  } yield code

  override def run: Task[ExitCode] = program.provide(
    Configuration.live,
    ServerImpl.live,
    RepositoryImpl.live,
    ConsumerImpl.live,
    QuillContext
  )
}
