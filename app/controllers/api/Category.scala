package controllers.api

import javax.inject._
import java.awt.Desktop.Action
import java.time.LocalDateTime
import scala.concurrent._
import scala.concurrent.Future
import scala.util.{Success, Failure}
import play.api._
import play.api.mvc._
import play.mvc.BodyParser
import play.api.libs.json._
import play.api.data.Form
import play.api.data.FormError
import play.api.data.Forms._
import play.api.data.format.{ Formats, Formatter }
import play.api.data.format.Formats._
import play.api.i18n.I18nSupport
import ixias.model.IdStatus.Exists
import cats.data._
import cats.implicits._

import lib.model._
import lib.persistence.default.CategoryRepository
import lib.persistence.default.CategoryRepository._
import play.api.libs.json.JsValue
import model._
import model.json._
import java.lang.Exception

@Singleton
class CategoryController @Inject()(
  val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext
)extends BaseController with I18nSupport {
  def getAll() = Action async { implicit req =>
    for {
      categoryEmbed <- CategoryRepository.list()
    } yield {
      val json = categoryEmbed.map(CategoryJsonResponseBody.write(_))
      Ok(Json.toJson(json))
    }
  }
  def getColorAll() = Action { implicit req =>
    val colors = Category.Color.values
    val json = colors.map(ColorResponseBody.write(_))
    Ok(Json.toJson(json))
  }
  def add() = Action(parse.json) async { implicit req =>
    req.body.validate[CategoryJsonRequestBody].fold(
      errors => {
        val json = ErrorJson.write(errors.toString)
        Future.successful(BadRequest(Json.toJson(json)))
      },
      categoryJson => {
        val category = Category.apply(
          categoryJson.name,
          categoryJson.slug,
          categoryJson.color
        )
        for {
          categoryAdd <- CategoryRepository.add(category)
        } yield {
          val json = MessageJson("登録しました")
          Ok(Json.toJson(json))
        }
      }
    )
  }
  def get(id:Long) = Action async { implicit req =>
    (for{
      category <- OptionT( CategoryRepository.get(Category.Id(id)))
    }yield {
      val json = CategoryJsonResponseBody.write(category)
      Ok(Json.toJson(json))
    })
    .toRight(NotFound(Json.toJson(ErrorJson.write("Category=" + id + " は存在しません。"))))
      .merge
  }
  def update(id:Long) = Action(parse.json) async { implicit req =>
    req.body.validate[CategoryJsonRequestBody].fold(
      errors => {
        val json = ErrorJson.write(errors.toString)
        Future.successful(BadRequest(Json.toJson(json)))
      },
      categoryJson => {
        val categoryEmbededId = new Category(
          id = Some(Category.Id(id)),
          name = categoryJson.name,
          slug = categoryJson.slug,
          color = categoryJson.color,
        ).toEmbeddedId //EmbededId型に変換
        val categoryUpdateRepo = CategoryRepository.update(categoryEmbededId)
        for {
          categoryUpdate <- categoryUpdateRepo
        } yield {
          categoryUpdate match {
            case None => {
              val json = ErrorJson.write("Category=" + id + " は存在しません。")
              NotFound(Json.toJson(json))
            }
            case Some(_) => {
              val json = MessageJson("Category=" + id + " を更新しました")
              Ok(Json.toJson(json))
            }
          }
        }
      }
    )
  }
  def delete(id: Long) = Action async { implicit req =>
    val categoryId = Category.Id(id)
    val categoryRepo = CategoryRepository.remove(categoryId)
    for {
      categoryDelete <- categoryRepo
    } yield {
      categoryDelete match {
        case categoryDelete =>
          val json = MessageJson("Category=" + id + " を削除しました")
          Ok(Json.toJson(json))
      }
    }
  }
}