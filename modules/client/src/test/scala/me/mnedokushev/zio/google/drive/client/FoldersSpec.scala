package me.mnedokushev.zio.google.drive.client

import zio._
import zio.test._
import zio.test.TestAspect._
import Fixtures._
import syntax._

object FoldersSpec extends ZIOSpecDefault with Utils {

  val layer: TaskLayer[Folders with Files] =
    ZLayer.make[Folders with Files](driverLive(appName), Folders.live, Files.live)

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("FoldersSpec")(
      test("create folder") {
        for {
          created <- Folders.create(folderName)
        } yield assertTrue(
          created.getId.nonEmpty,
          created.getName == folderName,
          created.getMimeType == MimeTypes.Folder
        )
      },
      test("created folder should be present in the files list") {
        for {
          created <- Folders.create(folderName)
          list    <- Files.list()
        } yield assertTrue(list.contains(created))
      },
      test("create folder in a given sub folder") {
        for {
          parentFolder <- Folders.create(folderName)
          subFolder    <- Folders.create(subFolderName, Some(parentFolder.getFileId))
        } yield assertTrue(
          parentFolder.getId.nonEmpty,
          subFolder.getId.nonEmpty,
          subFolder.getName == subFolderName,
          subFolder.getParents.contains(parentFolder.getId)
        )
      },
      test("delete folder with files in it") {
        for {
          folder        <- Folders.create(folderName)
          file          <- Files.create(filePath1, fileName1, Some(folder.getFileId))
          listBefore    <- Files.list()
          _             <- Files.delete(folder.getFileId)
          listAfter     <- Files.list()
          listCreatedIds = List(folder, file).getIds
        } yield assertTrue(
          listCreatedIds.forall(listBefore.getIds.contains(_)),
          !listCreatedIds.forall(listAfter.getIds.contains(_))
        )
      },
      test("delete folder recursively") {
        for {
          folder        <- Folders.create(folderName)
          file1         <- Files.create(filePath1, fileName1, Some(folder.getFileId))
          subFolder     <- Folders.create(subFolderName, Some(folder.getFileId))
          file2         <- Files.create(filePath2, fileName2, Some(subFolder.getFileId))
          listBefore    <- Files.list()
          _             <- Files.delete(folder.getFileId)
          listAfter     <- Files.list()
          listCreatedIds = List(folder, file1, subFolder, file2).getIds
        } yield assertTrue(
          listCreatedIds.forall(listBefore.getIds.contains(_)),
          !listCreatedIds.forall(listAfter.getIds.contains(_))
        )
      },
      test("list only directories") {
        for {
          created <- ZIO.collectAll(List(Files.create(filePath1, fileName1), Folders.create(folderName)))
          list    <- Folders.list()
        } yield assertTrue(
          list.forall(_.getMimeType == MimeTypes.Folder),
          list.exists(_.getName == folderName),
          !created.getIds.forall(list.getIds.contains(_))
        )
      }
    ).provideLayer(layer) @@ afterAll(cleanup(layer))

}
