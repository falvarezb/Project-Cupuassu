package fjab.chess

/**
  * Representation of an finite chess board as a graph based on the moves of a knight using Warnsdorff heuristic:
  * we always move to a neighbour with minimum number of neighbours
  */
trait WarnsdorffChessBoard extends FiniteChessBoard {

  self: BoardDimension =>

  override def neighbours(coordinate: Coordinate): Seq[Coordinate] = {
    val firstOrderNeighbours = super.neighbours(coordinate)
    firstOrderNeighbours.map{
      case firstOrderNeighbour => (firstOrderNeighbour,super.neighbours(firstOrderNeighbour).size)
    }.sortBy{
      case (_, numberOfSecondOrderNeighbours) => numberOfSecondOrderNeighbours
    }.map{
      case (firstOrderNeighbour, _) => firstOrderNeighbour
    }
  }
}
