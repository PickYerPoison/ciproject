import java.util.ArrayList;

/**
 * 
 */

/**
 * @author Graham
 *
 */
public class BalancedPlayer extends Player {

	/**
	 * 
	 */
	public BalancedPlayer() {
		super("BalancedPlayer");
	}

	/**
	 * @param n
	 */
	public BalancedPlayer(String n) {
		super(n);
	}

	/* (non-Javadoc)
	 * @see Player#defend(Node, Node, int)
	 */
	@Override
	int defend(Node from, Node to, int num) {
		// always defend with as many units as possible
		if (to.getUnits() > 1)
			return 2;
		else
			return 1;
	}

	/* (non-Javadoc)
	 * @see Player#occupy(Node, Node)
	 */
	@Override
	int occupy(Node from, Node to) {
		// keep half the units (round down)
		return from.getUnits() - (from.getUnits()/2);
	}

	/* (non-Javadoc)
	 * @see Player#place()
	 */
	@Override
	Node place() {
		// reference array for preferred degrees of nodes
		int[] pref = {4, 3, 5, 2, 6};
		
		// determine if this is the first time we're placing a unit
		if (graph.getNumOwnedNodes(this) == 0) {
			// try to place a unit in the first node with a preferred degree
			for (int x : pref) {
				if (graph.getNumNodesWithDegree(x) > 0) {
					ArrayList<Node> degNodes = graph.getNodesWithDegree(x);
					
					// prune nodes belonging to players
					int index = 0;
					while (index < degNodes.size()) {
						if (degNodes.get(index).getOwner() != null)
							degNodes.remove(index);
						else
							index++;
					}
					
					if (degNodes.size() > 0)
						return degNodes.get(0);
				}
			}
		}
		// if we already have units on the field
		else {
			// are there unowned nodes on the field? (initial placement phase)
			if (graph.getNumOwnedNodes(null) > 0) {
				// see if we can place a unit in a node next to one of our own
				
				// get an ArrayList of nodes we own
				ArrayList<Node> nodes = graph.getOwnedNodes(this);
				
				// create an ArrayList to store candidates for placement
				ArrayList<Node> toPlace = new ArrayList<Node>(0);
				
				// iterate through all of our nodes and add any adjacent unowned nodes
				for (Node node : nodes) {
					for (Node adjNode : node.getAdj()) {
						if (adjNode.getOwner() == null)
							toPlace.add(adjNode);
					}
				}
				
				// did we find a single candidate?
				if (toPlace.size() == 1) {
					// if so, place a unit at the first node we found
					return toPlace.get(0);
				}
				// did we find multiple candidates?
				if (toPlace.size() > 1) {
					// if so, place the unit at the one with the most preferred degree
					for (int x : pref) {
						for (Node node : toPlace) {
							if (node.getAdj().size() == x)
								return node;
						}
					}
				}
				// did we find no candidates?
				else {
					// try to place a unit in the first node with a preferred degree
					for (int x : pref) {
						if (graph.getNumNodesWithDegree(x) > 0) {
							ArrayList<Node> degNodes = graph.getNodesWithDegree(x);
							
							// prune nodes belonging to players
							int index = 0;
							while (index < degNodes.size()) {
								if (degNodes.get(index).getOwner() != null)
									degNodes.remove(index);
								else
									index++;
							}
							
							if (degNodes.size() > 0)
								return degNodes.get(0);
						}
					}
				}
			}
			// do all the nodes have units in them? (regular placement phase)
			else {
				// try to make every node have the same number of units
				
				// create an ArrayList of all our nodes
				ArrayList<Node> nodes = graph.getOwnedNodes(this);
				
				// find the average units per node (round down)
				int units = 0;
				for (Node node : nodes) {
					units += node.getUnits();
				}
				int avgUnits = units/nodes.size();
				
				// place the unit in the first node that is below average
				Node minNode = nodes.get(0);
				for (Node node : nodes) {
					if (node.getUnits() < avgUnits)
						return node;
					else if (node.getUnits() < minNode.getUnits())
						minNode = node;
				}
				
				// if no nodes are below average, place it in the node with the fewest units
				return minNode;
			}
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see Player#turn()
	 */
	@Override
	void turn() {
		// get all owned nodes
		ArrayList<Node> nodes = graph.getOwnedNodes(this);
		
		/*
		 * ATTACK STEP
		 */
		
		// sort nodes in order of decreasing number of units
		boolean sorted = false;
		while (!sorted) {
			sorted = true;
			int index = 0;
			while (index < nodes.size() - 1) {
				// compare this node to the next one
				if (nodes.get(index).getUnits() < nodes.get(index+1).getUnits()) {
					sorted = false;
					
					// move this node to the end of the list
					nodes.add(nodes.get(index));
					nodes.remove(index);
				}
				else
					index++;
			}
		}
		
		// iterate through the owned nodes and attack adjacent enemy nodes
		// start from own node with most units and attack enemy nodes in increasing order of number of units
		int index = 0;
		while (index < nodes.size()) {
			Node node = nodes.get(index); 
			
			// get the nodes adjacent to this one
			ArrayList<Node> adjNodes = new ArrayList<Node>(node.getAdj());
			
			// prune out nodes not belonging to the enemy
			int index2 = 0;
			while (index2 < adjNodes.size()) {
				if (adjNodes.get(index2).getOwner() == this) {
					adjNodes.remove(index2);
					adjNodes.trimToSize();
				}
				else
					index2++;
			}		
			
			// sort the adjacent enemy nodes in order of least to most units
			sorted = false;
			while (!sorted) {
				sorted = true;
				index2 = 0;
				while (index2 < adjNodes.size() - 1) {
					// compare this node to the next one
					if (adjNodes.get(index2).getUnits() > adjNodes.get(index2+1).getUnits()) {
						sorted = false;
						
						// move this node to the end of the list
						adjNodes.add(adjNodes.get(index2));
						adjNodes.remove(index2);
					}
					else
						index2++;
				}
			}
			
			
			// iterate through the adjacent nodes; call off the attack if any units are lost
			for (Node adjNode : adjNodes) {
				int prevUnits = node.getUnits();
				// while we have units and while the adjacent node belongs to the enemy and while we haven't lost a battle, attack
				while (node.getUnits() > 1 && adjNode.getOwner() != this && node.getUnits() == prevUnits) {
					// update the pre-battle number of units
					prevUnits = node.getUnits();
					
					// commence the attack
					attack(node, adjNode, Math.min(node.getUnits()-1, 3));
					
					// add the node to the list of owned nodes if we captured it
					if (adjNode.getOwner() == this)
						nodes.add(adjNode);
				}
			}
			
			index++;
		}
		
		/*
		 * FORTIFY STEP 
		 */
		// bring the node with the fewest units up to the average
		Node to = null;
		Node from = null;
		int toMove = 0;
		
		// get all owned nodes
		nodes = graph.getOwnedNodes(this);
		
		// eliminate nodes not adjacent to other owned nodes
		index = 0;
		while (index < nodes.size()) {
			boolean hasAdj = false;
			for (Node adj : nodes.get(index).getAdj()) {
				if (adj.getOwner() == this) {
					hasAdj = true;
					break;
				}
			}
			if (!hasAdj)
				nodes.remove(index);
			else
				index++;
		}
		
		// if there are no nodes or only one node left, don't fortify
		if (nodes.size() > 1) {
			// find the average units per node (round down)
			int units = 0;
			for (Node node : graph.getOwnedNodes(this)) {
				units += node.getUnits();
			}
			int avgUnits = units/graph.getNumOwnedNodes(this);
			
			// prune all nodes below average
			index = 0;
			while (index < nodes.size()) {
				if (nodes.get(index).getUnits() >= avgUnits)
					nodes.remove(index);
				else
					index++;
			}
			
			// find the first node that can be brought up to the average by an adjacent node
			for (Node node : nodes) {
				// get the nodes adjacent to this one
				ArrayList<Node> adjNodes = new ArrayList<Node>(node.getAdj());
				
				// prune out nodes belonging to the enemy
				index = 0;
				while (index < adjNodes.size()) {
					if (adjNodes.get(index).getOwner() != this) {
						adjNodes.remove(index);
						adjNodes.trimToSize();
					}
					else
						index++;
				}
				
				// see if an adjacent node can supply this one without dropping below average
				for (Node adjNode : adjNodes) {
					if (adjNode.getUnits() >= avgUnits + (avgUnits - node.getUnits())) {
						from = adjNode;
						to = node;
						toMove = avgUnits - node.getUnits();
						break;
					}
				}
				
				// are we good to go?
				if (from != null)
					break;
			}
			
			// fortify using the given nodes and amounts, or don't fortify if no nodes were selected
			if (from != null && to != null && toMove > 0)
				fortify(from, to, toMove);
		}
	}
}
