package me.mnedokushev.zio.google.drive.client

import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import zio._
import zio.macros.accessible

import java.io.IOException
import java.util.Collections
import scala.jdk.CollectionConverters.ListHasAsScala

@accessible
trait Folders {
  def create(name: String, parentFolderId: Option[FileId] = None): IO[IOException, File]
  def list(): IO[IOException, List[File]]
}

object Folders {

  val live: URLayer[Drive, Folders] =
    ZLayer {
      ZIO.serviceWith[Drive](FoldersLive)
    }

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
