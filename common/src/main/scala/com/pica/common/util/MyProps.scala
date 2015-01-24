package com.pica.common.util

import java.util.Properties
import scala.io.Source
import java.io.File
import scala.collection.JavaConverters._

object MyProps {

  val props = new Properties
  private var loaded = false
  val config = "/config.properties"
  private var envMap: Option[Map[String, String]] = None

  loadProperties()
  
  def overrideConfig(path: String) {
    val source = Source.fromFile(new File(path))
    props.load(source.reader) 
    loaded = true
  }
    
  def loadProperties() {
    if(System.getenv("CONFIG_TYPE") == null || System.getenv("CONFIG_TYPE").toLowerCase != "env") {
       val source = Source.fromURL(getClass.getResource(config))
       props.load(source.reader)        
    }
    else {
      envMap = Some(System.getenv().asScala.toMap)       
    }
  }
  
  def apply(key: String) : Option[String] = {
    envMap match {
      case Some(x) =>x.get(key)
      case _ => {
        val value = props.getProperty(key)
        value match  {
          case null => None
          case _ => Some(value)
        }
      }
    }
  }

}