package com.techmonad.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.techmonad.kafka.{Consumer, PublisherActor}

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

//https://github.com/akka/akka-http/blob/master/docs/src/test/scala/docs/http/scaladsl/server/WebSocketExampleSpec.scala