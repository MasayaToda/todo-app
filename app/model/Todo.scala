package model

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
  form: Form[TodoFormValue] 
) extends ViewValueTodo

case class ViewValueTodoEdit(
  data: Todo ,
  form: Form[TodoFormValue] 
) extends ViewValueTodo

case class TodoForm(
    // categoryId: Long,
    title: String,
    body: String,
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