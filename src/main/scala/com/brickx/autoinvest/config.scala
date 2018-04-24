package com.brickx
package autoinvest

import std._

import org.http4s.Uri

object config {

  case class AppConfig(trading: TradingConfig)

  case class TradingConfig(endpoint: Uri)

  // FIXME bug already reported https://github.com/pureconfig/pureconfig/issues/382
  def loadConfig: Result[AppConfig] = ???
  // pureconfig.loadConfig[AppConfig]("com.brickx.autoinvest")

}
