package me.mnedokushev.zio.google.drive.client

import zio._
import zio.test._
import zio.test.TestAspect._
import Fixtures._

object FoldersSpec extends ZIOSpecDefault with Utils {

  val layer: TaskLayer[Folders with Files] =
    ZLayer.make[Folders with Files](driverLive(appName), Folders.live, Files.live)

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("FoldersSpec")(
      test("create folder") {
        for {
          created <- Folders.create(folderPath, folderName)
        } yield assertTrue(
          created.getId.nonEmpty,
          created.getName == folderName,
          created.getMimeType == MimeTypes.Folder
        )
      },
      test("created folder should be present in the files list") {
        for {
          created <- Folders.create(folderPath, folderName)
          list    <- Files.list()
        } yield assertTrue(list.contains(created))
      },
      test("create folder in a given sub folder") {
        for {
          parentFolder <- Folders.create(folderPath, folderName)
          subFolder    <- Folders.create(subFolderPath, subFolderName, Some(parentFolder.getId))
        } yield assertTrue(
          parentFolder.getId.nonEmpty,
          subFolder.getId.nonEmpty,
          subFolder.getName == subFolderName,
          subFolder.getParents.contains(parentFolder.getId)
        )
      }
    ).provideLayer(layer) @@ afterAll(cleanup(layer))

}
