package im.dig.trial.messenger.services.message

import akka.actor.{ActorSystem, Props}
import im.dig.trial.messenger.services.message.actors.MessageServiceActor
import im.dig.trial.messenger.services.message.cluster.{AuthService, AuthServiceClient, CrudServiceClient, MessageServiceApi}
import im.dig.trial.messenger.services.message.rest.MessageService

import scala.concurrent.ExecutionContext

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("MessengerBackend")
    implicit val ec: ExecutionContext = system.dispatcher
    val authServiceClient = system.actorOf(Props[AuthServiceClient], "auth-service-client")
    val crudServiceClient = system.actorOf(Props[CrudServiceClient], "crud-service-client")
    val authService = new AuthService(authServiceClient)
    val messageServiceActor = system.actorOf(Props(classOf[MessageServiceActor], crudServiceClient), "message-service-actor")
    val messageServiceApi = new MessageServiceApi(crudServiceClient, messageServiceActor)
    new MessageService(authService, messageServiceApi).run()
  }
}
