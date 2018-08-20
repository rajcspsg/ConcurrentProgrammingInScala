import NestingTransactionsExample.TSortedList

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import scala.concurrent.stm._
import scala.util.control.Breaks._

object TransactionExceptionsPart2 extends App {

  import TransactionExceptionsExample._

  val lst1 = new TSortedList
  lst1.insert(4).insert(9).insert(1).insert(16)

  Future {
    breakable {
      atomic { implicit txn =>
        for(n <- List(1, 2, 3)) {
          //pop(lst, n)
          break
        }
      }
    }
  }

  println(s"after removing - $lst")

  import scala.util.control._

  Future {
    breakable {
      atomic.withControlFlowRecognizer {
        case c : ControlThrowable => false
      } { implicit txn =>
        for(n <- List(1,2)) {
          pop(lst1, n)
         // break
        }

      }
    }
  }

  println(s"after removing - $lst1")
  Thread.sleep(2000)
}




