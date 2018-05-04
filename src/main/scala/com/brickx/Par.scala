package com.brickx

import std._

trait Par[F[_]] {
  def apply2[A, B, C](fa: F[A], fb: F[B])(ff: (A, B) => C): F[C]
}

object Par {
  @inline def apply[F[_]](implicit F: Par[F]): Par[F] = F

  implicit def ioPar[E]: Par[IO[E, ?]] = new Par[IO[E, ?]] {
    override def apply2[A, B, C](fa: IO[E, A], fb: IO[E, B])(ff: (A, B) => C): IO[E, C] =
      fa.par(fb).map(ff.tupled)
  }
}
