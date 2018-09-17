package Core.API

import io.vertx.core.http.HttpMethod
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.handler.sockjs.SockJSHandler
import io.vertx.scala.ext.web.{Router => VertxRouter}

object Router{
    def apply(vertx:Vertx): VertxRouter = {
        val r=VertxRouter.router(vertx)
        r.route(HttpMethod.POST,"/login").handler(io.vertx.scala.ext.web.handler.BodyHandler.create()).handler(Core.API.Handler.Login)
        r.route("/notification/register/*").handler(SockJSHandler.create(vertx, Core.sockJSOptions).socketHandler(Core.API.Handler.registerNotification))
        r.route("/external/register/*").handler(SockJSHandler.create(vertx, Core.sockJSOptions).socketHandler(Core.API.Handler.externalRegister))
        //r.route("/hub/:hubname/*").handler(io.vertx.scala.ext.web.handler.BodyHandler.create()).handler() //TODO: access extern hub
        r
    }
}