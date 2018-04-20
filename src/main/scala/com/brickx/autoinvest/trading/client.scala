package com.brickx
package autoinvest
package trading

import std._
import api._
import cats.effect.Sync
import org.http4s.{ EntityDecoder, EntityEncoder, Uri, Request }
import org.http4s.client.Client
import org.http4s.Method.POST
import org.http4s.argonaut._
import argonaut.{ DecodeJson, EncodeJson }
import argonaut.ArgonautScalaz._

trait Trading[F[_]] {
  def getPortfolio(userId: UserId, date: Maybe[Date]): F[IList[Position]]
  def getOrderBook(propertyCode: String): F[IList[SimpleOrderView]]
  def createOrder(propertyCode: String, request: CreateOrderRequest): F[PendingOrder]
}

object Trading {
  def default[F[_]: Sync](client: Client[F], endpoint: Uri): Trading[F] =
    new Http4sTrading(client, endpoint)
}

final class Http4sTrading[F[_]: Sync](client: Client[F], endpoint: Uri) extends Trading[F] {

  implicit def jsonEntityDecoder[A: DecodeJson]: EntityDecoder[F, A] = jsonOf[F, A]
  implicit def jsonEntityEncoder[A: EncodeJson]: EntityEncoder[F, A] = jsonEncoderOf[F, A]

  override def getPortfolio(userId: UserId, date: Maybe[Date]): F[IList[Position]] =
    // TODO error handling
    client.expect[IList[Position]](endpoint / "accounts" / userId.toString / "portfolio")

  override def getOrderBook(propertyCode: String): F[IList[SimpleOrderView]] =
    // TODO error handling
    client.expect[IList[SimpleOrderView]](endpoint / "orderbooks" / propertyCode)

  override def createOrder(propertyCode: String, request: CreateOrderRequest): F[PendingOrder] = {
    val req = Request(POST, endpoint / "orderbooks" / propertyCode / "pendingOrder").withBody(request)
    // TODO error handling
    Sync[F].flatMap(req)(client.expect[PendingOrder])
  }
}
