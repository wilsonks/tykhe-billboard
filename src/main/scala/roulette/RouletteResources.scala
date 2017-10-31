package roulette

import display.ecs.Resources
import scala.collection.JavaConverters._

class RouletteResources(path: String) extends Resources(path) {
  override def loadParticleEffects(): Unit = {
    super.loadParticleEffects()
    particleEffects.keySet().asScala.foreach(s =>
      if (!particleEffectNamesToLoad.contains(s)) particleEffects.remove(s))
    particleEffectNamesToLoad.iterator().asScala.foreach(name => particleEffects.put(name, loadParticleEffect(name)))
  }
}