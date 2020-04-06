name := "ProjectFunctional"

version := "0.1"

scalaVersion := "2.11.8"


dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-core" % "2.8.7"
dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.7"
dependencyOverrides += "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.8.7"

libraryDependencies += "jp.co.bizreach" %% "aws-kinesis-scala" % "0.0.12"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.6.4"

libraryDependencies += "ai.x" %% "play-json-extensions" % "0.40.2"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.7.0"



