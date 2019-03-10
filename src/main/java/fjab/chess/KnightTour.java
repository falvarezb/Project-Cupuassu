package fjab.chess;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Java program for Knight Tour problem
class KnightTour {
  static int N = 7;
//  static int initialX = 0;
//  static int initialY = 1;

  /* A utility function to check if i,j are
  valid indexes for N*N chessboard */
  static boolean isSafe(int x, int y, int sol[][]) {
    return (x >= 0 && x < N && y >= 0 &&
      y < N && sol[x][y] == -1);
  }

  /* A utility function to print solution
  matrix sol[N][N] */
  static void printSolution(int sol[][]) {
    for (int x = 0; x < N; x++) {
      for (int y = 0; y < N; y++)
        System.out.print(sol[x][y] + " ");
      System.out.println();
    }
  }

  /* This function solves the Knight Tour problem
  using Backtracking. This function mainly
  uses solveKTUtil() to solve the problem. It
  returns false if no complete tour is possible,
  otherwise return true and prints the tour.
  Please note that there may be more than one
  solutions, this function prints one of the
  feasible solutions. */
  static boolean solveKT(int initialX, int initialY) {
    int sol[][] = new int[N][N];

    /* Initialization of solution matrix */
    for (int x = 0; x < N; x++)
      for (int y = 0; y < N; y++)
        sol[x][y] = -1;

	/* xMove[] and yMove[] define next move of Knight.
		xMove[] is for next value of x coordinate
		yMove[] is for next value of y coordinate */
    int xMove[] = {2, 1, -1, -2, -2, -1, 1, 2};
    int yMove[] = {1, 2, 2, 1, -1, -2, -2, -1};

    // Since the Knight is initially at the first block
    sol[initialX][initialY] = 0;

		/* Start from 0,0 and explore all tours using
		solveKTUtil() */
    if (!solveKTUtil(0, 0, 1, sol, xMove, yMove)) {
      System.out.println("Solution does not exist");
      return false;
    } else
      printSolution(sol);

    return true;
  }

  /* A recursive utility function to solve Knight
  Tour problem */
  static boolean solveKTUtil(int x, int y, int movei,
                             int sol[][], int xMove[],
                             int yMove[]) {
    int k, next_x, next_y;
    if (movei == N * N)
      return true;

		/* Try all next moves from the current coordinate
			x, y */
    for (k = 0; k < 8; k++) {
      next_x = x + xMove[k];
      next_y = y + yMove[k];
      if (isSafe(next_x, next_y, sol)) {
        sol[next_x][next_y] = movei;
        if (solveKTUtil(next_x, next_y, movei + 1,
          sol, xMove, yMove))
          return true;
        else
          sol[next_x][next_y] = -1;// backtracking
      }
    }

    return false;
  }

  static int[][] calculateOctant(int dim){
    int[][] octant = new int[dim][dim];
    int limit = dim % 2 == 0 ? dim / 2 : dim / 2 + 1;
    for(int i=0; i<limit; i++){
      for(int j=i; j<limit; j++){
        octant[i][j] = 1;
      }
    }
    return octant;
  }

  /* Driver program to test above functions */
  public static void main(String args[]) {

    int [][] octant = calculateOctant(N);

    ExecutorService fixedExecutorService = Executors.newFixedThreadPool(8);


    for(int i=0; i<N; i++){
      for(int j=0; j<N; j++){
        int value = octant[i][j];
        if(value == 1){
          int ilambda = i;
          int jlambda = j;
          fixedExecutorService.submit(() -> {
            System.out.println("Candidates: " + ilambda + "_" + jlambda);
            long startTime = System.currentTimeMillis();
            solveKT(ilambda,jlambda);
            System.out.println("Solution for: " + ilambda + "_" + jlambda);
            System.out.println("time:" + (-startTime + System.currentTimeMillis()));
          });
        }
      }
    }

    fixedExecutorService.shutdown();
  }
}

