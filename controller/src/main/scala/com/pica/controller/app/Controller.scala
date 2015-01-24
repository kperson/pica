package com.pica.controller.app

import akka.actor.ActorSystem
import akka.actor.Props
import akka.io.IO
import spray.can.Http
import com.pica.common.util.MyProps

object Controller extends App {

  implicit val system = ActorSystem("pica")

  val service = system.actorOf(Props[ControllerServiceActor], "controller-service")
  
  IO(Http) ! Http.Bind(service, MyProps("SERVER_HOST").get, port = MyProps("SERVER_PORT").get.toInt)
   
}