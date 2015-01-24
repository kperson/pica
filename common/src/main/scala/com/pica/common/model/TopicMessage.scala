package com.pica.common.model

import spray.json.DefaultJsonProtocol
import com.codahale.jerkson.Json.generate
import com.codahale.jerkson.Json.parse
import spray.httpx.marshalling.Marshaller
import spray.http.MediaTypes._
import spray.http.HttpEntity
import spray.httpx.unmarshalling.SimpleUnmarshaller
import spray.http.ContentTypeRange


case class TopicMessage(messageType: String, body: String)

object TopicMessage extends DefaultJsonProtocol  {

  implicit val TopicMessageFormat =
    Marshaller.of[TopicMessage](`application/json`) { (value, contentType, ctx) =>
      ctx.marshalTo(HttpEntity(contentType, generate(value)))
    }
  
     implicit val TopicMessageUnmarshaller =  new SimpleUnmarshaller[TopicMessage] {
      
     val accept: ContentTypeRange = `application/json`
     
     val canUnmarshalFrom = Seq(accept)
      
      def unmarshal(entity: HttpEntity) = protect(parse[TopicMessage](entity.asString))
    
     }  

  
}