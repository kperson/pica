package com.pica.common.model

case class BuildInstructions(buildId: String, dockerFileLocation: String, imageName: String, registryUrl: String)
case class BuildRequest(status: String, instructions: Option[BuildInstructions])