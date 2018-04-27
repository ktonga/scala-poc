package com.brickx
package autoinvest

import std._
import scalaz.syntax.std.either._

import org.http4s.Uri
import pureconfig.modules.http4s._

object Config {

  case class AppConfig(trading: TradingConfig, events: EventsConfig, db: DbConfig)

  case class TradingConfig(endpoint: Uri)

  case class EventsConfig(bootstrapServers: String, groupId: String, topicName: String)

  case class DbConfig(url: String, user: String, password: String)

  def loadConfig: Result[AppConfig] =
    pureconfig
      .loadConfig[AppConfig]("com.brickx.autoinvest")
      .disjunction
      .leftMap(Error.config)

}
