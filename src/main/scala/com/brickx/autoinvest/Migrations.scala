package com.brickx
package autoinvest

import std._, Z._
import Config.DbConfig

import cats.effect.Sync
import org.flywaydb.core.Flyway
import shims._

trait Migrations[F[_]] {
  def migrate: F[Unit]
}

object Migrations {

  def default[F[_]: Sync](config: DbConfig)(implicit L: Log[F]): Migrations[F] =
    new FlywayMigrations(config)

  private class FlywayMigrations[F[_]: Sync](config: DbConfig)(implicit L: Log[F])
      extends Migrations[F] {

    override def migrate: F[Unit] =
      Sync[F]
        .delay({
          val flyway = new Flyway()
          flyway.setDataSource(config.url, config.user, config.password)
          flyway.migrate()
        })
        .logged(ms => s"Migrations: $ms migrations performed")
        .void

  }

}
