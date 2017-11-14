package roulette

import java.util.concurrent.CountDownLatch

import better.files.File
import com.typesafe.config.ConfigFactory
import device.cammegh.slingshot._
import device.io._
import display.io.WindowConfig
import monix.execution.Scheduler
import monix.execution.schedulers.SchedulerService
import monix.reactive.Observable
import pureconfig._
import roulette.State.Running
import roulette.ecs.{BillboardSceneRB,BillboardScene}
import scodec.bits.ByteVector

import scala.concurrent.duration._

object BillboardApp extends App {

  implicit val scheduler: SchedulerService = Scheduler.fixedPool("usb", 4)

  //main thread will wait until gate is open - gate is open when ui thread on complete.
  val latch = new CountDownLatch(1)

  val conf = ConfigFactory.load

  val file = File(conf.getString("persistence.file"))

  val seed: State = if (file.exists) {
    file.readDeserialized[Running]
  } else {
    file.createIfNotExists(asDirectory = false, createParents = true)
    file.writeSerialized(Running("EU-01", Seq.empty[String], 100, 100, 10000))
    file.readDeserialized[Running]
  }

  val config = loadConfigOrThrow[WindowConfig]("window")

  val window = if(conf.getString("app.theme") == "red-black") BillboardSceneRB(seed) else BillboardScene(seed)

  val (scene, ui) = display.io.desktop.open(window -> config)


  val device = Observable.interval(10.seconds)
    .map { x => (math.random() * 37).toInt }
    .debug("number")
    .map { s => (" " + s).takeRight(2) }
    .map(s => ByteVector(s.toCharArray.map(_.toByte)).bits)
    .debug("bits")

  // device
  //  val hub = device.io.usb.hub(device.io.usb.pl2303)
  //  hub.scan.foreach {
  //    case DeviceAttached(usb) =>
  //      println(s"device attached $usb")
  //      val (_, wheel) = hub.open(usb)
  //        .pipe(device.io.reader(Input.codec))
  //        .unicast
  //      wheel.foreach(scene.onNext)
  //    case DeviceDetached(usb) =>
  //      println(s"device detached $usb")
  //  }
  //  val device = Observable.repeatEval(io.StdIn.readLine())
  //    .takeWhile(_.nonEmpty)
  //    .map(s => (if (s.length == 2) s else s + "\r\n").hex.bits)
  //    .doOnTerminate(_ => latch.countDown())
  //    .debug("<")
  //    .debug("<<")

  Thread.sleep(5000L)
  device.decode(Input.codec)
    .debug("protocol")
    .collect {
      case Win(num) => Event.SpinCompleted(num)
    }.foreach(scene.onNext)

  // latch gate is open when ui thread on complete.
  ui.doOnTerminate(_ => latch.countDown()).foreach {
    case s: State.Running => file.writeSerialized(s)
    case _ =>
  }
  latch.await()
  scheduler.shutdown()
  scheduler.awaitTermination(2.seconds, Scheduler.global)
}
