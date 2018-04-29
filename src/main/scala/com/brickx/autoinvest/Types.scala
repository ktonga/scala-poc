package com.brickx
package autoinvest

import std._

trait Types {

  // TODO better types and codecs
  type UserId     = String //java.util.UUID
  type AccountId  = String //java.util.UUID
  type OrderId    = String //java.util.UUID
  type Date       = String //java.time.LocalDate
  type DateTime   = String //java.time.ZonedDateTime
  type BigDecimal = Double //java.math.BigDecimal

}
