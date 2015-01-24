package com.pica.controller.app

import akka.actor.Actor
import org.json4s.DefaultFormats

class ControllerServiceActor extends Actor with BuildService with TopicService {

  def actorRefFactory = context
  
  def receive = runRoute(buildRoute ~ topicRoute)
  
  override def json4sJacksonFormats = DefaultFormats


}