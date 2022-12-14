package controllers

import javax.inject._
import java.awt.Desktop.Action
import java.time.LocalDateTime
import scala.concurrent._
import scala.concurrent.Future
import scala.util.{Success, Failure}
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.data.Form
import play.api.data.FormError
import play.api.data.Forms._
import play.api.data.format.{ Formats, Formatter }
import play.api.data.format.Formats._
import play.api.i18n.I18nSupport
import ixias.model.IdStatus.Exists

import lib.model._
import lib.persistence.default.{TodoRepository, CategoryRepository}
import play.api.libs.json.JsValue
import model._
import java.lang.Exception


@Singleton
class TodoController @Inject()(
  val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext
)extends BaseController with I18nSupport {
  // categoryIdのカスタムフォーマット定義
  implicit val categoryIdFormatter = new Formatter[Category.Id] {
    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Category.Id] =
      Formats.longFormat.bind(key, data).right.map(Category.Id.apply)

    def unbind(key: String, value: Category.Id): Map[String, String] = Map(key -> value.toString)
  }
  val categoryIdMapping = of[Category.Id]
  val todoForm: Form[TodoForm] = Form (
      mapping(
        "categoryId" -> categoryIdMapping, // カスタムマッピング
        "title" -> nonEmptyText,
        "body"  -> nonEmptyText,
        "state" -> shortNumber,
      )(TodoForm.apply)(TodoForm.unapply)
    )
  def page_list() = Action async { implicit req =>
    val todoRepo = TodoRepository.list()
    val categoryRepo = CategoryRepository.list()
    for {
      todoEmbed <- todoRepo
      categoryEmbed <- categoryRepo
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
    val todoRepo = TodoRepository.get(Todo.Id(id))
    val categoryRepo = CategoryRepository.list()
    for {
      optionTodo <- todoRepo
      categoryEmbed <- categoryRepo
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
    val categoryRepo = CategoryRepository.list()
    for {
      categoryEmbed <- categoryRepo
    } yield {
      val vv = ViewValueTodoAdd(
        categories = categoryEmbed.map(category => Category.ViewSelectList(category.id.toString, category.v.name)),
        form = todoForm
      )
      Ok(views.html.todo.add(vv))
    }
    
  }
  def page_edit(id:Long) = Action async { implicit req =>
    val todoRepo = TodoRepository.get(Todo.Id(id))
    val categoryRepo = CategoryRepository.list()
    for {
      optionTodo <- todoRepo
      categoryEmbed <- categoryRepo
    } yield {
      optionTodo match {
        case None => NotFound("Todo=" + id + " は存在しません。");
        case Some(todoEmbed) => {
          // println(todo)
          val vv = ViewValueTodoEdit(
            categories = categoryEmbed.map(category => Category.ViewSelectList(category.id.toString, category.v.name)),
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
      val todoRepo = TodoRepository.remove(todoId)
      for {
        todoDelete <- todoRepo
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
        val vv = ViewValueError(
            message = errorform.toString
          )
        Future.successful(BadRequest(views.html.error(vv)))
      },
      successform => {
        
        val todo = Todo.apply(
          Category.Id(successform.categoryId),
          successform.title,
          successform.body,
          Todo.Status.apply(successform.state.toShort)
        )
        for {
          todoAddRepo <- TodoRepository.add(todo)
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
        val vv = ViewValueError(
            message = errorform.toString
          )
        Future.successful(BadRequest(views.html.error(vv)))
      },
      successform => {
        val todoEmbededId = new Todo(
          id = Some(Todo.Id(id)),
          categoryId = Category.Id(successform.categoryId),
          title = successform.title,
          body = successform.body,
          state = Todo.Status.apply(successform.state.toShort),
          ).toEmbeddedId //EmbededId型に変換
        val todoUpdateRepo = TodoRepository.update(todoEmbededId)
        for {
          todo <- todoUpdateRepo
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