import rx.lang.scala.Observable
import rx.lang.scala.schedulers.ComputationScheduler

object SchedulerComputationApp extends App {
  val scheduler = ComputationScheduler()
  val numbers = Observable.from(0 to 20)
  numbers.subscribe(n => println(s"current Thread ${Thread.currentThread().getName} num $n"))
  numbers.observeOn(scheduler).subscribe(n => println(s"current Thread ${Thread.currentThread().getName} num $n"))
  Thread.sleep(2000)
}
