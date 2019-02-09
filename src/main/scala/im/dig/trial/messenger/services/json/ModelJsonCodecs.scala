package im.dig.trial.messenger.services.json

import argonaut._, Argonaut._
import im.dig.trial.messenger.services.model._

trait ModelJsonCodecs extends BaseCodecs {

  import JavaInstances._

  implicit val messageCodec: CodecJson[Message] =
    casecodec4(Message.apply, Message.unapply)("messageId", "ownerId", "content", "createdOn")

  implicit val fileCodec: CodecJson[File] =
    casecodec4(File.apply, File.unapply)("fileId", "ownerId", "originalName", "uploadedOn")

  implicit val personalMessageCodec: CodecJson[PersonalMessage] =
    casecodec4(PersonalMessage.apply, PersonalMessage.unapply)("receiverId", "senderId", "messageId", "sentOn")

  implicit val personalFileCodec: CodecJson[PersonalFile] =
    casecodec4(PersonalFile.apply, PersonalFile.unapply)("receiverId", "senderId", "fileId", "sentOn")

  implicit val chatCodec: CodecJson[Chat] =
    casecodec2(Chat.apply, Chat.unapply)("chatId", "title")

  implicit val chatMessageCodec: CodecJson[ChatMessage] =
    casecodec4(ChatMessage.apply, ChatMessage.unapply)("chatId", "senderId", "messageId", "sentOn")

  implicit val chatFileCodec: CodecJson[ChatFile] =
    casecodec4(ChatFile.apply, ChatFile.unapply)("chatId", "senderId", "fileId", "sentOn")

}
