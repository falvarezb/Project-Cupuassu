package fjab.chess

import fjab.chess.apps.{KnightTourInFiniteBoardApp, WarnsdorffKnightTourApp}
import org.scalatest.FunSpec

class KnightTourTest extends FunSpec{

  describe("in a board of dimensions (6,6)"){
    val dim = (6,6)
    describe("starting at (2,2)"){
      val from = (2,2)
      val solution = List((2,2), (4,3), (6,4), (5,6), (3,5), (1,6), (2,4), (4,5), (6,6), (5,4), (6,2), (4,1), (5,3), (6,1), (4,2), (6,3), (5,1), (3,2), (1,1), (2,3), (1,5), (3,6), (5,5), (3,4), (2,6), (1,4), (3,3), (1,2), (3,1), (5,2), (4,4), (6,5), (4,6), (2,5), (1,3), (2,1))
      it("the path should be "){
        assert(KnightTourInFiniteBoardApp(dim._1, dim._2).findPath(List(List(from))) == solution)
      }
    }

    describe("starting at (1,1)"){
      val from = (1,1)
      val solution = List((1,1), (3,2), (5,3), (6,5), (4,6), (2,5), (1,3), (3,4), (5,5), (3,6), (1,5), (2,3), (3,5), (1,6), (2,4), (1,2), (3,1), (5,2), (6,4), (5,6), (4,4), (6,3), (5,1), (4,3), (2,2), (1,4), (2,6), (4,5), (6,6), (5,4), (6,2), (4,1), (3,3), (2,1), (4,2), (6,1))
      it("the path should be "){
        val actualValue = KnightTourInFiniteBoardApp(dim._1, dim._2).findPath(List(List(from)))
        assert(actualValue == solution)
      }
    }
  }

  describe("in a board of dimensions (8,8)"){
    val dim = (8,8)
    describe("starting at (1,1)"){
      val from = (1,1)
      val solution = List((1,1), (3,2), (5,3), (7,4), (8,6), (7,8), (5,7), (3,8), (1,7), (2,5), (4,6), (6,7), (8,8), (7,6), (6,8), (4,7), (2,8), (1,6), (3,7), (5,8), (6,6), (8,7), (7,5), (5,6), (7,7), (6,5), (4,4), (3,6), (4,8), (2,7), (1,5), (2,3), (3,5), (1,4), (2,2), (4,1), (3,3), (2,1), (1,3), (3,4), (5,5), (4,3), (5,1), (7,2), (8,4), (6,3), (8,2), (6,1), (4,2), (5,4), (6,2), (8,1), (7,3), (8,5), (6,4), (8,3), (7,1), (5,2), (3,1), (1,2), (2,4), (4,5), (2,6), (1,8))
      it("the path should be "){
        assert(KnightTourInFiniteBoardApp(dim._1, dim._2).findPath(List(List(from))) == solution)
      }
    }

    ignore("starting at (2,2)"){
      val from = (2,2)
      val solution = List((1,1), (3,2), (5,3), (4,5), (2,4), (0,5), (1,3), (3,4), (5,5), (4,3), (5,1), (3,0), (4,2), (5,0), (3,1), (5,2), (4,0), (2,1), (0,0), (1,2), (0,4), (2,5), (4,4), (2,3), (1,5), (0,3), (2,2), (0,1), (2,0), (4,1), (3,3), (5,4), (3,5), (1,4), (0,2), (1,0))
      it("the path should be "){
        assert(KnightTourInFiniteBoardApp(dim._1, dim._2).findPath(List(List(from))) == solution)
      }
    }
  }

  describe("neighbours of (1,2) in a 7x7 board"){
    it("when using Warnsdorff algorithm"){
      val result = WarnsdorffKnightTourApp(7,7).neighbours((1,2))
      assert(result == Seq((3,1), (2,4), (3,3)))
    }
  }
}

