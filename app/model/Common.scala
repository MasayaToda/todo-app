/**
 *
 * to do sample project
 *
 */
package model

/**
 * 全ページに最低限必要な要素をtrait化。親のhtmlは引数でこのtraitを受け取ることで
 * このtraitを継承したclassも引数に投げることができる
 */
trait ViewValueCommon {
  val title:  String      // pageのタイトル
  val cssSrc: Seq[String] // pageで読み込むcssのファイル名
  val jsSrc:  Seq[String] // pageで読み込むjavascriptのファイル名
}

object ViewValueCommon {
  val defaultCssSrc: Seq[String] = Seq("bootstrap.min.css")
  val defaultJsSrc: Seq[String]  = Seq("bootstrap.min.js", "jquery.3.6.1.min.js")
}