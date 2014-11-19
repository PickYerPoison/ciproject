import java.util.ArrayList;

/**
 * The Node class represents the territories of Risk. Each Node is a territory,
 * containing a number of units belonging to a particular player. 
 * 
 * @author Graham Netherton
 *
 */
public class Node {
	
	private
		Player owner = null;
		int units = 0;
		ArrayList<Node> adjacent = new ArrayList<Node>(0);
		boolean checked = false;
	
	/*
	 *	CONSTRUCTORS 
	 */
	/**
	 * Constructor for the Node class. Initializes the owner to null
	 * and the number of units to 0 and creates the adjacency ArrayList.
	 */
	public Node() {	}
	
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
	
	/**
	 * Gets if the node has been checked.
	 * @return Boolean indicating if the node has been checked yet.
	 */
	public boolean getChecked() {
		return checked;
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
	 * Sets the number of units in this node.
	 * @param u Integer number of units to set the node to have.
	 */
	public void setUnits(int u) {
		units = u;
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
	
	/**
	 * Sets the checked boolean.
	 * @param b What to set checked to.
	 */
	public void setChecked(boolean b) {
		checked = b;
	}
	
	/*
	 * 	UTILITY
	 */
	 
	/**
	 * Generates an integer value representing the threat to this node. The value is based on the
	 * relative distance of and number of units within other players' nodes. An individual node's
	 * contribution to the threat level is given by (number of units) * (range at node).
	 * @param range Integer representing how much further out to check.
	 * @param player The calling node's owner.
	 * @return An integer representing the threat to the node.
	 */
	public int getThreat(int range, Player player) {
		// indicate that this node has been checked
		setChecked(true);
		
		// create variable to store this node's accumulated threat
		int threat = 0;
		
		// range determines distance to check, so end this feeler if it's gone too far
		if (range >= 1) {
			// because the range diminishes over distance, it multiplies instead of adds
			threat += getUnits() * range;
			
			if (range > 1) {
				ArrayList<Node> uncheckedEnemyAdj = new ArrayList<Node>(getAdj());
				
				int index = 0;
				while (index < uncheckedEnemyAdj.size()) {
					if (uncheckedEnemyAdj.get(index).getOwner() == getOwner() || uncheckedEnemyAdj.get(index).getChecked())
						uncheckedEnemyAdj.remove(index);
					else
						index++;
				}
				// iterate through the adjacent nodes
				for (Node node : uncheckedEnemyAdj) {
					// get the node's threat level if:
					// - it is not owned by this player
					// - it has not already been checked
					if (!node.getChecked())
						threat += node.getThreat(range-1, player);
				}
			}
		}
		
		// return this node's branching threat
		return threat;
	}
	
	/**
	 * Initial getThreat call that automatically fills out values.
	 * Should only be called through the graph containing the nodes!
	 * @param range Integer representing how many nodes out to check.
	 * @return An integer representing the threat to the node.
	 */
	public int getThreat(int range) {
		setChecked(true);
		int threat = 0;
		ArrayList<Node> uncheckedEnemyAdj = new ArrayList<Node>(getAdj());
		
		int index = 0;
		while (index < uncheckedEnemyAdj.size()) {
			if (uncheckedEnemyAdj.get(index).getOwner() == getOwner() || uncheckedEnemyAdj.get(index).getChecked())
				uncheckedEnemyAdj.remove(index);
			else
				index++;
		}
		
		for (Node node : uncheckedEnemyAdj) {
			// get the node's threat level if:
			// - it is not owned by this player
			// - it has not already been checked
			if (!node.getChecked())
				threat += node.getThreat(range, getOwner());
		}
		return threat;
	}
	
	/**
	 * Generates an integer value representing the adjacency threat to this node. The value is
	 * based on the the relative distance of nodes not belonging to the node's player. An
	 * individual node's contribution to  the threat level is based on its range from the
	 * original calling node.
	 * @param range Integer representing how much further out to check.
	 * @param player The calling node's owner.
	 * @return An integer representing the threat to the node.
	 */
	public int getAdjThreat(int range, Player player) {
		// indicate that this node has been checked
		setChecked(true);
		
		// create variable to store this node's accumulated threat
		int threat = 0;
		
		// range determines distance to check, so end this feeler if it's gone too far
		if (range >= 1) {
			// because the range diminishes over distance, add it to give closer nodes more weight
			threat += range;
			
			if (range > 1) {
				ArrayList<Node> uncheckedEnemyAdj = new ArrayList<Node>(getAdj());
				
				int index = 0;
				while (index < uncheckedEnemyAdj.size()) {
					if (uncheckedEnemyAdj.get(index).getOwner() == getOwner() || uncheckedEnemyAdj.get(index).getChecked())
						uncheckedEnemyAdj.remove(index);
					else
						index++;
				}
				// iterate through the adjacent nodes
				for (Node node : uncheckedEnemyAdj) {
					// get the node's threat level if:
					// - it is not owned by this player
					// - it has not already been checked
					if (!node.getChecked())
						threat += node.getAdjThreat(range-1, player);
				}
			}
		}
		
		// return this node's branching threat
		return threat;
	}
	
	/**
	 * Initial getAdjacencyThreat call that automatically fills out values.
	 * Should only be called through the graph containing the node! 
	 * @param range Integer representing how much further out to check.
	 * @return An integer representing the threat to the node.
	 */
	public int getAdjThreat(int range) {
		setChecked(true);
		int threat = 0;
		ArrayList<Node> uncheckedEnemyAdj = new ArrayList<Node>(getAdj());
		
		int index = 0;
		while (index < uncheckedEnemyAdj.size()) {
			if (uncheckedEnemyAdj.get(index).getOwner() == getOwner() || uncheckedEnemyAdj.get(index).getChecked())
				uncheckedEnemyAdj.remove(index);
			else
				index++;
		}
		
		for (Node node : uncheckedEnemyAdj) {
			// get the node's threat level if:
			// - it is not owned by this player
			// - it has not already been checked
			if (!node.getChecked())
				threat += node.getAdjThreat(range, getOwner());
		}
		return threat;
	}
	
	public int getNumNodes() {
		// check this node off
		setChecked(true);
		
		// count this node to start
		int num = 1;
		
		// check with all adjacent nodes
		ArrayList<Node> uncheckedAdj = new ArrayList<Node>(getAdj());
		
		int index = 0;
		while (index < uncheckedAdj.size()) {
			if (uncheckedAdj.get(index).getChecked())
				uncheckedAdj.remove(index);
			else
				index++;
		}
		
		for (Node node : uncheckedAdj) {
			if (!node.getChecked())
				num += node.getNumNodes();
		}
		
		return num;
	}
}