import NestingTransactionsExample.TSortedList

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import scala.annotation.tailrec

object SingleOperationTransactions extends App {
  import scala.concurrent.stm._

  case class Node(val elem: Int, val next: Ref[Node]) {
    def append(n: Node): Unit = atomic { implicit txn =>
      val oldNext = next()
      next() = n
      n.next() = oldNext
    }
    def nextNode: Node = next.single()
    def appendIfEnd(n: Node) = next.single.transform {
      oldNext => if(oldNext == null) n else oldNext
    }

    def printNode(): Unit = {
      @tailrec
      def printNode(n: Node): Unit = {
        if(n == null) return
        print(n.elem + ",")
        val nextRef = n.next
        if(nextRef == null) return
        val nextNode = nextRef.single()
        printNode(nextNode)
      }
      printNode(this)
    }
  }

  val list = Node(1, Ref(Node(2, Ref(Node(3, Ref(Node(4, Ref(Node(5, null)))))))))

  val f = Future {list.append(Node(2, Ref(null)))}
  val g = Future {list.append(Node(3, Ref(null)))}

  for(_ <- f; _ <- g) println()

  list.printNode()
  Thread.sleep(5000)


}