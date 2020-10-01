package Bernold.todo

import java.util.UUID

import cats.effect.IO
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Await
import io.finch._
import io.finch.catsEffect._
import io.finch.circe._
import io.circe.generic.auto._


object Main extends App {

  case class Message(hello: String)



  //ENDPOINTS
  //This endpoint is the standard "/" endpoint and returns a page showing OK.
  def healthcheck: Endpoint[IO, String] = get(pathEmpty) {
    Ok("OK")
  }

  //Quick "helo world" to get started...
  def hello: Endpoint[IO, String] = get("hello") {Ok("hello to you!")}

  def tasks: Endpoint[IO, List[Item]] = get("tasks") { Ok(InMemoryStorage().getAll()) }

  //creates a universally unique id for Item.
  private def postTask: Endpoint[Item] = body.as[UUID =>Item].map(f => f(UUID.randomUUID()))

  //Create a list element by element when adding a task
  def addTask: Endpoint[IO, Item] = post("task" :: postTask){ item: Item =>
    val newItem = InMemoryStorage().add(item)

    Ok(newItem)
  }

  //ATTACHING ENDPOINTS TO SERVICE
  def service: Service[Request, Response] = Bootstrap
    .serve[Text.Plain](healthcheck)
    .serve[Application.Json](hello)
    .serve[Application.Json](tasks)
  //    .serve[Application.Json](helloWorld :+: testing)
    .toService

  Await.ready(Http.server.serve(":8081", service))
}