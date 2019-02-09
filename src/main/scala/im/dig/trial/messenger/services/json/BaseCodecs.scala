package im.dig.trial.messenger.services.json

import argonaut.Argonaut._
import argonaut.{CodecJson, DecodeJson, EncodeJson}
import cats._
import cats.implicits._
import im.dig.trial.messenger.services.model.Read
import im.dig.trial.messenger.services.model.ReadSyntax._

trait BaseCodecs {

  implicit def encodeSeq[A : EncodeJson]: EncodeJson[Seq[A]] = EncodeJson {
    seq => seq.toVector.asJson
  }

  implicit def decodeSeq[A : DecodeJson]: DecodeJson[Seq[A]] = DecodeJson {
    cursor => cursor.as[Vector[A]].map(_.toSeq)
  }

  implicit def encodeIndexedSeq[A : EncodeJson]: EncodeJson[IndexedSeq[A]] = EncodeJson {
    indexedSeq => indexedSeq.toVector.asJson
  }

  implicit def decodeIndexedSeq[A : DecodeJson]: DecodeJson[IndexedSeq[A]] = DecodeJson {
    cursor => cursor.as[Vector[A]].map(_.toIndexedSeq)
  }

  implicit def codecReadShow[A : Read : Show]: CodecJson[A] = CodecJson (
    a => a.show.asJson,
    decoder => for { string <- decoder.as[String] } yield string.read[A]
  )

}
