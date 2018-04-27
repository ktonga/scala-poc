package com.brickx
package autoinvest

import std._
import TradingTypes._
import argonaut._, Argonaut._, ArgonautScalaz._
import org.scalacheck.{ Arbitrary, Gen }, Arbitrary.arbitrary
import OrderSide.encode

trait TradingOrphanInstances {

  implicit val OrderStatusEncodeJson: EncodeJson[OrderStatus] = EncodeJson.StringEncodeJson.contramap(_.name)
  implicit val PositionEncodeJson           = EncodeJson.derive[Position]
  implicit val SimpleOrderViewEncodeJson    = EncodeJson.derive[SimpleOrderView]
  implicit val PendingOrderEncodeJson       = EncodeJson.derive[PendingOrder]
  implicit val CreateOrderRequestDecodeJson = DecodeJson.derive[CreateOrderRequest]
  implicit val TradingErrorEncodeJson       = EncodeJson.derive[TradingError]

  implicit def maybeArbitrary[A: Arbitrary]: Arbitrary[Maybe[A]] =
    Arbitrary(Gen.option(arbitrary[A]).map(Maybe.fromOption))

  implicit val PositionArbitrary: Arbitrary[Position] =
    Arbitrary {
      for {
        accountId        <- arbitrary[AccountId]
        propertyCode     <- arbitrary[String]
        quantity         <- arbitrary[Int]
        lastAcquiredDate <- arbitrary[Date]
      } yield Position(accountId, propertyCode, quantity, lastAcquiredDate)
    }

  implicit val OrderSideArbitrary: Arbitrary[OrderSide] =
    Arbitrary(Gen.oneOf(OrderSide.values.toList))

  implicit val OrderStatusArbitrary: Arbitrary[OrderStatus] =
    Arbitrary(Gen.oneOf(OrderStatus.values.toList))

  implicit val SimpleOrderViewArbitrary: Arbitrary[SimpleOrderView] =
    Arbitrary {
      for {
        price      <- arbitrary[BigDecimal]
        quantity   <- arbitrary[Int]
        orderSide  <- arbitrary[OrderSide]
        accountId  <- arbitrary[AccountId]
        date       <- arbitrary[DateTime]
        commission <- arbitrary[BigDecimal]
      } yield SimpleOrderView(price, quantity, orderSide, accountId, date, commission)
    }

  implicit val PendingOrderArbitrary: Arbitrary[PendingOrder] =
    Arbitrary {
      for {
        orderId            <- arbitrary[OrderId]
        propertyCode       <- arbitrary[String]
        orderSide          <- arbitrary[OrderSide]
        status             <- arbitrary[OrderStatus]
        quantity           <- arbitrary[Int]
        price              <- arbitrary[BigDecimal]
        date               <- arbitrary[DateTime]
        commission         <- arbitrary[BigDecimal]
        isBuyOrder         <- arbitrary[Boolean]
        totalPrice         <- arbitrary[BigDecimal]
        totalPriceInclFees <- arbitrary[BigDecimal]
      } yield
        PendingOrder(orderId,
                     propertyCode,
                     orderSide,
                     status,
                     quantity,
                     price,
                     date,
                     commission,
                     isBuyOrder,
                     totalPrice,
                     totalPriceInclFees)
    }

  implicit val CreateOrderRequestArbitrary: Arbitrary[CreateOrderRequest] =
    Arbitrary {
      for {
        accountId  <- arbitrary[AccountId]
        orderSide  <- arbitrary[OrderSide]
        quantity   <- arbitrary[Int]
        price      <- arbitrary[BigDecimal]
        commission <- arbitrary[BigDecimal]
        timestamp  <- arbitrary[Maybe[DateTime]]
      } yield CreateOrderRequest(accountId, orderSide, quantity, price, commission, timestamp)
    }

  implicit val TradingErrorArbitrary: Arbitrary[TradingError] =
    Arbitrary(arbitrary[String].map(TradingError(_)))

}
