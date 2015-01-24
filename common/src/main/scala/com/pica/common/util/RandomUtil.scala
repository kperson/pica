package com.pica.common.util

object RandomUtil {

  lazy val random = new scala.util.Random
  val alphabet = "abcdefghijklmnopqrstuvwxyz0123456789"
 
   
  lazy val machineId = randomString(10)
    
  def randomString(length: Int) = {
    Stream.continually(random.nextInt(alphabet.size)).map(alphabet).take(length).mkString
  }
  
  def randomKey(length: Int) = {
    randomString(length) + machineId
  }
  
  def randomInt(length: Int) = {
    random.nextInt(Integer.MAX_VALUE)
  }  
  
  def randomLong() = {
    random.nextLong
  }
  
}