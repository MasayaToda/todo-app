package model

import java.time.LocalDate
import play.api.data.Form
import play.api.data.Forms._
import lib.model.Todo
import lib.model.Todo._
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
  data: Seq[Todo]
) extends ViewValueTodo
/**
  * Todo参照のViewValue
  *
  * @param data
  */
case class ViewValueTodoShow(
  data: Todo 
) extends ViewValueTodo

/**
  * Todo登録のViewValue
  *
  * @param data
  */
case class ViewValueTodoAdd(
  form: Form[FormValue] 
) extends ViewValueTodo

case class TodoForm(
    // categoryId: Long,
    title: String,
    body: String,
)