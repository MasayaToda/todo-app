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
import lib.model.Todo._
import lib.persistence.default.TodoRepository

import model._
import java.lang.Exception


@Singleton
class TodoController @Inject()(
  val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext
)extends BaseController with I18nSupport {
  val todoForm: Form[Todo.FormValue] = Form (
      mapping(
        // "categoryId" -> longNumber,
        "title" -> nonEmptyText,
        "body"  -> nonEmptyText,
      )(Todo.FormValue.apply)(Todo.FormValue.unapply)
    )
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
    val vv = ViewValueTodoAdd(
      form = todoForm
    )
    Ok(views.html.todo.add(vv))
  }
  def page_edit(id:Long) = Action async { implicit req =>
    for {
      optionTodo <- TodoRepository.get(Todo.Id(id))
    } yield {
      optionTodo match {
        case None => NotFound("Todo=" + id + " は存在しません。");
        case Some(todoEmbed) => {
          // println(todo)
          val vv = ViewValueTodoEdit(
            data = todoEmbed.v,
            form = todoForm.fill(
              Todo.FormValue(
                todoEmbed.v.title,
                todoEmbed.v.body,
              )
            )
          )
          Ok(views.html.todo.edit(vv))
        }
      }
    }
  }
  def page_add_submit() = Action async { implicit req =>
    todoForm.bindFromRequest.fold(
      errorform => {
        // Future[play.api.mvc.Result]に合わせないとコンパイル通らないので、一旦失敗したら一覧へ戻す
        for {
          todo <- TodoRepository.index()
        } yield {
          val vv = ViewValueError(
            message = errorform.toString
          )
          BadRequest(views.html.error(vv))
        }
      },
      successform => {
        val todo = Todo.apply(
          1, // categoryID 一旦固定値
          successform.title,
          successform.body,
          Todo.Status.IS_INACTIVE
        )
        for {
          _ <- TodoRepository.add(todo)
        } yield {
          Redirect(routes.TodoController.page_list())
                  .flashing("success" -> "Todoを追加しました!!")
        }
      }
    )
  }
  def page_update_submit(id:Long) = Action async { implicit req =>
    todoForm.bindFromRequest.fold(
      errorform => {
        // Future[play.api.mvc.Result]に合わせないとコンパイル通らないので、一旦失敗したら一覧へ戻す
        for {
          todo <- TodoRepository.index()
        } yield {
          val vv = ViewValueError(
            message = errorform.toString
          )
          BadRequest(views.html.error(vv))
        }
      },
      successform => {
        val todoEmbededId = new Todo(
          id = Some(Todo.Id(id)),
          categoryId = 1, // categoryID 一旦固定値
          title = successform.title,
          body = successform.body,
          state = Todo.Status.IS_INACTIVE,
        ).toEmbeddedId //EmbededId型に変換
        for {
          _ <- TodoRepository.update(todoEmbededId)
        } yield {
          Redirect(routes.TodoController.page_list())
                  .flashing("success" -> "Todoを更新しました!!")
        }
      }
    )
  }
}