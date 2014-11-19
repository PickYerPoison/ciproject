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
	 * Gets an ArrayList<Node> of all the nodes.
	 * @return ArrayList<Node> consisting of all the nodes.
	 */
	public ArrayList<Node> getNodes() {
		return nodes;
	}
	
	/**
	 * Clears all nodes from the graph.
	 */
	public void clear() {
		nodes.clear();
		nodes.trimToSize();
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
	public void addAdj(Node n1, Node n2) {
		n1.addAdj(n2);
		n2.addAdj(n1);
		return;
	}
	
	/**
	 * Attempts to find the nth node with the specified number of adjacent nodes.
	 * @param adj The number of required adjacent nodes.
	 * @param n How many nodes in the process should look.
	 * @return The nth suitable node, or null if less than n were found.
	 */
	public Node getNodeWithAdj(int adj, int n) {
		for (Node node : nodes) {
			if (node.getAdj().size() == adj) {
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
	 * Returns the number of nodes in the graph with a specified degree.
	 * @param d Integer degree for the nodes to have.
	 * @return Number of nodes with the specified degree.
	 */
	public int getNumNodesWithDegree(int d) {
		int num = 0;
		for (Node node : nodes) {
			if (node.getAdj().size() == d)
				num++;
		}
		return num;
	}
	
	/**
	 * Returns an ArrayList<Node> of all the nodes in the graph with a specified degree.
	 * @param d Integer degree for the nodes to have.
	 * @return ArrayList<Node> of the nodes.
	 */
	public ArrayList<Node> getNodesWithDegree(int d) {
		ArrayList<Node> toReturn = new ArrayList<Node>(0);
		for (Node node : nodes) {
			if (node.getAdj().size() == d)
				toReturn.add(node);
		}
		return toReturn;
	}
	
	/**
	 * Counts the nodes owned by a given player.
	 * @param player The player making the request.
	 * @return The number of nodes owned by the player.
	 */
	public int getNumOwnedNodes(Player player) {
		int ownedNodes = 0;
		for (Node node : nodes) {
			if (node.getOwner() == player) {
				ownedNodes++;
			}
		}
		return ownedNodes;
	}
	
	/**
	 * Returns an ArrayList<Node> consisting of all the nodes owned by a given player.
	 * @param player The player making the request.
	 * @return An ArrayList<Node> of the nodes owned.
	 */
	public ArrayList<Node> getOwnedNodes(Player player) {
		ArrayList<Node> ownedNodes = new ArrayList<Node>(0);
		for (Node node : nodes) {
			if (node.getOwner() == player) {
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
	
	/**
	 * Places a unit in a node. Returns a boolean indicating if there was an error.
	 * @param node The node to place the unit at.
	 * @param player The player placing the unit.
	 * @return True if there was an error, false if there wasn't.
	 */
	public boolean placeUnit(Node node, Player player) {
		boolean error = true;
		
		// no node was given
		if (node == null)
			player.showError("PLACE ERROR: Tried to put a unit in null instead of a node!");
		// the node is owned by a different player
		else if (node.getOwner() != null && node.getOwner() != player)
			player.showError("PLACE ERROR: Tried to put a unit an another player's node!");
		else {
			node.addUnits(1);
			node.setOwner(player);
			error = false;
		}
		
		return error;
	}
	
	/**
	 * Wrapper for the node threat calculation.
	 * @param range How many nodes out to check.
	 * @param node The node to start at.
	 * @return The threat level of the node.
	 */
	public int getThreat(int range, Node node) {
		int threat = node.getThreat(range);
		for (Node n : nodes)
			n.setChecked(false);
		return threat;
	}
	
	/**
	 * Wrapper for the node adjacency threat calculation.
	 * @param range How many nodes out to check.
	 * @param node The node to start at.
	 * @return The threat level of the node.
	 */
	public int getAdjThreat(int range, Node node) {
		int threat = node.getAdjThreat(range+1);
		for (Node n : nodes)
			n.setChecked(false);
		return threat;
	}
}