package server

import akka.actor.{Actor, Props}
import common.FileSystem
import scala.concurrent.{Future, ExecutionContext}
import ExecutionContext.Implicits.global
import scala.util.Try
import akka.actor._
import akka.pattern.pipe
import akka.event.Logging

class FTPServerActor(fileSystem: FileSystem) extends Actor {

  import FTPServerActor._

  override def receive: Receive = {
    case GetFileList(dir) =>
      //println(fileSystem.files.snapshot.map(_._2).filter(_.isDir).filter(_.path.contains("src")).mkString("\n"))
      val filesMap = fileSystem.getFileList(dir)
      val files = filesMap.map(_._2).to[Seq]
      sender ! files
    case CopyFile(srcpath, destpath) =>
      Future {
        Try(fileSystem.copyFile(srcpath, destpath))
      } pipeTo sender
    case DeleteFile(path) =>
      Future {
        Try(fileSystem.deleteFile(path))
      } pipeTo sender
    case FindFiles(regex) =>
      Future {
        Try(fileSystem.findFiles(regex))
      } pipeTo sender
    case m =>
      println(s"received msg $self sent by ${sender()}\n" + m)
  }



}


object FTPServerActor {
  sealed trait Command
  case class GetFileList(dir: String) extends Command
  case class CopyFile(src: String, dest: String) extends Command
  case class DeleteFile(path: String) extends Command
  case class FindFiles(regex: String) extends Command
  def apply(fileSystem: FileSystem) = Props(classOf[FTPServerActor], fileSystem)
}