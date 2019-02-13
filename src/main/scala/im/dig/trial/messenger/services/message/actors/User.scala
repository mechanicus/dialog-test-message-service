package im.dig.trial.messenger.services.message.actors

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import akka.pattern.{ask, pipe}
import im.dig.trial.messenger.services.model._

import scala.concurrent.duration._

/**
  * Актор, обрабатывающий подключения конкретного пользователя к сервису
  * Хранит ассоциативный массив sessionId -> `актор, обслуживающий отдельное
  * подключение клиента к сервису (сессия)`
  * Распределяет пользовательские события по сессиям клиентов
  * @param userId - id пользователя
  */
final class User(userId: UserId) extends Actor {

  import context.dispatcher

  override def receive: Receive = work(Map.empty)

  private def work(sessions: Map[SessionId, ActorRef]): Receive = {
    case ("StartSession", sessionId: SessionId) =>
      val session = context.actorOf(Props(classOf[Session], userId, sessionId))
      (session ? "GetFlow")(1.second) pipeTo sender()
      context.become(work(sessions + (sessionId -> session)))
    case ("Disconnected", sessionId: SessionId) =>
      sessions.get(sessionId).foreach { session => session ! PoisonPill }
      val otherSessions = sessions - sessionId
      if (otherSessions.isEmpty) {
        context.parent ! (("UserDisconnected", userId))
      } else {
        context.become(work(otherSessions))
      }
    case pm: PersonalMessage =>
      sessions.values.foreach { session => session ! pm }
    case pf: PersonalFile =>
      sessions.values.foreach { session => session ! pf }
    case cm: ChatMessage =>
      sessions.values.foreach { session => session ! cm }
    case cf: ChatFile =>
      sessions.values.foreach { session => session ! cf }
  }

}
