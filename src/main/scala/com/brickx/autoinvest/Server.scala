package com.brickx
package autoinvest

import std._
import Config.ServerConfig
import cats.effect.Effect
import cats.syntax.functor._
import org.http4s.HttpService
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder

trait Server[F[_]] {
  def start: F[Unit]
}

object Server {

  def default[F[_]: Effect](config: ServerConfig): Server[F] =
    new Http4sServer[F](config)

  class Http4sServer[F[_]: Effect](config: ServerConfig) extends Server[F] with Http4sDsl[F] {

    override def start: F[Unit] =
      BlazeBuilder[F]
        .bindHttp(config.port, config.host)
        .mountService(service, "/")
        .start
        .void

    private def service: HttpService[F] = HttpService[F] {
      case GET -> Root / "ping" => Ok("pong")
    }

  }
}
