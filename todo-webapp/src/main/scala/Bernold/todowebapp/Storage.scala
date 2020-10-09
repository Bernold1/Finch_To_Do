package Bernold.todowebapp

import java.util.UUID
import scala.util.{Try, Success, Failure}

//Heavily inspired by https://hackernoon.com/todo-application-using-finch-and-twitterserver-52fa08318c87

//Using case class to model immutable data.
case class Item(id: UUID, taskName: String)

// The most idiomatic way to use an scala.Option instance is to treat it as a collection or monad and use map,flatMap, filter, or foreach:
//Monads are structures that represent sequential computations, see scala.Option documentation.
trait StorageFunctionality {
  def getAll(): List[Item]

  def add(item: Item): Item

  def get(id: UUID): Option[Item]

  def update(item: Item): Option[Item]

  def delete(item: Item): Option[Item]
}

class Storage extends StorageFunctionality{
  private val db = scala.collection.mutable.ListBuffer.empty[Item]

  override def getAll(): List[Item] = db.toList

  override def add(item: Item): Item = {
    db += item
    item
  }

  override def get(id: UUID): Option[Item] = db.find(_.id == id)

  override def update(item: Item): Option[Item] =
    for {
      //Zips this iterable collection with its indices.
      //High order function:
      n <- db.zipWithIndex.find { case (x, s) => x.id == item.id }.map(_._2)
      _ <- Try {
        db.update(n, item)
      }.toOption
      newItem <- get(item.id)
    } yield newItem

  override def delete(item: Item): Option[Item] =
    for {
      n <- db.zipWithIndex.find { case (x, s) => x.id == item.id }.map(_._2)
      x <- Try {
        db.remove(n)
      }.toOption
    } yield x
}

//singleton
object InMemoryStorage{
  private val storage: StorageFunctionality = new Storage()
  def apply(): StorageFunctionality = storage
}

