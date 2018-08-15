package Core

import io.vertx.scala.ext.web.{RoutingContext, Router => VertxRouter}

package object Web {
    val router={
        val router=VertxRouter.router(Global.vertx)
        val template = io.vertx.scala.ext.web.templ.HandlebarsTemplateEngine.create()
        template.setMaxCacheSize(0)
        val templateHandler = io.vertx.scala.ext.web.handler.TemplateHandler.create(template)
        router.get("/*").handler((routingContext: RoutingContext) => {
            templateHandler.handle(routingContext)
        }).failureHandler((routingContext: RoutingContext) => {
            routingContext.response().end(routingContext.failure().getMessage)
        })
        router
    }
}
