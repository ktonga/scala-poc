package com.brickx
package autoinvest

import std._
import scalaz.syntax.std.either._

import org.http4s.Uri
import pureconfig.modules.http4s._

object config {

  case class AppConfig(trading: TradingConfig, events: EventsConfig)

  case class TradingConfig(endpoint: Uri)

  case class EventsConfig(
    bootstrapServers: String,
    groupId: String,
    topicName: String
  )

  def loadConfig: Result[AppConfig] =
    pureconfig
      .loadConfig[AppConfig]("com.brickx.autoinvest")
      .disjunction
      .leftMap(Error.config)

}
