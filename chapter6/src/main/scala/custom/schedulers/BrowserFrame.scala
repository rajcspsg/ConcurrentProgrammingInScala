package custom.schedulers

import rx.lang.scala.{Observable, Scheduler, Subscription}

import scala.swing._
import scala.swing.event.{ButtonClicked, ValueChanged}


object utils {
  implicit class ButtonOps(val self: Button) {
    def clicks = Observable.create[Unit] {obs =>
      self.reactions += {
        case ButtonClicked(_) => obs.onNext(())
      }
      Subscription()
    }
  }

  implicit  class TextFieldOps(val self: TextField) {
    def texts = Observable.create[String] { obs =>
      self.reactions += {
        case ValueChanged(_) => obs.onNext(self.text)
      }
      Subscription()
    }
  }

  import java.util.concurrent.Executor
  import rx.schedulers.Schedulers.{from => fromExecutor}
  import javax.swing.SwingUtilities.invokeLater

  val swingScheduler = new Scheduler {
    val asJavaScheduler = fromExecutor(new Executor {
      override def execute(command: Runnable): Unit = invokeLater(command)
    })
  }
}

 class BrowserFrame extends MainFrame {

  title = "MiniBrowser"

  val specUrl = "http://www.w3.org/Addressing/URL/url-spec.txt"

  val urlfied = new TextField(specUrl)
  val pagefirld = new TextArea()

  val button = new Button {
    text = "Feeling Lucky"
  }

  contents = new BorderPanel {
    import BorderPanel.Position._
    layout(new BorderPanel {
      layout(new Label("URL:")) = West
      layout(urlfied) = Center
      layout(button)= East
    }) = North
    layout(pagefirld) = Center
  }
  size = new Dimension(1024, 768)
}


