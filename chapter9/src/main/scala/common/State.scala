package common

import java.io.File
import java.text.SimpleDateFormat

import common.FileSystem.Copying
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.TrueFileFilter

import scala.collection._
import scala.concurrent._
import scala.collection.JavaConverters._
import scala.concurrent.stm._

import FileSystem._



case class FileInfo(path: String, name: String, parent: String,
                    modified: String, isDir: Boolean, size: Long, state: State) {
  def toRow = Array[AnyRef](name, if (isDir) "" else size / 1000 + "kB", modified)
  def toFile = new File(path)
}


object FileInfo {

  val dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

  def apply(path: String, name: String, parent: String,
            modified: String, isDir: Boolean, size: Long, state: State): FileInfo = new FileInfo(path, name, parent, modified, isDir, size, state)

  def apply(file: File, state: State): FileInfo = apply(file.getAbsolutePath(), file.getName(), file.getParent(),
    file.lastModified().toString, file.isDirectory(), file.getTotalSpace, state)

  def apply(file: File): FileInfo = {
    val path = file.getPath
    val name = file.getName
    val parent = file.getParent
    val modified = dateFormat.format(file.lastModified)
    val isDir = file.isDirectory
    val size = if (isDir) -1 else FileUtils.sizeOf(file)
    FileInfo(path, name, parent, modified, isDir, size, FileSystem.Idle)
  }

  def creating(file: File, size: Long): FileInfo = {
    val path = file.getPath
    val name = file.getName
    val parent = file.getParent
    val modified = dateFormat.format(System.currentTimeMillis)
    val isDir = false
    FileInfo(path, name, parent, modified, isDir, size, FileSystem.Created)
  }


}

class FileSystem(val rootpath: String) {
  val files = TMap[String, FileInfo]()

  def init() = atomic { implicit txn =>
    files.clear()
    val rootDir = new File(rootpath)
    val all = TrueFileFilter.INSTANCE

    val fileIterator = FileUtils.iterateFilesAndDirs(rootDir, all, all).asScala
    for(file <- fileIterator) {
      val info = FileInfo(file)
      files(info.path) = info
    }
  }

  def getFileList(dir: String): Map[String, FileInfo] = atomic { implicit txn =>
    files.filter(_._2.parent == dir)
  }

  def copyFile(srcpath: String, destpath: String): String = atomic { implicit txn =>
    import FileSystem._
    val srcfile = new File(srcpath)
    val destfile = new File(destpath)
    val info = files(srcpath)
    if (files.contains(destpath)) sys.error(s"Destination $destpath already exists.")
    info.state match {
      case Created => sys.error(s"File $srcpath being created.")
      case Deleted => sys.error(s"File $srcpath already deleted.")
      case Idle | Copying(_) =>
        files(srcpath) = info.copy(state = info.state.inc)
        files(destpath) = FileInfo.creating(destfile, info.size)
        Txn.afterCommit { _ => copyOnDisk(srcfile, destfile) }
        srcpath
    }
  }

  private def copyOnDisk(srcfile: File, destfile: File): Unit = {
    FileUtils.copyFile(srcfile, destfile)
    atomic { implicit txn =>
      val ninfo = files(srcfile.getPath)
      files(srcfile.getPath) = ninfo.copy(state = ninfo.state.dec)
      files(destfile.getPath) = FileInfo(destfile, Copying(1))
    }
  }

  def deleteFile(srcpath: String): String = atomic { implicit txn =>
    val info = files(srcpath)
    info.state match {
      case Created => sys.error(s"File $srcpath not yet created.")
      case Copying(_) => sys.error(s"Cannot delete $srcpath, file being copied.")
      case Deleted => sys.error(s"File $srcpath already being deleted.")
      case Idle =>
        files(srcpath) = info.copy(state = Deleted)
        Txn.afterCommit { _ =>
          FileUtils.forceDelete(info.toFile)
          files.single.remove(srcpath)
        }
        srcpath
    }
  }

  def findFiles(regex: String): Seq[FileInfo] = {
    val snapshot = files.single.snapshot
    val infos = snapshot.values.toArray
    infos.par.filter(_.path.matches(regex)).seq
  }

}

object FileSystem {

  sealed trait State {
    def inc: State
    def dec: State
  }

  case object Created extends State {
    override def inc = sys.error("File being created.")
    override def dec = sys.error("File being created.")
  }

  case object Idle extends State {
    override def inc = Copying(1)
    override def dec = sys.error("Idle not copied.")
  }

  case class Copying(n: Int) extends State {
    override def inc = Copying(n + 1)
    override def dec = if (n > 1) Copying(n - 1) else Idle
  }

  case object Deleted extends State {
    override def inc = sys.error("Cannot copy deleted.")
    override def dec = sys.error("Deleted not copied")
  }

}



