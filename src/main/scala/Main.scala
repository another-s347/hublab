import User.{BaseDeviceInfo, BaseUserInfo}
import User.UserManager.Credential

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object Main{
    def main(args: Array[String]): Unit = {
        Global.DB.Load() andThen {
            case Success(value)=>
                Global.DB.User.SetUserInfo(
                    BaseUserInfo("test",credential = Credential("test"),defaultDeviceName = "host",devices = List(BaseDeviceInfo("host","pc"),BaseDeviceInfo("node","test")))
                ) andThen {
                    case Success(value)=>
                        Core.Load() onComplete {
                            case Success(value)=>
                                print("up")
                            case Failure(exception)=>
                                print("down")
                        }
                }
        }

        import scala.io.StdIn._
        readLine()
    }
}