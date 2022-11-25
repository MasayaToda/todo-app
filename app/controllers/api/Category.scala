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
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import ixias.model.IdStatus.Exists
import lib.model._
import lib.persistence.default.{TodoRepository, CategoryRepository}

import model._
import model.json._
import java.lang.Exception


@Singleton
class CategoryController @Inject()(
  val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext
)extends BaseController with I18nSupport {
  val categoryForm: Form[CategoryForm] = Form (
      mapping(
        "name" -> nonEmptyText,
        "slug" -> nonEmptyText.verifying("半角英数字のみ許可されています", name => name matches """[0-9a-zA-Z]+"""),
        "color"  -> shortNumber,
      )(CategoryForm.apply)(CategoryForm.unapply)
  )
  def getAll() = Action async { implicit req =>
    for {
      categoryEmbed <- CategoryRepository.list()
    } yield {
      // val json = categoryEmbed.map(TodoCategoryJson.write(_,categoryEmbed))
      val json = MessageJson("テストデータ")
      Ok(Json.toJson(json))
    }
  }
  def add() = Action async { implicit req =>
    categoryForm.bindFromRequest.fold(
      errorform => {
        val json = ErrorJson.write(errorform.toString)
        Future.successful(BadRequest(Json.toJson(json)))
      },
      successform => {
        val category = Category.apply(
          successform.name,
          successform.slug,
          Category.Color.apply(successform.color)
        )
        for {
          _ <- CategoryRepository.add(category)
        } yield {
          val json = MessageJson("登録しました")
          Ok(Json.toJson(json))
        }
      }
    )
  }
  def get(id:Long) = Action async { implicit req =>
    for {
      optionCategory <- CategoryRepository.get(Category.Id(id))
    } yield {
      optionCategory match {
        case None => NotFound("Category=" + id + " は存在しません。");
        case Some(categoryEmbed) => {
          val json = MessageJson("更新しました")
          Ok(Json.toJson(json))
        }
      }
    }
  }
  def update(id:Long) = Action async { implicit req =>
    categoryForm.bindFromRequest.fold(
      errorform => {
        val json = ErrorJson.write(errorform.toString)
        Future.successful(BadRequest(Json.toJson(json)))
      },
      successform => {
        val categoryEmbededId = new Category(
          id = Some(Category.Id(id)),
          name = successform.name,
          slug = successform.slug,
          color = Category.Color.apply(successform.color.toShort),
        ).toEmbeddedId //EmbededId型に変換
        for {
          category <- CategoryRepository.update(categoryEmbededId)
        } yield {
          category match {
            case None => NotFound("Category=" + id + " は存在しません。")
            case Some(_) => {
              val json = MessageJson("更新しました")
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
      val todoRepo = TodoRepository.removeCategoryId(categoryId)
      for {
        categoryDelete <- categoryRepo
        todoUpdate <- todoRepo
      } yield {
        val json = MessageJson("削除しました")
        Ok(Json.toJson(json))
      }
  }
}