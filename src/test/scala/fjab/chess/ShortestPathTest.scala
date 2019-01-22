package fjab.chess

import org.scalatest.FunSpec

class ShortestPathTest extends FunSpec {

  describe("shortest path between (1,1) -> (4,4)"){
    val from = (1,1)
    val to = (4,4)
    val solution = List((1,1), (3,2), (4,4))
    it("in infinite board"){
      assert(ShortestPathInInfiniteBoardApp(to).findPath(List(List(from))) == solution)
    }
    it("in finite board of dimensions (4,4)"){
      val dim = (4,4)
      assert(ShortestPathInFiniteBoardApp(dim._1,dim._2, to).findPath(List(List(from))) == solution)
    }
  }

  describe("shortest path between (1,1) -> (9,2)"){
    val from = (1,1)
    val to = (9,2)
    val solution = List((1,1), (3,2), (5,3), (6,5), (7,3), (9,2))
    it("in infinite board"){
      assert(ShortestPathInInfiniteBoardApp(to).findPath(List(List(from))) == solution)
    }
    it("in finite board of dimensions (4,4)"){
      val dim = (4,4)
      assert(ShortestPathInFiniteBoardApp(dim._1, dim._2, to).findPath(List(List(from))) == List())
    }
  }

  describe("shortest path between (1,1) -> (11,11)"){
    val from = (1,1)
    val to = (11,11)
    val solution = List((1, 1), (3, 2), (5, 3), (7, 4), (9, 5), (11, 6), (13, 7), (12, 9), (11, 11))
    it("in infinite board"){
      assert(ShortestPathInInfiniteBoardApp(to).findPath(List(List(from))) == solution)
    }
    it("in finite board of dimensions (15,15)"){
      val dim = (15,15)
      assert(ShortestPathInFiniteBoardApp(dim._1, dim._2, to).findPath(List(List(from))) == solution)
    }

    it("in finite board of dimensions (11,11)"){
      val dim = (11,11)
      assert(ShortestPathInFiniteBoardApp(dim._1, dim._2, to).findPath(List(List(from))) == List((1,1), (3,2), (5,3), (7,4), (9,5), (11,6), (10,8), (9,10), (11,11)))
    }
  }





}

