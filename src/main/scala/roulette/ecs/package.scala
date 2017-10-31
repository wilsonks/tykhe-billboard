package roulette

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.uwsoft.editor.renderer.components.label.LabelComponent
import com.uwsoft.editor.renderer.components.{TintComponent, ZIndexComponent}
import display.ecs._
import rx.{Ctx, Obs, Rx}

package object ecs {

  implicit class TpEntityOps(val src: Entity) extends AnyVal {

    def index: ZIndexComponent = src.apply(classOf[ZIndexComponent])

    def label: LabelComponent = src.apply(classOf[LabelComponent])
  }

  implicit class TpRxOps[T](val src: Rx[T]) extends AnyVal {

    def paints(label: LabelComponent)(implicit ev: T =:= Color, owner: Ctx.Owner): Obs = src.foreach(t => label.style.fontColor = t)

    def paints(tint: TintComponent)(implicit ev: T =:= Color, owner: Ctx.Owner): Obs = src.foreach(t => tint.color = t)

    def paints(label: LabelComponent, tint: TintComponent)(implicit ev: T =:= Color, owner: Ctx.Owner): Obs = src.foreach { t =>
      label.style.fontColor = t
      tint.color = t
    }

    def resets(e: Entity)(implicit ev: T =:= Option[String], owner: Ctx.Owner): Obs = src.map(ev(_).getOrElse("")).updates(e)

    def updates(e: Entity)(implicit ev: T =:= String, owner: Ctx.Owner): Obs = src.foreach(t => e.label.setText(t))

    def shows(e1: Entity, e2: Entity, e3: Entity*)(implicit ev: T =:= Boolean, owner: Ctx.Owner): Obs = src.foreach { show =>
      e1.item.show(show)
      e2.item.show(show)
      e3.foreach(_.item.show(show))
    }

    def updatesWithColor(e: Entity, col: Color)(implicit ev: T =:= String, owner: Ctx.Owner): Obs = src.foreach {
      t => {
        e.label.style.fontColor = col
        e.tint.color = col
        e.label.setText(t)
      }
    }
  }

}

