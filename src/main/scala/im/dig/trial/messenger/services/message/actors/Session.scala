package im.dig.trial.messenger.services.message.actors

import java.io.IOException

import akka.NotUsed
import akka.actor.{Actor, ActorSystem, PoisonPill}
import akka.http.scaladsl.model.ws
import akka.http.scaladsl.model.ws.{BinaryMessage, TextMessage}
import akka.stream._
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import argonaut._, Argonaut._
import im.dig.trial.messenger.services.model._
import im.dig.trial.messenger.services.json.JsonCodecs._

import scala.concurrent.Future

// объект уведомления о событии, посылаемом клиенту в сокет
final case class Response(eventName: String, data: Json)
object Response {
  implicit val responseCodec: CodecJson[Response] =
    casecodec2(Response.apply, Response.unapply)("eventName", "data")
}

/**
  * Актор, обслуживающий сессию отдельного подключения пользовательского
  * клиента к сервису
  * Кодирует все приходящие события в json и отправляет клиенту через websocket
  * @param userId - id пользователя
  * @param sessionId - id сессии
  */
final class Session(userId: UserId, sessionId: SessionId) extends Actor {

  private implicit val system: ActorSystem = context.system
  private implicit val mat: ActorMaterializer = ActorMaterializer()

  private val sink: Sink[String, NotUsed] = Sink.actorRef(self, "ConnectionClosed")
  private val (socket, publisher) = Source
    .actorRef[Response](1000, OverflowStrategy.dropHead)
    .toMat(Sink.asPublisher(false))(Keep.both)
    .run()


  override def postStop(): Unit = {
    socket ! PoisonPill
  }

  override def receive: Receive = {
    case "GetFlow" =>
      val innerFlow = Flow.fromSinkAndSource(sink, Source.fromPublisher(publisher))
      val flow: Flow[ws.Message, ws.Message, NotUsed] = decoderFlow via innerFlow via encoderFlow
      sender() ! flow
    case "ConnectionClosed" =>
      context.parent ! (("Disconnected", sessionId))
    case pm: PersonalMessage =>
      socket ! Response("PersonalMessage", pm.asJson)
    case pf: PersonalFile =>
      socket ! Response("PersonalFile", pf.asJson)
    case cm: ChatMessage =>
      socket ! Response("ChatMessage", cm.asJson)
    case cf: ChatFile =>
      socket ! Response("ChatFile", cf.asJson)
  }

  // игнорируем все сообщения от клиента
  // этот websocket только для отправки уведомлений клиенту
  private def decoderFlow(implicit mat: Materializer): Flow[ws.Message, String, NotUsed] = {
    val ignoreErrors: Supervision.Decider = _ => Supervision.Resume
    Flow[ws.Message].mapAsync(1) {
      case tm: TextMessage =>
        tm.textStream.runWith(Sink.ignore)
        Future.failed(new IOException("This websocket is only publisher"))
      case bm: BinaryMessage =>
        bm.dataStream.runWith(Sink.ignore)
        Future.failed(new IOException("This websocket is only publisher"))
    }.withAttributes(ActorAttributes.supervisionStrategy(ignoreErrors))
  }

  // кодируем уведомления в json и отправляем клиету как текст
  private def encoderFlow: Flow[Response, ws.Message, NotUsed] = {
    Flow[Response].map(_.asJson.nospaces).map(TextMessage(_))
  }

}
