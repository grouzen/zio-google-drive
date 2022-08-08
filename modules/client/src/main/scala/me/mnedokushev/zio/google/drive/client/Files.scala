package me.mnedokushev.zio.google.drive.client

import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import zio._
import com.google.api.services.drive.model.File
import zio.macros.accessible

import java.io
import java.io.{ IOException, OutputStream }
import zio.nio.file.Path

import java.util.Collections
import scala.jdk.CollectionConverters.ListHasAsScala

@accessible
trait Files {
  def create(filePath: Path, name: String, parentFolderId: Option[FileId] = None): IO[IOException, File]
  def downloadTo(fileId: FileId, outputStream: OutputStream): IO[IOException, Unit]
  def list(): IO[IOException, List[File]]
  def delete(fileId: FileId): IO[IOException, Unit]
  def update(fileId: FileId, filePath: Path): IO[IOException, Unit]
}

object Files {

  val live: URLayer[Drive, Files] =
    ZLayer {
      ZIO.serviceWith[Drive](FilesLive)
    }

}

final case class FilesLive(drive: Drive) extends Files {

  override def create(filePath: Path, name: String, parentFolderId: Option[FileId] = None): IO[IOException, File] =
    ZIO.attemptBlockingIO {
      val metadata     = new File()
      val file         = new io.File(filePath.toString)
      val mediaContent = new FileContent(null, file)

      metadata.setName(name)
      parentFolderId.foreach { id =>
        metadata.setParents(Collections.singletonList(FileId.unwrap(id)))
      }

      val create = drive.files().create(metadata, mediaContent)
      parentFolderId.foreach(_ => create.setFields(Metadata.DefaultFields))

      create.execute()
    }

  override def downloadTo(fileId: FileId, outputStream: OutputStream): IO[IOException, Unit] =
    ZIO.attemptBlockingIO(drive.files().get(FileId.unwrap(fileId)).executeMediaAndDownloadTo(outputStream))

  override def list(): IO[IOException, List[File]] =
    ZIO.attemptBlockingIO(drive.files().list().execute().getFiles.asScala.toList)

  override def delete(fileId: FileId): IO[IOException, Unit] =
    ZIO.attemptBlockingIO(drive.files().delete(FileId.unwrap(fileId)).execute())

  override def update(fileId: FileId, filePath: Path): IO[IOException, Unit] =
    ZIO.attemptBlockingIO {
      val file         = new io.File(filePath.toString)
      val mediaContent = new FileContent(null, file)

      drive.files().update(FileId.unwrap(fileId), new File(), mediaContent).execute()
    }
}
