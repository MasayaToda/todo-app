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

import lib.model._
import lib.persistence.default.{TodoRepository, CategoryRepository}

import model._
import java.lang.Exception


@Singleton
class TodoController @Inject()(
  val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext
)extends BaseController with I18nSupport {
  val todoForm: Form[TodoForm] = Form (
      mapping(
        "categoryId" -> longNumber,
        "title" -> nonEmptyText,
        "body"  -> nonEmptyText,
        "state" -> shortNumber,
      )(TodoForm.apply)(TodoForm.unapply)
    )
  def page_list() = Action async { implicit req =>
    for {
      todoEmbed <- TodoRepository.index()
      categoryEmbed <- CategoryRepository.index()
    } yield {
      val vv = ViewValueTodoList(
        data = todoEmbed.map(todo=>{
          TodoCategory(
            todo.id,
            todo.v.categoryId,
            todo.v.title,
            todo.v.body,
            todo.v.state,
            todo.v.createdAt,
            todo.v.updatedAt,
            categoryEmbed.find(_.id == todo.v.categoryId).map(_.v.name),
            categoryEmbed.find(_.id == todo.v.categoryId).map(_.v.color.css),
          )
        })
      )
      Ok(views.html.todo.list(vv))
    }
  }

  def page_show(id:Long) = Action async { implicit req =>
    for {
      optionTodo <- TodoRepository.get(Todo.Id(id))
      categoryEmbed <- CategoryRepository.index()
    } yield {
      optionTodo match {
        case None => NotFound("Todo=" + id + " は存在しません。");
        case Some(todoEmbed) => {
          // println(todo)
          val vv = ViewValueTodoShow(
            data = TodoCategory(
              todoEmbed.id,
              todoEmbed.v.categoryId,
              todoEmbed.v.title,
              todoEmbed.v.body,
              todoEmbed.v.state,
              todoEmbed.v.createdAt,
              todoEmbed.v.updatedAt,
              categoryEmbed.find(_.id == todoEmbed.v.categoryId).map(_.v.name),
              categoryEmbed.find(_.id == todoEmbed.v.categoryId).map(_.v.color.css),
            )
          )
          Ok(views.html.todo.show(vv))
        }
      }
    }
  }
  def page_add() = Action async { implicit req =>
    for {
      categoryEmbed <- CategoryRepository.index()
    } yield {
      val vv = ViewValueTodoAdd(
        categories = categoryEmbed.map(category => (category.id.toString -> category.v.name)),
        form = todoForm
      )
      Ok(views.html.todo.add(vv))
    }
    
  }
  def page_edit(id:Long) = Action async { implicit req =>
    for {
      optionTodo <- TodoRepository.get(Todo.Id(id))
      categoryEmbed <- CategoryRepository.index()
    } yield {
      optionTodo match {
        case None => NotFound("Todo=" + id + " は存在しません。");
        case Some(todoEmbed) => {
          // println(todo)
          val vv = ViewValueTodoEdit(
            categories = categoryEmbed.map(category => (category.id.toString -> category.v.name)),
            id = todoEmbed.id,
            form = todoForm.fill(
              TodoForm(
                todoEmbed.v.categoryId,
                todoEmbed.v.title,
                todoEmbed.v.body,
                todoEmbed.v.state.code,
              )
            )
          )
          Ok(views.html.todo.edit(vv))
        }
      }
    }
  }
  def page_delete(id: Long) = Action async { implicit req =>
      val todoId = Todo.Id(id)
      for {
        todoDelete <- TodoRepository.remove(todoId)
      } yield {
        todoDelete match {
          case _ =>
            Redirect(routes.TodoController.page_list())
              .flashing("warning" -> "Todoを削除しました")
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
          Category.Id(successform.categoryId),
          successform.title,
          successform.body,
          Todo.Status.apply(successform.state.toShort)
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
          categoryId = Category.Id(successform.categoryId),
          title = successform.title,
          body = successform.body,
          state = Todo.Status.apply(successform.state.toShort),
        ).toEmbeddedId //EmbededId型に変換
        for {
          todo <- TodoRepository.update(todoEmbededId)
        } yield {
          todo match {
            case None => NotFound("Todo=" + id + " は存在しません。")
            case Some(_) => Redirect(routes.TodoController.page_list())
                .flashing("success" -> "Todoを更新しました!!")
          }
        }
      }
    )
  }
}