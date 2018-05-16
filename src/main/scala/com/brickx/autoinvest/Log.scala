package com.brickx
package autoinvest

import std._

import org.slf4j.{ Logger, LoggerFactory }
import cats.effect.Sync

trait Log[F[_]] {
  def log(le: Log.LogEvent): F[Unit]
  def error(error: Error): F[Unit]
  def warn(msg: String, args: Any*): F[Unit]
  def info(msg: String, args: Any*): F[Unit]
  def debug(msg: String, args: Any*): F[Unit]
}

object Log {

  sealed trait Level
  object Level {
    case object Warn  extends Level
    case object Info  extends Level
    case object Debug extends Level
  }

  sealed trait LogEvent
  object LogEvent {

    final case class ErrorLogEvent(error: Error) extends LogEvent
    final case class LevelLogEvent(level: Level, msg: String, args: List[Any]) extends LogEvent

    def error(error: Error): LogEvent = ErrorLogEvent(error)
    def warn(msg: String, args: Any*): LogEvent = LevelLogEvent(Level.Warn, msg, args.toList)
    def info(msg: String, args: Any*): LogEvent = LevelLogEvent(Level.Info, msg, args.toList)
    def debug(msg: String, args: Any*): LogEvent = LevelLogEvent(Level.Debug, msg, args.toList)
  }

  def default[F[_]: Sync]: Log[F] = new Slf4jLog

  private class Slf4jLog[F[_]](implicit F: Sync[F]) extends Log[F] {
    val logger: Logger = LoggerFactory.getLogger("com.brickx.autoinvest")

    override def log(le: LogEvent): F[Unit] = le match {
      case LogEvent.ErrorLogEvent(err) => error(err)
      case LogEvent.LevelLogEvent(level, msg, args) => level match {
        case Level.Warn => warn(msg, args:_*)
        case Level.Info => info(msg, args:_*)
        case Level.Debug => debug(msg, args:_*)
      }
    }

    override def error(error: Error): F[Unit] =
      F.delay(error match {
        case err => logger.error(err.toString)
      })

    override def warn(msg: String, args: Any*): F[Unit] =
      F.delay(logger.warn(msg, args.toArray))

    override def info(msg: String, args: Any*): F[Unit] =
      F.delay(logger.info(msg, args.toArray))

    override def debug(msg: String, args: Any*): F[Unit] =
      F.delay(logger.debug(msg, args.toArray))

  }

}
