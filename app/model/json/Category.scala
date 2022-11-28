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
  color: Short,
)

object  CategoryJsonRequestBody {
  implicit val reads: Reads[CategoryJsonRequestBody] = (
    (JsPath \ "name").read[String](minLength[String](1)) and
    (JsPath \ "slug").read[String](minLength[String](1)) and
    (JsPath \ "color").read[Short]
  )(CategoryJsonRequestBody.apply _)
  implicit val writes: Writes[CategoryJsonRequestBody] = Json.writes[CategoryJsonRequestBody]
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
  implicit val format: Format[CategoryJsonResponseBody] = Format(Json.reads[CategoryJsonResponseBody], Json.writes[CategoryJsonResponseBody])

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