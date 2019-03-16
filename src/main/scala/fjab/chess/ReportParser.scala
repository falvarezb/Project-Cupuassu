package fjab.chess

import scala.collection.mutable
import scala.io.Source

object ReportParser extends App {

  parseFile("./reports/_7x7","pathsFromSquare1_1_1552259339316.txt")

  def parseFile(path: String, fileName: String) = {
    val lines = Source.fromFile(s"$path/$fileName").getLines().toStream
    val result = parseLines(lines)
    val content =
      result.
        toList.
        map{
          case (key, _) => key
        }.
        sorted.
        map(key => (key, result(key))).
        map(x => s"${x._1},${x._2}").
        mkString("\n")

    writeToFile(s"$path/${fileName}_report.csv", content)
  }

  def parseLines(lines: Stream[String]) = {

    val buckets: mutable.Map[Int, Int] = mutable.Map()

    var pathCounter = 0
    for(line <- lines){
      if(line.startsWith("List")) pathCounter += 1
      else if(line.startsWith("====>")) {
        val key = parseTime(line)
        val previousNumberOfPathsInThisBucket = buckets.get(key).getOrElse(0)
        buckets += (key -> (pathCounter + previousNumberOfPathsInThisBucket))
        pathCounter = 0
      }
    }

    buckets
  }

  def parseTime(line: String): Int = {
    //Example: ====> 8 permutations tried in 1.6251166666666665 min
    line.split(' ')(5).split('.')(0).toInt
  }

}
