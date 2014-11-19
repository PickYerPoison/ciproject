import java.util.ArrayList;

/**
 * @author Graham Netherton
 *
 */
public class Game {

	private
		Graph graph;
		ArrayList<Player> players;
	
	/**
	 * Constructor for the Game class.
	 */
	public Game() {
		graph = new Graph();
		players = new ArrayList<Player>(0);
	}
	
	/**
	 * Adds a player to the game.
	 * @param player The player to add.
	 */
	public void addPlayer(Player player) {
		players.add(player);
		player.setGraph(graph);
		return;
	}
	
	/**
	 * Sets up the Risk map for use by connecting nodes until there are a specified
	 * number of nodes of each degree (specifying degrees 2 through 6).
	 * @param nodes Integer array of size 5 specifying how many nodes of each degree to add.
	 */
	public void setupMap(int[] nodes) {
		//  number of nodes that still need to be adjusted
		int toAdjust = 0;
		for (int i = 0; i < 5; i++)
			toAdjust += nodes[i];
			
		// create all the nodes
		for (int i = 0; i < toAdjust; i++) {
			graph.addNode();
		}
		
		
		// bring all nodes to degree 1
		Node n1 = graph.getNodeWithAdj(0, 1);
		Node n2 = graph.getNodeWithAdj(0, 2);
		while (n1 != null) {
			graph.addAdj(n1, n2);
			n1 = graph.getNodeWithAdj(0, 1);
			n2 = graph.getNodeWithAdj(0, 2);
		}

		// bring all nodes to degree 2
		n1 = graph.getNodeWithAdj(1, 1);
		n2 = graph.getNodeWithAdj(1, 2);
		while (n1 != null) {
			graph.addAdj(n1, n2);
			n1 = graph.getNodeWithAdj(1, 1);
			n2 = graph.getNodeWithAdj(1, 2);
		}
		
		// reduce the number of nodes left to adjust
		toAdjust -= nodes[0];
		
		// bring toAdjust nodes to degree 3
		n1 = graph.getNodeWithAdj(2, 1);
		n2 = graph.getNodeWithAdj(2, 2);
		for (int i = 0; i < toAdjust; i++) {
			n1.addAdj(n2);
			n1 = graph.getNodeWithAdj(2, 1);
			n2 = graph.getNodeWithAdj(2, 2);
		}
		
		// reduce the number of nodes left to adjust
		toAdjust -= nodes[1];
		
		// bring toAdjust nodes to degree 4
		n1 = graph.getNodeWithAdj(3, 1);
		n2 = graph.getNodeWithAdj(3, 2);
		for (int i = 0; i < toAdjust; i++) {
			n1.addAdj(n2);
			n1 = graph.getNodeWithAdj(3, 1);
			n2 = graph.getNodeWithAdj(3, 2);
		}
		
		// reduce the number of nodes left to adjust
		toAdjust -= nodes[2];
		
		// bring toAdjust nodes to degree 5
		n1 = graph.getNodeWithAdj(4, 1);
		n2 = graph.getNodeWithAdj(4, 2);
		for (int i = 0; i < toAdjust; i++) {
			n1.addAdj(n2);
			n1 = graph.getNodeWithAdj(4, 1);
			n2 = graph.getNodeWithAdj(4, 2);
		}
		
		// reduce the number of nodes left to adjust
		toAdjust -= nodes[3];
		
		// bring toAdjust nodes to degree 6
		n1 = graph.getNodeWithAdj(5, 1);
		n2 = graph.getNodeWithAdj(5, 2);
		for (int i = 0; i < toAdjust; i++) {
			n1.addAdj(n2);
			n1 = graph.getNodeWithAdj(5, 1);
			n2 = graph.getNodeWithAdj(5, 2);
		}
	}

	/**
	 * Runs the game. The game map and the player list should already be set up!
	 * @param unloadPlayers Boolean specifying whether the list of players should be reset afterwards.
	 * @param unloadMap Boolean specifying whether the graph of nodes should be reset afterwards.
	 */
	public void runGame(boolean unloadPlayers, boolean unloadMap) {
		// add players. if there's an error somewhere, inPlay is set to 0.
		int inPlay = players.size();
		
		// determine how many units each player will start with
		int units = 0;
		switch (inPlay) {
			case 3: units = 35; break;
			case 4: units = 30; break;
			case 5: units = 25; break;
			case 6: units = 20; break;
			default: inPlay = 0; System.out.println("SETUP ERROR: Invalid number of players!");
		}
		
		// have the players distribute their units
		for (int i = 0; i < units; i++) {
			for (Player player : players)
				graph.placeUnit(player.place(), player);
		}
		
		// turns in game thus far
		int turns = 0;
		int maxTurns = 500;
		
		// cycle through players
		while (inPlay > 1) {
			inPlay = 0;
			
			System.out.println("Starting turn " + turns + "...");
			
			for (Player player : players) {
				if (player.hasLost() == false) {
					inPlay++;
					int newUnits = graph.getNumOwnedNodes(player)/3;
					for (int i = 0; i < newUnits; i++)
						graph.placeUnit(player.place(), player);
					player.turn();
				}
			}
			
			// increase the turn counter
			turns++;
			
			// break out of the loop if maxTurns was exceeded
			if (turns > maxTurns) {
				System.out.println("Maximum turns exceeded. Ending game...");
				break;
			}
		}

		System.out.println("The game took " + turns + " turns.");
		
		// declare the winner if no error occurred
		if (inPlay == 1) {
			for (Player player : players) {
				if (player.hasLost() == false) {
					System.out.println("The winner of the match is " + player.getName() + ".");
				}
			}
		}
		
		// unload any specified assets
		if (unloadPlayers) {
			players.clear();
			players.trimToSize();
		}
		if (unloadMap) {
			graph.clear();
		}
	}
	
	/**
	 * Overloaded runGame() that doesn't unload anything afterwards.
	 */
	public void runGame() {
		runGame(false, false);
	}
}