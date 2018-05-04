package com.brickx
package autoinvest

import std._
import Config.DbConfig

import java.time.{ Clock, OffsetDateTime }
import cats.effect.Effect
import cats.syntax.functor._
import doobie.imports._
import fs2.Stream

final case class Pending(accountId: AccountId, created: OffsetDateTime)
object Pending {
  def create(accountId: AccountId)(implicit clock: Clock): Pending =
    Pending(accountId, OffsetDateTime.now(clock))
}

trait Db[F[_]] {
  def savePending(pending: Pending): F[Unit]
  def deletePending(accountId: AccountId): F[Unit]
  def listPending: Stream[F, String]
}

object Db {

  def default[F[_]: Effect](xa: Transactor[F]): Db[F] =
    new DoobieDb(xa)

  def defaultTransactor[F[_]: Effect](config: DbConfig): Transactor[F] =
    Transactor.fromDriverManager[F](
      "org.postgresql.Driver",
      config.url,
      config.user,
      config.password
    )

  private class DoobieDb[F[_]: Effect](xa: Transactor[F]) extends Db[F] {

    override def savePending(pending: Pending): F[Unit] =
      // TODO deal with timestamps later
      sql"""
        INSERT INTO pending (account_id, created)
        VALUES (${pending.accountId}, CURRENT_TIMESTAMP)
      """.update.run.transact(xa).void

    override def deletePending(accountId: AccountId): F[Unit] =
      sql"""
        DELETE FROM pending
        WHERE account_id = $accountId
      """.update.run.transact(xa).void

    override def listPending: Stream[F, String] =
      sql"""
        SELECT account_id
        FROM pending
      """.query[AccountId].stream.transact(xa)
  }

}
