package com.brickx
package autoinvest

import std._, Z._

import java.lang.Throwable
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

    final case class ErrorLogEvent(msg: String, throwable: Maybe[Throwable])   extends LogEvent
    final case class LevelLogEvent(level: Level, msg: String, args: List[Any]) extends LogEvent

    def error(error: Error): LogEvent            = errorToLog(error)
    def warn(msg: String, args: Any*): LogEvent  = LevelLogEvent(Level.Warn, msg, args.toList)
    def info(msg: String, args: Any*): LogEvent  = LevelLogEvent(Level.Info, msg, args.toList)
    def debug(msg: String, args: Any*): LogEvent = LevelLogEvent(Level.Debug, msg, args.toList)
  }

  def default[F[_]: Sync]: Log[F] = new Slf4jLog

  private class Slf4jLog[F[_]](implicit F: Sync[F]) extends Log[F] {
    val logger: Logger = LoggerFactory.getLogger("com.brickx.autoinvest")

    override def log(le: LogEvent): F[Unit] = le match {
      case LogEvent.ErrorLogEvent(m, t) =>
        F.delay(t.cata(logger.error(m, _), logger.error(m)))
      case LogEvent.LevelLogEvent(level, msg, args) =>
        level match {
          case Level.Warn  => warn(msg, args: _*)
          case Level.Info  => info(msg, args: _*)
          case Level.Debug => debug(msg, args: _*)
        }
    }

    override def error(error: Error): F[Unit] =
      log(errorToLog(error))

    override def warn(msg: String, args: Any*): F[Unit] =
      F.delay(logger.warn(msg, args.toArray))

    override def info(msg: String, args: Any*): F[Unit] =
      F.delay(logger.info(msg, args.toArray))

    override def debug(msg: String, args: Any*): F[Unit] =
      F.delay(logger.debug(msg, args.toArray))
  }

  private def errorToLog(error: Error): LogEvent.ErrorLogEvent =
    LogEvent.ErrorLogEvent.tupled(error match {
      case ConfigError(failures) =>
        val fs = failures.toList.mkString("\n", "\n", "\n")
        ("Cannot load configuration:" + fs, Maybe.empty)
      case MessageError(msg) =>
        (msg, Maybe.empty)
      case ThrowableError(cause) =>
        ("Unknown error", Just(cause))
      case OtherError(other) =>
        (other.toString, Maybe.empty)
      case ComposeError(errors: NonEmptyList[Error]) =>
        val es = errors.map(e => errorToLog(e).msg).foldLeft("\n")(_ + "\n" + _)
        (s"Multiple errors (${errors.size}): $es", Maybe.empty)
      case AddContextError(error, msg, ctx) =>
        (s"${msg}\nContext: ${ctx}\nCause: ${errorToLog(error).msg}", Maybe.empty)
    })
}
