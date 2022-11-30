package model.json

import play.api.libs.json._
import play.api.libs.json.Reads._
import java.time.LocalDateTime
import play.api.data.Form
import play.api.data.Forms._
import lib.model.{Todo, Category}
import lib.model.Todo._
import lib.model.Category._
import play.api.libs.functional.syntax._

case class TodoJsonRequestBody(
  categoryId: Long,
  title: String,
  body: String,
  state: Short,
)

object  TodoJsonRequestBody {
  implicit val reads: Reads[TodoJsonRequestBody] = (
    (JsPath \ "categoryId").read[Long] and
    (JsPath \ "title").read[String](minLength[String](1)) and
    (JsPath \ "body").read[String](minLength[String](1)) and
    (JsPath \ "state").read[Short]
  )(TodoJsonRequestBody.apply _)
  implicit val writes: Writes[TodoJsonRequestBody] = Json.writes[TodoJsonRequestBody]
}
case class TodoCategoryJsonResponseBody(
    id: Long,
    categoryId: Long,
    title: String,
    body: String,
    state: Short,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime,
    categoryName: String,
    categoryColor: String,
)
object  TodoCategoryJsonResponseBody {
  implicit val format: Format[TodoCategoryJsonResponseBody] = Format(Json.reads[TodoCategoryJsonResponseBody], Json.writes[TodoCategoryJsonResponseBody])

  def write(todo: Todo.EmbeddedId, categories: Seq[Category.EmbeddedId]): TodoCategoryJsonResponseBody = {
    TodoCategoryJsonResponseBody(
      id           = todo.id,
      categoryId   = todo.v.categoryId,
      title        = todo.v.title,
      body      = todo.v.body,
      state      = todo.v.state.code,
      createdAt = todo.v.createdAt,
      updatedAt = todo.v.updatedAt,
      categories.find(_.id == todo.v.categoryId).map(_.v.name).getOrElse("未設定"),
      categories.find(_.id == todo.v.categoryId).map(_.v.color.css).getOrElse("未設定"),
    )
  }
}

case class StatusResponseBody(
  code: Short,
  name: String
)
object  StatusResponseBody {
  implicit val format: Format[StatusResponseBody] = Format(Json.reads[StatusResponseBody], Json.writes[StatusResponseBody])

  def write(status: Todo.Status): StatusResponseBody = {
    StatusResponseBody(
      code   = status.code,
      name   = status.name,
    )
  }
}