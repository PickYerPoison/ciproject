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
		int[] riskMap = {5, 13, 12, 7, 5};
		game.setupMap(riskMap);
		
		// Add the players (between 3 and 6).
		game.addPlayer(new DefensivePlayer("DefP1"));
		game.addPlayer(new DefensivePlayer("DefP2"));
		game.addPlayer(new DefensivePlayer("DefP3"));
		
		game.runGame();
	}
}
