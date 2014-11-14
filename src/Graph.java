import java.util.ArrayList;

/**
 * The Graph class acts as a container for the nodes that form the Risk map.
 * It contains all functions used to gain access to nodes, but not necessarily
 * all functions used to modify them.
 * 
 * @author Graham Netherton
 *
 */
public class Graph {

	private
		ArrayList<Node> nodes;	
	
	/**
	 * Constructor for the Graph class.
	 */
	public Graph() {
		nodes = new ArrayList<Node>(0);
	}
	
	/**
	 * Adds a new node to the graph.
	 */
	public void addNode() {
		nodes.add(new Node());
		return;
	}
	
	/**
	 * Adds a two-way adjacency link between two nodes.
	 * @param n1 The first node.
	 * @param n2 The second node.
	 */
	public void addAdjacencyLink(Node n1, Node n2) {
		n1.addAdjacent(n2);
		n2.addAdjacent(n1);
		return;
	}
	
	/**
	 * Attempts to find the first node with the specified number of adjacent nodes.
	 * @param adj The number of required adjacent nodes.
	 * @return The first suitable node, or null if none were found.
	 */
	public Node getNodeWithAdjacency(int adj) {
		return getNodeWithAdjacency(adj, 1);
	}
	
	/**
	 * Attempts to find the nth node with the specified number of adjacent nodes.
	 * @param adj The number of required adjacent nodes.
	 * @param n How many nodes in the process should look.
	 * @return The nth suitable node, or null if less than n were found.
	 */
	public Node getNodeWithAdjacency(int adj, int n) {
		for (Node node : nodes) {
			if (node.getAdjacent().size() == adj) {
				if (n == 1) {
					return node;
				}
				else {
					n -= 1;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Returns an ArrayList<Node> consisting of all the nodes owned by a given player.
	 * @param player The player making the request.
	 * @return An ArrayList of the nodes owned.
	 */
	public ArrayList<Node> getOwnedNodes(Player player) {
		ArrayList<Node> ownedNodes = new ArrayList<Node>(0);
		for (Node node : nodes) {
			if (node.getPlayer() == player) {
				ownedNodes.add(node);
			}
		}
		return ownedNodes;
	}
	
	/**
	 * Moves units from one node to another. 
	 * @param from The node to move the units from.
	 * @param to The node to move the units to.
	 * @param num The number of units to move.
	 */
	public void moveUnits(Node from, Node to, int num) {
		to.addUnits(num);
		from.addUnits(-num);
	}
}