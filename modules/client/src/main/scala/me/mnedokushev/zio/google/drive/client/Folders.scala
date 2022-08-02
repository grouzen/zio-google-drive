package me.mnedokushev.zio.google.drive.client

import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import zio._

import java.io.IOException
import java.util.Collections

trait Folders {
  def create(folderPath: String, name: String, parentFolderId: Option[String] = None): IO[IOException, File]
}

object Folders {

  val live: URLayer[Drive, Folders] =
    ZLayer {
      ZIO.serviceWith[Drive](FoldersLive)
    }

  def create(folderPath: String, name: String, parentFolderId: Option[String] = None): ZIO[Folders, IOException, File] =
    ZIO.serviceWithZIO[Folders](_.create(folderPath, name, parentFolderId))

}

final case class FoldersLive(drive: Drive) extends Folders {

  override def create(folderPath: String, name: String, parentFolderId: Option[String] = None): IO[IOException, File] =
    ZIO.attemptBlockingIO {
      val metadata = new File()

      metadata.setName(name)
      metadata.setMimeType(MimeTypes.Folder)
      parentFolderId.foreach { id =>
        metadata.setParents(Collections.singletonList(id))
      }

      val create = drive.files().create(metadata)
      parentFolderId.foreach(_ => create.setFields(Metadata.DefaultFields))

      create.execute()
    }
}
