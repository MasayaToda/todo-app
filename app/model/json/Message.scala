package model.json

import play.api.libs.json._

case class MessageJson(
    message: String,
)
object  MessageJson {

  implicit val reads:  Reads[MessageJson]  = Json.reads[MessageJson]
  implicit val writes: Writes[MessageJson] = Json.writes[MessageJson]
}