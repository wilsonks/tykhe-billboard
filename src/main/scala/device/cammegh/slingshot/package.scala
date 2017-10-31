package device.cammegh

import scodec.bits.{BitVector, ByteVector}
import scodec.codecs._
import scodec.{Attempt, Codec, DecodeResult, Decoder, Encoder, Err}

package object slingshot {

  val char8: Codec[Char] = fixedSizeBytes(1, ascii).xmap[Char](_.head, String.valueOf)

  /**
    * Returns codec that applies the given `choices` until success.
    * On failure, an [[scodec.Err.insufficientBits]] is returned in collected and returned. This enables the codec to
    * be used in a streaming context.
    *
    * @param choices codec choices
    * @return choice codec
    */
  def oneOf[A](choices: Codec[A]*): Codec[A] = Codec(
    Encoder.choiceEncoder(choices: _*),
    new Decoder[A] {
      override def decode(bits: BitVector): Attempt[DecodeResult[A]] = {
        @annotation.tailrec def go(rem: List[Decoder[A]], acc: List[Err]): Attempt[DecodeResult[A]] = rem match {
          case Nil => Attempt.failure(acc.collectFirst {
            case err: Err.InsufficientBits => err
          }.getOrElse(acc.headOption.getOrElse(Err("unknown choice"))))
          case hd :: tl => hd.decode(bits) match {
            case res@Attempt.Successful(_) => res
            case Attempt.Failure(err) => go(tl, err :: acc)
          }
        }

        go(choices.toList, Nil)
      }
    })

  implicit class CodecOps(val src: Codec[String]) extends AnyVal {

    def toInt: Codec[Int] = src.xmap[Int](_.toInt, _.toString)
  }

  implicit class HexString(val src: String) extends AnyVal {

    /** Returns a [[scodec.bits.ByteVector]] with the characters in the string. */
    def hex: ByteVector = ByteVector(src.toCharArray: _*)
  }

}
