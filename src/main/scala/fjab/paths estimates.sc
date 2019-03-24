//=====> Knights tour upper bound on 5x5

//first estimate: a Knight can make 8 different moves, therefore:

val firstEstimate5 = math.pow(8, 25)

/*
  however, no all squares have 8 neighbours:

   4 squares with 2 moves
   8 squares with 3 moves
   8 squares with 4 moves
   4 squares with 6 moves
   1 square with 8 moves

 */

val upperBound5 = math.pow(2, 4) *
  math.pow(3, 8) *
  math.pow(4, 8) *
  math.pow(6, 4) *
  math.pow(8, 1)

/*
    Given that a Knight cannot go back to a previously visited square, we can at least
    substract 1 neighbour from each square.

    For instance, starting at (1,1), from where it is possible to make 2 moves:
 */

val upperBoundRefinement5 = 2 *
  math.pow(1, 3) *
  math.pow(2, 8) *
  math.pow(3, 8) *
  math.pow(5, 4) *
  math.pow(7, 1)



//=====> Knights tour upper bound on 6x6

val firstEstimate6 = math.pow(8, 36)

val upperBound6  = math.pow(2, 4) *
  math.pow(3, 8) *
  math.pow(4, 12) *
  math.pow(6, 8) *
  math.pow(8, 4)


val upperBoundRefinement6 = 2 *
  math.pow(1, 3) *
  math.pow(2, 8) *
  math.pow(3, 12) *
  math.pow(5, 8) *
  math.pow(7, 4)



//=====> Knights tour upper bound on 8x8

val firstEstimate8 = math.pow(8, 64)

val upperBound8 = math.pow(2, 4) *
  math.pow(3, 8) *
  math.pow(4, 20) *
  math.pow(6, 16) *
  math.pow(8, 16)

val upperBoundRefinement8 = 2 *
  math.pow(1, 3) *
  math.pow(2, 8) *
  math.pow(3, 20) *
  math.pow(5, 16) *
  math.pow(7, 16)


11380+4759*2+1779+6545

val minutesInYear = 60*24*365