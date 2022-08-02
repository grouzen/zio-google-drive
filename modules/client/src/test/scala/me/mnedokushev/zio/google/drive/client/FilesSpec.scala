package me.mnedokushev.zio.google.drive.client

import zio._
import zio.nio.channels.FileChannel
import zio.nio.file.Path
import zio.test._
import zio.test.TestAspect._

import java.io.{ ByteArrayOutputStream, IOException }

object FilesSpec extends ZIOSpecDefault {

  val layer: TaskLayer[Files] =
    ZLayer.make[Files](driverLive("test-app"), Files.live)

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("FilesSpec")(
      test("create file") {
        for {
          result <- Files.create("test/1.txt", "1.txt")
        } yield assertTrue(result.getId.nonEmpty)
      },
      test("get file") {
        for {
          created        <- Files.create("test/1.txt", "1.txt")
          os              = new ByteArrayOutputStream()
          _              <- Files.downloadTo(created.getId, os)
          inputByteArray <- getFileContent("test/1.txt")
          outputByteArray = os.toByteArray
        } yield assertTrue(inputByteArray.sameElements(outputByteArray))
      },
      test("list files") {
        for {
          files   <- ZIO.collectAll(List(Files.create("test/1.txt", "1.txt"), Files.create("test/1.txt", "2.txt")))
          filesIds = files.map(_.getId)
          list    <- Files.list()
          listIds  = list.map(_.getId)
        } yield assertTrue(filesIds.forall(id => listIds.contains(id)))
      },
      test("delete file") {
        for {
          forDelete  <- Files.create("test/1.txt", "1.txt")
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
          original  <- Files.create("test/1.txt", "1.txt")
          originalOs = new ByteArrayOutputStream()
          _         <- Files.downloadTo(original.getId, originalOs)
          _         <- Files.update(original.getId, original, "test/2.txt")
          updatedOs  = new ByteArrayOutputStream()
          _         <- Files.downloadTo(original.getId, updatedOs)
        } yield assertTrue(!updatedOs.toByteArray.sameElements(originalOs.toByteArray))
      }
    ).provideLayer(layer) @@ afterAll(cleanup.provideLayer(layer).ignore)

  private def cleanup =
    for {
      files <- Files.list()
      _     <- ZIO.foreachDiscard(files)(f => Files.delete(f.getId))
    } yield ()

  private def getFileContent(filePath: String): ZIO[Any, IOException, Array[Byte]] =
    ZIO.scoped {
      for {
        createdFileChannel <- FileChannel.open(Path("test/1.txt"))
        createdSize        <- createdFileChannel.size
        createdChunk       <- createdFileChannel.flatMapBlocking(_.readChunk(createdSize.toInt))
      } yield createdChunk.toArray[Byte]
    }

}
