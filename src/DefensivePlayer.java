import java.util.ArrayList;
import java.util.Arrays;

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
		int toThreat = to.getThreat(range);
		int fromThreat = from.getThreat(range);
		
		/* create a ratio; err on the side of defending the older owned node
		 * ratio = to / from
		 * uses two equations: to + from = units, and to = ratio * from
		 * -> from * ratio + from = units
		 * -> ratio * (from + 1) = units
		 * -> from + 1 = units / ratio
		 * -> from = units / ratio + 1
		 * -> to = ratio * 	      from        , round down
		 * -> to = ratio * (units / ratio + 1), round down
		 */
		double ratio = toThreat / fromThreat;
		int units = from.getUnits();
		int toSend  = (int)(ratio * (units / ratio + 1));
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
			int[] adjThreat = new int[degNodes.size()];
			
			// fill the array
			for (int i = 0; i < degNodes.size(); i++)
				adjThreat[i] = degNodes.get(i).getAdjThreat(range);
			
			// find the index of the node with the least adjacency threat
			int min = adjThreat[0];
			int mIndex = 0;
			for (int i = 0; i < degNodes.size(); i++) {
				if (adjThreat[i] < min) {
					min = adjThreat[i];
					mIndex = i;
				}
			}
			
			// place a unit at that node
			return degNodes.get(mIndex);
			
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
						adjThreat[i] = toPlace.get(i).getAdjThreat(range);
					
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
					// place a unit at an unowned node, starting with the ones with the least adjacency
					for (int i = 2; i < 6; i++) {
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
							int[] adjThreat = new int[degNodes.size()];
							
							// fill the array
							for (int j = 0; j < degNodes.size(); j++)
								adjThreat[j] = degNodes.get(j).getAdjThreat(range);
							
							// find the index of the node with the least adjacency threat
							int min = adjThreat[0];
							int mIndex = 0;
							for (int j = 0; j < degNodes.size(); j++) {
								if (adjThreat[j] < min) {
									min = adjThreat[j];
									mIndex = j;
								}
							}
							
							// place a unit at that node
							return degNodes.get(mIndex);
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
				int mIndex = 0;
				for (int i = 0; i < nodes.size(); i++) {
					int threat = nodes.get(i).getThreat(range);
					if (threat > max) {
						max = threat;
						mIndex = i;
					}
				}
				
				// place the unit at the most threatened node
				return nodes.get(mIndex);
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
		Node[] nodesArray = graph.getOwnedNodes(this).toArray(new Node[0]);
		
		// sort the array of nodes based on their threat levels
		Arrays.sort(nodesArray);
		
		// turn it back into an ArrayList to make other methods easier
		ArrayList<Node> nodes = new ArrayList<Node>(Arrays.asList(nodesArray));
		
		/*
		 * ATTACK STEP
		 */
		
		// iterate through the owned nodes and attack adjacent enemy nodes with fewer units
		int index = 0;
		while (index < nodes.size()) {
			Node node = nodes.get(index); 
			
			// get the nodes adjacent to this one
			ArrayList<Node> adjNodes = node.getAdj();
			
			// iterate through the enemy nodes
			for (Node adjNode : adjNodes) {
				// while we have units and while the adjacent node belongs to the enemy and has fewer units than this one, attack
				while (node.getUnits() > 1 && adjNode.getOwner() != this && adjNode.getUnits() <= node.getUnits()) {
					attack(node, adjNode, Math.max(node.getUnits()-1, 3));
					
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
		int maxDif = 0;
		int mIndex = 0;
		for (Node node : nodes) {
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
			
			// 
		}
		
		return;
	}
}
