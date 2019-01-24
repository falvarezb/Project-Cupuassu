package fjab.chess.apps

import fjab.chess.{BoardDimension, Coordinate, Destination, ShortestPathProblem, WarnsdorffChessBoard}
import fjab.graph.GraphImproved

case class ShortestPathInFiniteBoardApp(x: Int, y: Int, destination: Coordinate) extends GraphImproved[Coordinate] with WarnsdorffChessBoard with ShortestPathProblem with BoardDimension with Destination[Coordinate]

object ShortestPathInFiniteBoardApp{
  def apply(x: Int, y: Int, destination: Coordinate): ShortestPathInFiniteBoardApp = new ShortestPathInFiniteBoardApp(x, y, destination)
}
