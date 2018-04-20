package com.brickx
package autoinvest
package trading

import std._, S._

object api {
  // TODO better types
  type UserId     = java.util.UUID
  type AccountId  = java.util.UUID
  type OrderId    = java.util.UUID
  type Date       = java.time.LocalDate
  type DateTime   = java.time.ZonedDateTime
  type BigDecimal = java.math.BigDecimal

  final case class Position(accountId: AccountId,
                            propertyCode: String,
                            quantity: Int,
                            lastAcquiredDate: Date)

  sealed abstract class OrderSide(val name: String)
  object OrderSide {
    case object BUY  extends OrderSide("buy")
    case object SELL extends OrderSide("sell")

    val values = IList(BUY, SELL)
    val byName = values.map(x => x.name -> x).toMap
  }

  sealed abstract class OrderStatus(val name: String)
  object OrderStatus {
    case object PENDING          extends OrderStatus("open")
    case object CANCELLED        extends OrderStatus("cancelled")
    case object COMPLETE         extends OrderStatus("complete")
    case object PARTIAL          extends OrderStatus("partial")
    case object PARTIAL_COMPLETE extends OrderStatus("partial complete")

    val values = IList(PENDING, CANCELLED, COMPLETE, PARTIAL, PARTIAL_COMPLETE)
    val byName = values.map(x => x.name -> x).toMap
  }

  final case class SimpleOrderView(price: BigDecimal,
                                   quantity: Int,
                                   orderSide: OrderSide,
                                   accountId: AccountId,
                                   date: DateTime,
                                   commission: BigDecimal)

  final case class PendingOrder(orderId: OrderId,
                                propertyCode: String,
                                orderSide: OrderSide,
                                status: OrderStatus,
                                quantity: Int,
                                price: BigDecimal,
                                date: DateTime,
                                commission: BigDecimal,
                                isBuyOrder: Boolean,
                                totalPrice: BigDecimal,
                                totalPriceInclFees: BigDecimal)

  final case class CreateOrderRequest(accountId: AccountId,
                                      orderSide: OrderSide,
                                      quantity: Int,
                                      price: BigDecimal,
                                      commission: BigDecimal,
                                      timestamp: Maybe[DateTime])
}
