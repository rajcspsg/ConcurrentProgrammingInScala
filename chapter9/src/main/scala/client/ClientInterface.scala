package client

import scala.swing.MainFrame
import scala.swing._
import scala.swing.event._
import javax.swing.table._
import javax.swing._
import javax.swing.border._
import java.awt.Color

import common.FileInfo

import scala.swing.BorderPanel.Position._

abstract class FTPClientFrame extends MainFrame {

  title = "ScalaFTP"

  class FilePane extends BorderPanel {
    object pathBar extends BorderPanel {
      val label = new Label("Path:")
      val filePath = new TextField(".") {
        border = BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true)
        editable = false
      }
      val upButton = new Button("^")
      layout(label) = West
      layout(filePath) = Center
      layout(upButton) = East
      border = BorderFactory.createEmptyBorder(2, 2, 2, 2)
    }
    layout(pathBar) = North

    object scrollPane extends ScrollPane {
      val columnNames = Array[AnyRef]("Filename", "Size", "Date modified")
      val fileTable = new Table {
        showGrid = true
        model = new DefaultTableModel(columnNames, 0) {
          override def isCellEditable(r: Int, c: Int) = false
        }
        selection.intervalMode = Table.IntervalMode.Single
      }
      contents = fileTable
    }
    layout(scrollPane) = Center

    object buttons extends GridPanel(1, 2) {
      val copyButton = new Button("Copy")
      val deleteButton = new Button("Delete")
      contents += copyButton
      contents += deleteButton
    }

    layout(buttons) = South

    var parent: String = "."
    var dirFiles: Seq[FileInfo] = Nil

    def table = scrollPane.fileTable

    def currentPath = pathBar.filePath.text
  }

  object files extends GridPanel(1, 2) {
    val leftPane = new FilePane
    val rightPane = new FilePane
    contents += leftPane
    contents += rightPane

    def opposite(pane: FilePane) = {
      if (pane eq leftPane) rightPane else leftPane
    }
  }

  object menu extends MenuBar {
    object file extends Menu("File") {
      val exit = new MenuItem("Exit ScalaFTP")
      contents += exit
    }
    object help extends Menu("Help") {
      val about = new MenuItem("About...")
      contents += about
    }
    contents += file
    contents += help
  }

  object status extends BorderPanel {
    val label = new Label("connecting...", null, Alignment.Left)
    layout(new Label("Status: ")) = West
    layout(label) = Center
  }

  contents = new BorderPanel {
    layout(menu) = North
    layout(files) = Center
    layout(status) = South
  }

}


