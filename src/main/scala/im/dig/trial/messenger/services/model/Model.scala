package im.dig.trial.messenger.services.model

import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util

import cats._
import cats.implicits._
import org.apache.commons.codec.binary.Hex


// здесь собраны классы общей для всех микросервисов модели
// подробное описание в CRUD-сервисе


@SerialVersionUID(1L)
final case class SHA256(bytes: Array[Byte])

object SHA256 {
  private val random = new SecureRandom()
  def generate(): SHA256 = {
    val bytes = Array.fill[Byte](32)(0)
    random.nextBytes(bytes)
    SHA256(bytes)
  }
  implicit val showSHA256: Show[SHA256] =
    sha256 => Hex.encodeHexString(sha256.bytes)
  implicit val readSHA256: Read[SHA256] = string => {
    if (string.length =!= 64)
      throw new IllegalArgumentException(s"String '$string' is not a SHA256 hash")
    SHA256(Hex.decodeHex(string))
  }
  implicit val eqSHA256: Eq[SHA256] = (x, y) => util.Arrays.equals(x.bytes, y.bytes)
}



@SerialVersionUID(1L)
final case class Nickname(value: String)

object Nickname {
  implicit val readNickname: Read[Nickname] = { string =>
    if (string.length < 3)
      throw new IllegalArgumentException("Nickname should contain at least 3 characters")
    if (string.exists(!_.isLetterOrDigit))
      throw new IllegalArgumentException("Nickname should be alphanumeric")
    Nickname(string)
  }
  implicit val showNickname: Show[Nickname] = _.value
}



@SerialVersionUID(1L)
final case class NonEmptyString(value: String)

object NonEmptyString {
  implicit val readNonEmptyString: Read[NonEmptyString] = { string =>
    if (string.isEmpty)
      throw new IllegalArgumentException("String should be non empty")
    NonEmptyString(string)
  }
  implicit val showNonEmptyString: Show[NonEmptyString] = _.value
}



@SerialVersionUID(1L)
final case class Filename(value: String)

object Filename {
  private val invalidFilenameCharacters = """\/:*?"<>|""".toSet + '\u0000'
  implicit val readFilename: Read[Filename] = { string =>
    if (string.exists(char => invalidFilenameCharacters.contains(char)))
      throw new IllegalArgumentException("The filename contains invalid characters")
    Filename(string)
  }
  implicit val showFilename: Show[Filename] = _.value
}



@SerialVersionUID(1L)
final case class User (
  userId: UserId,
  nickname: Nickname
)

@SerialVersionUID(1L)
final case class Session (
  sessionId: SessionId,
  userId: UserId
)

@SerialVersionUID(1L)
final case class Message (
  messageId: MessageId,
  ownerId: UserId,
  content: NonEmptyString,
  createdOn: LocalDateTime
)

@SerialVersionUID(1L)
final case class File (
  fileId: FileId,
  ownerId: UserId,
  originalName: Filename,
  uploadedOn: LocalDateTime
)

@SerialVersionUID(1L)
final case class PersonalMessage (
  receiverId: UserId,
  senderId: UserId,
  messageId: MessageId,
  sentOn: LocalDateTime
)

@SerialVersionUID(1L)
final case class PersonalFile (
  receiverId: UserId,
  senderId: UserId,
  fileId: FileId,
  sentOn: LocalDateTime
)

@SerialVersionUID(1L)
final case class Chat (
  chatId: ChatId,
  title: NonEmptyString
)

@SerialVersionUID(1L)
final case class ChatMember (
  chatId: ChatId,
  userId: UserId
)

@SerialVersionUID(1L)
final case class ChatMessage (
  chatId: ChatId,
  senderId: UserId,
  messageId: MessageId,
  sentOn: LocalDateTime
)

@SerialVersionUID(1L)
final case class ChatFile (
  chatId: ChatId,
  senderId: UserId,
  fileId: FileId,
  sentOn: LocalDateTime
)


final case class ServiceUnavailable(serviceName: String)
  extends RuntimeException(s"Service '$serviceName' is unavailable.")


object JavaInstances {

  private val localDateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

  implicit val readLocalDateTime: Read[LocalDateTime] =
    string => LocalDateTime.parse(string, localDateTimeFormatter)
  implicit val showLocalDateTime: Show[LocalDateTime] =
    localDateTime => localDateTime.format(localDateTimeFormatter)

}
