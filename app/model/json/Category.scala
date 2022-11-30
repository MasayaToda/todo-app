package model.json

import play.api.libs.json._
import play.api.libs.json.Reads._
import java.time.LocalDateTime
import play.api.data.Form
import play.api.data.Forms._
import lib.model.Category
import lib.model.Category._
import play.api.libs.functional.syntax._

case class CategoryJsonRequestBody(
  name:  String,
  slug:  String,
  color: Category.Color,
  )
object  CategoryJsonRequestBody {
  implicit val readColor: Reads[Category.Color] = (
    (JsPath \ "code").read[Short] and
    (JsPath \ "name").read[String](minLength[String](1)) and
    (JsPath \ "css").read[String]
  )((code, name, css) => Category.Color(code = code))
  implicit val reads: Reads[CategoryJsonRequestBody] = (
    (JsPath \ "name").read[String](minLength[String](1)) and
    (JsPath \ "slug").read[String](minLength[String](1)) and
    (JsPath \ "color").read[Category.Color]
  )(CategoryJsonRequestBody.apply _)
}
case class CategoryJsonResponseBody(
    id:       Long,
  name:       String,
  slug:       String,
  color:      Short,
  updatedAt:  LocalDateTime,
  createdAt:  LocalDateTime
)
object  CategoryJsonResponseBody {
    implicit val format: Format[CategoryJsonResponseBody] = Json.format

  def write(category: Category.EmbeddedId): CategoryJsonResponseBody = {
    CategoryJsonResponseBody(
      id           = category.id,
      name        = category.v.name,
      slug        = category.v.slug,
      color      = category.v.color.code,
      createdAt = category.v.createdAt,
      updatedAt = category.v.updatedAt
    )
  }
}