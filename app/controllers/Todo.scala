package controllers

import javax.inject._
import java.awt.Desktop.Action
import java.time.LocalDateTime
import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._

import lib.model.Todo
import lib.model.Todo._
import lib.persistence._

import model._

@Singleton
class TodoController @Inject()(
  val controllerComponents: ControllerComponents
) extends BaseController {
  def index() = Action { implicit req =>
    val vv = ViewValueTodo(
      data = Seq(Todo(Some(Id(1)),1,"タイトル","本文",LocalDateTime.now,LocalDateTime.now))
    )
    Ok(views.html.todo.list(vv)(views.html.todo.add(vv)))
  }
  def show(id:Long) = Action { implicit req =>
    val testTodo = Todo(Some(Id(id)),1,"タイトル","本文",LocalDateTime.now,LocalDateTime.now)
    Ok(views.html.todo.show(testTodo))
  }
}