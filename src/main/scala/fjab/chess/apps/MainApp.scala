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
import scala.concurrent.duration._

object MainApp extends App {


  def chessboard(dimension: Int) = {
    val summary: mutable.Map[String, mutable.Map[String,ListBuffer[Long]]] = mutable.Map()
    val threadPool = Executors.newFixedThreadPool(8)
    implicit val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(threadPool)

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

    threadPool.shutdown()
  }


  def enumeratePaths(
                      sq: Coordinate,
                      threads: Int,
                      yieldTime: FiniteDuration = 60*24*30 minutes,
                      reportInterval: Int = Int.MaxValue,
                      app: KnightTourProblem,
                      seedFilePath: String): Unit = {

    //Program initialisation
    val pathSeed = loadSeedFile(new File(seedFilePath))
    val (x, y) = app.boardDimension
    assert(x == y, "only square boards accepted")
    val globalStart = System.currentTimeMillis()
    val fileName = composeFilename(x, sq, globalStart, "pathsCount")
    val threadPool = Executors.newFixedThreadPool(threads)
    implicit val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(threadPool)
    var totalPaths = 0l

    //Program parameters summary
    {
      val content =
        s"""
           |program started at ${new Date()}
           |=============== dimension: $x*$y ==================
           |=============== square: $sq ==================
           |=============== yield time: ${yieldTime.length} ${yieldTime.unit} ==================
           |=============== report interval: every $reportInterval solutions ==================
           |=============== seed: $pathSeed ==========
           |=============== number of tasks: ${pathSeed.length} ==========
           |=============== number of threads: $threads ==================
           |=============== implementation used: ${app.toString} ==================
           |""".stripMargin

      println(content)
      writeToFile(fileName, content)
    }



    Await.result(Future.sequence(pathSeed.map {
      n =>
        Future {
          val start = System.currentTimeMillis()
          val numPaths: Long = app.enumeratePaths(
            from = Seq(n),
            yieldTime = `minute -> ms`(yieldTime.length),
            reportInterval = reportInterval)

          this.synchronized {

            totalPaths += numPaths

            //Task result summary
            val summaryContent = s"${Thread.currentThread().getName} initial path: $n, paths: $numPaths, time: ${`ms -> minute`(System.currentTimeMillis() - start)} min \n"
            println(summaryContent)

            val content =
              s"""
                 |$summaryContent
              """.stripMargin
            writeToFile(fileName, content)
          }
        }
    }), FiniteDuration(100, DAYS))


    //Program result summary
    {
      val content =
        s"""
           |totalPaths: $totalPaths
           |Global duration: ${`ms -> minute`(System.currentTimeMillis() - globalStart)} min
           |""".stripMargin

      println(content)
      writeToFile(fileName, content)
    }

    threadPool.shutdown()
  }

  /**
    * Each thread calculates a path via a different neighbour of the given square.
    *
    * The current implementation does not interrupt running threads or scheduled tasks when a path is found.
    * As a consequence, this program may return as many paths as neighbours
    */
  @MultiThread
  def `pathsFromSquare - backtracking with seed`(sq: Coordinate, threads: Int, yieldTime: FiniteDuration, app: KnightTourProblem, seed: Seq[List[(Int,Int)]] = Nil): Unit = {

    val (x, y) = app.boardDimension
    assert(x == y, "only square boards accepted")

    val globalStart = System.currentTimeMillis()
    val fileName = composeFilename(x, sq, globalStart)
    val allNeighbours = app.neighbours(sq).map(n => List(n, sq))
    val selectedNeighbours: Seq[List[(Int, Int)]] = if (seed.nonEmpty) seed else allNeighbours.take(threads)

    {
      val content =
        s"""
           |program started at ${new Date()}
           |=============== dimension: $x*$y ==================
           |=============== square: $sq ==================
           |=============== yield time: ${yieldTime.length} ${yieldTime.unit} ==================
           |=============== list of all neighbours: $allNeighbours ==========
           |=============== list of selected neighbours: $selectedNeighbours ==========
           |=============== number of threads: $threads ==================
           |=============== implementation used: ${app.toString} ==================
           |""".stripMargin

      println(content)
      writeToFile(fileName, content)
    }

    val threadPool = Executors.newFixedThreadPool(threads)
    implicit val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(threadPool)

    var totalPaths = 0l

    Await.result(Future.sequence(selectedNeighbours.map {
      n =>
        Future {
          val result: Seq[(app.Path, Long)] = app.findPaths(Seq(n), `minute -> ms`(yieldTime.length))

          this.synchronized {

            totalPaths += result.length
            val summaryContent = s"neighbour: $n, ${result.length} paths \n"
            println(summaryContent)

            val content =
              s"""
                |$summaryContent
                |${result.map(tuple => tuple._1).map(_.toString()).mkString("\n")}
                |computation times: $n - ${result.map(tuple => tuple._2).map(_.toString()).mkString(",")}
              """.stripMargin
            writeToFile(fileName, content)
          }
        }
    }), FiniteDuration(100, DAYS))

    {
      val content =
        s"""
           |totalPaths: $totalPaths
           |Global duration: ${`ms -> minute`(System.currentTimeMillis() - globalStart)} min
           |""".stripMargin

      println(content)
      writeToFile(fileName, content)
    }

    threadPool.shutdown()
  }

  /**
    * Calculation of paths using the method of permutation rotation
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
    * @param reportInterval Time interval (in minutes) to report progress
    * @param solutions List of solutions already found
    * @param permutationInterval Tuple of values representing the starting permutation and the number of permutations to calculate
    */
  @MultiThread
  def `pathsFromSquare - permutation rotation method`(dimension: Int, sq: Coordinate, threads: Int, yieldTime: FiniteDuration, reportInterval: Int, solutions: mutable.Set[List[(Int,Int)]] = mutable.Set(), permutationInterval: (Int, Int) = (0, 40320)): Unit = {

    assert(yieldTime.unit == TimeUnit.MINUTES)
    val (from, num) = permutationInterval
    val moves = List((2, 1), (1, 2), (-1, 2), (-2, 1), (-2, -1), (-1, -2), (1, -2), (2, -1)).permutations.toList.slice(from, from + num)
    val globalStart = System.currentTimeMillis()
    val fileName = composeFilename(dimension, sq, globalStart)

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
      writeToFile(solutionsFilename(dimension, sq), content, true)
    }

    threadPool.shutdown()
  }

  def resumePathsFromSquare(dimension: Int, sq: Coordinate, threads: Int, yieldTime: FiniteDuration, reportInterval: Int, permutationInterval: (Int, Int) = (0, 40320)): Unit = {
    val solutions = loadSolutions(new File(solutionsFilename(dimension, sq)))
    `pathsFromSquare - permutation rotation method`(dimension, sq, threads, yieldTime, reportInterval, solutions, permutationInterval)
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


  //(7 to 7) foreach (chessboard(_))

  //List((1,1)).foreach(allPathsStartingAtSquare(_, 1000, WarnsdorffKnightTourApp(dim, dim)))
  //List((1,1)).foreach(allPathsStartingAtSquare(_, 2, KnightTourInFiniteBoardApp(dim, dim)))
  //octant(dim).foreach(square(dim, _))
  //List((1,2),(1,4),(2,3),(3,4),(4,4)).foreach(square(dim, _))

  val seed11  = (
    ListBuffer[List[(Int,Int)]]()
    += List((1,1),(3,2), (1,3)).reverse //
    += List((1,1),(3,2), (5,1)).reverse //
    += List((1,1),(3,2), (2,4)).reverse //*2
    //+= List((1,1),(3,2), (5,3)).reverse symmetrical to (1,1),(3,2), (2,4) by 180 degrees rotation
    += List((1,1),(3,2), (4,4)).reverse //
    ).toList

  val seed22  = (
    ListBuffer[List[(Int,Int)]]()
      += List((2,2),(4,1),(6,2)).reverse //
      += List((2,2),(4,1),(5,3)).reverse //
      += List((2,2),(4,1),(3,3)).reverse //
      //+= List((1,1),(3,2), (5,3)).reverse symmetrical to (1,1),(3,2), (2,4) by 180 degrees rotation
      //+= List((1,1),(3,2), (4,4)).reverse //
    ).toList


  //`pathsFromSquare - backtracking with seed`(Configuration.square, Configuration.numberThreads, Configuration.yieldTime, KnightTourInFiniteBoardApp(Configuration.dim, Configuration.dim))

  enumeratePaths(
    sq = Configuration.square,
    threads = Configuration.numberThreads,
    yieldTime = Configuration.yieldTime,
    reportInterval = Configuration.reportInterval,
    app = KnightTourInFiniteBoardApp(Configuration.dim, Configuration.dim),
    seedFilePath = Configuration.seedFile
  )

  //pathsFromSquare(Configuration.dim, Configuration.square, Configuration.numberThreads, Configuration.yieldTime, Configuration.reportInterval)
  //resumePathsFromSquare(new File("./reports/_7x7/pathsFromSquare1_1_state.txt"), Configuration.numberThreads, Configuration.yieldTime, Configuration.reportInterval, (4440, 216))

//  println(summary)
//  println(s"Global duration: ${-globalStart + System.currentTimeMillis()}")
  //threadPool.shutdownNow()

}
