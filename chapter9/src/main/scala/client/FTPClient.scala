package client

import javax.swing.UIManager

import scala.concurrent.Await
import scala.swing.SimpleSwingApplication

object FTPClient extends SimpleSwingApplication {

  try {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
  } catch {
    case e: Exception =>
      // ignore
      println(s"could not change look&feel: $e")
  }

  def top = new FTPClientFrame with FTPClientApi with FTPClientLogic {
    def host = hostArg
  }

  var hostArg: String = ""

  override def main(args: Array[String]) {
    hostArg = if (args.length > 0) args(0) else {
      println("usage (from sbt):")
      println("    run <ftp-server-url>")
      println("    runMain org.learningconcurrency.ch9.FTPClient <ftp-server-url>")
      sys.exit(1)
    }
    super.main(args)
    import scala.concurrent.duration._
    println(s"${Await.result(top.connected, 4 seconds)}")
  }

}
