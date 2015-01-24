package com.pica.common.util

import java.io.BufferedReader
import java.net.URLEncoder
import java.net.URL
import java.io.InputStreamReader

object URLUtil {

  def urlCollection[A](url: String, parameters: Map[String, String] = Map.empty, headers: Map[String, String] = Map.empty)(f: BufferedReader => A) : A = {
    val finalUrl:String = !parameters.isEmpty match {
      case true => {
        val urlBuilder = new StringBuilder
        urlBuilder.append(url)
        urlBuilder.append("?")
        for ((key, value) <- parameters) {
          urlBuilder.append(key + "=" + URLEncoder.encode(value,"UTF-8"))
          urlBuilder.append("&")
        }
        val x = urlBuilder.toString
        x.substring(0, x.length - 1)
      }
      case _ => {
        url
      }
    }
    
    val urlInstance = new URL(finalUrl)
    val urlConnection = urlInstance.openConnection 
    for ((key, value) <- parameters) {
      urlConnection.setRequestProperty(key, value)  
    }
    
	val in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream))	
	f(in)
  }
  
}