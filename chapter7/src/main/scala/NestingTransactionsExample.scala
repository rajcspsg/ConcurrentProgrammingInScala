
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import scala.annotation.tailrec
import scala.concurrent.stm.{Ref, atomic}

object NestingTransactionsExample extends App {

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
  }

  def nodeToString(n: Node): String = atomic { implicit txn =>
    val b = new StringBuilder
    var curr: Node = n
    while (curr != null) {
      b ++= s"${curr.elem}, "
      curr = curr.next()
    }
    b.toString()
  }

  class TSortedList {
    val head = Ref[Node](null)

    override def toString: String = atomic { implicit txn =>
      val h = head()
      nodeToString(h)
    }

    def insert(x: Int): this.type = atomic {implicit txn =>
      @tailrec def insert(n: Node): Unit = {
        if(n.next() == null || n.next().elem > x)
          n.append(new Node(x, Ref(null)))
        else insert(n.next())
      }
      if(head() == null || head().elem > x)
        head() = new Node(x, Ref(head()))
      else insert(head())
      this
    }
  }

  val sortedList = new TSortedList
  val f = Future{sortedList.insert(1); sortedList.insert(4)}
  val g = Future{sortedList.insert(2); sortedList.insert(3)}

  for(_ <- f; _ <- g) println()

  println("sorted list is \n")
  Thread.sleep(2000)
  println(sortedList.toString)
}
