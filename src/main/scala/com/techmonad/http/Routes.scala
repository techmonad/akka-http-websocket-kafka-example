package com.techmonad.http

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives.{complete, handleWebSocketMessages, onComplete, path, pathEndOrSingleSlash, _}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import com.techmonad.kafka.PublisherActor

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class Routes(implicit system: ActorSystem, materializer: ActorMaterializer, queryResultPublisher: ActorRef) {

  val socketRoutes =
    pathEndOrSingleSlash {
      complete("WS server is alive\n")
    } ~
      path("connect") {
        get {
          parameters('query_id.as[String]) { queryId =>

            val handler = system.actorOf(SocketHandlerActor.props(queryId))
            // subscribe from kafka
            queryResultPublisher ! PublisherActor.Subscribe(queryId, handler)

            val futureFlow: Future[Flow[Message, Message, _]] =
              (handler ? GetWebsocketFlow) (3.seconds).mapTo[Flow[Message, Message, _]]
            onComplete(futureFlow) {
              case Success(flow) => handleWebSocketMessages(flow)
              case Failure(err) => complete(err.toString)
            }
          }
        }
      }
}


case object GetWebsocketFlow