package com.brickx
package autoinvest

import std._, S._
import argonaut._, Argonaut._, ArgonautScalaz._

object TradingTypes {

  final case class Position(accountId: AccountId,
                            propertyCode: String,
                            quantity: Int,
                            lastAcquiredDate: Date)

  object Position {
    implicit val decode = DecodeJson.derive[Position]
  }

  sealed abstract class OrderSide(val name: String)
  object OrderSide {
    case object BUY  extends OrderSide("buy")
    case object SELL extends OrderSide("sell")

    val values = IList(BUY, SELL)
    val byName = values.map(x => x.name -> x).toMap

    implicit val decode: DecodeJson[OrderSide] =
      DecodeJson.optionDecoder(v => v.string.flatMap(byName.lookup), "OrderSide")
    implicit val encode: EncodeJson[OrderSide] =
      EncodeJson.StringEncodeJson.contramap(_.name)
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

    implicit val decode: DecodeJson[OrderStatus] =
      DecodeJson.optionDecoder(v => v.string.flatMap(byName.lookup), "OrderStatus")
  }

  final case class SimpleOrderView(price: BigDecimal,
                                   quantity: Int,
                                   orderSide: OrderSide,
                                   accountId: AccountId,
                                   date: DateTime,
                                   commission: BigDecimal)

  object SimpleOrderView {
    implicit val decode = DecodeJson.derive[SimpleOrderView]
  }

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

  object PendingOrder {
    implicit val decode = DecodeJson.derive[PendingOrder]
  }

  final case class CreateOrderRequest(accountId: AccountId,
                                      orderSide: OrderSide,
                                      quantity: Int,
                                      price: BigDecimal,
                                      commission: BigDecimal,
                                      timestamp: Maybe[DateTime])

  object CreateOrderRequest {
    implicit val endode = EncodeJson.derive[CreateOrderRequest]
  }

  case class TradingError(message: String)

  object TradingError {
    implicit val decode = DecodeJson.derive[TradingError]
  }
}
