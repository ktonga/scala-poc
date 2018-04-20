package com.brickx
package autoinvest
package trading

import std._
import api._

trait Trading[F[_]] {
  def getPortfolio(userId: UserId, date: Maybe[Date]): F[IList[Position]]
  def getOrderBook(propertyCode: String): F[IList[SimpleOrderView]]
  def createOrder(propertyCode: String, request: CreateOrderRequest): F[PendingOrder]
}
