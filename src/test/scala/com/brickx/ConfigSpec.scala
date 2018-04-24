package com.brickx
package autoinvest

import std._
import config._

import org.specs2._
import org.specs2.matcher.DisjunctionMatchers
import org.http4s.Uri

class ConfigSpec extends Specification with DisjunctionMatchers {

  def is = s2"""
    AppConfig must
      succeed loading for valid file  $configOk
  """

  def configOk = {
    val expected = AppConfig(TradingConfig(Uri.uri("http://localhost:9000")))
    loadConfig must be_\/-(expected)
  }
}
