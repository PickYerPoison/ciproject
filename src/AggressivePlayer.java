import java.util.ArrayList;

/**
 * 
 */

/**
 * @author Graham
 *
 */
public class AggressivePlayer extends Player {
	/**
	 * Default constructor for the aggressive player.
	 */
	public AggressivePlayer() {
		super("AggressivePlayer");
	}

	/**
	 * Extended constructor for the aggressive player.
	 * @param n Name to give to the player.
	 */
	public AggressivePlayer(String n) {
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
		// move the max number of units to the newly captured node
		return from.getUnits()-1;
	}

	/* (non-Javadoc)
	 * @see Player#place()
	 */
	@Override
	Node place() {
		// determine if this is the first time we're placing a unit
		if (graph.getNumOwnedNodes(this) == 0) {
			// place the first unit down in an aggressive node (adjacency of 6)
			// choose the one with the most adjacency threat
			ArrayList<Node> degNodes = new ArrayList<Node>(0);
			
			for (int i = 6; i >= 2; i--) {
				degNodes = graph.getNodesWithDegree(i);
				
				// prune nodes belonging to other players
				int index = 0;
				while (index < degNodes.size()) {
					if (degNodes.get(index).getOwner() != null)
						degNodes.remove(index);
					else
						index++;
				}
				
				// are there any possible candidates?
				if (degNodes.size() > 0) {
					// find the node with the most adjacency threat using the default range
					int max = graph.getAdjThreat(range, degNodes.get(0));
					Node toPlace = degNodes.get(0);
					
					for (Node node : degNodes) {
						int adjThreat = graph.getAdjThreat(range, node);
						if (adjThreat > max) {
							max = adjThreat;
							toPlace = node;
						}
					}
					
					// place a unit at that node
					return toPlace;
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
					// if so, place a unit there
					return toPlace.get(0);
				}
				// did we find multiple candidates?
				else if (toPlace.size() > 1) {
					// find the node with the most adjacency threat using the default range
					int[] adjThreat = new int[toPlace.size()];
					
					// fill the array
					for (int i = 0; i < toPlace.size(); i++)
						adjThreat[i] = graph.getAdjThreat(range, toPlace.get(i));
					
					// find the index of the node with the least adjacency threat
					int max = adjThreat[0];
					int mIndex = 0;
					for (int i = 0; i < toPlace.size(); i++) {
						if (adjThreat[i] > max) {
							max = adjThreat[i];
							mIndex = i;
						}
					}
					
					// place a unit at that node
					return toPlace.get(mIndex);
				}
				// did we find no candidates?
				else {
					// place a unit at an unowned node, starting with the ones with the least adjacency threat
					for (int i = 6; i >= 2; i--) {
						// get all nodes of this degree
						ArrayList<Node> degNodes = graph.getNodesWithDegree(i);
						
						// remove nodes owned by players
						int index = 0;
						while (index < degNodes.size()) {
							if (degNodes.get(index).getOwner() != null) {
								degNodes.remove(index);
								degNodes.trimToSize();
							}
							else
								index++;
						}
						
						// is there only one left?
						if (degNodes.size() == 1)
							return degNodes.get(0);
						// are there multiple ones left?
						else if (degNodes.size() > 1) {
							// find the node with the least adjacency threat using the default range
							int max = graph.getAdjThreat(range, degNodes.get(0));
							Node toReturn = degNodes.get(0);
							
							for (Node node : degNodes) {
								int adjThreat = graph.getAdjThreat(range, node);
								if (adjThreat > max) {
									max = adjThreat;
									toReturn = node;
								}
							}

							// place a unit at that node
							return toReturn;
						}
					}
				}
			}
			// do all the nodes have units in them? (regular placement phase)
			else {
				// place the new unit in the most threatened node we own
				
				// create an ArrayList of all our nodes
				ArrayList<Node> nodes = graph.getOwnedNodes(this);
				
				// iterate through to find the most threatened node
				int max = 0;
				Node toPlace = nodes.get(0); 
				for (Node node : nodes) {
					int threat = graph.getThreat(range, node);
					if (threat > max) {
						max = threat;
						toPlace = node;
					}
				}
				
				// place the unit at the most threatened node
				return toPlace;
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
			
			
			// iterate through the adjacent nodes
			for (Node adjNode : adjNodes) {
				// while we have units and while the adjacent node belongs to the enemy, attack
				while (node.getUnits() > 1 && adjNode.getOwner() != this) {
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
		// move units to the most threatened node adjacent to enemies from the least threatened node adjacent to that
		Node to = null;
		Node from = null;
		
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
		
		// if there are no nodes left, don't fortify
		if (nodes.size() > 1) {
		
			// find the most threatened node in the group
			int maxThreat = -1;
			Node mostThreatened = null;
			for (Node node : nodes) {
				int threat = graph.getThreat(range, node);
				if (threat > maxThreat) {
					maxThreat = threat;
					mostThreatened = node;
				}
			}
			
			// find the least threatened node adjacent to it
			int minThreat = maxThreat;
			Node leastThreatened = null;
			for (Node node : mostThreatened.getAdj()) {
				if (node.getOwner() == this) {
					int threat = graph.getThreat(range, node);
					if (threat < minThreat) {
						minThreat = threat;
						leastThreatened = node;
					}
				}
			}
			
			to = mostThreatened;
			from = leastThreatened;
			
			// fortify using the given nodes and amounts, or don't fortify if no nodes were selected
			if (from != null && to != null && from.getUnits()-1 > 0)
				fortify(from, to, from.getUnits()-1);
		}
	}
}