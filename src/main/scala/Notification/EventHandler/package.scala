package Notification

import Exception.MessageConvertException
import io.vertx.core.Handler
import io.vertx.lang.scala.json.{Json, JsonObject}
import io.vertx.scala.core.eventbus.Message

package object EventHandler {
//    val hubRegister: Handler[Message[JsonObject]] = (message: io.vertx.scala.core.eventbus.Message[JsonObject]) =>
//        try {
//            val request = message.body()
//            val sourceName = request.getString("name")
//            val sessionId = Core.SetSource(sourceName)
//            message.reply(Json.emptyObj().put("result",true).put("session",sessionId))
//        }
//        catch {
//            case e: Exception.HubSessionException =>
//                message.reply(Json.emptyObj().put("result",false).put("error",e.getMessage))
//            case e: MessageConvertException =>
//                message.reply(Json.emptyObj().put("result",false).put("error","request format is not correct:" + e.getMessage))
//        }
//
//    val taskRegister: Handler[Message[JsonObject]] = (message: io.vertx.scala.core.eventbus.Message[JsonObject]) =>
//        try {
//            val request = Message.HubRequest().fromJson(message.body())
//            Core.GetSourceFrom(request.session)
//            val dst = Message.Destination.NotlDestination().fromJson(request.content.getJsonObject("destination"))
//            val id = NotificationTask.Make(NotificationTask.GenerateIterator(dst),destination=dst).taskId
//            message.reply(Json.emptyObj().put("result",true).put("taskId",id))
//        }
//        catch {
//            case e: HubException.HubSessionException =>
//                message.reply(Json.emptyObj().put("result",false).put("error",e.getMessage))
//            case e: MessageConvertException =>
//                message.reply(Json.emptyObj().put("result",false).put("error","request format is not correct:" + e.getMessage))
//        }
//
//    val notificationPush: Handler[Message[JsonObject]] = (message: io.vertx.scala.core.eventbus.Message[JsonObject]) =>
//        try {
//            val request = HubMessage.HubRequest().fromJson(message.body())
//            val source = Core.GetSourceFrom(request.session)
//            val m = HubMessage.Push.PushMessage().fromJson(request.content)
//            val finalMsg = m.toNotlMessage(source)
//            NotificationTask.MakeWithMessage(NotificationTask.GenerateIterator(m.dst), finalMsg,destination = m.dst).Start()
//            message.reply(Json.emptyObj().put("result",true))
//        }
//        catch {
//            case e: HubException.HubSessionException =>
//                message.reply(Json.emptyObj().put("result",false).put("error",e.getMessage))
//            case e: MessageConvertException =>
//                message.reply(Json.emptyObj().put("result",false).put("error","request format is not correct:" + e.getMessage))
//        }
//
//    val notificationPushToTask: Handler[Message[JsonObject]] = (message: io.vertx.scala.core.eventbus.Message[JsonObject]) =>
//        try {
//            val request = HubMessage.HubRequest().fromJson(message.body())
//            val source = Core.GetSourceFrom(request.session)
//            val notificationTaskId = request.content.getString("taskId")
//            val m = HubMessage.Push.PushMessage().fromJson(request.content.getJsonObject("message"))
//            val finalMsg = m.toNotlMessage(source)
//            if(NotificationTask.PushToTask(notificationTaskId, finalMsg)){
//                message.reply(Json.emptyObj().put("result",true))
//            }
//            else{
//                message.reply(Json.emptyObj().put("result",false).put("error",s"task $notificationTaskId already a message"))
//            }
//        }
//        catch {
//            case e: HubSessionException =>
//                message.reply(Json.emptyObj().put("result",false).put("error",e.getMessage))
//            case e: MessageConvertException =>
//                message.reply(Json.emptyObj().put("result",false).put("error","request format is not correct:" + e.getMessage))
//            case e: NotlTaskException =>
//                message.reply(Json.emptyObj().put("result",false).put("error",e.getMessage))
//        }
}
