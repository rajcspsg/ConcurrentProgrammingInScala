package server

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import common.FileSystem

object FTPServer extends App {


  def remotingConfig(port: Int) = ConfigFactory.parseString(s"""
akka {
  actor.provider = "akka.remote.RemoteActorRefProvider"
  remote {
    enabled-transports = ["akka.remote.netty.tcp"]
    netty.tcp {
      hostname = "127.0.0.1"
      port = $port
    }
  }
}
  """)

  def remotingSystem(name: String, port: Int): ActorSystem = ActorSystem(name, remotingConfig(port))

  val fileSystem = new FileSystem(".")
  fileSystem.init()
  val port = args(0).toInt

  val actorSystem = remotingSystem("FTPServerSystem", port)
  val actor = actorSystem.actorOf(FTPServerActor(fileSystem), "server")
  print(s"actor is ${actor.path}")
}
