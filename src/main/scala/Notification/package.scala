import java.util.UUID

import Notification.Message.Destination
import Notification.Message.Notl.NotificationMessage
import _root_.Data.File
import io.vertx.lang.scala.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

package object Notification {
    def Send(source: String, request: Message.Push.PushMessage): Future[String] = {
        request.dst.typ match {
            case Destination.DstType.BROADCAST => ???
            case Destination.DstType.USER => SendToUser(source, request)
            case Destination.DstType.DEVICES => ???
            case Destination.DstType.USERDEVICE => SendToUserDevice(source, request)
        }
    }

    private def SendToUser(source: String, request: Message.Push.PushMessage): Future[String] = {
        val username = request.dst.user
        Global.DB.User.IsUserExist(username) flatMap { b =>
            if (b) {
                val message = request.msg
                message.source = source
                val messageID = UUID.randomUUID().toString
                message.globalMessageUID = messageID
                val messageFilename = messageID
                val file = File(fileName = messageFilename, prefix = "notification", data = message.toJson(), user = username)
                Global.DB.Data.SetFile(file) flatMap { _ => User.Session.GetOnlineUser(username) } flatMap { user =>
                    user.deviceByID.view.filter(b => b._2.connection.isDefined).foreach(d => {
                        d._2.connection.get.write(message.toJson().toBuffer)
                    })
                    Future.successful(messageID)
                }
            }
            else {
                Future.failed(new Exception("user do not exist"))
            }
        }
    }

    private def _SendToUserDevice(username: String, deviceName: String, message: NotificationMessage): Future[String] = {
        Global.DB.User.IsUserExist(username) flatMap { b =>
            if (b) {
                val messageFilename = message.globalMessageUID
                val messageID = message.globalMessageUID
                User.Session.IsUserOnline(username) flatMap { ifOnline => {
                    if (ifOnline) { // User is online
                        val user = User.Session.onlineUsers(username)
                        if (user.baseUserInfo.devices.exists(b => b.name == deviceName)) { // Device exist
                            user.deviceByID.view.find(b => b._2.deviceConfig.name == deviceName && b._2.connection.isDefined) match {
                                case Some(value) => // Device is online
                                    value._2.connection.get.write(message.toJson().toBuffer)
                                    Future.successful(messageID)
                                case None => // Device is offline, save message to file
                                    val file = File(fileName = messageFilename, prefix = s"notification-$deviceName", data = message.toJson(), user = username)
                                    Global.DB.Data.SetFile(file) transform {
                                        case Success(_) => Success(messageID)
                                    }
                            }
                        }
                        else { // Device do not exist
                            Future.failed(new Exception("device do not exist"))
                        }
                    }
                    else { // User is offline, save message to file
                        val file = File(fileName = messageFilename, prefix = s"notification-$deviceName", data = message.toJson(), user = username)
                        Global.DB.Data.SetFile(file) transform {
                            case Success(_) => Success(messageID)
                        }
                    }
                }
                }
            }
            else {
                Future.failed(new Exception("user do not exist"))
            }
        }
    }

    private def SendToUserDevice(source: String, request: Message.Push.PushMessage): Future[String] = {
        val deviceName = request.dst.devices.head
        val username = request.dst.user
        val message: NotificationMessage = request.msg
        message.source = source
        val messageID = UUID.randomUUID().toString
        message.globalMessageUID = messageID
        _SendToUserDevice(username, deviceName, message)
    }

    def ResendToUserDevice(user: String, deviceName: String): Future[Unit] = {
        Global.DB.go(connection => {
            connection.findFuture("file", Json.emptyObj().put("prefix", s"notification-$deviceName").put("user", user))
        }) flatMap { s =>
            val t: List[Future[Unit]] = s.toList.map(value => {
                val message = value.getJsonObject("data")
                val notlmsg = NotificationMessage().fromJson(message)
                _SendToUserDevice(user, deviceName, notlmsg) transform {
                    case Success(_) => println(s"resend message ${notlmsg.globalMessageUID} to user:$user device:$deviceName")
                        Success()
                } flatMap { _=>
                    Global.DB.go(connection=>{
                        connection.findOneAndDeleteFuture("file",Json.emptyObj().put("fileName",value.getString("fileName")))
                    }) transform { case Success(_)=>Success() }
                }
            })
            Future.sequence(t) transform {
                case Success(_) => Success()
            }
        }
    }
}
