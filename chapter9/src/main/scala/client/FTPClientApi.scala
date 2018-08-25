package client

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import akka.pattern.ask
import common.FileInfo
import server.FTPServerActor

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import scala.util.Try

trait FTPClientApi {

  implicit val timeout: Timeout  = Timeout(40 seconds)
  private val props = Props(classOf[FTPClientActor], timeout)

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

   val system = remotingSystem("FTPClientSystem", 0)

   val clientActor = system.actorOf(props)

  val connected = {
    val f = clientActor ? FTPClientActor.Start("127.0.0.1:2551")
    f.mapTo[Boolean]
  }

  def getFileList(d: String): Future[(String, Seq[FileInfo])] = {
    val f = clientActor ? FTPServerActor.GetFileList(d)
    f.mapTo[Seq[FileInfo]].map(fs => (d, fs))
  }

  def copyFile(src: String, dest: String): Future[String] = {
    val f = clientActor ? FTPServerActor.CopyFile(src, dest)
    f.mapTo[Try[String]].map(_.get)
  }

  def deleteFile(srcpath: String): Future[String] = {
    val f = clientActor ? FTPServerActor.DeleteFile(srcpath)
    f.mapTo[Try[String]].map(_.get)
  }
}
