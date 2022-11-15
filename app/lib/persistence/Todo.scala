package lib.persistence

import scala.concurrent.Future
import slick.jdbc.JdbcProfile
import ixias.persistence.SlickRepository

import lib.model.Todo  // ixias.modelで定義したものをimport

case class TodoRepository[P <: JdbcProfile]()(implicit val driver: P)
  extends SlickRepository[Todo.Id, Todo, P]
  with db.SlickResourceProvider[P] {

  import api._
  def index(): Future[Seq[Todo]] =
    RunDBAction(TodoTable, "slave") { _
      .result
  }
  def get(id: Id): Future[Option[EntityEmbeddedId]] =
    RunDBAction(TodoTable, "slave") { _
      .filter(_.id === id)
      .result.headOption
  }

  /**
    * Add User Data
   */
  def add(entity: EntityWithNoId): Future[Id] =
    RunDBAction(TodoTable) { slick =>
      slick returning slick.map(_.id) += entity.v
    }
  def update(entity: EntityEmbeddedId): Future[Option[EntityEmbeddedId]] =
    RunDBAction(TodoTable) { slick =>
      val row = slick.filter(_.id === entity.id)
      for {
        old <- row.result.headOption
        _   <- old match {
          case None    => DBIO.successful(0)
          case Some(_) => row.update(entity.v)
        }
      } yield old
    }
  def remove(id: Id): Future[Option[EntityEmbeddedId]] =
    RunDBAction(TodoTable) { slick =>
      val row = slick.filter(_.id === id)
      for {
        old <- row.result.headOption
        _   <- old match {
          case None    => DBIO.successful(0)
          case Some(_) => row.delete
        }
      } yield old
    }
}