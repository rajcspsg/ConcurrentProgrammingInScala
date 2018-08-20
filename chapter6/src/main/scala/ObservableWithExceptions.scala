import rx.lang.scala._

object ObservableWithExceptions extends App {
  val o = Observable.just(1, 2) ++ Observable.error(new RuntimeException("test exception"))++ Observable.just(3,4)
  o.subscribe(x => println(s"number $x"), t => println(s"an error occured: $t"))
}
