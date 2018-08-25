package client

import java.io.File

import common.FileInfo

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}
import scala.swing._
import scala.swing.event._
import javax.swing.table._
import javax.swing._
import javax.swing.border._
import java.awt.{Color, event}
import java.awt.event.MouseAdapter
import rx.lang.scala.{Observable, Subscription}


trait FTPClientLogic {

  self: FTPClientFrame with FTPClientApi =>

  def swing(body: => Unit) = {
    val r = new Runnable { override def run = body }
    javax.swing.SwingUtilities.invokeLater(r)
  }

  connected.onComplete {
    case Failure(t) =>
      swing {status.label.text = s"Couldn't connect due to $t"}
    case Success(false) =>
      swing { status.label.text = "couldn't find the server" }
    case Success(true) =>
      swing {
        status.label.text = "Connected!"
        refreshPane(files.leftPane)
        refreshPane(files.rightPane)
      }
  }

  def updatePane(pane: FilePane, dir: String, files: Seq[FileInfo]): Unit = {
    val table = pane.scrollPane.fileTable
    table.model match {
      case d: DefaultTableModel =>
        d.setRowCount(0)
        pane.parent = if (dir == ".") "." else dir.take(dir.lastIndexOf(File.separator))
        pane.dirFiles = files.sortBy(!_.isDir)
        for (f <- pane.dirFiles) d.addRow(f.toRow)
    }
  }

  def refreshPane(pane: FilePane): Unit = {
    val dir = pane.pathBar.filePath.text
    getFileList(dir) onComplete {
      case Success((dir, files)) =>
        swing { updatePane(pane, dir, files) }
      case Failure(t) =>
        swing { status.label.text = s"Could not update file pane: $t" }
    }
  }

  implicit class TableOps(val self: Table) {
    def rowDoubleClicks = Observable[Int] { sub =>
      self.peer.addMouseListener(new MouseAdapter {
        override def mouseClicked(e: event.MouseEvent): Unit =  {
          if(e.getClickCount == 2) {
            val row = self.peer.getSelectedRow
            sub.onNext(row)
          }
        }
      })
    }
  }

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

  def setupPane(pane: FilePane): Unit = {
    val fileClicks = pane.table.rowDoubleClicks.map(row => pane.dirFiles(row))
    fileClicks.filter(_.isDir).subscribe { fileInfo =>
      pane.pathBar.filePath.text = pane.pathBar.filePath.text + File.separator + fileInfo.name
      refreshPane(pane)
    }
    pane.pathBar.upButton.clicks.subscribe { _ =>
      pane.pathBar.filePath.text = pane.parent
      refreshPane(pane)
    }

    def rowActions(button: Button): Observable[FileInfo] = button.clicks
      .map(_ => pane.table.peer.getSelectedRow)
      .filter(_ != -1)
      .map(row => pane.dirFiles(row))
    def setStatus(txt: String) = {
      status.label.text = txt
      refreshPane(files.leftPane)
      refreshPane(files.rightPane)
    }

    val rowCopies = rowActions(pane.buttons.copyButton)
      .map(info => (info, files.opposite(pane).currentPath))
    rowCopies.subscribe { t =>
      val (info, destDir) = t
      val dest = destDir + File.separator + info.name
      copyFile(info.path, dest) onComplete {
        case Success(s) =>
          swing { setStatus(s"File copied: $s") }
        case Failure(t) =>
          swing { setStatus(s"Could not copy file: $t")}
      }
    }

    val rowDeletes = rowActions(pane.buttons.deleteButton)
    rowDeletes.subscribe { info =>
      deleteFile(info.path) onComplete {
        case Success(s) =>
          swing { setStatus(s"File deleted: $s") }
        case Failure(t) =>
          swing { setStatus(s"Could not delete file: $t") }
      }
    }
  }

  setupPane(files.leftPane)
  setupPane(files.rightPane)

  menu.file.exit.reactions += {
    case ButtonClicked(_) =>
      system.stop(clientActor)
      system.terminate()
      sys.exit(0)
  }

  menu.help.about.reactions += {
    case ButtonClicked(_) =>
      Dialog.showMessage(message = "ScalaFTP version 0.1, made in Switzerland", title = "About ScalaFTP")
  }

}
