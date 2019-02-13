package im.dig.trial.messenger.services.messages

import java.time.LocalDateTime

import im.dig.trial.messenger.services.model._


// здесь хранятся классы сообщений для выполнения
// запросов к другим микросервисам


sealed abstract class AuthServiceMessage
@SerialVersionUID(1L)
final case class GetUserId(sessionId: SessionId) extends AuthServiceMessage



sealed abstract class CrudServiceMessage
sealed abstract class MessageAction extends CrudServiceMessage
sealed abstract class FileAction extends CrudServiceMessage
sealed abstract class PersonalMessageAction extends CrudServiceMessage
sealed abstract class PersonalFileAction extends CrudServiceMessage
sealed abstract class ChatAction extends CrudServiceMessage
sealed abstract class ChatMemberAction extends CrudServiceMessage
sealed abstract class ChatMessageAction extends CrudServiceMessage
sealed abstract class ChatFileAction extends CrudServiceMessage


@SerialVersionUID(1L)
final case class CreateMessage(
  messageId: MessageId,
  ownerId: UserId,
  content: NonEmptyString,
  createdOn: LocalDateTime
) extends MessageAction

@SerialVersionUID(1L)
final case class ReadMessage(
  messageId: MessageId
) extends MessageAction

@SerialVersionUID(1L)
final case class ReadMessages(
  messageIds: Set[MessageId]
) extends MessageAction



@SerialVersionUID(1L)
final case class ReadFiles(
  fileIds: Set[FileId]
) extends FileAction



@SerialVersionUID(1L)
final case class CreatePersonalMessage(
  receiverId: UserId,
  senderId: UserId,
  messageId: MessageId,
  sentOn: LocalDateTime
) extends PersonalMessageAction

@SerialVersionUID(1L)
final case class GetPersonalMessageUpdates(
  userId: UserId,
  from: LocalDateTime
) extends PersonalMessageAction

@SerialVersionUID(1L)
final case class GetPersonalMessageHistory(
  user1: UserId,
  user2: UserId,
  to: LocalDateTime,
  limit: Int
) extends PersonalMessageAction



@SerialVersionUID(1L)
final case class CreatePersonalFile(
  receiverId: UserId,
  senderId: UserId,
  fileId: FileId,
  sentOn: LocalDateTime
) extends PersonalFileAction

@SerialVersionUID(1L)
final case class GetPersonalFileUpdates(
  userId: UserId,
  from: LocalDateTime
) extends PersonalFileAction

@SerialVersionUID(1L)
final case class GetPersonalFileHistory(
  user1: UserId,
  user2: UserId,
  to: LocalDateTime,
  limit: Int
) extends PersonalFileAction



@SerialVersionUID(1L)
final case class CreateChat(
  chatId: ChatId,
  title: NonEmptyString
) extends ChatAction

@SerialVersionUID(1L)
final case class ReadChat(
  chatId: ChatId
) extends ChatAction



@SerialVersionUID(1L)
final case class CreateChatMember(
  chatId: ChatId,
  userId: UserId
) extends ChatMemberAction

@SerialVersionUID(1L)
final case class GetChatMembers(
  chatId: ChatId
) extends ChatMemberAction

@SerialVersionUID(1L)
final case class GetUserChats(
  userId: UserId
) extends ChatMemberAction



@SerialVersionUID(1L)
final case class CreateChatMessage(
  chatId: ChatId,
  senderId: UserId,
  messageId: MessageId,
  sentOn: LocalDateTime
) extends ChatMessageAction

@SerialVersionUID(1L)
final case class GetChatsMessageUpdates(
  chatIds: Set[ChatId],
  from: LocalDateTime
) extends ChatMessageAction

@SerialVersionUID(1L)
final case class GetChatMessageHistory(
  chatId: ChatId,
  to: LocalDateTime,
  limit: Int
) extends ChatMessageAction



@SerialVersionUID(1L)
final case class CreateChatFile(
  chatId: ChatId,
  senderId: UserId,
  fileId: FileId,
  sentOn: LocalDateTime
) extends ChatFileAction

@SerialVersionUID(1L)
final case class GetChatsFileUpdates(
  chatIds: Set[ChatId],
  from: LocalDateTime
) extends ChatFileAction

@SerialVersionUID(1L)
final case class GetChatFileHistory(
  chatId: ChatId,
  to: LocalDateTime,
  limit: Int
) extends ChatFileAction
