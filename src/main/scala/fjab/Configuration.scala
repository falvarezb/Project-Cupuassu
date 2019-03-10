package fjab

import com.typesafe.config.{Config, ConfigFactory}
import fjab.chess.Coordinate

import scala.concurrent.duration._

object Configuration {

  private[this] val config: Config = ConfigFactory.load()

  val square: Coordinate = config.getStringList("square").get(0).split("_").map(_.toInt).toList match{
    case List(x,y) => (x,y)
  }
  val yieldTime: FiniteDuration = (config.getIntList("yieldTime").get(0): Int) minutes
  val reportInterval: Int = config.getIntList("reportInterval").get(0)
}
