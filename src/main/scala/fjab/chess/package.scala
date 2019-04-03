package fjab

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.nio.file.StandardOpenOption.{APPEND, CREATE, WRITE, TRUNCATE_EXISTING}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.Try

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

  def writeToFile(fileName: String, content: String, overwrite: Boolean = false): Unit = {
    Try{
      Files.write(Paths.get(fileName), content.toString.getBytes(StandardCharsets.UTF_8), CREATE, if(overwrite) TRUNCATE_EXISTING else APPEND, WRITE)
    }.recover{
      case t => t.printStackTrace()
    }
    ()
  }

  def `minute -> ms`(minutes: Long): Long = minutes * 60 * 1000
  def `ms -> minute`(ms: Long): Double = ms / 60.0 / 1000

  def loadSolutions(initialState: File): mutable.Set[List[(Int, Int)]] = {
    val set:mutable.Set[List[(Int, Int)]] = mutable.Set()

    val lines = Source.fromFile(initialState).getLines().toList
    for(line <- lines){
      set += deserialisePath(line)
    }
    set
  }

  /**
    * Seed file must contain lines representing the base path to be used by the program.
    * The lines must have the following format:
    *
    * List((1,1), (3,2), (5,3))
    *
    */
  def loadSeedFile(seedFile: File): List[List[(Int, Int)]] = {
    val paths = ListBuffer[List[(Int, Int)]]()

    val lines = Source.fromFile(seedFile).getLines().toList
    for(line <- lines){
      paths += deserialisePath(line).reverse
    }
    paths.toList
  }

  def deserialisePath(line: String): List[(Int, Int)] = {
    val l: ListBuffer[(Int,Int)] = ListBuffer()
    line.drop(5).replace("),", "):").split(':').foreach{ x =>
      val xtrimmed = x.trim
      l.append((xtrimmed(1).asDigit,xtrimmed(3).asDigit))
    }
    l.toList
  }

  def solutionsFilename(dimension: Int, sq: Coordinate): String = {
    s"reports/_${dimension}x$dimension/pathsFromSquare${sq._1}_${sq._2}_solutions.txt"
  }

  def composeFilename(dimension: Int, sq: Coordinate, globalDate: Long, prefix: String = "paths") = s"reports/_${dimension}x$dimension/${prefix}FromSquare${sq._1}_${sq._2}_$globalDate.txt"

  def composeFilename(dimension: Int, globalDate: Long, prefix: String) = s"reports/_${dimension}x$dimension/${prefix}_$globalDate.txt"
}
