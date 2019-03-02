package fjab.chess.apps

import java.util.Date
import java.util.concurrent.Executors

import fjab.chess.{Coordinate, KnightTourProblem, octant, printPath}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.{DAYS, FiniteDuration}
import scala.concurrent.{Await, ExecutionContext, Future}

object MainApp extends App {

  val summary: mutable.Map[String, mutable.Map[String,ListBuffer[Long]]] = mutable.Map()
  val threadPool = Executors.newFixedThreadPool(8)
  implicit val ec = ExecutionContext.fromExecutor(threadPool)


  def chessboard(dimension: Int) = {
      println(s"=============== dimension: $dimension*$dimension ==================")

      val app = WarnsdorffKnightTourApp(dimension, dimension)
      val map = mutable.Map[String,ListBuffer[Long]]()
      summary += (s"$dimension" -> map)
      val squares = octant(dimension)
      println(s"number of elements to calculate: ${squares.length}")

      Await.result(Future.sequence(squares.map {
        coordinate =>
          Future {
            val start = System.currentTimeMillis()
            val from = (coordinate._1, coordinate._2)
            val result = app.findPath(List(List(from)))
            val time = -start + System.currentTimeMillis()
            this.synchronized {
              val threadName = Thread.currentThread().getName
              println(threadName)
              println(result)
              println(s"$time ms")
              printPath(result)
              if(map.get(threadName).isDefined) map(threadName) += time
              else map += (threadName -> ListBuffer(time))
              ()
            }
          }
      }), FiniteDuration(100, DAYS))
  }

  def square(dimension: Int, sq: Coordinate) = {
    println(s"=============== dimension: $dimension*$dimension ==================")
    println(s"=============== square: ${sq.toString()} ==================")

    val app = WarnsdorffKnightTourApp(dimension, dimension)
    val map = mutable.Map[String,ListBuffer[Long]]()
    summary += (s"${sq.toString()}" -> map)
    val neighboursList: Seq[Coordinate] = app.neighbours(sq)
    println(s"number of elements to calculate: ${neighboursList.length}")

    Await.result(Future.sequence(neighboursList.map {
      n =>
        Future {
          val start = System.currentTimeMillis()
          val result = app.findPath(List(List(n, sq)))
          val time = -start + System.currentTimeMillis()
          this.synchronized {
            val threadName = Thread.currentThread().getName
            println(threadName)
            println(result)
            println(s"$time ms")
            printPath(result)
            if(map.get(threadName).isDefined) map(threadName) += time
            else map += (threadName -> ListBuffer(time))
            ()
          }
        }
    }), FiniteDuration(100, DAYS))
  }

  def allPathsStartingAtSquare(dimension: Int, sq: Coordinate, numberOfSolutions: Int, implementationStrategy: KnightTourProblem) = {
    println(s"=============== dimension: $dimension*$dimension ==================")
    println(s"=============== square: ${sq.toString()} ==================")

    val start = System.currentTimeMillis()
    val result = implementationStrategy.findPaths(List(List(sq)), numberOfSolutions)
    val time = -start + System.currentTimeMillis()

    val threadName = Thread.currentThread().getName
    println(threadName)
    result foreach println
    println(s"$time ms")
    //result foreach printPath
    ()


  }

  println(s"program started at ${new Date()}")
  val globalStart = System.currentTimeMillis()

  //(7 to 7) foreach (chessboard(_))

  val dim = 8
  List((1,1)).foreach(allPathsStartingAtSquare(dim, _, 1000, WarnsdorffKnightTourApp(dim, dim)))
  List((1,1)).foreach(allPathsStartingAtSquare(dim, _, 2, KnightTourInFiniteBoardApp(dim, dim)))
  //octant(dim).foreach(square(dim, _))
  //List((1,2),(1,4),(2,3),(3,4),(4,4)).foreach(square(dim, _))
  //-> List((4,4)).foreach(square(dim, _))
  //xxx List((1,2)).foreach(square(dim, _))
  //xxx List((3,4)).foreach(square(dim, _))

  println(summary)
  println(s"Global duration: ${-globalStart + System.currentTimeMillis()}")
  threadPool.shutdownNow()

}
