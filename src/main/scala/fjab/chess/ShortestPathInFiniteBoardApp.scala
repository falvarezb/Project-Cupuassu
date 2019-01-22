package fjab.chess

import fjab.graph.GraphImproved

case class ShortestPathInFiniteBoardApp(x: Int, y: Int, destination: Coordinate) extends GraphImproved[Coordinate] with FiniteChessBoard with ShortestPathSolution with BoardDimension with Destination[Coordinate]

object ShortestPathInFiniteBoardApp{
  def apply(x: Int, y: Int, destination: Coordinate): ShortestPathInFiniteBoardApp = new ShortestPathInFiniteBoardApp(x, y, destination)
}
