package me.mnedokushev.zio.google.drive.client

import zio.nio.file.Path

object Fixtures {

  val appName = "test-app"

  val folderPath    = Path("test")
  val folderName    = "test-dir"
  val subFolderPath = folderPath / "subtest"
  val subFolderName = "subtest-dir"
  val fileName1     = "1.txt"
  val filePath1     = folderPath / fileName1
  val fileName2     = "2.txt"
  val filePath2     = folderPath / fileName2

}
