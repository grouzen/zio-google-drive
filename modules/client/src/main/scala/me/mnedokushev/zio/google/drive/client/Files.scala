package me.mnedokushev.zio.google.drive.client

import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import zio._
import com.google.api.services.drive.model.File

import java.io
import java.io.{ IOException, OutputStream }
import java.util.Collections
import scala.jdk.CollectionConverters.ListHasAsScala

trait Files {
  def create(filePath: String, name: String, parentFolderId: Option[String] = None): IO[IOException, File]
  def downloadTo(fileId: String, outputStream: OutputStream): IO[IOException, Unit]
  def list(): IO[IOException, List[File]]
  def delete(fileId: String): IO[IOException, Unit]
  def update(fileId: String, filePath: String): IO[IOException, Unit]
}

object Files {

  val live: URLayer[Drive, Files] =
    ZLayer {
      ZIO.serviceWith[Drive](FilesLive)
    }

  def create(filePath: String, name: String, parentFolderId: Option[String] = None): ZIO[Files, IOException, File] =
    ZIO.serviceWithZIO[Files](_.create(filePath, name, parentFolderId))

  def downloadTo(fileId: String, outputStream: OutputStream): ZIO[Files, IOException, Unit] =
    ZIO.serviceWithZIO[Files](_.downloadTo(fileId, outputStream))

  def list(): ZIO[Files, IOException, List[File]] =
    ZIO.serviceWithZIO[Files](_.list())

  def delete(fileId: String): ZIO[Files, IOException, Unit] =
    ZIO.serviceWithZIO[Files](_.delete(fileId))

  def update(fileId: String, filePath: String): ZIO[Files, IOException, Unit] =
    ZIO.serviceWithZIO[Files](_.update(fileId, filePath))

}

final case class FilesLive(drive: Drive) extends Files {

  override def create(filePath: String, name: String, parentFolderId: Option[String] = None): IO[IOException, File] =
    ZIO.attemptBlockingIO {
      val metadata     = new File()
      val file         = new io.File(filePath)
      val mediaContent = new FileContent(null, file)

      metadata.setName(name)
      parentFolderId.foreach { id =>
        metadata.setParents(Collections.singletonList(id))
      }

      val create = drive.files().create(metadata, mediaContent)
      parentFolderId.foreach(_ => create.setFields(Metadata.DefaultFields))

      create.execute()
    }

  override def downloadTo(fileId: String, outputStream: OutputStream): IO[IOException, Unit] =
    ZIO.attemptBlockingIO(drive.files().get(fileId).executeMediaAndDownloadTo(outputStream))

  override def list(): IO[IOException, List[File]] =
    ZIO.attemptBlockingIO(drive.files().list().execute().getFiles.asScala.toList)

  override def delete(fileId: String): IO[IOException, Unit] =
    ZIO.attemptBlockingIO(drive.files().delete(fileId).execute())

  override def update(fileId: String, filePath: String): IO[IOException, Unit] =
    ZIO.attemptBlockingIO {
      val file         = new io.File(filePath)
      val mediaContent = new FileContent(null, file)

      drive.files().update(fileId, new File(), mediaContent).execute()
    }
}
