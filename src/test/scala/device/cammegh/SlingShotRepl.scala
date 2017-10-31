package device.cammegh

import java.util.concurrent.CountDownLatch

import device.io._
import monix.execution.Scheduler
import monix.execution.schedulers.SchedulerService
import monix.reactive.Observable

import scala.concurrent.duration._

/**
  * REPL to test `USPC Bee Tek Electronic Shoe` protocol.
  */
object SlingShotRepl extends App {

  import slingshot._

  implicit val scheduler: SchedulerService = Scheduler.singleThread("usb", daemonic = false)
  val latch = new CountDownLatch(1)
  println("press enter to exit")
  val device = Observable.repeatEval(io.StdIn.readLine())
    .takeWhile(_.nonEmpty)
    .map(s => (if (s.length == 2) s else s + "\r\n").hex.bits)
    .doOnTerminate(_ => latch.countDown())
    .debug("<")
    .decode(Input.codec)
    .debug("<<")
    .subscribe()
  latch.await()
  scheduler.shutdown()
  scheduler.awaitTermination(2.seconds, Scheduler.global)
}
