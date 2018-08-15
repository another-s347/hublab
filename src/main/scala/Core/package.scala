import io.vertx.core.logging.Logger
import io.vertx.scala.core.http.HttpServer
import io.vertx.scala.core.parsetools.JsonParser
import io.vertx.scala.ext.web.handler.StaticHandler
import io.vertx.scala.ext.web.handler.sockjs.SockJSHandlerOptions
import io.vertx.scala.ext.web.{Router => VertxRouter}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

package object Core {
    //Core objects
    private val vertx = Global.vertx
    private var coreLogger: Logger = io.vertx.core.logging.LoggerFactory.getLogger("core")
    private var parser: JsonParser = JsonParser.newParser()
    private var httpServer: Option[HttpServer] = None
    val sockJSOptions = SockJSHandlerOptions()
        .setHeartbeatInterval(Config.sockjsHeartbeatInterval)
    private val APIRouter = Core.API.Router(vertx)
    private val WebRouter=Core.Web.router
    private val RootRouter=VertxRouter.router(vertx)
    RootRouter.route("/static/*").handler(StaticHandler.create().setCachingEnabled(false))
    RootRouter.mountSubRouter("/api",APIRouter)
        .mountSubRouter("/web",WebRouter)

    var cause:Option[Throwable]=None

    def Load(): Future[Unit] = {
        val promise: Promise[Unit] =Promise()

        //websocket server
        val server: HttpServer = vertx.createHttpServer()
        server.requestHandler(RootRouter.accept(_)).listenFuture(Config.httpPort,Config.httpHost) onComplete {
            case Success(server)=>
                httpServer=Some(server)
                println(s"http server on ${Config.httpHost}:${Config.httpPort}")
                promise.success()
            case Failure(cause)=>
                Core.cause=Some(cause)
                println("http server listen fail",cause)
                promise.failure(cause)
        }

        promise.future
    }

    def GetHttpAddress():(String,Int)={
        (Config.httpHost,Config.httpPort)
    }

    def GetHeartbeatInterval():Int=
        Config.sockjsHeartbeatInterval

    def GetLoginAddress():String={
        "/notification/login/*"
    }
}
