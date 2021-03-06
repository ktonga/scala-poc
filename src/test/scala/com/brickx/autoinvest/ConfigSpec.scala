package com.brickx
package autoinvest

import std._
import Config._

import org.specs2._
import org.specs2.matcher.DisjunctionMatchers
import org.http4s.Uri

class ConfigSpec extends Specification with DisjunctionMatchers {

  def is = s2"""
    AppConfig must
      succeed loading for valid file  $configOk
  """

  def configOk = {
    val expected = AppConfig(
      TradingConfig(Uri.uri("http://localhost:9000")),
      EventsConfig("localhost:9092", "auto-invest", "auto-invest"),
      DbConfig("jdbc:postgresql://localhost:5432/autoinvest", "autoinvest", "shhh"),
      ServerConfig(8080, "localhost")
    )

    loadConfig must be_\/-(expected)
  }
}
