object ObservablesFuture extends App {
  import rx.lang.scala._
  import scala.concurrent._
  import ExecutionContext.Implicits.global

  val f = Future {"back to the future"}

  val o = Observable.create[String] { obs =>

    f foreach {case s => obs.onNext(s); obs.onCompleted()}
    f.failed foreach{case t => obs.onError(t)}

    Subscription()
  }

  o.subscribe(x => println(x))
}
