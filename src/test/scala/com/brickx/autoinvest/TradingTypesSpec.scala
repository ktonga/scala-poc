package com.brickx
package autoinvest

import std._
import TradingTypes._
import argonaut._
import org.scalacheck.Arbitrary
import org.specs2.{ ScalaCheck, Specification }

class TradingTypesSpec extends Specification with TradingOrphanInstances with ScalaCheck {

  def is = s2"""
    API types must be decoded from JSON
      Position             ${encodeDecodeJson[Position]}
      SimpleOrderView      ${encodeDecodeJson[SimpleOrderView]}
      PendingOrder         ${encodeDecodeJson[PendingOrder]}
      CreateOrderRequest   ${encodeDecodeJson[CreateOrderRequest]}
      TadingError          ${encodeDecodeJson[TradingError]}
  """

  def encodeDecodeJson[A: EncodeJson: DecodeJson: Arbitrary] = prop { a: A =>
    EncodeJson.of[A].encode(a).as[A].result must beRight(a)
  }

}
