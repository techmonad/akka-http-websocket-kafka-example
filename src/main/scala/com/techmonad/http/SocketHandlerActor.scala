package com.techmonad.http

import akka.actor.{Actor, Props}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Flow, GraphDSL, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, FlowShape, OverflowStrategy}
import com.techmonad.kafka.PublisherActor
import com.techmonad.logger.Logging

import scala.concurrent.Future
import scala.concurrent.duration._

class SocketHandlerActor(queryId: String) extends Actor with Logging {

  import PublisherActor._

  implicit val system = context.system

  implicit val mat = ActorMaterializer()

  implicit val ec = system.dispatcher

  val (webSocket, publisher) = Source
    .actorRef[String](1000, OverflowStrategy.fail)
    .toMat(Sink.asPublisher(fanout = false))(Keep.both)
    .run()

  override def receive: Receive = {
    case GetWebsocketFlow =>
      val flow = Flow.fromGraph(GraphDSL.create() { implicit b =>
        val textMsgFlow: FlowShape[Message, String] = b.add(Flow[Message]
          .mapAsync(1) {
            case tm: TextMessage => tm.toStrict(2.seconds).map(_.text)
            case bm: BinaryMessage =>
              bm.dataStream.runWith(Sink.ignore)
              Future.failed(new IllegalArgumentException("Invalid message"))
          })
        val sourcePublisher = b.add(Source.fromPublisher(publisher).map(TextMessage(_)))
        textMsgFlow ~> Sink.foreach[String](self ! _)
        FlowShape(textMsgFlow.in, sourcePublisher.out)
      })
      sender ! flow
    case StatusMessage(id, status) =>
      if (queryId == id) {
        webSocket ! status
      } else {
        warn(s"Invalid status: actual query id $queryId [invalid query : $id]")
      }

    case message: String =>
      warn(s"Message from client web socket $message")
  }

}

object SocketHandlerActor {

  def props(queryId: String): Props =
    Props(classOf[SocketHandlerActor], queryId)


}