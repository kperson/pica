package com.pica.common.model

import com.codahale.jerkson.Json.generate
import com.codahale.jerkson.Json.parse
import spray.http.ContentType.apply
import spray.http.MediaTypes._
import spray.json.DefaultJsonProtocol
import spray.httpx.marshalling.Marshaller
import spray.http.HttpEntity
import spray.httpx.unmarshalling.SimpleUnmarshaller
import spray.http.ContentTypeRange

case class TopicMessageEvent(message: TopicMessage, topic: String, createdAt: Long)

object TopicMessageEvent extends DefaultJsonProtocol  {

  implicit val TopicMessageEventFormat =
    Marshaller.of[TopicMessageEvent](`application/json`) { (value, contentType, ctx) =>
      ctx.marshalTo(HttpEntity(contentType, generate(value)))
    }
  
   implicit val TopicMessageEventUnmarshaller =  new SimpleUnmarshaller[TopicMessageEvent] {
      
     val accept: ContentTypeRange = `application/json`
     
     val canUnmarshalFrom = Seq(accept)
      
      def unmarshal(entity: HttpEntity) = protect(parse[TopicMessageEvent](entity.asString))
    }    

  
}