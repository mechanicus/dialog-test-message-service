package im.dig.trial.messenger.services.message.rest.controller

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.RejectionHandler
import argonaut.Argonaut._
import im.dig.trial.messenger.services.message.rest.view.JsonCodecs._
import im.dig.trial.messenger.services.message.rest.view.Responses

/**
  * Кастомный `RejectionHandler`, который мапит стандартные сообщения
  * фреймворка об ошибках HTTP в json формат.
  */
trait CustomRejectionHandler {

  protected implicit val rejectionHandler: RejectionHandler = RejectionHandler.default.mapRejectionResponse {
    case response@HttpResponse(_, _, entity: HttpEntity.Strict, _) =>
      val code = response.status.intValue
      val status = response.status.reason.toUpperCase.replaceAll(" ", "_")
      val message = entity.data.utf8String
      response.withEntity(ContentTypes.`application/json`, Responses.error(code, status, message).asJson.spaces4)
    case other => other
  }

}
