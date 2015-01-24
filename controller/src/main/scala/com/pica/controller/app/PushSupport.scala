package com.pica.controller.app

import scala.annotation.implicitNotFound
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

import spray.routing.HttpService
import spray.util.actorSystem

trait PushSupport extends HttpService {
      
  def in[U](duration: FiniteDuration)(body: => U)(implicit ex: ExecutionContext) : Unit = actorSystem.scheduler.scheduleOnce(duration)(body) 

}