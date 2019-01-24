package fjab.chess.apps

import fjab.chess.{Coordinate, Destination, InfiniteChessBoard, ShortestPathProblem}
import fjab.graph.GraphImproved

case class ShortestPathInInfiniteBoardApp(destination: Coordinate) extends GraphImproved[Coordinate] with InfiniteChessBoard with ShortestPathProblem with Destination[Coordinate]

object ShortestPathInInfiniteBoardApp{
  def apply(destination: Coordinate): ShortestPathInInfiniteBoardApp = new ShortestPathInInfiniteBoardApp(destination)
}