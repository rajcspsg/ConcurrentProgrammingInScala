package custom.schedulers

import rx.lang.scala.{Observable, Subscription}

import scala.swing._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.Source
import scala.swing.event.ButtonClicked
import utils._



trait BrowserLogic {

  self: BrowserFrame =>

  def suggestRequest(term: String): Observable[String] = {
    val url = s"http://suggestqueries.google.com/complete/search?client=fierfox&q=$term"
    val request = Future {
      Source.fromURL(url).mkString
    }
    Observable.from(request).timeout(0.5.seconds).onErrorReturn(e => "no suggestion")
  }

  def pageRequest(url: String): Observable[String] = {
    val request = Future{Source.fromURL(url).mkString}
    Observable.from(request).timeout(4.seconds).onErrorReturn(e => s"couldn't load page $e")
  }

  urlfied.texts.map(suggestRequest).concat.observeOn(swingScheduler).subscribe(response=> pagefirld.text = response)
  button.clicks.map(_ => pageRequest(urlfied.text)).concat.observeOn(swingScheduler).subscribe(response => pagefirld.text = response)
}
