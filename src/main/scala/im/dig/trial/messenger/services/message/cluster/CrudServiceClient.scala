package im.dig.trial.messenger.services.message.cluster

import akka.actor.{Actor, ActorRef, RootActorPath, Terminated}
import akka.pattern.ask
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{MemberEvent, MemberUp}
import akka.pattern.pipe
import akka.util.Timeout
import im.dig.trial.messenger.services.messages.CrudServiceMessage
import im.dig.trial.messenger.services.model.ServiceUnavailable

import scala.concurrent.Future
import scala.concurrent.duration._


final class CrudServiceClient extends Actor {

  private val cluster = Cluster(context.system)
  private implicit val timeout: Timeout = Timeout(1.second)
  import context.dispatcher

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberEvent])

  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive: Receive = awaitingForCrudService

  private def awaitingForCrudService: Receive = {
    case MemberUp(member) =>
      if (member.hasRole("crud-service")) {
        val selection = context.actorSelection(RootActorPath(member.address) / "user" / "crud-service")
        selection.resolveOne pipeTo self
      }
    case _: CrudServiceMessage =>
      Future.failed(ServiceUnavailable("crud-service")) pipeTo sender()
    case crudService: ActorRef =>
      context.watch(crudService)
      context.become(work(crudService))
  }

  private def work(crudService: ActorRef): Receive = {
    case m: CrudServiceMessage => (crudService ? m) pipeTo sender()
    case Terminated(a) =>
      if (a == crudService) {
        context.unwatch(crudService)
        context.become(awaitingForCrudService)
      }
  }

}
