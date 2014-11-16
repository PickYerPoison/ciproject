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
		
		// debug output
                
		Node n = graph.getNodeWithAdjacency(2, 1);
		int i = 1;
		while (n != null) {
			i++;
			n = graph.getNodeWithAdjacency(2, i);
		}
		System.out.println("Number of degree 2 nodes: " + (i-1));
		n = graph.getNodeWithAdjacency(3, 1);
		i = 1;
		while (n != null) {
			i++;
			n = graph.getNodeWithAdjacency(3, i);
		}
		System.out.println("Number of degree 3 nodes: " + (i-1));
		n = graph.getNodeWithAdjacency(4, 1);
		i = 1;
		while (n != null) {
			i++;
			n = graph.getNodeWithAdjacency(4, i);
		}
		System.out.println("Number of degree 4 nodes: " + (i-1));
		n = graph.getNodeWithAdjacency(5, 1);
		i = 1;
		while (n != null) {
			i++;
			n = graph.getNodeWithAdjacency(5, i);
		}
		System.out.println("Number of degree 5 nodes: " + (i-1));
		n = graph.getNodeWithAdjacency(6, 1);
		i = 1;
		while (n != null) {
			i++;
			n = graph.getNodeWithAdjacency(6, i);
		}
		System.out.println("Number of degree 6 nodes: " + (i-1));
		
	}
	
	/**
	 * Determines if a given player has lost.
	 * @param player The player to check.
	 * @return True if the player has lost, false if they haven't.
	 */
	public boolean hasLost(Player player) {
		if (graph.getNumOwnedNodes(player) == 0)
			return true;
		else
			return false;
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