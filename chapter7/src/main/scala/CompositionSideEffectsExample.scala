import scala.concurrent.{Future, ExecutionContext}
import ExecutionContext.Implicits.global
import scala.concurrent.stm._

object CompositionSideEffectsExample extends App{

  var t:InTxn = _

  val myValue = Ref(0)

  def inc() = atomic { implicit txn =>
    t = txn
    println(s"Incrementing ${myValue()}")
    myValue( ) = myValue() + 1
  }

  def inc2() = atomic { implicit  txn =>
    val valAtStart = myValue()

    Txn.afterCommit { _ =>
      println(s"incrementing $valAtStart")
    }

    myValue() = myValue() + 1
  }

  Future {
    inc2()
  }

  Future {
    inc2()
  }

  Thread.sleep(5000)

  //println(s"myValue == ${myValue()(t)}")
}
