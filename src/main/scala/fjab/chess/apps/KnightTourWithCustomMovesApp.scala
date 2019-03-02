package fjab.chess.apps

import fjab.chess.{BoardDimension, Coordinate, FiniteChessBoard, KnightTourProblem}

case class KnightTourWithCustomMovesApp(x: Int, y: Int, override val moves: List[Coordinate]) extends KnightTourProblem with FiniteChessBoard with BoardDimension


