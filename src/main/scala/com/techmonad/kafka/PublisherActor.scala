package com.techmonad.kafka

import akka.actor.{Actor, ActorRef, Cancellable, Props}
import com.techmonad.json.JsonHelper._
import com.techmonad.logger.Logging

import scala.concurrent.duration.{FiniteDuration, _}

class PublisherActor(consumer: Consumer) extends Actor with Logging {

  import PublisherActor._

  val delay: FiniteDuration = 100 millis

  implicit val dispatcher = context.dispatcher


  var subscriptions = Map[String, ActorRef]()

  def scheduleOnce(delay: FiniteDuration): Cancellable =
    context.system.scheduler.scheduleOnce(delay, self, Read)


  override def preStart(): Unit = {
    scheduleOnce(delay)
    super.preStart()
  }

  override def receive: Receive = {
    case Read =>
      val messages = consumer.read()
      info(s"Message from kafka  [$messages]")
      if (messages.nonEmpty) {
        messages foreach { json =>
          val message = parse(json).extract[StatusMessage]
          subscriptions.get(message.queryId) match {
            case Some(subscriber) => subscriber ! message
            case None => warn(s"Subscriber does not exist for $message ")
          }

        }
        self ! Read
      } else {
        scheduleOnce(delay = 1 seconds)

      }
    case Subscribe(queryId, subscriber) =>
      info(s"Subscription queryId $queryId and subscriber $subscriber")
      subscriptions = subscriptions + (queryId -> subscriber)

    case unknownMessage =>
      warn(s"Unknown Message [$unknownMessage]")
  }

}

object PublisherActor {

  def props(consumer: Consumer) =
    Props(classOf[PublisherActor], consumer)

  case object Read

  case class Subscribe(queryId: String, actorRef: ActorRef)

  case class StatusMessage(queryId: String, status: String)

}
