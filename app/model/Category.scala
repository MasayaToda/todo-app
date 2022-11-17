package model

import java.time.LocalDateTime
import play.api.data.Form
import play.api.data.Forms._
import lib.model.Category
import lib.model.Category._
/**
  * Category画面系の親ViewValue
  */
class ViewValueCategory(
) extends ViewValueCommon {
  import ViewValueCommon._

  override val title: String = "Categoryアプリ"
  // default+ページ固有の差分ファイルだけ追加
  override val cssSrc: Seq[String] = defaultCssSrc
  override val jsSrc: Seq[String]  = "todo.js" +: defaultJsSrc
}
/**
  * Category一覧のViewValue
  * 
  * @param data
  */
case class ViewValueCategoryList(
  data: Seq[Category]
) extends ViewValueCategory
/**
  * Category参照のViewValue
  *
  * @param data
  */
case class ViewValueCategoryShow(
  data: Category
) extends ViewValueCategory

/**
  * Category登録のViewValue
  *
  * @param data
  */
case class ViewValueCategoryAdd(
  form: Form[CategoryForm] 
) extends ViewValueCategory

case class ViewValueCategoryEdit(
  id: Long ,
  form: Form[CategoryForm] 
) extends ViewValueCategory

case class CategoryForm (
    name: String,
    slug: String,
    color: Short,
  )