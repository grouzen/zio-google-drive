package me.mnedokushev.zio.google.drive

import zio.prelude.Newtype

package object appcli {

  object FileId extends Newtype[String]
  type FileId = FileId.Type

}
