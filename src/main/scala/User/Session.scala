package User

import User.UserManager.LoginInfo
import io.vertx.scala.ext.web.handler.sockjs.SockJSSocket

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable
import scala.concurrent.Future
import scala.util.{Failure, Success}

//TODO: identity cache
object Session {
    //Session ID = Username + Device Runtime ID
    type SessionId = String

    val onlineUsersLocker = new Object
    val onlineUsers = new mutable.HashMap[String, UserObject]()

    case class Identity(User:UserObject,Device:DeviceObject)

    def GetOnlineIdentity(sessionId: String):Future[Identity]={
        val s = sessionId.split('|')
        val deviceId = s(1)
        val username = s(0)
        GetOnlineUser(username) flatMap { user=> Future{
            user.deviceByID(deviceId)
        } transform {
            case Success(value)=>Success(Identity(user,value))
            case Failure(exception)=>
                Failure(exception)
        }}
    }

    def GetOnlineUser(username:String):Future[UserObject]=Future {
        onlineUsersLocker.synchronized {
            if(onlineUsers.contains(username)){
                onlineUsers(username)
            }
            else
                throw new Exception("user is offline")
        }
    }

    def IsUserOnline(username: String): Future[Boolean] = Future {
        onlineUsersLocker.synchronized {
            onlineUsers.contains(username)
        }
    }

    def CheckSessionId(sessionId: String): Future[UserObject] = Future {
        val s = sessionId.split('|')
        val deviceId = s(1)
        val username = s(0)
        onlineUsersLocker.synchronized {
            onlineUsers(username)
        }
    }

    def RegisterDeviceConnection(connection: SockJSSocket, session: String) = Future {
        val s = session.split('|')
        val deviceId = s(1)
        val username = s(0)
        val userObj = onlineUsersLocker.synchronized {
            onlineUsers(username)
        }
        userObj.SetConnection(deviceId, connection)
    }

    def UnregisterDeviceConnection(session: String) = Future {
        val s = session.split('|')
        val deviceId = s(1)
        val username = s(0)
        val userObj = onlineUsersLocker.synchronized {
            onlineUsers(username)
        }
        userObj.UnsetConnection(deviceId)
    }

    def RegisterDevice(username: String, loginInfo: LoginInfo): Future[SessionId] = Future {
        val userObj = onlineUsersLocker.synchronized {
            onlineUsers(username)
        }
        val deviceTmpId=userObj.registerDevice(loginInfo.deviceName)
        s"$username|$deviceTmpId"
    }

    def RegisterUser(baseUserInfo: BaseUserInfo, loginInfo: LoginInfo): Future[SessionId] = Future {
        val userObj = new UserObject(baseUserInfo)
        val username = baseUserInfo.name
        val deviceTmpId = onlineUsersLocker.synchronized {
            onlineUsers.put(username, userObj)
            if (username == "test") {
                userObj.registerDevice(loginInfo.deviceName, Some(loginInfo.deviceName))
            }
            else
                userObj.registerDevice(loginInfo.deviceName)
        }
        if (deviceTmpId.isEmpty) {
            throw new Exception("device temp id is empty")
        }
        else {
            s"$username|$deviceTmpId"
        }
    }
}