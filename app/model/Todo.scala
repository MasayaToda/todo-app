package model

import play.api.libs.json._
import java.time.LocalDateTime
import play.api.data.Form
import play.api.data.Forms._
import lib.model.{Todo, Category}
import lib.model.Todo._
import lib.model.Category._
/**
  * Todo画面系の親ViewValue
  */
class ViewValueTodo(
) extends ViewValueCommon {
  import ViewValueCommon._

  override val title: String = "Todoアプリ"
  // default+ページ固有の差分ファイルだけ追加
  override val cssSrc: Seq[String] = defaultCssSrc
  override val jsSrc: Seq[String]  = "todo.js" +: defaultJsSrc
}
/**
  * Todo一覧のViewValue
  * 
  * @param data
  */
case class ViewValueTodoList(
  data: Seq[TodoCategory]
) extends ViewValueTodo
/**
  * Todo参照のViewValue
  *
  * @param data
  */
case class ViewValueTodoShow(
  data: TodoCategory 
) extends ViewValueTodo

/**
  * Todo登録のViewValue
  *
  * @param data
  */
case class ViewValueTodoAdd(
  categories: Seq[Category.ViewSelectList],
  form: Form[TodoForm] 
) extends ViewValueTodo

case class ViewValueTodoEdit(
  categories: Seq[Category.ViewSelectList],
  id: Todo.Id ,
  form: Form[TodoForm] 
) extends ViewValueTodo

case class TodoForm (
    categoryId: Category.Id,
    title: String,
    body: String,
    state: Short,
  )
case class TodoCategory(
    id: Todo.Id,
    categoryId: Category.Id,
    title: String,
    body: String,
    state: Status,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime,
    categoryName: Option[String],
    categoryColor: Option[String]
)
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