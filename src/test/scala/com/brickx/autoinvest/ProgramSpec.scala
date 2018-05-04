package com.brickx
package autoinvest

import std._
import TradingTypes._
import org.specs2.{ ScalaCheck, Specification }
import fs2.Stream
import scalaz.ioeffect.Task
import scalaz.ioeffect.catz._
import java.time.Clock
import scala.Function.const
import Program._
import TradingOrphanInstances._

class ProgramSpec extends Specification with ScalaCheck with Fakes {

  implicit val clock = Clock.systemDefaultZone()

  def is = s2"""
    Program must
      fetch trading data for each event                $fetchTradingData
      compute auto-invest action for trading data      $computeAction
      create orders on trading and save pendings on db $processAction
  """

  val fetchTradingData = prop { (accIds: List[AccountId], pos: Position, sov: SimpleOrderView) =>
    val events  = Stream.emits(accIds)
    val trading = new ReadFakeTrading(id => pos.copy(accountId = id), sov)
    val program = new DefaultProgram(NoOpEvents, trading, NoOpDb, const(noOpResult))
    val actions = events.covary[Task].through(program.toAction).compile.toList
    val idsBack: List[String] = unsafePerformIO(actions).collect {
      case CannotProcessNow(id, _) => id
    }
    idsBack.must_===(accIds)
  }

  val computeAction = prop {
    (accIds: List[AccountId], pos: Position, sov: SimpleOrderView, cor: CreateOrderRequest) =>
      val events  = Stream.emits(accIds)
      val trading = new ReadFakeTrading(id => pos.copy(accountId = id), sov)
      val compute: ComputeAutoInvest = {
        case TradingData(ICons(p, _), _) => Result.ok(cor.copy(accountId = p.accountId))
        case other                       => Result.error(Error.other(other))
      }
      val program = new DefaultProgram(NoOpEvents, trading, NoOpDb, compute)
      val actions = events.covary[Task].through(program.toAction).compile.toList
      val idsBack: List[String] = unsafePerformIO(actions).collect {
        case CreateOrder(req) => req.accountId
      }
      idsBack.must_===(accIds)
  }

  val processAction = prop {
    (accIdOrReq: List[Either[AccountId, CreateOrderRequest]], po: PendingOrder) =>
      val actions =
        Stream.emits(accIdOrReq.map(_.fold(CannotProcessNow(_, NoOpError), CreateOrder(_))))
      val trading = new WriteFakeTrading(po)
      val db      = new FakeDb
      val program = new DefaultProgram(NoOpEvents, trading, db, const(noOpResult))
      unsafePerformIO(actions.covary[Task].to(program.performAction).compile.drain)
      val ids      = accIdOrReq.collect { case Left(id) => id }
      val reqs     = accIdOrReq.collect { case Right(req) => req }
      val pendings = db.pendings.map(_.accountId)
      val created  = trading.created
      List(
        pendings must containTheSameElementsAs(ids),
        created must containTheSameElementsAs(reqs)
      )
  }

}

trait Fakes {

  val NoOpError                = Error.message("no-op")
  def noOpResult[A]: Result[A] = Result.error(NoOpError)
  def noOpTask[A]: Task[A]     = IO.fail(NoOpError)

  val NoOpEvents: Events[Task] =
    new Events[Task] {
      override def subscribe: Stream[Task, String] = Stream.empty
    }

  class NoOpTrading extends Trading[Task] {
    override def getPortfolio(userId: UserId, date: Maybe[Date]): Task[IList[Position]] = noOpTask
    override def getOrderBook(propertyCode: String): Task[IList[SimpleOrderView]]       = noOpTask
    override def createOrder(propertyCode: String,
                             request: CreateOrderRequest): Task[PendingOrder] = noOpTask
  }

  val NoOpTrading: Trading[Task] = new NoOpTrading

  class ReadFakeTrading(p: UserId => Position, sov: SimpleOrderView) extends NoOpTrading {
    override def getPortfolio(userId: UserId, date: Maybe[Date]): Task[IList[Position]] =
      IO.now(IList(p(userId)))
    override def getOrderBook(propertyCode: String): Task[IList[SimpleOrderView]] =
      IO.now(IList(sov))
  }

  class WriteFakeTrading(po: PendingOrder) extends NoOpTrading {
    var created: List[CreateOrderRequest] = List()
    override def createOrder(propertyCode: String,
                             request: CreateOrderRequest): Task[PendingOrder] =
      IO.point(created = created :+ request).const(po)
  }

  class NoOpDb extends Db[Task] {
    override def savePending(pending: Pending): Task[Unit]       = noOpTask
    override def deletePending(accountId: AccountId): Task[Unit] = noOpTask
    override def listPending: Stream[Task, String]               = Stream.empty
  }

  val NoOpDb: Db[Task] = new NoOpDb

  class FakeDb extends NoOpDb {
    var pendings: List[Pending] = List()
    override def savePending(pending: Pending): Task[Unit] =
      IO.point(pendings = pendings :+ pending).toUnit
  }
}
