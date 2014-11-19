import java.util.ArrayList;

/**
 * The Node class represents the territories of Risk. Each Node is a territory,
 * containing a number of units belonging to a particular player. 
 * 
 * @author Graham Netherton
 *
 */
public class Node implements Comparable<Node> {
	
	private
		Player owner;
		int units;
		ArrayList<Node> adjacent;
	
	/*
	 *	CONSTRUCTORS 
	 */
	/**
	 * Constructor for the Node class. Initializes the owner to null
	 * and the number of units to 0 and creates the adjacency ArrayList.
	 */
	public Node() {
		owner = null;
		units = 0;
		adjacent = new ArrayList<Node>(0);
	}
	
	/*
	 * 	ACCESSORS
	 */
	/**
	 * Returns the owner of this node.
	 * @return The player who owns this node.
	 */
	public Player getOwner() {
		return owner;
	}
	
	/**
	 * Returns the number of units in this node.
	 * @return The number of units in this node.
	 */
	public int getUnits() {
		return units;
	}
	
	/**
	 * Returns the adjacent nodes.
	 * @return The ArrayList containing the adjacent nodes.
	 */
	public ArrayList<Node> getAdj() {
		return adjacent;
	}

	/*
	 * 	MUTATORS
	 */
	/**
	 * Sets the owner of this node.
	 * @param player The new owner.
	 */
	public void setOwner(Player player) {
		owner = player;
		return;
	}
	
	/**
	 * Add/subtract units to/from the node. 
	 * @param add Number of units to add to the node, or to subtract if negative.
	 */
	public void addUnits(int add) {
		units += add;
		return;
	}

	/**
	 * Adds an adjacency link to the given node.
	 * @param node Node to link to.
	 */
	public void addAdj(Node node) {
		adjacent.add(node);
		return;
	}
	
	/*
	 * 	UTILITY
	 */
	 
	/**
	 * Generates an integer value representing the threat to this node. The value is based on the
	 * relative distance of and number of units within other players' nodes. An individual node's
	 * contribution to the threat level is given by (number of units) * (range at node).
	 * @param checked ArrayList<Node> consisting of already-checked nodes.
	 * @param player The calling node's owner.
	 * @param range Integer representing how much further out to check.
	 * @return An integer representing the threat to the node.
	 */
	public int getThreat(int range, Player player, ArrayList<Node> checked) {
		// add this node to the list of checked nodes
		checked.add(this);
		
		// create variable to store this node's accumulated threat
		int threat = 0;
		
		// add the threat level for this node if it's not owned by the player
		if (getOwner() != player) {
			// because the range diminishes over distance, it multiplies instead of adds 
			threat += getUnits() * range;
		}
		
		// range determines distance to check, so end this feeler if it's gone too far
		if (range > 0) {			
			// iterate through the adjacent nodes
			for (Node node : adjacent) {
				// get the node's threat level if:
				// - it is not owned by this player
				// - it is not in the list of already checked nodes
				if (node.getOwner() != player && checked.contains(node) == false) {
					threat += node.getThreat(range-1, player, checked);
				}
			}
		}
		
		return threat;
	}
	
	/**
	 * Initial getThreat call that automatically fills out values. 
	 * @param range Integer representing how much further out to check.
	 * @return An integer representing the threat to the node.
	 */
	public int getThreat(int range) {
		return getThreat(range, getOwner(), new ArrayList<Node>(0));
	}
	
	/**
	 * Generates an integer value representing the adjacency threat to this node. The value is
	 * based on the the relative distance of nodes not belonging to the node's player. An
	 * individual node's contribution to  the threat level is based on its range from the
	 * original calling node.
	 * @param checked ArrayList<Node> consisting of already-checked nodes.
	 * @param player The calling node's owner.
	 * @param range Integer representing how much further out to check.
	 * @return An integer representing the threat to the node.
	 */
	public int getAdjThreat(int range, Player player, ArrayList<Node> checked) {
		// add this node to the list of checked nodes
		checked.add(this);
		
		// create variable to store this node's accumulated threat
		int threat = 0;
		
		// add the threat level for this node if it's not owned by the player
		if (getOwner() != player) 
			threat += range;
		
		// range determines distance to check, so end this feeler if it's gone too far
		if (range > 0) {
			// iterate through the adjacent nodes
			for (Node node : adjacent) {
				// get the node's threat level if:
				// - it is not owned by this player
				// - it is not in the list of already checked nodes
				if (node.getOwner() != player && checked.contains(node) == false) {
					threat += node.getAdjThreat(range-1, player, checked);
				}
			}
		}
		
		return threat;
	}
	
	/**
	 * Initial getAdjacencyThreat call that automatically fills out values. 
	 * @param range Integer representing how much further out to check.
	 * @return An integer representing the threat to the node.
	 */
	public int getAdjThreat(int range) {
		return getAdjThreat(range, getOwner(), new ArrayList<Node>(0));
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Node n) {
		return getThreat(3) - n.getThreat(3);
	}
}