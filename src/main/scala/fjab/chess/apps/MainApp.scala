package fjab.chess.apps

import java.util.Date
import java.util.concurrent.Executors

import fjab.chess.{octant, printPath}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.{DAYS, FiniteDuration}
import scala.concurrent.{Await, ExecutionContext, Future}

object MainApp extends App {

  val summary: mutable.Map[String, mutable.Map[String,ListBuffer[Long]]] = mutable.Map()
  val threadPool = Executors.newFixedThreadPool(4)
  implicit val ec = ExecutionContext.fromExecutor(threadPool)


  def chessboard(dimension: Int) = {
      println(s"=============== dimension: $dimension*$dimension ==================")

      val map = mutable.Map[String,ListBuffer[Long]]()
      summary += (s"$dimension" -> map)
      val numElements = octant(dimension)
      println(s"number of elements to calculate: ${numElements.length}")

      Await.result(Future.sequence(numElements.map {
        coordinate =>
          Future {
            val start = System.currentTimeMillis()
            val from = (coordinate._1, coordinate._2)
            val result = WarnsdorffKnightTourApp(dimension, dimension).findPath(List(List(from)))
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

  println(s"program started at ${new Date()}")
  val globalStart = System.currentTimeMillis()
  (7 to 7) foreach (chessboard(_))
  println(summary)
  println(s"Global duration: ${-globalStart + System.currentTimeMillis()}")
  threadPool.shutdownNow()

}
