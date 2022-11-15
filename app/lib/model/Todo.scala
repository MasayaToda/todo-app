package lib.model

import ixias.model._
import java.time.LocalDateTime

import Todo._
case class Todo (
  id:         Option[Id],
  categoryId: Int,
  title:      String,
  body:       String,
  updatedAt:  LocalDateTime = NOW,
  createdAt:  LocalDateTime = NOW
)extends EntityModel[Id]

object Todo {
  
  val  Id = the[Identity[Id]]
  type Id = Long @@ Todo
  type WithNoId = Entity.WithNoId [Id, Todo]
  type EmbeddedId = Entity.EmbeddedId[Id, Todo]

  def build(categoryId: Int,title: String, body: String): Todo#WithNoId = {
    new Entity.WithNoId(
      new Todo(
        id    = None,
        categoryId = categoryId,
        title = title,
        body  = body
      )
    )
  }
}