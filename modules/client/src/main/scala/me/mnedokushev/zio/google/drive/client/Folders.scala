package me.mnedokushev.zio.google.drive.client

import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import zio._
import zio.nio.file.Path

import java.io.IOException
import java.util.Collections

import scala.jdk.CollectionConverters.ListHasAsScala

trait Folders {
  def create(name: String, parentFolderId: Option[FileId] = None): IO[IOException, File]
  def list(): IO[IOException, List[File]]
}

object Folders {

  val live: URLayer[Drive, Folders] =
    ZLayer {
      ZIO.serviceWith[Drive](FoldersLive)
    }

  def create(name: String, parentFolderId: Option[FileId] = None): ZIO[Folders, IOException, File] =
    ZIO.serviceWithZIO[Folders](_.create(name, parentFolderId))

  def list(): ZIO[Folders, IOException, List[File]] =
    ZIO.serviceWithZIO[Folders](_.list)

}

final case class FoldersLive(drive: Drive) extends Folders {

  override def create(name: String, parentFolderId: Option[FileId] = None): IO[IOException, File] =
    ZIO.attemptBlockingIO {
      val metadata = new File()

      metadata.setName(name)
      metadata.setMimeType(MimeTypes.Folder)
      parentFolderId.foreach { id =>
        metadata.setParents(Collections.singletonList(id.toString))
      }

      val create = drive.files().create(metadata)
      parentFolderId.foreach(_ => create.setFields(Metadata.DefaultFields))

      create.execute()
    }

  override def list(): IO[IOException, List[File]] =
    ZIO.attemptBlockingIO {
      val filesList = drive.files().list()

      filesList.setQ(s"mimeType='${MimeTypes.Folder}'")
      filesList.execute().getFiles.asScala.toList
    }

}
