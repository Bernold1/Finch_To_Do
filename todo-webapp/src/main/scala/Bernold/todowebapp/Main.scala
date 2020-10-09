package Bernold.todowebapp

import java.util.UUID

import cats.effect.IO
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Await
import io.finch._
import io.finch.catsEffect._
import io.finch.circe._
import io.circe.generic.auto._

//Some to do functionality found from finch documentation best practices:
// https://finagle.github.io/finch/best-practices.html

object Main extends App {

  case class Message(hello: String)

  def healthcheck: Endpoint[IO, String] = get(pathEmpty) {
    Ok("OK")
  }

  def helloWorld: Endpoint[IO, Message] = get("hello") {
    Ok(Message("World"))
  }

  def hello: Endpoint[IO, Message] = get("hello" :: path[String]) { s: String =>
    Ok(Message(s))
  }

  def mathstuff(a:Double, b:Double) = {
    a*b
  }

  def randomMultiplier: Endpoint[IO, Double] = get("multiply"){Ok(mathstuff(scala.math.random, scala.math.random))}

  def allToDos: Endpoint[IO, List[Item]] = get("todos") { Ok(InMemoryStorage().getAll()) }

  val postedToDo: Endpoint[IO , Item] = jsonBody[UUID =>Item].map(_(UUID.randomUUID()))

  def addToDo: Endpoint[IO, Item] = post("todo" :: postedToDo){todo: Item =>
    val newToDo = InMemoryStorage().add(todo)
    Ok(newToDo)
  }

  //For editing
  val patchedToDo: Endpoint[IO, Item => Item] = jsonBody[Item=>Item]

  def service: Service[Request, Response] = Bootstrap
    .serve[Text.Plain](healthcheck)
    .serve[Application.Json](helloWorld :+: hello)
    .serve[Application.Json](randomMultiplier)
    .serve[Application.Json](allToDos)
    .serve[Application.Json](addToDo)
    .toService

  Await.ready(Http.server.serve(":8081", service))
}