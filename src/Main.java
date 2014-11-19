/**
 * 
 */

/**
 * @author Graham
 *
 */
public class Main {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Game game = new Game();
		
		/* 
		 * Set up the game map. Specifications for the default Risk map:
		 * Degree	Nodes
		 * 2		5
		 * 3		13
		 * 4		12
		 * 5		7
		 * 6		5
		 */
		int[][] riskMap = { {1, 3, 23}, {0, 2, 3, 4}, {1, 4, 5, 13}, {0, 1, 4, 6}, {1, 2, 3, 5, 7}, 
				{2, 4, 7}, {3, 7, 8}, {4, 5, 6, 8}, {6, 7, 9}, {8, 10, 11}, {9, 11, 12}, {9, 10, 12, 33}, 
				{10, 11}, {2, 14, 15}, {13, 15, 16, 17}, {13, 14, 16, 18}, {14, 15, 17, 18, 19}, 
				{14, 16, 19, 20, 25, 28}, {15, 16, 19, 33}, {16, 17, 18, 28, 32, 33}, {17, 21, 25, 29}, 
				{20, 22, 24, 26, 29}, {21, 23, 24}, {0, 22, 24, 26, 27}, {21, 22, 23, 26}, {17, 20, 28, 29, 30}, 
				{21, 23, 24, 27, 29}, {23, 26}, {17, 19, 25, 30, 32, 34}, {20, 21, 25, 26, 30, 31}, 
				{25, 28, 29, 31}, {29, 30, 38}, {19, 28, 33, 34}, {11, 18, 19, 32, 34, 35}, {28, 32, 33, 35, 37}, 
				{33, 34, 36}, {35, 37}, {34, 36}, {31, 39, 40}, {38, 40, 41}, {38, 39, 41}, {39, 40} };
 
		game.setupMap(riskMap);
		
		// Add the players (between 3 and 6).
		game.addPlayer(new DefensivePlayer("DefP1"));
		game.addPlayer(new DefensivePlayer("DefP2"));
		game.addPlayer(new DefensivePlayer("DefP3"));
		
		game.runGame();
	}
}
