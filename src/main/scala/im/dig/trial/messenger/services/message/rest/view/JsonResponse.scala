package im.dig.trial.messenger.services.message.rest.view

import java.net.HttpURLConnection._


/** Базовый класс, представляющий HTTP-отклик API в формате json */
sealed abstract class JsonResponse[+A] {
  def code: Int
  def status: String
}

/** Успешный отклик (HTTP коды 2XX-3XX) */
private[view]
final case class Success[+A] (
  override val code: Int,
  override val status: String,
  result: A
) extends JsonResponse[A]

/** Отклик с ошибкой (HTTP коды 4XX-5XX) */
private[view]
final case class Error (
  override val code: Int,
  override val status: String,
  message: String
) extends JsonResponse[String]


object Responses {

  def ok[A](value: A): JsonResponse[A] = success(HTTP_OK, "OK", value)

  def ok2[A](name: String, value: A): JsonResponse[Map[String, A]] = ok(Map(name -> value))

  def unauthorized(message: String): JsonResponse[String] = error(HTTP_UNAUTHORIZED, "UNAUTHORIZED", message)

  def notFound(message: String): JsonResponse[String] = error(HTTP_NOT_FOUND, "NOT_FOUND", message)

  def badRequest(message: String): JsonResponse[String] = error(HTTP_BAD_REQUEST, "BAD_REQUEST", message)

  def internalError(ex: Throwable): JsonResponse[String] = error(HTTP_INTERNAL_ERROR, "INTERNAL_ERROR", ex.getMessage)

  def success[A](code: Int, status: String, result: A): JsonResponse[A] = Success(code, status, result)

  def error(code: Int, status: String, message: String): JsonResponse[String] = Error(code, status, message)

}
