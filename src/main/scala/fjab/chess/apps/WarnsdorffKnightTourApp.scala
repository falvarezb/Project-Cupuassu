package fjab.chess.apps

import fjab.chess.{BoardDimension, KnightTourProblem, WarnsdorffChessBoard}

case class WarnsdorffKnightTourApp(x: Int, y: Int) extends KnightTourProblem with WarnsdorffChessBoard with BoardDimension


object WarnsdorffKnightTourApp{
  def apply(x: Int, y: Int): WarnsdorffKnightTourApp = new WarnsdorffKnightTourApp(x, y)
}