import AtomicHistoryBad.{clen, urls}

object AtomicHistorySTM extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.concurrent.stm._

  val urls = Ref[List[String]](Nil)
  val clen = Ref(0)

  def addUrl(url: String): Unit = atomic { implicit txn =>
    urls() = url :: urls()
    clen() = clen() + url.length + 1
  }

  def getUrlArray(): Array[Char] = atomic { implicit txn =>
    val array = new Array[Char](clen())
    for ((character, i) <- urls().map(_ + "\n").flatten.zipWithIndex) {
      array(i) = character
    }
    array
  }

  Future {
    addUrl("http://scala-lang.org")
    addUrl("https://github.com/scala/scala")
    addUrl("http://www.scala-lang.org/api")
    println("done browsing")
  }

  Thread.sleep(25)

  Future {
    try { println(s"sending: ${getUrlArray().mkString}") }
    catch { case e: Exception => println(s"problems getting the array $e") }
  }

  Thread.sleep(2000)

  println("clen " + clen.single() + "\nurls" + urls.single())
}
