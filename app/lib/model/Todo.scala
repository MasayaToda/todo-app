package lib.model

import ixias.model._
import ixias.util.EnumStatus
import java.time.LocalDateTime

import Todo._
case class Todo (
  id:         Option[Id],
  categoryId: Long,
  title:      String,
  body:       String,
  state:       Status,
  updatedAt:  LocalDateTime = NOW,
  createdAt:  LocalDateTime = NOW
)extends EntityModel[Id]

object Todo {
  
  val  Id = the[Identity[Id]]
  type Id = Long @@ Todo
  type WithNoId = Entity.WithNoId [Id, Todo]
  type EmbeddedId = Entity.EmbeddedId[Id, Todo]
  // ステータス定義
  //~~~~~~~~~~~~~~~~~
  sealed abstract class Status(val code: Short, val name: String) extends EnumStatus
  object Status extends EnumStatus.Of[Status] {
    case object IS_INACTIVE extends Status(code = 0,   name = "TODO(着手前)")
    case object IS_DOING   extends Status(code = 1, name = "進行中")
    case object IS_DONE   extends Status(code = 2, name = "完了")
  }

  def build(categoryId: Long,title: String, body: String, state: Status): Todo#WithNoId = {
    new Entity.WithNoId(
      new Todo(
        id    = None,
        categoryId = categoryId,
        title = title,
        body  = body,
        state  = state
      )
    )
  }
}