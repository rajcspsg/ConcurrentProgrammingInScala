import rx.lang.scala.Observable
import scala.concurrent.duration._

object ObseervableCombinatorsApp extends App {

  val odds = Observable.interval(0.5.seconds).filter(_ % 2 == 1).map(n => s"num $n").take(7)
  odds.subscribe(x => println(x), e => println(s"unexpected $e"), () => println("no more numbers"))
  Thread.sleep(4000)

}
