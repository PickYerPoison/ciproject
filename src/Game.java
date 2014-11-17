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
		
		// set up the map
		setupMap();
		
		// add players
		int inPlay = 0; // increase this counter whenever you add a player!
		
		// determine how many pieces each player will start with
		int pieces = 0;
		switch (inPlay) {
			case 3: pieces = 35; break;
			case 4: pieces = 30; break;
			case 5: pieces = 25; break;
			case 6: pieces = 20; break;
			default: pieces = 0;
		}
		
		// have the players distribute their pieces
		for (int i = 0; i < pieces; i++) {
			for (Player player : players)
				graph.placeUnit(player.place(), player);
		}
		
		// cycle through players
		while (inPlay > 1) {
			for (Player player : players) {
				if (player.hasLost() == false) {
					int newUnits = graph.getNumOwnedNodes(player)/3;
					for (int i = 0; i < newUnits; i++)
						graph.placeUnit(player.place(), player);
					player.turn();
				}
			}
		}
		
		// declare the winner
		for (Player player : players) {
			if (player.hasLost() == false) {
				System.out.println("The winner of the match is " + player.getName() + ".");
			}
		}
	}
	
	/**
	 * Adds a player to the game.
	 * @param player The player to add.
	 */
	public void addPlayer(Player player) {
		players.add(player);
		return;
	}
	
	/**
	 * Sets up the Risk map for use by connecting nodes until there are
	 * the correct number of nodes of each degree.
	 * Degree	Nodes
	 * 2		5
	 * 3		13
	 * 4		12
	 * 5		7
	 * 6		5
	 */
	public void setupMap() {
		//  number of nodes that still need to be adjusted
		int toAdjust = 5 + 13 + 12 + 7 + 5;
		
		// create all the nodes
		for (int i = 0; i < toAdjust; i++) {
			graph.addNode();
		}
		
		
		// bring all nodes to degree 1
		Node n1 = graph.getNodeWithAdjacency(0, 1);
		Node n2 = graph.getNodeWithAdjacency(0, 2);
		while (n1 != null) {
			graph.addAdjacent(n1, n2);
			n1 = graph.getNodeWithAdjacency(0, 1);
			n2 = graph.getNodeWithAdjacency(0, 2);
		}

		// bring all nodes to degree 2
		n1 = graph.getNodeWithAdjacency(1, 1);
		n2 = graph.getNodeWithAdjacency(1, 2);
		while (n1 != null) {
			graph.addAdjacent(n1, n2);
			n1 = graph.getNodeWithAdjacency(1, 1);
			n2 = graph.getNodeWithAdjacency(1, 2);
		}
		
		// reduce the number of nodes left to adjust
		toAdjust -= 5;
		
		// bring toAdjust nodes to degree 3
		n1 = graph.getNodeWithAdjacency(2, 1);
		n2 = graph.getNodeWithAdjacency(2, 2);
		for (int i = 0; i < toAdjust; i++) {
			n1.addAdjacent(n2);
			n1 = graph.getNodeWithAdjacency(2, 1);
			n2 = graph.getNodeWithAdjacency(2, 2);
		}
		
		// reduce the number of nodes left to adjust
		toAdjust -= 13;
		
		// bring toAdjust nodes to degree 4
		n1 = graph.getNodeWithAdjacency(3, 1);
		n2 = graph.getNodeWithAdjacency(3, 2);
		for (int i = 0; i < toAdjust; i++) {
			n1.addAdjacent(n2);
			n1 = graph.getNodeWithAdjacency(3, 1);
			n2 = graph.getNodeWithAdjacency(3, 2);
		}
		
		// reduce the number of nodes left to adjust
		toAdjust -= 12;
		
		// bring toAdjust nodes to degree 5
		n1 = graph.getNodeWithAdjacency(4, 1);
		n2 = graph.getNodeWithAdjacency(4, 2);
		for (int i = 0; i < toAdjust; i++) {
			n1.addAdjacent(n2);
			n1 = graph.getNodeWithAdjacency(4, 1);
			n2 = graph.getNodeWithAdjacency(4, 2);
		}
		
		// reduce the number of nodes left to adjust
		toAdjust -= 7;
		
		// bring toAdjust nodes to degree 6
		n1 = graph.getNodeWithAdjacency(5, 1);
		n2 = graph.getNodeWithAdjacency(5, 2);
		for (int i = 0; i < toAdjust; i++) {
			n1.addAdjacent(n2);
			n1 = graph.getNodeWithAdjacency(5, 1);
			n2 = graph.getNodeWithAdjacency(5, 2);
		}
	}
}