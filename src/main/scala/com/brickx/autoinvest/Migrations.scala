package com.brickx
package autoinvest

import std._
import Config.DbConfig

import cats.effect.Sync
import org.flywaydb.core.Flyway

trait Migrations[F[_]] {
  def migrate: F[Unit]
}

object Migrations {

  def default[F[_]: Sync](config: DbConfig): Migrations[F] =
    new FlywayMigrations(config)

  private class FlywayMigrations[F[_]: Sync](config: DbConfig) extends Migrations[F] {

    override def migrate: F[Unit] = Sync[F].delay {
      val flyway = new Flyway()
      flyway.setDataSource(config.url, config.user, config.password)
      val _ = flyway.migrate()
    }

  }

}
