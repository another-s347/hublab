import Notification.Message
import Notification.Message.Destination.{DstType, NotlDestination}
import Notification.Message.Notl.NotificationMessage
import User.{BaseDeviceInfo, BaseUserInfo}
import User.UserManager.Credential

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object Main{
    def main(args: Array[String]): Unit = {
        Global.DB.Load() andThen {
            case Success(value)=>
                Core.Load() onComplete {
                    case Success(value)=>
                        Debug.RegisterTestUser() flatMap (_=>{
                            Debug.LoginTestUser()
                        }) onComplete (_ =>
                            println("up"))
                    case Failure(exception)=>
                        print("down")
                }
        }

        import scala.io.StdIn._
        readLine()
    }
}