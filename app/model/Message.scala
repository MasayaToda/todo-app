package model

import play.api.libs.json._
import lib.model.Todo
import lib.model.Todo._

case class MessageJson(
    message: String,
)
object  MessageJson {

  implicit val reads:  Reads[MessageJson]  = Json.reads[MessageJson]
  implicit val writes: Writes[MessageJson] = Json.writes[MessageJson]

  def write(message: String): MessageJson = {
    MessageJson(
      message = message
    )
  }
}