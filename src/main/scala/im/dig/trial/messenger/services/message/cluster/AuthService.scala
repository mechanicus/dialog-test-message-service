package im.dig.trial.messenger.services.message.cluster

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import im.dig.trial.messenger.services.messages.GetUserId
import im.dig.trial.messenger.services.model.{SessionId, UserId}

import scala.concurrent.Future
import scala.concurrent.duration._

final class AuthService(private val authServiceClient: ActorRef) {

  private implicit val timeout: Timeout = Timeout(1.second)

  def getUserId(sessionId: SessionId): Future[Option[UserId]] =
    (authServiceClient ? GetUserId(sessionId)).mapTo[Option[UserId]]

}
