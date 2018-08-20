import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.annotation.tailrec
import scala.concurrent.Future

object AtomicHistoryBadExample extends App {

  val urls = new AtomicReference[List[String]](Nil)
  val clen = new AtomicInteger(0)


  def addUrl(url: String): Unit = {
    @tailrec
    def append(): Unit = {
      val oldUrls = urls.get()
      val newUrls = url :: oldUrls
      if(!urls.compareAndSet(oldUrls, newUrls)) append()
    }
   append()
    clen.addAndGet(url.length + 1)
  }

  def getUrlArray(): Array[Char] = {
    val array = new Array[Char](clen.get)
    val urlList = urls.get()
    for((ch, i) <- urlList.map(_ + "\n").flatten.zipWithIndex ){
      array(i) = ch
    }
    array
  }


  Future {
    try {
      println(s"sending: ${getUrlArray().mkString}")
    } catch {
      case e: Exception=> println(s"oops $e")
    }
  }

  Future {
    addUrl("http://scala-lang.org")
    addUrl("http://github.com/scala")
    addUrl("http://www.scala-lang.org/api")
    //addUrl("")

    println("done browsing")
  }

  Thread.sleep(2000)
  println("clen " + clen.get() + "\nurls" + urls.get())
}
