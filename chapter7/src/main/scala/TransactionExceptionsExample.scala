import scala.concurrent.Future
import scala.util.Failure

object TransactionExceptionsExample extends App {

  import NestingTransactionsExample._
  import scala.concurrent.{Future, ExecutionContext}
  import ExecutionContext.Implicits.global
  import scala.concurrent.stm._

  def pop(xs: TSortedList, n: Int): Unit = atomic { implicit txn =>
    var left = n
    while(left > 0) {
      xs.head() = xs.head().next()
      left -= 1
    }
  }

  val lst = new TSortedList
  lst.insert(4).insert(9).insert(1).insert(16)

  Future {
    pop(lst, 2)

  } foreach{ case _ => println(s"removed 2 elements ; list - $lst")}

  Thread.sleep(2000)


  Future {
    pop(lst, 3)

  } onComplete { case Failure(t) => println(s"whoa $t list - $lst")}

  Thread.sleep(4000)
}
