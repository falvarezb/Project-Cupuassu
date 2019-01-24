package fjab.chess.apps

import java.util.Date
import java.util.concurrent.Executors

import fjab.chess.{octant, printPath}

import scala.concurrent.duration.{DAYS, FiniteDuration}
import scala.concurrent.{Await, ExecutionContext, Future}

object MainApp extends App {

  println(s"program started at ${new Date()}")
  val threadPool = Executors.newFixedThreadPool(4)
  implicit val ec = ExecutionContext.fromExecutor(threadPool)

  def chessboard(dimension: Int) = {
      println(s"=============== dimension: $dimension*$dimension ==================")

      Await.result(Future.sequence(octant(dimension).map {
        coordinate =>
          Future {
            val start = System.currentTimeMillis()
            val from = (coordinate._1, coordinate._2)
            val result = WarnsdorffKnightTourApp(dimension, dimension).findPath(List(List(from)))
            this.synchronized {
              println("")
              println((from, result, -start + System.currentTimeMillis()))
              printPath(result)
            }
          }
      }), FiniteDuration(10, DAYS))
  }

  (7 to 7) foreach (chessboard(_))
  println("end")
  threadPool.shutdown()

}
