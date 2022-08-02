package me.mnedokushev.zio.google.drive.appcli

import zio._
import zio.nio.file.Path

import java.io.IOException

trait Sync {
  def download(fileId: FileId, into: Path): IO[IOException, Unit]
}
