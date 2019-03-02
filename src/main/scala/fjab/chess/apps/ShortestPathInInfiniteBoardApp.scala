package fjab.chess.apps

import fjab.chess.{Coordinate, Destination, InfiniteChessBoard, ShortestPathProblem}
import fjab.graph.GraphImproved

case class ShortestPathInInfiniteBoardApp(destination: Coordinate) extends GraphImproved[Coordinate] with InfiniteChessBoard with ShortestPathProblem with Destination[Coordinate]