import java.io.File

import org.apache.commons.io.monitor.{FileAlterationListenerAdaptor, FileAlterationMonitor, FileAlterationObserver}
import rx.lang.scala.{Observable, Subscription}

object ObservableSubscriptionApp extends  App {

  println(s"please modify and save the directory ${new File(".").getAbsolutePath}")
  while(true) {
    val sub = modified(".").subscribe(n => println(s"$n modified"))

    Thread.sleep(1000)
    sub.unsubscribe()

  }

  println("monitoring done")


  def modified(dir: String): Observable[String]  = {
    Observable.create { observer =>
      val fileMonitor = new FileAlterationMonitor(1000)
      val fileObs = new FileAlterationObserver(dir)
      val fileLis = new FileAlterationListenerAdaptor {
        override def onFileChange(file: File) {
          observer.onNext(file.getName)
        }
      }
      fileObs.addListener(fileLis)
      fileMonitor.addObserver(fileObs)
      fileMonitor.start()

      Subscription {fileMonitor.stop()}
    }
  }


 /* def hotModified(dir: String): Observable[String] = {
    val fileMonitor = new FileAlterationMonitor(1000)
    fileMonitor.start()
    val fileObs = new FileAlterationObserver(dir)
     fileMonitor.add
  }
*/
}
