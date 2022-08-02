package me.mnedokushev.zio.google.drive.client

import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import zio._
import com.google.api.services.drive.model.File

import java.io
import java.io.OutputStream
import scala.jdk.CollectionConverters.ListHasAsScala

trait Files {
  def create(srcFilePath: String, name: String): Task[File]
  def downloadTo(fileId: String, outputStream: OutputStream): Task[Unit]
  def list(): Task[List[File]]
  def delete(fileId: String): Task[Unit]
  def update(fileId: String, content: File, filePath: String): Task[Unit]
}

final case class FilesLive(drive: Drive) extends Files {

  override def create(filePath: String, name: String): Task[File] =
    ZIO.attempt {
      val metadata     = new File()
      val file         = new io.File(filePath)
      val mediaContent = new FileContent(null, file)

      metadata.setName(name)

      drive.files().create(metadata, mediaContent).execute()
    }

  override def downloadTo(fileId: String, outputStream: OutputStream): Task[Unit] =
    ZIO.attempt(drive.files().get(fileId).executeMediaAndDownloadTo(outputStream))

  override def list(): Task[List[File]] =
    ZIO.attempt(drive.files().list().execute().getFiles.asScala.toList)

  override def delete(fileId: String): Task[Unit] =
    ZIO.attempt(drive.files().delete(fileId).execute())

  override def update(fileId: String, content: File, filePath: String): Task[Unit] =
    ZIO.attempt {
      val file         = new io.File(filePath)
      val mediaContent = new FileContent(null, file)

      drive.files().update(fileId, new File(), mediaContent).execute()
    }
}

object Files {

  val live: URLayer[Drive, Files] =
    ZLayer {
      for {
        drive <- ZIO.service[Drive]
      } yield FilesLive(drive)
    }

  def create(srcFilePath: String, name: String): RIO[Files, File] =
    ZIO.serviceWithZIO[Files](_.create(srcFilePath, name))

  def downloadTo(fileId: String, outputStream: OutputStream): RIO[Files, Unit] =
    ZIO.serviceWithZIO[Files](_.downloadTo(fileId, outputStream))

  def list(): RIO[Files, List[File]] =
    ZIO.serviceWithZIO[Files](_.list())

  def delete(fileId: String): RIO[Files, Unit] =
    ZIO.serviceWithZIO[Files](_.delete(fileId))

  def update(fileId: String, content: File, filePath: String): RIO[Files, Unit] =
    ZIO.serviceWithZIO[Files](_.update(fileId, content, filePath))

}
