package im.dig.trial.messenger.services.message.rest.view

import argonaut.Argonaut._
import argonaut._
import im.dig.trial.messenger.services.json.ModelJsonCodecs
import im.dig.trial.messenger.services.message.cluster.{ChatHistory, ConversationHistory, Updates}

object JsonCodecs extends EncodeJsons with ModelJsonCodecs {

  implicit def encodeJsonResponse[A : EncodeJson]: EncodeJson[JsonResponse[A]] = EncodeJson {
    case Success(code, status, result) =>
      ("code" := code) ->:
        ("status" := status) ->:
        ("result" := result) ->:
        jEmptyObject
    case Error(code, status, messages) =>
      ("code" := code) ->:
        ("status" := status) ->:
        ("messages" := messages) ->:
        jEmptyObject
  }

  implicit val conversationHistoryCodec: CodecJson[ConversationHistory] =
    casecodec4(ConversationHistory.apply, ConversationHistory.unapply)("messages", "files", "personalMessages", "personalFiles")

  implicit val chatHistoryCodec: CodecJson[ChatHistory] =
    casecodec4(ChatHistory.apply, ChatHistory.unapply)("messages", "files", "chatMessages", "chatFiles")

  implicit val updatesCodec: CodecJson[Updates] =
    casecodec6(Updates.apply, Updates.unapply)("messages", "files", "personalMessages", "personalFiles", "chatMessages", "chatFiles")

}
