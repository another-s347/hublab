package User

import io.vertx.lang.scala.json.{Json, JsonObject}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object UserManager {

    def JsonToLoginInfo(j: JsonObject): LoginInfo = {
        LoginInfo(Credential(j.getString("password")), j.getString("username"), j.getString("deviceName"))
    }

    def Signin(loginInfo: LoginInfo): Future[Session.SessionId] = {
        Global.DB.User.GetUserInfo(loginInfo.username) flatMap ((userInfo: BaseUserInfo) => Future {
            if (userInfo.credential.password != loginInfo.credential.password) //TODO: enhance verify method
                throw new Exception("password not correct")
            userInfo
        }) flatMap (userInfo => {
            Session.IsUserOnline(loginInfo.username) flatMap (s => {
                if (s) Session.RegisterDevice(loginInfo.username, loginInfo)
                else Session.RegisterUser(userInfo, loginInfo)
            })
        })
    }

    def Signup(baseUserInfo: BaseUserInfo): Future[String] = {
        Global.DB.User.IsUserExist(baseUserInfo.name) flatMap (isExist => {
            if (isExist) Future.failed(new Exception(s"user ${baseUserInfo.name} already exist"))
            else Global.DB.User.SetUserInfo(baseUserInfo)
        })
    }

    case class LoginInfo(credential: Credential, username: String, deviceName: String)

    case class Credential(password: String) {
        def toJson: JsonObject = {
            Json.emptyObj().put("Password", password)
        }
    }

    object Credential {
        def fromJson(j: JsonObject): Credential = {
            Credential(j.getString("Password"))
        }
    }
}
