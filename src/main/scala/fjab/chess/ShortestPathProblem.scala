package fjab.chess

import fjab.graph.Graph

import scala.collection.mutable.ListBuffer

/**
  * Implementation of Graph to find the shortest path between 2 vertices of the graph
  *
  */
trait ShortestPathProblem extends Graph[Coordinate]{

  self: Destination[Coordinate] =>

  /**
    * The nature of the problem requires a breadth-first search in order to find the shortest path
    */
  override def addNeighbours(verticesToExplore: ListBuffer[Path], neighbours: Seq[Path]): Unit =
    verticesToExplore ++= neighbours

  override def isSolution(path: Path): Boolean = path.head == destination
}
