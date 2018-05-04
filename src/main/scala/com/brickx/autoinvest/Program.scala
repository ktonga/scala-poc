package com.brickx
package autoinvest

import std._, Z._
import TradingTypes._
import java.time.Clock
import cats.effect.Sync
import shims._
import fs2.{ Pipe, Sink }

trait Program[F[_]] {
  def processEvents: F[Unit]
}

object Program {

  final case class TradingData(portfolio: IList[Position], orderBook: IList[SimpleOrderView])

  sealed abstract class AutoInvestAction
  final case class CreateOrder(request: CreateOrderRequest)              extends AutoInvestAction
  final case class CannotProcessNow(accountId: AccountId, reason: Error) extends AutoInvestAction

  type ComputeAutoInvest = TradingData => Result[CreateOrderRequest]

  def default[F[_]: Sync](events: Events[F],
                          trading: Trading[F],
                          db: Db[F],
                          computeAutoInvest: ComputeAutoInvest)(implicit clock: Clock): Program[F] =
    new DefaultProgram(events, trading, db, computeAutoInvest)

  class DefaultProgram[F[_]: Sync](events: Events[F],
                                   trading: Trading[F],
                                   db: Db[F],
                                   computeAutoInvest: ComputeAutoInvest)(implicit clock: Clock)
      extends Program[F] {

    override def processEvents: F[Unit] =
      events.subscribe
        .through(toAction)
        .to(performAction)
        .compile
        .drain

    val toAction: Pipe[F, String, AutoInvestAction] = _.evalMap(fetchTradingData).map {
      case (accId, td) =>
        td.flatMap(computeAutoInvest)
          .fold(CannotProcessNow(accId, _), CreateOrder(_))
    }

    private def fetchTradingData(accountId: AccountId): F[(AccountId, Result[TradingData])] =
      ^(
        trading.getPortfolio(accountId, "today".just),
        trading.getOrderBook("some-prop")
      )(TradingData.apply).attemptR.map((accountId, _))

    val performAction: Sink[F, AutoInvestAction] =
      _.evalMap {
        case CreateOrder(order)         => trading.createOrder("some-prop", order).void
        case CannotProcessNow(accId, _) => db.savePending(Pending.create(accId))
      }
  }
}
