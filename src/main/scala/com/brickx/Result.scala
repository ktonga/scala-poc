package com.brickx

import std._

import java.lang.Throwable
import pureconfig.error.ConfigReaderFailures
import java.lang.Exception
import scala.util.control.ControlThrowable

trait ResultTypes {

  sealed abstract class Error extends Exception with ControlThrowable {
    val asThrowable: Throwable = this
  }
  case class ConfigError(failures: ConfigReaderFailures)          extends Error
  case class MessageError(msg: String)                            extends Error
  case class OtherError[E](other: E)                              extends Error
  case class ComposeError(errors: NonEmptyList[Error])            extends Error
  case class ThrowableError(cause: Throwable, msg: Maybe[String]) extends Error

  object Error {
    def config(failures: ConfigReaderFailures): Error = ConfigError(failures)
    def message(msg: String): Error                   = MessageError(msg)
    def other[E](other: E): Error                     = OtherError(other)
    def compose(errors: NonEmptyList[Error])          = ComposeError(errors)
    def throwable(cause: Throwable, msg: Maybe[String] = Maybe.empty): Error =
      ThrowableError(cause, msg)

    def fromThrowable(t: Throwable): Error = t match {
      case e: Error => e
      case _        => throwable(t)
    }
  }

  type Result[A]  = Error \/ A
  type ResultV[A] = ValidationNel[Error, A]

  object Result {
    def ok[A](a: A): Result[A]            = Disjunction.right(a)
    def error[A](error: Error): Result[A] = Disjunction.left(error)
  }

}
