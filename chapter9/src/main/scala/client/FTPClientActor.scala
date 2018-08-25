package client

import akka.actor.{Actor, ActorIdentity, ActorRef, Identify}
import akka.util.Timeout
import akka.pattern.{ask, pipe}
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.actor._

class FTPClientActor(implicit val timeout: Timeout) extends Actor {

  import FTPClientActor._
  import server.FTPServerActor._

  def unconnected: Actor.Receive = {
    case Start(host) =>
      println(s"In ${self} received Start Mesage")
      val serverActorPath = s"akka.tcp://FTPServerSystem@$host/user/server"
      val serverActorSel = context.actorSelection(serverActorPath)
      serverActorSel ! Identify(())
      println(s"sent Identify(())message to $serverActorSel")
      context.become(connecting(sender))
  }

  def connecting(clientApp: ActorRef): Actor.Receive = {
    case ActorIdentity(_, Some(ref)) =>
      println(s"")
      clientApp ! true
      context.become(connected(ref))
    case ActorIdentity(_, None) =>
      clientApp ! false
      context.become(unconnected)
  }

  def connected(serverActor: ActorRef): Actor.Receive = {
    case command: Command =>
      (serverActor ? command) pipeTo sender()
  }

  override def receive: Receive = unconnected
}

object FTPClientActor {
  case class Start(serverActorUrl: String)
}
