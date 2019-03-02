package fjab.chess.apps

import fjab.chess.{BoardDimension, FiniteChessBoard, KnightTourProblem}

case class KnightTourInFiniteBoardApp(x: Int, y: Int) extends KnightTourProblem with FiniteChessBoard with BoardDimension
