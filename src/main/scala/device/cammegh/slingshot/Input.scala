package device.cammegh.slingshot

import scodec.Codec
import scodec.codecs._
import scodec.codecs.literals._

/**
  * `Cammegh slingshot 2 Roulette Wheel` Read protocol messages.
  */
sealed trait Input extends Product with Serializable


case class Win(number: String) extends Input

case class BonusWin(bonusType: BonusType,
                    winNumber: String) extends Input

case class Status(state: State,
                  spinCount: Int,
                  winNum: String,
                  warning: Warning,
                  speed: Int,
                  direction: Direction
                 ) extends Input


sealed trait BonusType extends Product with Serializable

object BonusType {
  val codec: Codec[BonusType] = mappedEnum[BonusType, String](fixedSizeBytes(1, ascii),
    Bonus1 -> "1",
    Bonus2 -> "2",
    Bonus3 -> "3",
    Bonus4 -> "4",
    Bonus5 -> "5",
    Bonus6 -> "6")

  case object Bonus1 extends BonusType

  case object Bonus2 extends BonusType

  case object Bonus3 extends BonusType

  case object Bonus4 extends BonusType

  case object Bonus5 extends BonusType

  case object Bonus6 extends BonusType

}

object BonusWin {
  val codec: Codec[BonusWin] = "bonus-type" | "*Y;".hex ~> (BonusType.codec ::
    (";".hex ~> ("wheel-direction" | fixedSizeBytes(3, ascii)) <~ "\r\n".hex)).as[BonusWin]
}


sealed trait Direction extends Product with Serializable

object Direction {
  val codec: Codec[Direction] = mappedEnum[Direction, String](fixedSizeBytes(1, ascii),
    ClockWise -> "0",
    AntiClockWise -> "1")


  case object ClockWise extends Direction

  case object AntiClockWise extends Direction

}

sealed trait Warning extends Product with Serializable

object Warning {

  val codec: Codec[Warning] = mappedEnum[Warning, String](fixedSizeBytes(1, ascii),
    NoWarning -> "0",
    BallRemoved -> "1",
    BallDirection -> "2",
    SensorBroken -> "4",
    LaunchFailed -> "8",
    Composite(Seq(BallRemoved, LaunchFailed)) -> "9",
    Composite(Seq(BallDirection, LaunchFailed)) -> "A",
    Composite(Seq(BallRemoved, BallDirection, LaunchFailed)) -> "B",
    Composite(Seq(SensorBroken, LaunchFailed)) -> "C",
    Composite(Seq(BallRemoved, SensorBroken, LaunchFailed)) -> "D",
    Composite(Seq(BallDirection, SensorBroken, LaunchFailed)) -> "E",
    Composite(Seq(BallRemoved, BallDirection, SensorBroken, LaunchFailed)) -> "F")

  case class Composite(warnings: Seq[Warning]) extends Warning

  case object NoWarning extends Warning

  case object BallRemoved extends Warning

  case object BallDirection extends Warning

  case object SensorBroken extends Warning

  case object LaunchFailed extends Warning

}

sealed trait State

case object WheelStart extends State

case object PlaceYourBets extends State

case object BallInRim extends State

case object NoMoreBets extends State

case object BallDetected extends State

case object TurnedIdle extends State

object State {
  val codec: Codec[State] = "wheel-state" | mappedEnum[State, String](fixedSizeBytes(1, ascii),
    WheelStart -> "1",
    PlaceYourBets -> "2",
    BallInRim -> "3",
    NoMoreBets -> "4",
    BallDetected -> "5",
    TurnedIdle -> "6")
}

object Status {
  val codec: Codec[Status] = "status" | "*X;".hex ~> (State.codec ::
    (";".hex ~> ("spin-num" | fixedSizeBytes(3, ascii).toInt)) ::
    (";".hex ~> ("win-num" | fixedSizeBytes(2, ascii))) ::
    (";".hex ~> ("warning-flags" | Warning.codec)) ::
    (";".hex ~> ("wheel-speed" | fixedSizeBytes(3, ascii).toInt)) ::
    (";".hex ~> ("wheel-direction" | Direction.codec) <~ "\r\n".hex)).as[Status]
}

object Win {
  val codec: Codec[Win] = "win-num" | fixedSizeBytes(2, ascii).as[Win]
}

object Input {

  val codec: Codec[Input] = "input" | oneOf[Input](
    Status.codec.upcast[Input],
    BonusWin.codec.upcast[Input],
    Win.codec.upcast[Input])
}
