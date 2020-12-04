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

  val postedToDo: Endpoint[IO , Item] = jsonBody[UUID =>Item].map(_(UUID.randomUUID()))

  //Create todos Endpoint
  def addToDo: Endpoint[IO, Item] = post("todo" :: postedToDo){todo: Item =>
    val newToDo = InMemoryStorage().add(todo)
    Ok(newToDo)
  }

  //Get all todos endpoint
  def allToDos: Endpoint[IO, List[Item]] = get("todos") { Ok(InMemoryStorage().getAll()) }

  //Delete endpoint (path[UUID] is a URL param):
  def removeToDo: Endpoint[IO, Item] = delete("todo" :: path[UUID]) {id: UUID =>
    val result = for {
      item <- InMemoryStorage().get(id)
      deleted <- InMemoryStorage().delete(item)
    } yield deleted

    result.fold[Output[Item]](NotFound(new Exception))(Ok)

  }

  //For editing
  def editToDo: Endpoint[IO, Item] = patch("todo"::path[UUID]::jsonBody[Item]){(id: UUID, updateToDo: Item) =>
    var result = for {
      edited <- InMemoryStorage().update(id,updateToDo) //hmm?
    } yield edited

      result.fold[Output[Item]](NotFound(new Exception))(Ok)
  }

  def getCompleted: Endpoint[IO, List[Item]] = get("todo/progress/inactive") { Ok(InMemoryStorage().getCompleted())}
  def getInProgress: Endpoint[IO, List[Item]] = get("todo/progress/active"){Ok(InMemoryStorage().getInProgress()) }

  def service: Service[Request, Response] = Bootstrap
    .serve[Text.Plain](healthcheck)
    .serve[Application.Json](helloWorld :+: hello)
    .serve[Application.Json](randomMultiplier)
    .serve[Application.Json](allToDos)
    .serve[Application.Json](addToDo)
    .serve[Application.Json](removeToDo)
    .serve[Application.Json](editToDo)
    .serve[Application.Json](getCompleted)
    .serve[Application.Json](getInProgress)
    .toService

  Await.ready(Http.server.serve(":8081", service))
}