import rx.lang.scala._

object ObservableLifeTimeApp extends App {
  val classics = Observable.from(List("Good Bad Ugly", "Titanic", "Ben Hur"))

  classics.subscribe(new Observer[String] {
    override def onCompleted(): Unit = println("no more movies")

    override def onNext(value: String): Unit = println(s"Movies Watchlist - $value")

    override def onError(error: Throwable): Unit = println(s"oops -$error")
  })
}
