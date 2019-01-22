package fjab.chess

/**
  * Representation of an finite chess board as a graph based on the moves of a knight
  */
trait FiniteChessBoard extends InfiniteChessBoard {

  self: BoardDimension =>

  override def neighbours(coordinate: Coordinate): Seq[Coordinate] =
    super.neighbours(coordinate).filter{ case (v, w) => v >= 1 && v<=x && w >=1 && w<=y }
}
