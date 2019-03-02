package fjab.chess.apps

import java.util.Date
import java.util.concurrent.Executors

import fjab.chess.{Coordinate, InfiniteChessBoard, KnightTourProblem, MultiThread, octant, printPath, writeToFile}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.{DAYS, FiniteDuration}
import scala.concurrent.{Await, ExecutionContext, Future}

object MainApp extends App {

  case class ThreadName(x: String) extends AnyVal
  case class ComputationTime(x: Long) extends AnyVal


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

  /**
    * Each thread calculates a path via a different neighbour of the given square.
    * The current implementation does not interrupt running threads or scheduled tasks when a path is found.
    * As a consequence, this program may return as many paths as neighbours
    */
  @MultiThread
  def anyPathStartingAtSquare(sq: Coordinate, app: KnightTourProblem, threads: Int) = {

    println(s"program started at ${new Date()}")
    val globalStart = System.currentTimeMillis()

    val (x, y) = app.boardDimension
    println(s"=============== dimension: $x*$y ==================")
    println(s"=============== square: ${sq.toString()} ==================")


    val programExecutionSummary: mutable.Map[ThreadName, ListBuffer[ComputationTime]] = mutable.Map()
    val threadPool = Executors.newFixedThreadPool(threads)
    implicit val ec = ExecutionContext.fromExecutor(threadPool)

    val fileName = s"pathsFromSquare${sq._1}_${sq._2}.txt"


    val neighboursList: Seq[Coordinate] = app.neighbours(sq)
    println(s"number of elements to calculate: ${neighboursList.length}")

    Await.result(Future.sequence(neighboursList.map {
      n =>
        Future {
          val start = System.currentTimeMillis()
          val result = app.findPath(List(List(n, sq)))
          val time = -start + System.currentTimeMillis()

          this.synchronized {
            val threadName = ThreadName(Thread.currentThread().getName)
            println(result)
            println(s"$time ms")
            if(programExecutionSummary.get(threadName).isDefined) programExecutionSummary(threadName) += ComputationTime(time)
            else programExecutionSummary += threadName -> ListBuffer(ComputationTime(time))

            val content = result.toString() + "\n" + s"$time ms" + "\n" + printPath(result) + "\n"
            writeToFile(fileName, content)
          }
        }
    }), FiniteDuration(100, DAYS))

    writeToFile(fileName, programExecutionSummary.toString())

    println(s"Global duration: ${-globalStart + System.currentTimeMillis()} ms")
    threadPool.shutdown()
  }

  @MultiThread
  def anyPathStartingAtSquare(dimension: Int, sq: Coordinate, threads: Int) = {

    println(s"program started at ${new Date()}")
    val globalStart = System.currentTimeMillis()

    println(s"=============== dimension: $dimension*$dimension ==================")
    println(s"=============== square: ${sq.toString()} ==================")


    val programExecutionSummary: mutable.Map[ThreadName, ListBuffer[ComputationTime]] = mutable.Map()
    val threadPool = Executors.newFixedThreadPool(threads)
    implicit val ec = ExecutionContext.fromExecutor(threadPool)

    val fileName = s"pathsFromSquare${sq._1}_${sq._2}.txt"

    val moves = List((2,1), (1,2), (-1,2), (-2,1), (-2,-1), (-1,-2), (1,-2), (2,-1)).permutations.toStream
    println(s"number of elements to calculate: ${moves.length}")

    Await.result(Future.sequence(moves.map {
      moves =>
        Future {
          val app = KnightTourWithCustomMovesApp(dimension, dimension, moves)
          val start = System.currentTimeMillis()
          val result = app.findPath(List(List(sq)))
          val time = -start + System.currentTimeMillis()

          this.synchronized {
            val threadName = ThreadName(Thread.currentThread().getName)
            println(result)
            println(s"$time ms")
            if(programExecutionSummary.get(threadName).isDefined) programExecutionSummary(threadName) += ComputationTime(time)
            else programExecutionSummary += threadName -> ListBuffer(ComputationTime(time))

            val content = result.toString() + "\n" + s"$time ms" + "\n" + printPath(result) + "\n"
            writeToFile(fileName, content)
          }
        }
    }), FiniteDuration(100, DAYS))

    writeToFile(fileName, programExecutionSummary.toString())

    println(s"Global duration: ${-globalStart + System.currentTimeMillis()} ms")
    threadPool.shutdown()
  }

  def allPathsStartingAtSquare(sq: Coordinate, numberOfSolutions: Int, app: KnightTourProblem) = {
    val (x, y) = app.boardDimension
    println(s"=============== dimension: $x*$y ==================")
    println(s"=============== square: ${sq.toString()} ==================")

    val start = System.currentTimeMillis()
    val result = app.findPaths(List(List(sq)), numberOfSolutions)
    val time = -start + System.currentTimeMillis()

    val threadName = Thread.currentThread().getName
    println(threadName)
    result foreach println
    println(s"$time ms")
    //result foreach printPath
    ()


  }

//  println(s"program started at ${new Date()}")
//  val globalStart = System.currentTimeMillis()

  //(7 to 7) foreach (chessboard(_))

  val dim = 8
  //List((1,1)).foreach(allPathsStartingAtSquare(_, 1000, WarnsdorffKnightTourApp(dim, dim)))
  //List((1,1)).foreach(allPathsStartingAtSquare(_, 2, KnightTourInFiniteBoardApp(dim, dim)))
  //octant(dim).foreach(square(dim, _))
  //List((1,2),(1,4),(2,3),(3,4),(4,4)).foreach(square(dim, _))
  //-> List((4,4)).foreach(square(dim, _))
  //xxx List((1,2)).foreach(square(dim, _))
  //xxx List((3,4)).foreach(square(dim, _))

  List((1,1)).foreach(anyPathStartingAtSquare(dim, _, 8))

//  println(summary)
//  println(s"Global duration: ${-globalStart + System.currentTimeMillis()}")
  threadPool.shutdownNow()

}
