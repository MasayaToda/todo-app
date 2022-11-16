package controllers

import javax.inject._
import java.awt.Desktop.Action
import java.time.LocalDateTime
import scala.concurrent._
import scala.concurrent.Future
import scala.util.{Success, Failure}
import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import ixias.model.IdStatus.Exists

import lib.model.Todo
import lib.persistence.default.TodoRepository

import model._
import java.lang.Exception

@Singleton
class TodoController @Inject()(
  val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext
)extends BaseController with I18nSupport {
  
  def page_list() = Action async { implicit req =>
    for {
      todo <- TodoRepository.index()
    } yield {
      val vv = ViewValueTodoList(
        data = todo
      )
      Ok(views.html.todo.list(vv))
    }
  }

  def page_show(id:Long) = Action async { implicit req =>
    for {
      optionTodo <- TodoRepository.get(Todo.Id(id))
    } yield {
      optionTodo match {
        case None => NotFound("Todo=" + id + " は存在しません。");
        case Some(todoEmbed) => {
          // println(todo)
          val vv = ViewValueTodoShow(
            data = todoEmbed.v
          )
          Ok(views.html.todo.show(vv))
        }
      }
    }
  }
  def page_add() = Action { implicit req =>
    val todoForm = Form (
      mapping(
        "categoryId" -> longNumber,
        "title" -> nonEmptyText,
        "body"  -> nonEmptyText,
      )(Todo.FormValue.apply)(Todo.FormValue.unapply)
    )
    val vv = ViewValueTodoAdd(
      form = todoForm
    )
    Ok(views.html.todo.add(vv))
  }
  def add() = Action { implicit req =>
    val todoForm = Form (
      mapping(
        "categoryId" -> longNumber,
        "title" -> nonEmptyText,
        "body"  -> nonEmptyText,
      )(Todo.FormValue.apply)(Todo.FormValue.unapply)
    )
    val vv = ViewValueTodoAdd(
      form = todoForm
    )
    Ok(views.html.todo.add(vv))
  }
}