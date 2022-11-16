package lib.model

import ixias.model._
import ixias.util.EnumStatus
import java.time.LocalDateTime

import Category._
case class Category (
  id:         Option[Id],
  name:       String,
  slug:      String,
  color:       Color,
  updatedAt:  LocalDateTime = NOW,
  createdAt:  LocalDateTime = NOW
)extends EntityModel[Id]

object Category {
  
  val  Id = the[Identity[Id]]
  type Id = Long @@ Category
  type WithNoId = Entity.WithNoId [Id, Category]
  type EmbeddedId = Entity.EmbeddedId[Id, Category]
  // Color定義
  //~~~~~~~~~~~~~~~~~
  sealed abstract class Color(val code: Short, val name: String, val css: String) extends EnumStatus
  object Color extends EnumStatus.Of[Color] {
    case object RED extends Color(code = 1,   name = "Category(着手前)", css = "danger")
    case object YELLO   extends Color(code = 2, name = "進行中", css = "warning")
    case object BLUE   extends Color(code = 3, name = "完了", css = "info")
  }
  def apply(name: String, slug: String, color: Color): WithNoId = {
    new Entity.WithNoId(
      new Category(
        id    = None,
        name = name,
        slug = slug,
        color  = color,
      )
    )
  }
  case class CategoryFormValue (
    name: String, slug: String, color: Color)
}