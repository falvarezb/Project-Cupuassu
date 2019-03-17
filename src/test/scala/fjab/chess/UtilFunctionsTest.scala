package fjab.chess

import org.scalatest.FunSpec

class UtilFunctionsTest extends FunSpec{

  describe("quadrant"){
    it("of a board 5*5"){
      val result = quadrant(5)
      assert(result == Seq((1,1),(1,2),(1,3),(1,4),(2,2),(2,3),(3,3)))
    }
    it("of a board 6*6"){
      val result = quadrant(6)
      assert(result == Seq((1,1),(1,2),(1,3),(1,4),(1,5),(2,2),(2,3),(2,4),(3,3)))
    }
  }

  describe("octant"){
    it("of a 5*5 board"){
      val result = octant(5)
      assert(result == Seq((1,1),(1,2),(1,3),(2,2),(2,3),(3,3)))
    }
    it("of a 6*6 board"){
      val result = octant(6)
      assert(result == Seq((1,1),(1,2),(1,3),(2,2),(2,3),(3,3)))
    }
    it("of a 7*7 board"){
      val result = octant(7)
      assert(result == Seq((1,1), (1,2), (1,3), (1,4), (2,2), (2,3), (2,4), (3,3), (3,4), (4,4)))
    }
  }

  describe("parsing initial state line"){
    it("should be successful"){
      val line = "List((1,1), (3,2), (5,3))"
      val result = deserialiseKnightsTour(line)
      assert(result == List((1,1), (3,2), (5,3)))
    }
  }
}
