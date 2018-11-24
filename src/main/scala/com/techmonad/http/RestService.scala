package com.techmonad.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.{Done, NotUsed}
import com.techmonad.kafka.{Consumer, PublisherActor}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object RestService {


  def main(args: Array[String]): Unit = {

    val kafkaServer = List("localhost:9092")
    val consumer = new Consumer(List("status_topic"), kafkaServer)

    implicit val system = ActorSystem("WebSocketApp")
    implicit val materializer = ActorMaterializer()
    implicit val dispatcher = system.dispatcher

    implicit val consumerActor = system.actorOf(PublisherActor.props(consumer), "PublisherActor")


    val routes = new Routes()

    Http()
      .bindAndHandle(routes.socketRoutes, "0.0.0.0", 9192)
      .onComplete {
        case Success(value) => println(value)
        case Failure(err) => println(err)
      }


  }
}
