package im.dig.trial.messenger.services.message.cluster

import java.time.LocalDateTime

import akka.NotUsed
import akka.actor.ActorRef
import akka.http.scaladsl.model.ws
import akka.pattern.ask
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import im.dig.trial.messenger.services.messages._
import im.dig.trial.messenger.services.model._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}


final case class Updates(
  messages: Seq[Message],
  files: Seq[File],
  personalMessages: Seq[PersonalMessage],
  personalFiles: Seq[PersonalFile],
  chatMessages: Seq[ChatMessage],
  chatFiles: Seq[ChatFile]
)

final case class ConversationHistory(
  messages: Seq[Message],
  files: Seq[File],
  personalMessages: Seq[PersonalMessage],
  personalFiles: Seq[PersonalFile],
)

final case class ChatHistory(
  messages: Seq[Message],
  files: Seq[File],
  chatMessages: Seq[ChatMessage],
  chatFiles: Seq[ChatFile]
)


final class MessageServiceApi(
  private val crudServiceClient: ActorRef,
  private val messageServiceActor: ActorRef
)(implicit ec: ExecutionContext) {

  private implicit val timeout: Timeout = Timeout(5.seconds)

  def joinClient(userId: UserId, sessionId: SessionId): Future[Flow[ws.Message, ws.Message, NotUsed]] =
    (messageServiceActor ? (("Connected", userId, sessionId)))(1.second).mapTo[Flow[ws.Message, ws.Message, NotUsed]]

  def createChat(chatId: ChatId, title: NonEmptyString): Unit = {
    crudServiceClient ! CreateChat(chatId, title)
  }

  def inviteToChat(chatId: ChatId, userId: UserId): Unit = {
    crudServiceClient ! CreateChatMember(chatId, userId)
  }

  def getChatInfo(chatId: ChatId): Future[Option[Chat]] =
    (crudServiceClient ? ReadChat(chatId)).mapTo[Option[Chat]]

  def getChatMembers(chatId: ChatId): Future[Seq[UserId]] =
    (crudServiceClient ? GetChatMembers(chatId)).mapTo[Seq[UserId]]

  def createMessage(messageId: MessageId, ownerId: UserId, content: NonEmptyString, createdOn: LocalDateTime): Unit = {
    crudServiceClient ! CreateMessage(messageId, ownerId, content, createdOn)
  }

  def getMessage(messageId: MessageId): Future[Option[Message]] =
    (crudServiceClient ? ReadMessage(messageId)).mapTo[Option[Message]]

  def sendPersonalMessage(receiverId: UserId, senderId: UserId, messageId: MessageId, sentOn: LocalDateTime): Unit = {
    crudServiceClient ! CreatePersonalMessage(receiverId, senderId, messageId, sentOn)
    messageServiceActor ! PersonalMessage(receiverId, senderId, messageId, sentOn)
  }

  def sendPersonalFile(receiverId: UserId, senderId: UserId, fileId: FileId, sentOn: LocalDateTime): Unit = {
    crudServiceClient ! CreatePersonalFile(receiverId, senderId, fileId, sentOn)
    messageServiceActor ! PersonalFile(receiverId, senderId, fileId, sentOn)
  }

  def sendChatMessage(chatId: ChatId, senderId: UserId, messageId: MessageId, sentOn: LocalDateTime): Unit = {
    crudServiceClient ! CreateChatMessage(chatId, senderId, messageId, sentOn)
    messageServiceActor ! ChatMessage(chatId, senderId, messageId, sentOn)
  }

  def sendChatFile(chatId: ChatId, senderId: UserId, fileId: FileId, sentOn: LocalDateTime): Unit = {
    crudServiceClient ! CreateChatFile(chatId, senderId, fileId, sentOn)
    messageServiceActor ! ChatFile(chatId, senderId, fileId, sentOn)
  }

  def getUpdates(userId: UserId, from: LocalDateTime): Future[Updates] = {
    val pmsFuture = (crudServiceClient ? GetPersonalMessageUpdates(userId, from)).mapTo[Seq[PersonalMessage]]
    val pfsFuture = (crudServiceClient ? GetPersonalFileUpdates(userId, from)).mapTo[Seq[PersonalFile]]
    val csFuture = (crudServiceClient ? GetUserChats(userId)).mapTo[Seq[ChatId]]
    val cmsFuture = csFuture.flatMap { chatIds =>
      (crudServiceClient ? GetChatsMessageUpdates(chatIds.toSet, from)).mapTo[Seq[ChatMessage]]
    }
    val cfsFuture = csFuture.flatMap { chatIds =>
      (crudServiceClient ? GetChatsFileUpdates(chatIds.toSet, from)).mapTo[Seq[ChatFile]]
    }
    val msFuture = pmsFuture.flatMap { personalMessages =>
      cmsFuture.flatMap { chatMessages =>
        (crudServiceClient ? ReadMessages((personalMessages.map(_.messageId) ++ chatMessages.map(_.messageId)).toSet))
          .mapTo[Seq[Message]]
      }
    }
    val fsFuture = pfsFuture.flatMap { personalFiles =>
      cfsFuture.flatMap { chatFiles =>
        (crudServiceClient ? ReadFiles((personalFiles.map(_.fileId) ++ chatFiles.map(_.fileId)).toSet)).mapTo[Seq[File]]
      }
    }
    for {
      personalMessages <- pmsFuture
      personalFiles <- pfsFuture
      chatMessages <- cmsFuture
      chatFiles <- cfsFuture
      messages <- msFuture
      files <- fsFuture
    } yield Updates(messages, files, personalMessages, personalFiles, chatMessages, chatFiles)
  }

  def getConversationHistory(me: UserId, interlocutor: UserId, to: LocalDateTime, limit: Int): Future[ConversationHistory] = {
    val pmsFuture = (crudServiceClient ? GetPersonalMessageHistory(me, interlocutor, to, limit)).mapTo[Seq[PersonalMessage]]
    val pfsFuture = (crudServiceClient ? GetPersonalFileHistory(me, interlocutor, to, limit)).mapTo[Seq[PersonalFile]]
    val msFuture = pmsFuture.flatMap { personalMessages =>
      (crudServiceClient ? ReadMessages(personalMessages.map(_.messageId).toSet)).mapTo[Seq[Message]]
    }
    val fsFuture = pfsFuture.flatMap { personalFiles =>
      (crudServiceClient ? ReadFiles(personalFiles.map(_.fileId).toSet)).mapTo[Seq[File]]
    }
    for {
      personalMessages <- pmsFuture
      personalFiles <- pfsFuture
      messages <- msFuture
      files <- fsFuture
    } yield ConversationHistory(messages, files, personalMessages, personalFiles)
  }

  def getChatHistory(chatId: ChatId, to: LocalDateTime, limit: Int): Future[ChatHistory] = {
    val cmsFuture = (crudServiceClient ? GetChatMessageHistory(chatId, to, limit)).mapTo[Seq[ChatMessage]]
    val cfsFuture = (crudServiceClient ? GetChatFileHistory(chatId, to, limit)).mapTo[Seq[ChatFile]]
    val msFuture = cmsFuture.flatMap { chatMessages =>
      (crudServiceClient ? ReadMessages(chatMessages.map(_.messageId).toSet)).mapTo[Seq[Message]]
    }
    val fsFuture = cfsFuture.flatMap { chatFiles =>
      (crudServiceClient ? ReadFiles(chatFiles.map(_.fileId).toSet)).mapTo[Seq[File]]
    }
    for {
      chatMessages <- cmsFuture
      chatFiles <- cfsFuture
      messages <- msFuture
      files <- fsFuture
    } yield ChatHistory(messages, files, chatMessages, chatFiles)
  }

}
