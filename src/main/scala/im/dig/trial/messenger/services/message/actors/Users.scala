package im.dig.trial.messenger.services.message.actors

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import akka.pattern.{ask, pipe}
import cats.implicits._
import im.dig.trial.messenger.services.model._

import scala.concurrent.duration._

/**
  * Актор, обслуживающий подключения пользователей к сервису
  * Хранит ассоциативный массив userId -> `актор, обслуживающий подключения
  * конкретного пользователя`
  * Распределяет приходящие события по конкретным пользователям
  */
final class Users extends Actor {

  private type UserActor = ActorRef
  import context.dispatcher

  override def receive: Receive = work(Map.empty)

  private def work(users: Map[UserId, UserActor]): Receive = {
    case ("StartSession", userId: UserId, sessionId: SessionId) =>
      users.get(userId) match {
        case Some(user) =>
          (user ? (("StartSession", sessionId)))(1.second) pipeTo sender()
        case None =>
          val user = context.actorOf(Props(classOf[User], userId), userId.show)
          val f = (user ? (("StartSession", sessionId)))(1.second) pipeTo sender()
          val s = sender()
          f.onComplete(_ => s ! (("UserConnected", userId, user)))
          context.become(work(users + (userId -> user)))
      }
    case ("UserDisconnected", userId: UserId) =>
      users.get(userId).foreach { user => user ! PoisonPill }
      val otherUsers = users - userId
      context.parent ! (("UserDisconnected", userId))
      context.become(work(otherUsers))
    case pm: PersonalMessage =>
      users.get(pm.receiverId).foreach { user => user ! pm }
    case pf: PersonalFile =>
      users.get(pf.receiverId).foreach { user => user ! pf }
    case (userId: UserId, cm: ChatMessage) =>
      users.get(userId).foreach { user => user ! cm }
    case (userId: UserId, cf: ChatFile) =>
      users.get(userId).foreach { user => user ! cf }
  }

}
