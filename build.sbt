import com.typesafe.sbt.SbtNativePackager.autoImport.NativePackagerHelper._

name := "UniHubLab"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies += "io.vertx" %% "vertx-lang-scala" % "3.5.3"
libraryDependencies += "io.vertx" %% "vertx-mongo-client-scala" % "3.5.3"
libraryDependencies += "io.vertx" %% "vertx-web-scala" % "3.5.3"
libraryDependencies += "io.vertx" %% "vertx-tcp-eventbus-bridge-scala" % "3.5.3"
libraryDependencies += "io.vertx" %% "vertx-web-templ-handlebars-scala" % "3.5.3"
libraryDependencies += "joda-time" % "joda-time" % "2.9.9"
libraryDependencies += "io.lemonlabs" %% "scala-uri" % "1.1.5"

mappings in Universal ++= directory(baseDirectory.value / "templates")
mappings in Universal ++= directory(baseDirectory.value / "webroot")

enablePlugins(JavaAppPackaging)