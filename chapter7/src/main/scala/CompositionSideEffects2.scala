object CompositionSideEffects2 extends App {

  import scala.concurrent.{Future, ExecutionContext}
  import ExecutionContext.Implicits.global
  import scala.concurrent.stm._

  case class Node(elem: Int, next: Ref[Option[Node]])

  def nodeToString(n: Node): String = atomic { implicit txn =>
    val b = new StringBuilder
    var curr: Node = n
    while (curr != null) {
      b ++= s"${curr.elem}, "
      curr = curr.next().getOrElse(null)
    }
    b.toString()
  }

  def nodeToStringBad(n: Node): String = {
    val b = new StringBuilder
    atomic { implicit txn =>
      var curr = n
      while (curr != null) {
        b ++= s"${curr.elem},"
        curr = curr.next().getOrElse(null)
      }
    }
    b.toString()
  }

  val ref1: Ref[Option[Node]] = Ref(Some(Node(2, Ref(Some(Node(3, Ref(None)))))))
  val list = Node(1, (ref1))
  println(nodeToString(list))
  println(nodeToStringBad(list))
}
