package com.pica.common.actor

case class PicaBroadcastMessage(topic: String, messageBody: Array[Byte])