import User.{BaseDeviceInfo, BaseUserInfo}
import User.UserManager.{Credential, LoginInfo}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

package object Debug {
    val testuserinfo=BaseUserInfo("test",credential = Credential("test"),defaultDeviceName = "debug",devices = List(BaseDeviceInfo("debug","debug")))

    def RegisterTestUser():Future[String]={
        User.UserManager.Signup(testuserinfo) transform {
            case Success(value)=>
                println("reg test user success:"+value)
                Success(value)
            case scala.util.Failure(exception)=>
                println("reg test user failure:"+exception.getMessage)
                Success("")
        }
    }

    def LoginTestUser():Future[String]={
        User.UserManager.Signin(LoginInfo(username = "test",deviceName = "debug",credential = Credential("test"))) andThen {
            case scala.util.Success(value)=>
                println("login success, session: "+value)
            case scala.util.Failure(exception)=>
                println("login fail:"+exception.getMessage)
        }
    }

    class DebugDevice{
        def Login():Future[String]={
            ???
        }
    }
}
