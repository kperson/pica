import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._
import spray.revolver.RevolverPlugin._

object QuestionAppApiBuild extends Build {

  val Organization = "com.pica"
  val Name = "Pica PAAS"
  val Version = "0.1.0-SNAPSHOT"
  val ScalaVersion = "2.11.1"
  val ScalatraVersion = "2.3.0"

  lazy val common = Project (
    "common",
    file("common"),
    settings = Seq(
      organization := Organization,
      name := "pica-common",
      version := Version,
      scalaVersion := ScalaVersion,
      resolvers += Classpaths.typesafeReleases,
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "2.2.3",
        "com.typesafe.akka" %% "akka-actor" % "2.3.8",
        "commons-io" % "commons-io" % "2.4",
        "io.spray" %% "spray-can" % "1.3.1",
        "io.spray" %% "spray-client" % "1.3.1",
        "io.spray" %% "spray-util" % "1.3.1",
        "io.spray" %% "spray-json" % "1.3.1",
        "org.json4s" %% "json4s-native" % "3.2.11",
        "org.json4s" %% "json4s-jackson" % "3.2.11",
        "com.gilt" %% "jerkson" % "0.6.6"
      )
    )
  )

  lazy val controller = Project (
    "controller",
    file("controller"),
    settings = Seq(
      organization := Organization,
      name := "pica-controller",
      version := Version,
      scalaVersion := ScalaVersion,
      resolvers += Classpaths.typesafeReleases,
      libraryDependencies ++= Seq(
        "io.spray" %% "spray-routing" % "1.3.1"
      )
    ) ++ Revolver.settings
  ).dependsOn(common)

  lazy val builder = Project (
    "builder",
    file("builder"),
    settings = Seq(
      organization := Organization,
      name := "pica-builder",
      fork in run := false,
      version := Version,
      scalaVersion := ScalaVersion,
      resolvers += Classpaths.typesafeReleases,
      libraryDependencies ++= Seq(
      )
    )
  ).dependsOn(common)

}
