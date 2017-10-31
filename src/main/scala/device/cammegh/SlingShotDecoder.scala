package device.cammegh

import device.cammegh.slingshot.Input
import scodec.bits.BitVector
import scodec.{Attempt, DecodeResult, Decoder, Err}

/**
  * `USPC Bee Tek Electronic Shoe` codec implementation.
  */
object SlingShotDecoder extends Decoder[Input] {

  // In Game mode, the device repeats the card characters after the first frame causing the decoder to fail on next frame.
  // This works around this by dumping the first two bytes on a decode failure.
  def decode(bits: BitVector): Attempt[DecodeResult[Input]] = Input.codec.decode(bits).recoverWith {
    case err: Err.InsufficientBits => Attempt.failure(err)
    case _ => bits.consumeThen(16)(msg => Attempt.failure(Err(msg)), (_, rem) => Input.codec.decode(rem))
  }
}