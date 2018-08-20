import rx.lang.scala.Observable

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object NestedObservableApp extends App {

  import scala.io.Source

  def fetchQuote(): Future[String] = Future {
      val url = "https://www.goodreads.com/quotes/tag/random"
      Source.fromURL(url).getLines().mkString
  }

  def fetchQuoteObservable(): Observable[String] = {
    Observable.from(fetchQuote())
  }

  def quotes: Observable[String] = {
    Observable.interval(0.5.seconds).take(7).flatMap {
      n => fetchQuoteObservable().map(s => s"$n) $s")
    }
  }

  quotes.subscribe(x => println(x))
  Thread.sleep(6000)
  quotes.subscribe(x => println(x))
  Thread.sleep(6000)
}

