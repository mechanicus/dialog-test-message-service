package im.dig.trial.messenger.services.model

import cats.implicits._

import scala.util.control.NonFatal

trait Read[A] {
  def read(string: String): A
  def readOption(string: String): Option[A] = try {
    read(string).pure[Option]
  } catch {
    case NonFatal(_) => None
  }
  def readEither(string: String): Either[Exception, A] = try {
    read(string).asRight
  } catch {
    case NonFatal(ex: Exception) => ex.asLeft
  }
}

object ReadSyntax {
  final implicit class ReadOps(private val string: String) extends AnyVal {
    def read[A : Read]: A = implicitly[Read[A]].read(string)
    def readOption[A : Read]: Option[A] = implicitly[Read[A]].readOption(string)
    def readEither[A : Read]: Either[Exception, A] = implicitly[Read[A]].readEither(string)
  }
}
