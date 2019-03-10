package fjab.graph

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

trait Graph[T] {

  type Vertex = T
  //Path to a Vertex
  type Path = List[Vertex]

  /**
    * This method traverses the graph and as it does so, builds all possible paths until finding the solution
    * as per the definition of the method 'isSolution'.
    * Note: the paths are stored on a mutable ListBuffer for efficiency
    *
    * The process to calculate the neighbours of a given vertex is specific for every type of graph and is implemented
    * by the method 'neighbours'. The method 'isVertexEligibleForPath' filters the neighbours that are eligible to be
    * included in the present path (for instance, in most cases it is desirable not to visit twice the same vertex).
    * The method 'addNeighbours' determines the way the graph is traversed: breadth-first or depth-first.
    *
    *
    * It is worth noticing that the present algorithm to traverse the graph is very generic and therefore sub-optimal
    * for problems like finding the shortest path between 2 vertices as the different paths do not share information
    * about vertices already visited.
    *
    *
    * @param from Initial vertices used to calculate the possible paths
    * @return Path The searched path or Nil if the desired path does not exist
    */
  def findPath(from: Seq[Path], yieldTime: Long = Long.MaxValue): Path = {

    //val deadEndsLength = new ListBuffer[Long]()
    val start = System.currentTimeMillis()
    val paths: ListBuffer[Path] = ListBuffer() ++= from

    @tailrec
    def next(): Path = paths.headOption match{
      case _ if System.currentTimeMillis() - start > yieldTime => Nil
      case None => Nil
      case Some(currentVertexPath) =>
        if(isSolution(currentVertexPath)) currentVertexPath.reverse
        else {
          val currentVertex = currentVertexPath.head
          val neighbourVertices = neighbours(currentVertex).filter(isVertexEligibleForPath(_, currentVertexPath))
//          if(neighbourVertices.isEmpty){
//            deadEndsLength += currentVertexPath.length
//          }
          val pathsToNeighbourVertices = neighbourVertices.map(_ :: currentVertexPath)
          paths.remove(0)
          addNeighbours(paths, pathsToNeighbourVertices)
          next()
        }
    }

    val r = next()

//      val l = deadEndsLength.length.toDouble
//      val avg = deadEndsLength.sum/l
//      val max = deadEndsLength.max
//      //val std = deadEnds.map(x => (x-avg)*(x-avg)).sum/l
//    val min = deadEndsLength.min
//      println(s"${Thread.currentThread().getName}: numDeadEnds:$l, avgDepth:$avg, maxDepth:$max, minDepth:$min")

    r



  }

  def findPaths(from: Seq[Path], numberOfSolutions: Int): Seq[Path] = {

    val foundPaths: ListBuffer[Path] = ListBuffer()
    val pathsInProgress: ListBuffer[Path] = ListBuffer() ++= from

    @tailrec
    def next(): Unit = pathsInProgress.headOption match{
      case None => //nothing
      case Some(currentVertexPath) =>
        if(isSolution(currentVertexPath)) {
          foundPaths += currentVertexPath.reverse
          pathsInProgress.remove(0)
        }
        else {
          val currentVertex = currentVertexPath.head
          val neighbourVertices = neighbours(currentVertex).filter(isVertexEligibleForPath(_, currentVertexPath))
          val pathsToNeighbourVertices = neighbourVertices.map(_ :: currentVertexPath)
          pathsInProgress.remove(0)
          addNeighbours(pathsInProgress, pathsToNeighbourVertices)
        }
        if(foundPaths.length < numberOfSolutions) next()
    }

    next()
    foundPaths
  }

  /**
    * Calculate the neighbours of the given vertex.
    * Implementations of this method depend on the nature of the graph: moves allowed from one vertex to another,
    * constraints, etc.
    */
  def neighbours(vertex: Vertex): Seq[Vertex]

  /**
    * This method adds the neighbours of the current vertex to the list of remaining vertices to explore and
    * determines the algorithm to traverse the graph, depth-first or breadth-first.
    *
    * Depending on whether the new paths are added at the front or the back of the list, the resulting traversing
    * algorithm will be depth-first or breadth-first respectively.
    *
    * Breadth-first algorithms are necessary to find the shortest path between 2 vertices or when looking
    * for paths in an infinite graph
    *
    */
  def addNeighbours(verticesToExplore: ListBuffer[Path], neighbours: Seq[Path]): Unit


  /**
    * Condition to be met by the path. The condition will be different for each particular problem, e.g.:
    * - path that passes through an specific vertex (condition used to find a path connecting 2 vertices)
    * - path that passes through all the vertices (condition used to find the solution of problems like the Knight's tour)
    */
  def isSolution(path: Path): Boolean

  /**
    * This method determines if the given vertex is to be added to the path. The implementation will depend on the
    * characteristics of the problem to solve.
    *
    * For instance, in most cases, it will be necessary to discard a vertex that is already included in the present path.
    */
  def isVertexEligibleForPath(vertex: Vertex, path: Path): Boolean

}


