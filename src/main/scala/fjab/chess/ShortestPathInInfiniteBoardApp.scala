package fjab.chess

import fjab.graph.GraphImproved

case class ShortestPathInInfiniteBoardApp(destination: Coordinate) extends GraphImproved[Coordinate] with InfiniteChessBoard with ShortestPathSolution with Destination[Coordinate]

object ShortestPathInInfiniteBoardApp{
  def apply(destination: Coordinate): ShortestPathInInfiniteBoardApp = new ShortestPathInInfiniteBoardApp(destination)
}