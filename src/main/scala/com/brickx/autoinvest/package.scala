package com.brickx

import std._, Z._, S._
import cats.effect.Sync
import java.lang.Throwable

package object autoinvest extends Types {

  final implicit class SyncEitherOps[F[_], A](private val self: F[Either[Error, A]])
      extends AnyVal {
    def rethrow(implicit F: Sync[F]): F[A] = F.rethrow(F.map(self)(_.widen[Throwable, A]))
  }

  final implicit class SyncOps[F[_], A](private val self: F[A]) extends AnyVal {
    def attemptR(implicit F: Sync[F]): F[Result[A]] =
      F.map(F.attempt(self))(_.disjunction.leftMap(Error.fromThrowable))
  }

}
