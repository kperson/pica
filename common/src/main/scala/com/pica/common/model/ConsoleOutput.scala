package com.pica.common.model

import com.codahale.jerkson.Json.parse

case class ConsoleOutput(line: String, isStandard: Boolean)

object ConsoleOutput {
  
  lazy val MessageType = "console"
  
  def unapply(topicMessage: TopicMessage) : Option[ConsoleOutput]  = {
    
    if(topicMessage.messageType == MessageType) {
      Some(parse[ConsoleOutput](topicMessage.body))
    }
    else {
      None
    }
    
  }
  
}