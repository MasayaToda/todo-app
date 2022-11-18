package lib.persistence

import scala.concurrent.Future
import slick.jdbc.JdbcProfile
import ixias.persistence.SlickRepository

import lib.model.Category  // ixias.modelで定義したものをimport

case class CategoryRepository[P <: JdbcProfile]()(implicit val driver: P)
  extends SlickRepository[Category.Id, Category, P]
  with db.SlickResourceProvider[P] {

  import api._
  def list(): Future[Seq[EntityEmbeddedId]] =
    RunDBAction(CategoryTable, "slave") { _
      .result
  }
  def get(id: Id): Future[Option[EntityEmbeddedId]] =
    RunDBAction(CategoryTable, "slave") { _
      .filter(_.id === id)
      .result.headOption
  }

  /**
    * Add User Data
   */
  def add(entity: EntityWithNoId): Future[Id] =
    RunDBAction(CategoryTable) { slick =>
      slick returning slick.map(_.id) += entity.v
    }
  def update(entity: EntityEmbeddedId): Future[Option[EntityEmbeddedId]] =
    RunDBAction(CategoryTable) { slick =>
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
    RunDBAction(CategoryTable) { slick =>
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