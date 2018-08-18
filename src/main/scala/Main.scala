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
                Global.DB.User.SetUserInfo(
                    BaseUserInfo("test",credential = Credential("test"),defaultDeviceName = "host",devices = List(BaseDeviceInfo("host","pc"),BaseDeviceInfo("node","test")))
                ) andThen {
                    case Success(value)=>
                        Core.Load() onComplete {
                            case Success(value)=>
                                println("up")
//                                Thread.sleep(5000)
//                                Notification.Send("main",
//                                    Message.Push.PushMessage(NotlDestination("test",Array("node"),DstType.USERDEVICE),NotificationMessage("","test message","test message detail"))
//                                ) onComplete {
//                                    case Success(value)=>
//                                        println("send message"+value)
//                                    case Failure(exception)=>
//                                        print(exception)
//                                }
                            case Failure(exception)=>
                                print("down")
                        }
                }
        }

        import scala.io.StdIn._
        readLine()
    }
}