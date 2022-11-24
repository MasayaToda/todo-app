package controllers.api

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
  def api_getAll() = Action async { implicit req =>
    val todoRepo = TodoRepository.list()
    val categoryRepo = CategoryRepository.list()
    for {
      todoEmbed <- todoRepo
      categoryEmbed <- categoryRepo
    } yield{
      val json = todoEmbed.map(TodoCategoryJson.write(_,categoryEmbed))
      Ok(Json.toJson(json))
    } 
  }
  def api_add() = Action async { implicit req =>
    TodoCategoryJson.form.bindFromRequest.fold(
      errorform => {
        val json = ErrorJson.write(errorform.toString)
        Future.successful(BadRequest(Json.toJson(json)))
      },
      successform => {
        
        val todo = Todo.apply(
          Category.Id(successform.categoryId),
          successform.title,
          successform.body,
          Todo.Status.apply(successform.state.toShort)
        )
        val todoAddRepo = TodoRepository.add(todo)
        for {
          _ <- todoAddRepo
        } yield {
          val json = MessageJson.write("登録しました")
          Ok(Json.toJson(json))
        }
      }
    )
  }
  def api_get(id:Long) = Action async { implicit req =>
    val todoRepo = TodoRepository.get(Todo.Id(id))
    val categoryRepo = CategoryRepository.list()
    for {
      optionTodo <- todoRepo
      categoryEmbed <- categoryRepo
    } yield {
      optionTodo match {
        case None => {
          val json = ErrorJson.write("Todo=" + id + " は存在しません。")
          NotFound(Json.toJson(json))
        };
        case Some(todoEmbed) => {
          val json = TodoCategoryJson.write(todoEmbed,categoryEmbed)
          Ok(Json.toJson(json))
        }
      }
    }
  }
  def api_update(id:Long) = Action async { implicit req =>
    TodoCategoryJson.form.bindFromRequest.fold(
      errorform => {
        val json = ErrorJson.write(errorform.toString)
        Future.successful(BadRequest(Json.toJson(json)))
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
            case None => {
              val json = ErrorJson.write("Todo=" + id + " は存在しません。")
              NotFound(Json.toJson(json))
            }
            case Some(_) => {
              val json = MessageJson.write("Todo=" + id + " を更新しました")
              Ok(Json.toJson(json))
            }
          }
        }
      }
    )
  }
  def api_delete(id: Long) = Action async { implicit req =>
      val todoId = Todo.Id(id)
      val todoRepo = TodoRepository.remove(todoId)
      for {
        todoDelete <- todoRepo
      } yield {
        todoDelete match {
          case _ =>
            val json = MessageJson.write("Todo=" + id + " を削除しました")
            Ok(Json.toJson(json))
        }
      }
  }
}