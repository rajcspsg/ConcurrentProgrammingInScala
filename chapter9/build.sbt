name := "ConcurrencyInPractice"
version := "0.1"
scalaVersion := "2.12.6"
description := "ConcurrencyInPractice"

resolvers ++= Seq(
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

fork := false

libraryDependencies += "org.scala-stm" %% "scala-stm" % "0.8"

libraryDependencies += "commons-io" % "commons-io" % "2.4"

libraryDependencies += "org.scala-lang.modules" %% "scala-async" % "0.9.6"

//libraryDependencies += "com.github.scala-blitz" %% "scala-blitz" % "1.2"

libraryDependencies += "io.reactivex" %% "rxscala" % "0.26.5"

libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "2.0.3"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.11"

libraryDependencies += "com.typesafe.akka" %% "akka-remote" % "2.5.11"

libraryDependencies += "com.storm-enroute" %% "scalameter-core" % "0.10.1"

libraryDependencies += "org.scalaz" %% "scalaz-concurrent" % "7.2.11"

libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.11"

//libraryDependencies += "com.quantifind" %% "wisp" % "0.0.4"

libraryDependencies += "org.scalafx" %% "scalafx" % "8.0.144-R12"

unmanagedJars in Compile += Attributed.blank(file(scala.util.Properties.javaHome) / "/lib/jfxrt.jar")

//libraryDependencies += "com.storm-enroute" %% "reactive-collections" % "0.5"