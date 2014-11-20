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
		// determine if this is the first time we're placing a unit
		if (graph.getNumOwnedNodes(this) == 0) {
			
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
				
				// did we find any candidates?
				if (toPlace.size() >= 1) {
					// if so, place a unit at the first node we found
					return toPlace.get(0);
				}
				// did we find no candidates?
				else {
					// place a unit at an unowned node
					return graph.getOwnedNodes(null).get(0);
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
		// TODO Auto-generated method stub
	}

}
