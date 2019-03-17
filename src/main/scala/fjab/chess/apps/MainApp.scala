package fjab.chess.apps

import java.io.File
import java.util.Date
import java.util.concurrent.{Executors, TimeUnit}

import fjab.Configuration
import fjab.chess._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.{DAYS, FiniteDuration}
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}

object MainApp extends App {

  val summary: mutable.Map[String, mutable.Map[String,ListBuffer[Long]]] = mutable.Map()
  val threadPool = Executors.newFixedThreadPool(8)
  implicit val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(threadPool)


  def chessboard(dimension: Int) = {
      println(s"program started at ${new Date()}")
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
    *
    * The current implementation does not interrupt running threads or scheduled tasks when a path is found.
    * As a consequence, this program may return as many paths as neighbours
    */
  @MultiThread
  def anyPathStartingAtSquare(sq: Coordinate, app: KnightTourProblem, threads: Int): Unit = {

    println(s"program started at ${new Date()}")
    val globalStart = System.currentTimeMillis()

    val (x, y) = app.boardDimension
    println(s"=============== dimension: $x*$y ==================")
    println(s"=============== square: ${sq.toString()} ==================")


    //val programExecutionSummary: mutable.Map[ThreadName, ListBuffer[ComputationTime]] = mutable.Map()
    val threadPool = Executors.newFixedThreadPool(threads)
    implicit val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(threadPool)

    val fileName = s"reports/pathsFromSquare${sq._1}_${sq._2}.txt"


    val neighboursList = app.neighbours(sq)
    println(s"neighbours list: $neighboursList")

    Await.result(Future.sequence(neighboursList.map {
      n =>
        Future {
          val start = System.currentTimeMillis()
          val result = app.findPath(List(List(n, sq)))
          val time = -start + System.currentTimeMillis()

          this.synchronized {
            //val threadName = ThreadName(Thread.currentThread().getName)
            println(result)
            println(s"$time ms")
            //if(programExecutionSummary.get(threadName).isDefined) programExecutionSummary(threadName) += ComputationTime(time)
            //else programExecutionSummary += threadName -> ListBuffer(ComputationTime(time))

            val content = result.toString() + "\n" + s"$time ms" + "\n" + printPath(result) + "\n"
            writeToFile(fileName, content)
          }
        }
    }), FiniteDuration(100, DAYS))

    //writeToFile(fileName, programExecutionSummary.toString())

    println(s"Global duration: ${-globalStart + System.currentTimeMillis()} ms")
    threadPool.shutdown()
  }

  /**
    * Each thread calculates a path by using a different permutation of the list of possible moves of a Knight:
    * List((2,1), (1,2), (-1,2), (-2,1), (-2,-1), (-1,-2), (1,-2), (2,-1))
    *
    * A thread will stop if it does not find any solution before yieldTime
    */

  /**
    * Multi-thread implementation
    *
    * Each thread calculates a path by using a different permutation of the list of possible moves of a Knight:
    * List((2,1), (1,2), (-1,2), (-2,1), (-2,-1), (-1,-2), (1,-2), (2,-1))
    *
    * A thread stops if:
    * 1. finds a solution
    * 2. it runs for longer than yieldTime
    *
    * A summary of the execution is stored in a local file
    *
    * @param dimension Dimension of the chessboard
    * @param sq Starting square
    * @param threads Number of threads to use
    * @param yieldTime Max time a thread can run
    * @param reportInterval Time interval to report progress
    * @param solutions List of solutions already found
    * @param permutationInterval Tuple of values representing the starting permutation and the number of permutations to calculate
    */
  @MultiThread
  def pathsFromSquare(dimension: Int, sq: Coordinate, threads: Int, yieldTime: FiniteDuration, reportInterval: Int, solutions: mutable.Set[List[(Int,Int)]] = mutable.Set(), permutationInterval: (Int, Int) = (0, 40320)): Unit = {

    assert(yieldTime.unit == TimeUnit.MINUTES)
    val (from, num) = permutationInterval
    val moves = List((2, 1), (1, 2), (-1, 2), (-2, 1), (-2, -1), (-1, -2), (1, -2), (2, -1)).permutations.toList.slice(from, from + num)
    val globalStart = System.currentTimeMillis()
    val fileName = s"reports/_${dimension}x$dimension/pathsFromSquare${sq._1}_${sq._2}_$globalStart.txt"

    {
      val content =
        s"""
           |program started at ${new Date()}
           |=============== dimension: $dimension*$dimension ==================
           |=============== square: $sq ==================
           |=============== yield time: ${yieldTime.length} ${yieldTime.unit} ==================
           |=============== report interval: $reportInterval permutations ==================
           |=============== number of permutations to calculate: ${moves.length}  ==================
           |=============== permutation interval: from $from to ${from + num}  ==================
           |=============== number of threads: $threads ==================
           |""".stripMargin

      println(content)
      writeToFile(fileName, content)
    }

    val computationTimeOfSuccessfulPaths = new ListBuffer[Long]()
    val threadPool = Executors.newFixedThreadPool(threads)
    implicit val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(threadPool)

    var counter = 0

    Await.result(Future.sequence(moves.map {
      moves =>
        Future {
            //val localDeadEnds = new ListBuffer[Long]()
            val app = KnightTourWithCustomMovesApp(dimension, dimension, moves)
            val localStart = System.currentTimeMillis()
            val result = app.findPath(List(List(sq)), `minute -> ms`(yieldTime.length))

          this.synchronized {

              counter += 1
              //deadEndTimes ++= localDeadEnds
              if(counter % reportInterval == 0){
                val content = s"\n====> $counter permutations tried in ${`ms -> minute`(System.currentTimeMillis() - globalStart)} min \n"
                println(content)
                writeToFile(fileName, content)
              }

              if(result.nonEmpty && !solutions.contains(result)) {
                solutions += result
                val time = -localStart + System.currentTimeMillis()
                println(result)
                println(s"$time ms")
                computationTimeOfSuccessfulPaths += time

                val content = result.toString() + "\n" + s"$time ms" + "\n"
                writeToFile(fileName, content)
            }
          }
        }
    }), FiniteDuration(100, DAYS))


    {
      val content =
        s"""
           |
           |Global duration: ${`ms -> minute`(System.currentTimeMillis() - globalStart)} min
           |Paths found: ${computationTimeOfSuccessfulPaths.length}
           |Computation times in ms: ${computationTimeOfSuccessfulPaths.mkString(",")}
           |""".stripMargin

      println(content)
      writeToFile(fileName, content)
    }

    {
      val content = solutions.map(_.toString()).mkString("\n")
      val stateFile = s"reports/_${dimension}x$dimension/pathsFromSquare${sq._1}_${sq._2}_state.txt"
      writeToFile(stateFile, content, true)
    }

    threadPool.shutdown()
  }

  def resumePathsFromSquare(initialState: File, threads: Int, yieldTime: FiniteDuration, reportInterval: Int, permutationInterval: (Int, Int) = (0, 40320)): Unit = {
    val state = loadSolutions(initialState)
    val dimension = math.sqrt(state.head.length).toInt
    val sq = state.head.head
    pathsFromSquare(dimension, sq, threads, yieldTime, reportInterval, state, permutationInterval)
  }

  def allPathsStartingAtSquare(sq: Coordinate, numberOfSolutions: Int, app: KnightTourProblem): Unit = {
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

  val dimension = Configuration.dim
  val square: Coordinate = Configuration.square
  //List((1,1)).foreach(allPathsStartingAtSquare(_, 1000, WarnsdorffKnightTourApp(dim, dim)))
  //List((1,1)).foreach(allPathsStartingAtSquare(_, 2, KnightTourInFiniteBoardApp(dim, dim)))
  //octant(dim).foreach(square(dim, _))
  //List((1,2),(1,4),(2,3),(3,4),(4,4)).foreach(square(dim, _))
  //-> List((4,4)).foreach(square(dim, _))
  //List((1,1)).foreach(anyPathStartingAtSquare(_, WarnsdorffKnightTourApp(dim, dim), 1))
  //xxx List((3,4)).foreach(square(dim, _))


  //pathsFromSquare(dimension, square, Configuration.numberThreads, Configuration.yieldTime, Configuration.reportInterval)
  resumePathsFromSquare(new File("./reports/_7x7/pathsFromSquare1_1_state.txt"), Configuration.numberThreads, Configuration.yieldTime, Configuration.reportInterval, (4440, 216))

//  println(summary)
//  println(s"Global duration: ${-globalStart + System.currentTimeMillis()}")
  threadPool.shutdownNow()

}
