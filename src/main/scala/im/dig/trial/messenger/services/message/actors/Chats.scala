package im.dig.trial.messenger.services.message.actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.pattern.ask
import im.dig.trial.messenger.services.messages.GetChatMembers
import im.dig.trial.messenger.services.model._

import scala.concurrent.duration._
import scala.util.{Failure, Success}



// на самом деле идея была материализовывать здесь чаты для всех
// подключенных пользователей и отсюда слать им напрямую сообщения
// чатов, но на такую реализацию у меня не хватило времени, поэтому
// сейчас здесь мы просто на каждое сообщение чата получаем список
// пользователей чата напрямую из базы и рассылаем им сообщения

final class Chats(private val crudServiceClient: ActorRef) extends Actor with ActorLogging {

  import context.dispatcher

  override def receive: Receive = work(Map.empty)

  private def work(chatUsers: Map[ChatId, Seq[(UserId, ActorRef)]]): Receive = {
    case ("UserConnected", userId: UserId, user: ActorRef) =>
    case ("UserDisconnected", userId: UserId) =>
    case cm: ChatMessage =>
      forEachChatMember(cm.chatId) { userId =>
        context.parent ! ((userId, cm))
      }
    case cf: ChatFile =>
      forEachChatMember(cf.chatId) { userId =>
        context.parent ! ((userId, cf))
      }

  }

  private def forEachChatMember(chatId: ChatId)(f: UserId => Unit): Unit =
    (crudServiceClient ? GetChatMembers(chatId))(5.seconds)
      .mapTo[Seq[UserId]]
      .onComplete {
        case Success(userIds) =>
          userIds.foreach { userId => f(userId) }
        case Failure(exception) =>
          log.error(exception, "Cannot get chat members.")
      }
}
