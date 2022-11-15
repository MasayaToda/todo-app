package model

import lib.model.Todo
import lib.model.Todo._

// Topページのviewvalue
case class ViewValueTodo(
  title:  String = "Todoアプリ",
  cssSrc: Seq[String] = Seq("bootstrap.min.css"),
  jsSrc:  Seq[String] = Seq("jquery.3.6.1.min.js","bootstrap.min.js","todo.js"),
  data:   Seq[Todo]
) extends ViewValueCommon