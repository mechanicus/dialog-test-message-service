package im.dig.trial.messenger.services.message.rest.controller

import akka.http.scaladsl.model.headers.HttpCookiePair
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import im.dig.trial.messenger.services.message.cluster.AuthService
import im.dig.trial.messenger.services.message.rest.controller.marshalling.Marshallers._
import im.dig.trial.messenger.services.message.rest.view.JsonCodecs._
import im.dig.trial.messenger.services.message.rest.view.Responses
import im.dig.trial.messenger.services.model.ReadSyntax._
import im.dig.trial.messenger.services.model.{SessionId, UserId}


trait Authentication {

  protected def authService: AuthService

  protected def authenticated(route: (UserId, SessionId) => Route): Route = {
    cookie("sessionId") {
      case HttpCookiePair(_, sessionIdString) =>
        sessionIdString.readEither[SessionId] match {
          case Right(sessionId) =>
            onSuccess(authService.getUserId(sessionId)) {
              case Some(userId) => route(userId, sessionId)
              case None => complete(Responses.unauthorized("unknown user"))
            }
          case Left(ex) => complete(Responses.badRequest(s"Cannot recognize sessionId cookie. ${ex.getMessage}"))
        }
    }
  }
}
