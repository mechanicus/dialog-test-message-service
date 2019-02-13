package im.dig.trial.messenger.services.message.cluster

import akka.actor.{Actor, ActorRef, RootActorPath, Terminated}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{MemberEvent, MemberUp}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import im.dig.trial.messenger.services.messages.AuthServiceMessage
import im.dig.trial.messenger.services.model.ServiceUnavailable

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}


/**
  * Актор akka кластера, явлющийся шлюзом доступа к auth-сервису
  */
final class AuthServiceClient extends Actor {

  private val cluster = Cluster(context.system)
  private implicit val ec: ExecutionContext = context.dispatcher
  private implicit val timeout: Timeout = Timeout(1.second)

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberEvent])

  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive: Receive = awaitingForAuthService

  private def awaitingForAuthService: Receive = {
    case MemberUp(member) =>
      if (member.hasRole("auth-service")) {
        val selection = context.actorSelection(RootActorPath(member.address) / "user" / "auth-service")
        selection.resolveOne pipeTo self
      }
    case authService: ActorRef =>
      context.watch(authService)
      context.become(work(authService))
    case _: AuthServiceMessage =>
      Future.failed(ServiceUnavailable("auth-service")) pipeTo sender()
  }

  private def work(authService: ActorRef): Receive = {
    case m: AuthServiceMessage => (authService ? m) pipeTo sender()
    case Terminated(a) =>
      if (a == authService) {
        context.unwatch(authService)
        context.become(awaitingForAuthService)
      }
  }

}
