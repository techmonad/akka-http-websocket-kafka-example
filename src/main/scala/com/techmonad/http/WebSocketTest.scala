package com.techmonad.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.{Done, NotUsed}

import scala.concurrent.Future

object WebSocketTest {


  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("WebSocketTest")
    implicit val materializer = ActorMaterializer()
    implicit val dispatcher = system.dispatcher

    val printSink: Sink[Message, Future[Done]] =
      Sink.foreach { case message: TextMessage.Strict => println("client received: " + message.text) }

    val helloSource: Source[Message, NotUsed] = Source.single(TextMessage("hello world!"))


    val flow: Flow[Message, Message, Future[Done]] =
      Flow.fromSinkAndSourceMat(printSink, helloSource)(Keep.left)

    val (upgradeResponse, closed) =
      Http().singleWebSocketRequest(WebSocketRequest("ws://localhost:9192/connect?query_id=abc12"), flow)

    val connected = upgradeResponse.map { upgrade =>
      if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
        println("switching protocols")
        Done
      } else {
        throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
      }
    }

    // in a real application you would not side effect here
    // and handle errors more carefully
    connected.onComplete(println)
    closed.foreach(_ => println("closed"))
  }
}

//ws://localhost:9192/connect?query_id=5bf189eb3b4496810778b2ce
