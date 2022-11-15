package controllers

import javax.inject._
import java.awt.Desktop.Action
import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Success, Failure}
import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._

import lib.model.Todo
import lib.persistence.default.TodoRepository

import model._

@Singleton
class TodoController @Inject()(
  val controllerComponents: ControllerComponents
) extends BaseController {
  def index() = Action async { implicit req =>
    for {
      todo <- TodoRepository.index()
    } yield {
      val vv = ViewValueTodo(
        data = todo
      )
      Ok(views.html.todo.list(vv)(views.html.todo.add(vv)))
    }
  }
  def show(id:Long) = Action { implicit req =>
    val vv = Todo(Some(Todo.Id(1)),1,"タイトル1","本文1",Todo.Status(0), LocalDateTime.now,LocalDateTime.now)
    Ok(views.html.todo.show(vv))
  }
}