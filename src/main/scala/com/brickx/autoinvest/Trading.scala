package com.brickx
package autoinvest

import std._, Z._
import TradingTypes._
import cats.effect.Sync
import org.http4s.{ EntityDecoder, EntityEncoder, Request, Uri }
import org.http4s.client.Client
import org.http4s.Method.{ GET, POST }
import org.http4s.Status.Successful
import org.http4s.argonaut._
import argonaut.{ DecodeJson, EncodeJson }
import argonaut.ArgonautScalaz._

trait Trading[F[_]] {
  def getPortfolio(userId: UserId, date: Maybe[Date]): F[Result[IList[Position]]]
  def getOrderBook(propertyCode: String): F[Result[IList[SimpleOrderView]]]
  def createOrder(propertyCode: String, request: CreateOrderRequest): F[Result[PendingOrder]]
}

object Trading {

  def default[F[_]: Sync](client: Client[F], endpoint: Uri): Trading[F] =
    new Http4sTrading(client, endpoint)

  private class Http4sTrading[F[_]: Sync](client: Client[F], endpoint: Uri) extends Trading[F] {

    implicit def jsonEntityDecoder[A: DecodeJson]: EntityDecoder[F, A] = jsonOf[F, A]
    implicit def jsonEntityEncoder[A: EncodeJson]: EntityEncoder[F, A] = jsonEncoderOf[F, A]

    override def getPortfolio(userId: UserId, date: Maybe[Date]): F[Result[IList[Position]]] = {
      val uri = endpoint / "accounts" / userId.toString / "portfolio"
      val req = Request[F](GET, uri)
      expect(req)
    }

    override def getOrderBook(propertyCode: String): F[Result[IList[SimpleOrderView]]] = {
      val uri = endpoint / "orderbooks" / propertyCode
      val req = Request[F](GET, uri)
      expect(req)
    }

    override def createOrder(propertyCode: String,
                             request: CreateOrderRequest): F[Result[PendingOrder]] = {
      val uri = endpoint / "orderbooks" / propertyCode / "pendingOrder"
      val req = Request(POST, uri).withBody(request)
      Sync[F].flatMap(req)(expect[PendingOrder])
    }

    private def expect[A](req: Request[F])(implicit d: EntityDecoder[F, A]): F[Result[A]] =
      client.fetch(req) {
        case Successful(resp) =>
          d.decode(resp, strict = false)
            .fold(Error.throwable(_, "Cannot decode response".just).left, _.right)
        case failedResponse =>
          EntityDecoder[F, TradingError]
            .decode(failedResponse, strict = true)
            .fold(Error.throwable(_, "Cannot decode error information".just).left,
                  Error.other(_).left)
      }

  }
}
