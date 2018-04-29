package com.brickx
package autoinvest

import std._
import scala.Predef._
import Config.DbConfig

import java.time.Clock
import doobie.imports._
import org.specs2.scalacheck.Parameters
import org.specs2.specification.BeforeAll
import org.specs2.{ ScalaCheck, Specification }
import scalaz.ioeffect.Task
import scalaz.ioeffect.catz._
import org.scalacheck._
import scalaz.std.list._, Arbitrary.arbContainer
import scalaz.syntax.traverse._

class DbSpec extends Specification with ScalaCheck with BeforeAll {

  implicit val params = Parameters(minTestsOk = 30)
  implicit val clock  = Clock.systemDefaultZone()

  val dbConfig = DbConfig("jdbc:postgresql://localhost:5432/autoinvest", "autoinvest", "shhh")

  val xa = Db.defaultTransactor[Task](dbConfig)

  override def beforeAll {
    val sql = s"""
      DROP SCHEMA public CASCADE;
      CREATE SCHEMA public;
      GRANT ALL ON SCHEMA public TO ${dbConfig.user};
      GRANT ALL ON SCHEMA public TO public;
    """
    val dropAndCreate = for {
      _ <- Update0(sql, None).run.transact(xa)
      _ <- Migrations.default[Task](dbConfig).migrate
    } yield ()
    unsafePerformIO(dropAndCreate)
  }

  def is = s2"""

    Db should
      save Pending account Id   $savePending
      delete Pending account Id $deletePending
      list Pending account Ids  $listPending

  """

  val db = Db.default(xa)

  implicit val accIdArbitrary: Arbitrary[AccountId] =
    Arbitrary(Gen.uuid.map(_.toString))

  val savePending = prop { accountId: AccountId =>
    val allIO =
      for {
        _           <- db.savePending(Pending.create(accountId))
        allPendings <- db.listPending.compile.toList
      } yield allPendings
    unsafePerformIO(allIO) must contain(accountId)
  }

  val deletePending = prop { accountId: AccountId =>
    val allIO =
      for {
        _           <- db.savePending(Pending.create(accountId))
        _           <- db.deletePending(accountId)
        allPendings <- db.listPending.compile.toList
      } yield allPendings
    unsafePerformIO(allIO) must not contain(accountId)
  }

  val listPending = prop { accountIds: List[AccountId] =>
    val allIO =
      for {
        _           <- accountIds.traverse_(id => db.savePending(Pending.create(id)))
        allPendings <- db.listPending.compile.toList
      } yield allPendings
    unsafePerformIO(allIO) must contain(allOf(accountIds:_*))
  }

}
