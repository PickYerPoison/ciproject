import java.util.ArrayList;

/**
 * 
 */

/**
 * @author Graham
 *
 */
public class DefensivePlayer extends Player {
	/**
	 * Constructor for the DefensivePlayer class.
	 */
	public DefensivePlayer() {
		super();
		name = "DefensivePlayer";
	}

	/**
	 * Constructor for the DefensivePlayer class that sets the name.
	 * @param n The name to give the player.
	 */
	public DefensivePlayer(String n) {
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
		// move units based on a ratio of threat levels vs. current units
		int toThreat = graph.getThreat(range, to);
		int fromThreat = graph.getThreat(range, from);
		
		// don't divide by zero!
		if (fromThreat == 0)
			return from.getUnits()-1;
		
		/* create a ratio; err on the side of defending the older owned node
		 * ratio = to / from
		 * uses two equations: to + from = units, and to = ratio * from
		 * -> from * ratio + from = units
		 * -> from * (ratio + 1) = units
		 * -> from = units / (ratio + 1)
		 * -> to = ratio * 	        from        , round down
		 * -> to = ratio * (units / (ratio + 1)), round down
		 */
		double ratio = toThreat / fromThreat;
		int units = from.getUnits();
		int toSend  = (int)(ratio * (units / (ratio + 1)));
		if (toSend < 1)
			toSend = 1;
		return toSend;
	}

	/* (non-Javadoc)
	 * @see Player#place()
	 */
	@Override
	Node place() {
		// determine if this is the first time we're placing a unit
		if (graph.getNumOwnedNodes(this) == 0) {
			// place the first unit down in a defensive node (adjacency of 2)
			// choose the one with the least adjacency threat
			ArrayList<Node> degNodes = graph.getNodesWithDegree(2);
			
			// find the node with the least adjacency threat using the default range
			int min = graph.getAdjThreat(range, degNodes.get(0));
			Node toPlace = degNodes.get(0);
			
			for (Node node : degNodes) {
				int adjThreat = graph.getAdjThreat(range, node);
				if (adjThreat < min) {
					min = adjThreat;
					toPlace = node;
				}
			}
			
			// place a unit at that node
			return toPlace;
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
					// find the node with the least adjacency threat using the default range
					int[] adjThreat = new int[toPlace.size()];
					
					// fill the array
					for (int i = 0; i < toPlace.size(); i++)
						adjThreat[i] = graph.getAdjThreat(range, toPlace.get(i));
					
					// find the index of the node with the least adjacency threat
					int min = adjThreat[0];
					int mIndex = 0;
					for (int i = 0; i < toPlace.size(); i++) {
						if (adjThreat[i] < min) {
							min = adjThreat[i];
							mIndex = i;
						}
					}
					
					// place a unit at that node
					return toPlace.get(mIndex);
				}
				// did we find no candidates?
				else {
					// place a unit at an unowned node, starting with the ones with the least adjacency threat
					for (int i = 2; i <= 6; i++) {
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
							int min = graph.getAdjThreat(range, degNodes.get(0));
							Node toReturn = degNodes.get(0);
							
							for (Node node : degNodes) {
								int adjThreat = graph.getAdjThreat(range, node);
								if (adjThreat < min) {
									min = adjThreat;
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
		/*
		// get all owned nodes
		Node[] nodesArray = graph.getOwnedNodes(this).toArray(new Node[0]);
		
		// sort the array of nodes based on their threat levels
		Arrays.sort(nodesArray);
		
		// turn it back into an ArrayList to make other methods easier
		ArrayList<Node> nodes = new ArrayList<Node>(Arrays.asList(nodesArray));
		*/
		
		// get all owned nodes
		ArrayList<Node> nodes = graph.getOwnedNodes(this);
		
		/*
		 * ATTACK STEP
		 */
		
		// iterate through the owned nodes and attack adjacent enemy nodes with fewer units
		int index = 0;
		while (index < nodes.size()) {
			Node node = nodes.get(index); 
			
			// get the nodes adjacent to this one
			ArrayList<Node> adjNodes = node.getAdj();
			
			// iterate through the adjacent nodes
			for (Node adjNode : adjNodes) {
				// while we have units and while the adjacent node belongs to the enemy and has fewer units than this one, attack
				while (node.getUnits() > 1 && adjNode.getOwner() != this && adjNode.getUnits() <= node.getUnits()) {
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
		// move as many units as possible between two nodes with the highest difference in threat levels
		/*int maxDif = 0;
		Node from = null;
		Node to = null;
		
		nodes = graph.getOwnedNodes(this);
		
		for (Node node : nodes) {
			// can this node transfer any units?
			if (node.getUnits() > 1) {
				// get the adjacent nodes
				ArrayList<Node> adjNodes = node.getAdj();
				
				// prune out nodes we don't own
				index = 0;
				while (index < adjNodes.size()) {
					if (adjNodes.get(index).getOwner() != this)
						adjNodes.remove(index);
					else
						index++;
				}
				
				// compare the difference in threat levels for each adjacent node
				for (Node adjNode : adjNodes) {
					int threatDif = node.getThreat(range) - adjNode.getThreat(range);
					if (threatDif >= maxDif) {
						maxDif = threatDif;
						to = adjNode;
						from = node;
					}
				}
			}
		}*/
		
		// move units to the most threatened node adjacent to enemies from the least threatened adjacent node
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