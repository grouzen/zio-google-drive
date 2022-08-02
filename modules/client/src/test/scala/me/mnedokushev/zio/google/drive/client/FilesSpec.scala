package me.mnedokushev.zio.google.drive.client

import zio._
import zio.nio.channels.FileChannel
import zio.nio.file.Path
import zio.test._
import zio.test.TestAspect._
import Fixtures._

import java.io.{ ByteArrayOutputStream, FileNotFoundException, IOException }

object FilesSpec extends ZIOSpecDefault with Utils {

  val layer: TaskLayer[Files with Folders] =
    ZLayer.make[Files with Folders](driverLive(appName), Files.live, Folders.live)

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("FilesSpec")(
      test("create file") {
        for {
          created           <- Files.create(filePath1, fileName1)
          localContent      <- getFileContent(filePath1)
          downloadedContent <- downloadContent(created.getId)
        } yield assertTrue(
          created.getId.nonEmpty,
          localContent.sameElements(downloadedContent)
        )
      },
      test("create files with the same name") {
        for {
          created1 <- Files.create(filePath1, fileName1)
          created2 <- Files.create(filePath1, fileName1)
        } yield assertTrue(
          created1.getName == created2.getName,
          created1.getId != created2.getId
        )
      },
      test("create file in a given directory") {
        for {
          folder  <- Folders.create(folderPath, folderName)
          created <- Files.create(filePath1, fileName1, Some(folder.getId))
        } yield assertTrue(
          folder.getId.nonEmpty,
          created.getId.nonEmpty,
          created.getName == fileName1,
          created.getParents.contains(folder.getId)
        )
      },
//      test("create directory failed") {
//        assertZIO(Files.create(dirPath, "test-dir"))(Assertion.throwsA[FileNotFoundException])
//      },
      test("get file") {
        for {
          created       <- Files.create(filePath1, fileName1)
          inputContent  <- getFileContent(filePath1)
          outputContent <- downloadContent(created.getId)
        } yield assertTrue(inputContent.sameElements(outputContent))
      },
      test("list files") {
        for {
          files   <- ZIO.collectAll(List(Files.create(filePath1, fileName1), Files.create(filePath1, fileName2)))
          filesIds = files.map(_.getId)
          list    <- Files.list()
          listIds  = list.map(_.getId)
        } yield assertTrue(filesIds.forall(id => listIds.contains(id)))
      },
      test("delete file") {
        for {
          forDelete  <- Files.create(filePath1, fileName1)
          listBefore <- Files.list()
          _          <- Files.delete(forDelete.getId)
          listAfter  <- Files.list()
        } yield assertTrue(
          listBefore != listAfter,
          listBefore.contains(forDelete),
          !listAfter.contains(forDelete)
        )
      },
      test("update file") {
        for {
          original        <- Files.create(filePath1, fileName1)
          originalContent <- downloadContent(original.getId)
          _               <- Files.update(original.getId, filePath2)
          updatedContent  <- downloadContent(original.getId)
        } yield assertTrue(!updatedContent.sameElements(originalContent))
      }
    ).provideLayer(layer) @@ afterAll(cleanup(layer))

  private def getFileContent(filePath: String): ZIO[Any, IOException, Array[Byte]] =
    ZIO.scoped {
      for {
        createdFileChannel <- FileChannel.open(Path(filePath))
        createdSize        <- createdFileChannel.size
        createdChunk       <- createdFileChannel.flatMapBlocking(_.readChunk(createdSize.toInt))
      } yield createdChunk.toArray[Byte]
    }

  private def downloadContent(fileId: String): ZIO[Files, IOException, Array[Byte]] = {
    val os = new ByteArrayOutputStream()

    Files.downloadTo(fileId, os).as(os.toByteArray)
  }

}
