import rx.lang.scala.{Observable, Subscription}

import scala.swing._
import scala.swing.event.ButtonClicked


object SchedulerSwingApp extends SimpleSwingApplication {
  def top = new MainFrame {
    title = "Swing Observable"
    val button = new Button {
      text = "Click"
    }

    contents = button

    val buttonClicks = Observable.create[Button] {obs =>

        button.reactions += {
          case ButtonClicked(_) => obs.onNext(button)
        }
      Subscription()
    }
    buttonClicks.subscribe(_ => println("button clicked"))
  }
}
