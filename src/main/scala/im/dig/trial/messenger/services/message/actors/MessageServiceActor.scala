package im.dig.trial.messenger.services.message.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.{ask, pipe}
import im.dig.trial.messenger.services.model._

import scala.concurrent.duration._

final class MessageServiceActor(crudServiceClient: ActorRef) extends Actor with ActorLogging {

  private val users = context.actorOf(Props[Users], "users")
  private val chats = context.actorOf(Props(classOf[Chats], crudServiceClient), "chats")
  import context.dispatcher

  override def receive: Receive = {
    case ("Connected", userId: UserId, sessionId: SessionId) =>
      (users ? (("StartSession", userId, sessionId)))(1.second) pipeTo sender()
    case ("UserConnected", userId: UserId, user: ActorRef) =>
      chats ! (("UserConnected", userId, user))
    case ("UserDisconnected", userId: UserId) =>
      chats ! (("UserDisconnected", userId))
    case pm: PersonalMessage => users ! pm
    case pf: PersonalFile => users ! pf
    case cm: ChatMessage => chats ! cm
    case cf: ChatFile => chats ! cf
    case (userId: UserId, cm: ChatMessage) => users ! ((userId, cm))
    case (userId: UserId, cf: ChatFile) => users ! ((userId, cf))
  }

}
