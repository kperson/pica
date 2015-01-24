package com.pica.builder.app

case class LocalBuildInstructions(buildId: String, directory: String, dockerFileLocation: String, imageName: String, registryUrl: String)