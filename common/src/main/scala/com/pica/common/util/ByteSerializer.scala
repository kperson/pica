package com.pica.common.util

import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream

object ByteSerializer {

  def serialize(x: Any) = {
    val baos = new ByteArrayOutputStream
    val oos = new ObjectOutputStream(baos)
    oos.writeObject(x)
    oos.flush
    oos.close
    baos.toByteArray
  }
   
  def deserialize(data: Array[Byte]) : Any = {
   val in = new ByteArrayInputStream(data)
    val is = new ObjectInputStream(in)
    is.readObject  
  }    
  
}