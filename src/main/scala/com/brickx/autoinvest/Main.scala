package com.brickx
package autoinvest

import std._
import scalaz.ioeffect.{ SafeApp, Task }
import scalaz.ioeffect.catz._
import Config.AppConfig
import org.http4s.client.blaze.Http1Client
import java.time.Clock

object Main extends SafeApp {
  type Error = std.Error

  implicit val clock = Clock.systemDefaultZone()

  override def run(args: List[String]): IO[Error, Unit] =
    // TODO figure out a better way to deal with `Error` vs `Throwable`
    bootstrap.leftMap(Error.fromThrowable)

  // TODO acquire server, httpClient and events using bracket to perform proper resource cleanup 
  def bootstrap: Task[Unit] =
    for {
      config     <- loadConfigIO
      _          <- Migrations.default(config.db).migrate
      _          <- Server.default(config.server).start
      events     = Events.default(config.events)
      httpClient <- Http1Client()
      trading    = Trading.default(httpClient, config.trading.endpoint)
      xa         = Db.defaultTransactor[Task](config.db)
      db         = Db.default(xa)
      _          <- Program.default(events, trading, db, computeAutoInvest).processEvents
    } yield ()

  def loadConfigIO: Task[AppConfig] =
    IO.absolve(IO.sync(Config.loadConfig))

  val computeAutoInvest: Program.ComputeAutoInvest = { _ =>
    Result.error(Error.message("Not implemented"))
  }
}
