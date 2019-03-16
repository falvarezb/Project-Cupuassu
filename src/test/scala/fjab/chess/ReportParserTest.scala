package fjab.chess

import org.scalatest.FunSuite

import scala.collection.mutable

class ReportParserTest extends FunSuite {

  test("testParseLines") {

    val input = Stream(
      "List((1,1)",
      "List((1,1)",
      "====> 8 permutations tried in 1.6251166666666665 min",
      "List((1,1)",
      "List((1,1)",
      "====> 16 permutations tried in 1.809 min",
      "List((1,1)",
      "====> 32 permutations tried in 2.9 min",
      "====> 40 permutations tried in 3.9 min",
    )

    val result: mutable.Map[Int, Int] = ReportParser.parseLines(input)

    assert(result(1) == 4)
    assert(result(2) == 1)
    assert(result(3) == 0)
  }

}
