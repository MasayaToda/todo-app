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
  def page_list() = Action async { implicit req =>
    for {
      categoryEmbed <- CategoryRepository.list()
    } yield {
      val vv = ViewValueCategoryList(
        data = categoryEmbed.map(_.v)
      )
      Ok(views.html.category.list(vv))
    }
  }

  def page_show(id:Long) = Action async { implicit req =>
    for {
      optionCategory <- CategoryRepository.get(Category.Id(id))
    } yield {
      optionCategory match {
        case None => NotFound("Category=" + id + " は存在しません。");
        case Some(categoryEmbed) => {
          // println(category)
          val vv = ViewValueCategoryShow(
            data = categoryEmbed.v
          )
          Ok(views.html.category.show(vv))
        }
      }
    }
  }
  def page_add() = Action { implicit req =>
    val vv = ViewValueCategoryAdd(
        form = categoryForm
      )
    Ok(views.html.category.add(vv))
  }
  def page_edit(id:Long) = Action async { implicit req =>
    for {
      optionCategory <- CategoryRepository.get(Category.Id(id))
    } yield {
      optionCategory match {
        case None => NotFound("Category=" + id + " は存在しません。");
        case Some(categoryEmbed) => {
          // println(category)
          val vv = ViewValueCategoryEdit(
            id = categoryEmbed.id,
            form = categoryForm.fill(
              CategoryForm(
                categoryEmbed.v.name,
                categoryEmbed.v.slug,
                categoryEmbed.v.color.code,
              )
            )
          )
          Ok(views.html.category.edit(vv))
        }
      }
    }
  }
  def page_delete(id: Long) = Action async { implicit req =>
      val categoryId = Category.Id(id)
      for {
        categoryDelete <- CategoryRepository.remove(categoryId)
        todoUpdate <- TodoRepository.removeCategoryId(categoryId)
      } yield {
        Redirect(routes.CategoryController.page_list())
              .flashing("warning" -> "Categoryを削除しました")
      }
  }
  def page_add_submit() = Action async { implicit req =>
    categoryForm.bindFromRequest.fold(
      errorform => {
        val vv = ViewValueError(
            message = errorform.toString
          )
        Future.successful(BadRequest(views.html.error(vv)))
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
          Redirect(routes.CategoryController.page_list())
                  .flashing("success" -> "Categoryを追加しました!!")
        }
      }
    )
  }
  def page_update_submit(id:Long) = Action async { implicit req =>
    categoryForm.bindFromRequest.fold(
      errorform => {
        val vv = ViewValueError(
            message = errorform.toString
          )
        Future.successful(BadRequest(views.html.error(vv)))
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
            case Some(_) => Redirect(routes.CategoryController.page_list())
                .flashing("success" -> "Categoryを更新しました!!")
          }
        }
      }
    )
  }
}