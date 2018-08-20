import rx.lang.scala.{Observable, Subscription}

object ObservablesCreate extends App {
  val vms = Observable.create[String] { obs =>
    obs.onNext("JVM")
    obs.onNext("DartVM")
    obs.onNext("V8")
    obs.onCompleted()
    Subscription()
  }

  vms.subscribe(x => println(x), e => println(s"oops - $e"), () => println("done"))
}
