package fjab.chess

import fjab.graph.Graph

import scala.collection.mutable.ListBuffer

/**
  * Implementation of Graph to find the path that visits all and every square of the board just once
  *
  */
trait KnightTourProblem extends Graph[Coordinate]{

  self: BoardDimension =>

  override def addNeighbours(verticesToExplore: ListBuffer[Path], neighbours: Seq[Path]): Unit =
    verticesToExplore.prependAll(neighbours) //depth-first search

  override def isSolution(path: Path): Boolean = path.length == x * y
}
