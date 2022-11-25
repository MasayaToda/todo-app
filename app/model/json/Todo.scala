package model.json

import play.api.libs.json._
import java.time.LocalDateTime
import play.api.data.Form
import play.api.data.Forms._
import lib.model.{Todo, Category}
import lib.model.Todo._
import lib.model.Category._

case class TodoCategoryJsonForm(
  categoryId: Long,
  title: String,
  body: String,
  state: Short,
)
case class TodoCategoryJson(
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
object  TodoCategoryJson {

	implicit val reads: Reads[TodoCategoryJson]   = Json.reads[TodoCategoryJson]
  implicit val writes: Writes[TodoCategoryJson] = Json.writes[TodoCategoryJson]
  def write(todo: Todo.EmbeddedId, categories: Seq[Category.EmbeddedId]): TodoCategoryJson = {
    TodoCategoryJson(
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
  val form: Form[TodoCategoryJsonForm] = Form(
    mapping(
        "categoryId" -> longNumber,
        "title" -> nonEmptyText,
        "body"  -> nonEmptyText,
        "state" -> shortNumber,
      )(TodoCategoryJsonForm.apply)(TodoCategoryJsonForm.unapply)
  )
}