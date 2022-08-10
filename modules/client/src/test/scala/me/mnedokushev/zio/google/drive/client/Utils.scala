package me.mnedokushev.zio.google.drive.client

import me.mnedokushev.zio.google.drive.client.FilesSpec.layer
import zio.{ TaskLayer, URIO, ZIO }
import syntax._

trait Utils {

  protected def cleanup(layer: TaskLayer[Files]): URIO[Any, Unit] =
    (for {
      files <- Files.list()
      _     <- ZIO.foreachDiscard(files)(f => Files.delete(f.getFileId))
    } yield ()).provideLayer(layer).ignore

}
