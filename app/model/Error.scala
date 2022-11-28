package model

import play.api.libs.json._
import lib.model.Todo
import lib.model.Todo._
/**
  * エラーVueValue
  */
case class ViewValueError(
  message : String
) extends ViewValueCommon {
  import ViewValueCommon._

  override val title: String = "エラー！"
  // default+ページ固有の差分ファイルだけ追加
  override val cssSrc: Seq[String] = defaultCssSrc
  override val jsSrc: Seq[String]  = "error.js" +: defaultJsSrc
}
case class ErrorJson(
    message: String,
)
object  ErrorJson {

  implicit val reads: Reads[ErrorJson]   = Json.reads[ErrorJson]
  implicit val writes: Writes[ErrorJson] = Json.writes[ErrorJson]
  def write(message: String): ErrorJson = {
    ErrorJson(
      message           = message
    )
  }
}