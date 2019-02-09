package im.dig.trial.messenger.services.message.rest.controller

import java.net.HttpURLConnection._

import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.ExceptionHandler
import im.dig.trial.messenger.services.message.rest.controller.marshalling.Marshallers._
import im.dig.trial.messenger.services.message.rest.view.JsonCodecs._
import im.dig.trial.messenger.services.message.rest.view.Responses
import im.dig.trial.messenger.services.model.ServiceUnavailable

/** Кастомный обработчик для отрисовки исключений в стандартном json-формате */
trait CustomExceptionHandler {

  protected implicit val customExceptionHandler: ExceptionHandler = ExceptionHandler {
    case ServiceUnavailable(serviceName) =>
      complete(Responses.error(HTTP_UNAVAILABLE, "UNAVAILABLE", s"$serviceName is unavailable"))
    case ex => complete(Responses.internalError(ex))
  }

}
