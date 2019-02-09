package im.dig.trial.messenger.services.message.rest

import java.time.LocalDateTime

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.{ActorMaterializer, Materializer}
import im.dig.trial.messenger.services.message.cluster.{AuthService, MessageServiceApi}
import im.dig.trial.messenger.services.message.rest.controller.marshalling.Marshallers._
import im.dig.trial.messenger.services.message.rest.controller.marshalling.Unmarshallers._
import im.dig.trial.messenger.services.message.rest.controller.{Authentication, CustomExceptionHandler, CustomRejectionHandler, RouteGroup}
import im.dig.trial.messenger.services.message.rest.view.JsonCodecs._
import im.dig.trial.messenger.services.message.rest.view.Responses
import im.dig.trial.messenger.services.model.JavaInstances._
import im.dig.trial.messenger.services.model._



final class MessageService(
  override protected val authService: AuthService,
  private val messageServiceApi: MessageServiceApi
) extends RouteGroup
     with Authentication
     with CustomRejectionHandler
     with CustomExceptionHandler
{

  private def routes: Route = {
    authenticated { (userId, sessionId) =>
      path("websockets" / "connect") {
        onSuccess(messageServiceApi.joinClient(userId, sessionId)) { flow =>
          handleWebSocketMessages(flow)
        }
      } ~
      path("messages") {
        post { formFields('content.as[NonEmptyString]) { content =>
          val messageId = SHA256.generate()
          val createdOn = LocalDateTime.now()
          messageServiceApi.createMessage(messageId, userId, content, createdOn)
          complete(Responses.ok2("messageId", messageId))
        }}
      } ~
      path("message" / SHA256Segment) { messageId =>
        onSuccess(messageServiceApi.getMessage(messageId)) { message =>
          complete(Responses.ok(message))
        }
      } ~
      pathPrefix("conversation" / SHA256Segment) { interlocutorId =>
        path("messages") {
          post { formFields(('receiverId.as[UserId], 'messageId.as[MessageId])) { (receiverId, messageId) =>
            val sentOn = LocalDateTime.now()
            messageServiceApi.sendPersonalMessage(receiverId, userId, messageId, sentOn)
            complete(Responses.ok2("sentOn", sentOn))
          }}
        } ~
        path("files") {
          post { formFields(('receiverId.as[UserId], 'fileId.as[FileId])) { (receiverId, fileId) =>
            val sentOn = LocalDateTime.now()
            messageServiceApi.sendPersonalFile(receiverId, userId, fileId, sentOn)
            complete(Responses.ok2("sentOn", sentOn))
          }}
        } ~
        path("history") {
          get { parameters(('to.as[LocalDateTime], 'limit.as[Int])) { (to, limit) =>
            onSuccess(messageServiceApi.getConversationHistory(userId, interlocutorId, to, limit)) { history =>
              complete(Responses.ok(history))
            }
          }}
        }
      } ~
      path("chats") {
        post { formFields('title.as[NonEmptyString]) { title =>
          val chatId = SHA256.generate()
          messageServiceApi.createChat(chatId, title)
          complete(Responses.ok2("chatId", chatId))
        }}
      } ~
      pathPrefix("chat" / SHA256Segment) { chatId =>
        get {
          onSuccess(messageServiceApi.getChatInfo(chatId)) { chat =>
            complete(Responses.ok(chat))
          }
        } ~
        path("members") {
          get {
            onSuccess(messageServiceApi.getChatMembers(chatId)) { members =>
              complete(Responses.ok(members))
            }
          } ~
          post { formFields('userId.as[UserId]) { userId =>
            messageServiceApi.inviteToChat(chatId, userId)
            complete(Responses.ok2("userId", userId))
          }}
        } ~
        path("messages") {
          post { formFields('messageId.as[MessageId]) { messageId =>
            val sentOn = LocalDateTime.now()
            messageServiceApi.sendChatMessage(chatId, userId, messageId, sentOn)
            complete(Responses.ok2("sentOn", sentOn))
          }}
        } ~
        path("files") {
          post { formFields('fileId.as[FileId]) { fileId =>
            val sentOn = LocalDateTime.now()
            messageServiceApi.sendChatFile(chatId, userId, fileId, sentOn)
            complete(Responses.ok2("sentOn", sentOn))
          }}
        } ~
        path("history") {
          get { parameters(('to.as[LocalDateTime], 'limit.as[Int])) { (to, limit) =>
            onSuccess(messageServiceApi.getChatHistory(chatId, to, limit)) { history =>
              complete(Responses.ok(history))
            }
          }}
        }
      } ~
      pathPrefix("updates") {
        path("from" / LocalDateTimeSegment) { from =>
          onSuccess(messageServiceApi.getUpdates(userId, from)) { updates =>
            complete(Responses.ok(updates))
          }
        }
      }
    }
  }

  def run()(implicit system: ActorSystem): Unit = {
    implicit val mat: Materializer = ActorMaterializer()
    Http().bindAndHandle(routes, "localhost", 8004)
  }

}
