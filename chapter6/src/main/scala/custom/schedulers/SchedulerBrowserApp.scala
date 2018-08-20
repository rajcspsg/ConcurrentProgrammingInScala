package custom.schedulers

import scala.swing.SimpleSwingApplication

object SchedulerBrowserApp extends SimpleSwingApplication {
  def top = new BrowserFrame with BrowserLogic

}
