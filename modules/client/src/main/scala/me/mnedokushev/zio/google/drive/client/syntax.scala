package me.mnedokushev.zio.google.drive.client

import com.google.api.services.drive.model.File

object syntax {

  implicit class FileOps(val file: File) extends AnyVal {

    def getFileId: FileId = FileId(file.getId)

  }

  implicit class FileListOps(val files: List[File]) extends AnyVal {

    def getIds: List[String]     = files.map(_.getId)
    def getFileIds: List[FileId] = files.map(_.getFileId)

  }

}
