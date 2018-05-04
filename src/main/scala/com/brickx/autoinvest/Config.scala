package com.brickx
package autoinvest

import std._
import scalaz.syntax.std.either._

import org.http4s.Uri
import pureconfig.modules.http4s._

object Config {

  final case class AppConfig(trading: TradingConfig, events: EventsConfig, db: DbConfig)

  final case class TradingConfig(endpoint: Uri)

  final case class EventsConfig(bootstrapServers: String, groupId: String, topicName: String)

  final case class DbConfig(url: String, user: String, password: String)

  def loadConfig: Result[AppConfig] =
    pureconfig
      .loadConfig[AppConfig]("com.brickx.autoinvest")
      .disjunction
      .leftMap(Error.config)

}
