package com.brickx

import std._
import cats.effect.Sync
import cats.syntax.either._
import scalaz.syntax.std.either._

package object autoinvest extends Types {

  final class SyncEitherOps[F[_], A](self: F[Either[Error, A]])(implicit F: Sync[F]) {
    def rethrow: F[A] = F.rethrow(F.map(self)(_.leftMap(_.asThrowable)))
  }

  implicit def syncEitherOps[F[_]: Sync, A](self: F[Either[Error, A]]): SyncEitherOps[F, A] =
    new SyncEitherOps(self)

  final class SyncOps[F[_], A](self: F[A])(implicit F: Sync[F]) {
    def attemptR: F[Result[A]] = F.map(F.attempt(self))(_.disjunction.leftMap(Error.fromThrowable))
  }

  implicit def syncOps[F[_]: Sync, A](self: F[A]): SyncOps[F, A] =
    new SyncOps(self)

}
