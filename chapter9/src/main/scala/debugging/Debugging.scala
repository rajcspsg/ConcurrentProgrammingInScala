package debugging

object Debug extends App {
  class Account(var money: Int)

  def send(a: Account, b: Account, n: Int) = a.synchronized {
    b.synchronized {
      a.money -= n
      b.money += n
    }
  }

  def thread(body: =>Unit): Thread = {
    val t = new Thread {
      override def run() = body
    }
    t.start()
    t
  }

  val a = new Account(1000)
  val b = new Account(2000)

  val t1 = thread { for(i <- 0 until 100) send(a,b,1)}
  val t2 = thread { for(i <- 0 until 100) send(b,a,1)}
  t1.join()
  t2.join()
}



