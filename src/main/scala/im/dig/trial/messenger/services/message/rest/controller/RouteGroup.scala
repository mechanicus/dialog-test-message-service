package im.dig.trial.messenger.services.message.rest.controller

import java.time.LocalDateTime

import akka.http.scaladsl.server.{PathMatcher, PathMatcher1}
import im.dig.trial.messenger.services.model.SHA256
import im.dig.trial.messenger.services.model.ReadSyntax._
import im.dig.trial.messenger.services.model.JavaInstances._

trait RouteGroup {

  protected val SHA256Segment: PathMatcher1[SHA256] = {
    PathMatcher("""[0-9a-fA-F]{64}""".r).flatMap { string =>
      string.readOption[SHA256]
    }
  }

  protected val LocalDateTimeSegment: PathMatcher1[LocalDateTime] = {
    PathMatcher("""[0-9-:.T]+""".r).flatMap { string =>
      string.readOption[LocalDateTime]
    }
  }

}
