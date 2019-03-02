package fjab

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.nio.file.StandardOpenOption.{APPEND, CREATE, WRITE}

import scala.collection.mutable.ListBuffer

package object chess {

  class MultiThread extends scala.annotation.StaticAnnotation
  type Coordinate = (Int, Int)

  implicit class RichTuple2(coordinate: Coordinate) {
    def +(other: Coordinate): Coordinate = (coordinate._1 + other._1, coordinate._2 + other._2)
    def -(other: Coordinate): Coordinate = (coordinate._1 - other._1, coordinate._2 - other._2)
  }

  def quadrant(dim: Int): Seq[Coordinate] = {

    val limit = if (dim % 2 == 0) dim / 2 else dim / 2 + 1
    val list: ListBuffer[Coordinate] = ListBuffer()

    for (i <- 1 until limit) {
      for (j <- i to dim - i) {
        list += ((i, j))
      }
    }

    list += ((limit, limit))
    list
  }

  def octant(dim: Int): Seq[Coordinate] = {

    val limit = if (dim % 2 == 0) dim / 2 else dim / 2 + 1
    val list: ListBuffer[Coordinate] = ListBuffer()

    for (i <- 1 to limit) {
      for (j <- i to limit) {
        list += ((i, j))
      }
    }

    list
  }

  def printPath(path: Seq[Coordinate]): String = {
    var result = "\n"
    val sideLength = math.sqrt(path.size).toInt
    for (i <- 1 to sideLength) {
      val row = new ListBuffer[Int]()
      for (j <- 1 to sideLength) {
        row.append(path.indexOf((i, j)) + 1)
      }
      result += row.map("%2d".format(_)).mkString("  ") + "\n"
    }
    result += "\n"
    result
  }

  def writeToFile(fileName: String, content: String): Unit = {
    try {
      Files.write(Paths.get(fileName), content.toString.getBytes(StandardCharsets.UTF_8), CREATE, APPEND, WRITE)
    }
    catch{
      case e: Exception => e.printStackTrace()
    }
    ()
  }
}
