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

  case class ViewSelectList(val id:String,val name:String)
  // Color定義
  //~~~~~~~~~~~~~~~~~
  sealed abstract class Color(val code: Short, val name: String, val css: String) extends EnumStatus
  object Color extends EnumStatus.Of[Color] {
    case object RED extends Color(code = 1,   name = "Red", css = "danger")
    case object YELLO   extends Color(code = 2, name = "Yello", css = "warning")
    case object BLUE   extends Color(code = 3, name = "Blue", css = "info")
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
}