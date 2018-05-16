package com.brickx

import std._, Z._, S._
import cats.effect.Sync
import java.lang.Throwable
import shims._

package object autoinvest extends Types {

  final implicit class SyncEitherOps[F[_], A](private val self: F[Either[Error, A]])
      extends AnyVal {
    def rethrow(implicit F: Sync[F]): F[A] = F.rethrow(F.map(self)(_.widen[Throwable, A]))
  }

  final implicit class SyncOps[F[_], A](private val self: F[A]) extends AnyVal {

    def attemptR(implicit F: Sync[F]): F[Result[A]] =
      F.map(F.attempt(self))(_.disjunction.leftMap(Error.fromThrowable))

    def logged(msg: A => String)(implicit F: Sync[F], L: Log[F]): F[A] =
      F.flatMap(attemptR)(_.fold(L.error, a => L.info(msg(a)))) *> self
  }

  def comp[A, B, C](f: B => C, g: A => B): A => C = f.compose(g)
}
