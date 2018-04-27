package com.brickx
package autoinvest

import std._
import Config.EventsConfig

import java.util.Properties
import org.apache.kafka.clients.consumer.KafkaConsumer
import scala.collection.JavaConverters._
import fs2.{ async, Stream }
import cats.effect.Effect
import scala.concurrent.ExecutionContext.Implicits.global

trait Events[F[_]] {
  def subscribe: Stream[F, String]
}

object Events {

  def default[F[_]](conf: EventsConfig)(implicit eff: Effect[F]): Events[F] =
    new KafkaFs2Consumer(conf)

  private class KafkaFs2Consumer[F[_]](conf: EventsConfig)(implicit eff: Effect[F]) extends Events[F] {

    private val kafkaConsumer: F[KafkaConsumer[String, String]] = eff.delay {
      val props = new Properties
      props.put("bootstrap.servers", conf.bootstrapServers)
      props.put("group.id", conf.groupId)
      props.put("enable.auto.commit", "true")
      props.put("auto.commit.interval.ms", "1000")
      props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
      props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")

      new KafkaConsumer[String, String](props)
    }

    // TODO implement back-pressure and manual commit
    private def withEvents(kc: KafkaConsumer[String, String])(withEvent: String => Unit): F[Unit] =
      eff.async { cb =>
        cb(Right(kc.subscribe(List(conf.topicName).asJava)))

        while (true) {
          val events = kc.poll(1000).asScala
          events.foreach { event =>
            withEvent(event.value)
          }
        }
      }

    override def subscribe: Stream[F, String] =
      for {
        q  <- Stream.eval(async.unboundedQueue[F, String])
        kc <- Stream.eval(kafkaConsumer)
        _ <- Stream.eval {
              withEvents(kc)(evt => async.unsafeRunAsync(q.enqueue1(evt))(_ => cats.effect.IO.unit))
            }
        record <- q.dequeue
      } yield record

  }
}
