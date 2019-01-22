package fjab.chess

import fjab.graph.Graph

/**
  * Representation of an infinite chess board as a graph based on the moves of a knight
  */
trait InfiniteChessBoard extends Graph[Coordinate]{

  //knight moves
  val moves: List[Coordinate] = List((2,1), (1,2), (-1,2), (-2,1), (-2,-1), (-1,-2), (1,-2), (2,-1))

  override def neighbours(coordinate: Coordinate): Seq[Coordinate] = moves.map( coordinate + _)

  /**
    * Avoid an infinite loop by not visiting previously visited vertices in the present path
    */
  override def isVertexEligibleForPath(vertex: Coordinate, path: Path): Boolean = !path.contains(vertex)
}
