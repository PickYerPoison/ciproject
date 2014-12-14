import java.util.ArrayList;
import java.util.Random;

/**
 * @author Graham
 *
 */
public class EvolvingPlayer extends Player {
	
	// strategy preferences
	// aggressive, balanced, defensive, wall
	double[] defendStrats = {0, -1, -1, -1};
	double[] occupyStrats = {0, 0, 0, -1};
	double[] placeStrats = {0, 0, 0, 0};
	double[] turnStrats = {0, 0, 0, -1};
	double[][] masterStrats = {defendStrats, occupyStrats, placeStrats, turnStrats};
	
	// current strategy list
	// defend, occupy, place, turn
	int[] currentStrats = {0, 0, 0, 0};
	
	// mutation stuff
	double learningRate = 0.01;
	double mutationRate = 3;
	int prevGamesWon = 0;
	
	/**
	 * 
	 */
	public EvolvingPlayer() {
	}

	/**
	 * @param n
	 */
	public EvolvingPlayer(String n) {
		super(n);
	}
	
	public void resetAlleles() {
		defendStrats[0] = 0; defendStrats[1] = -1; defendStrats[2] = -1; defendStrats[3] = -1;
		occupyStrats[0] = 0; occupyStrats[1] = 0; occupyStrats[2] = 0; occupyStrats[3] = -1; 
		placeStrats[0] = 0; placeStrats[1] = 0; placeStrats[2] = 0; placeStrats[3] = 0;
		turnStrats[0] = 0; turnStrats[1] = 0; turnStrats[2] = 0; turnStrats[3] = -1;
	}
	
	/* (non-Javadoc)
	 * @see Player#toString()
	 */
	@Override
	public String toString() {
		String out = "";
		
		// make strategy data cleaner
		for (double[] strat : masterStrats) {
			for (int i = 0; i < strat.length; i++) {
				int n = (int)(strat[i]*100);
				strat[i] = n/100.0;
			}
		}
		
		// output strategy data
		for (int i = 0; i < masterStrats.length; i++) {
			// output the name of the strat
			switch (i) {
				case 0: out += "      Defend Strats: {"; break;
				case 1: out += "      Occupy Strats: {"; break;
				case 2: out += "       Place Strats: {"; break;
				case 3: out += "        Turn Strats: {"; break;
			}
			
			// output the values of the strats
			for (int j = 0; j < masterStrats[i].length; j++) {
				int n = (int)(masterStrats[i][j]*100);
				out += n/100.0;
				if (j < masterStrats[i].length-1)
					out += ", ";
			}
			
			// close the list
			out += "}\n";
		}
		
		out += super.toString();
		
		return out;
	}
	
	public void gameStart() {
		// did we win the last game?
		boolean won = false;
		if (prevGamesWon < get("GAMES_WON")) {
			won = true;
			prevGamesWon++;
		}
		
		// if we did, add rand(1)*learningRate to
		if (won) {
			Random rand = new Random();
			
			// iterate through the current strats
			for (int i = 0; i < currentStrats.length; i++) {
				int strat = currentStrats[i];
				masterStrats[i][strat] += rand.nextDouble()*learningRate;
			}
		}
		
		// select strats for the next game; iterate through currentStrats
		for (int i = 0; i < currentStrats.length; i++) {
			Random rand = new Random();
			
			// create a new array to hold the mutated values
			double[] selection = new double[masterStrats[i].length];
			
			// mutate the values if they're positive
			for (int j = 0; j < selection.length; j++) {
				if (masterStrats[i][j] >= 0)
					selection[j] = masterStrats[i][j] + rand.nextDouble()*mutationRate;
				else
					selection[j] = 0;
			}
			
			// select the highest value
			double highest = 0;
			int highestIndex = 0;
			
			// find the index with the highest value
			for (int k = 0; k < selection.length; k++) {
				if (selection[k] > highest) {
					highest = selection[k];
					highestIndex = k;
				}
			}
			currentStrats[i] = highestIndex;
		}
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
		switch (currentStrats[1]) {
			case 0: return aggOccupy(from, to);
			case 1: return balOccupy(from, to);
			case 2: return defOccupy(from, to);
			case 3: return walOccupy(from, to);
			default: return 1;
		}
	}

	/* (non-Javadoc)
	 * @see Player#place()
	 */
	@Override
	Node place() {
		switch (currentStrats[2]) {
			case 0: return aggPlace();
			case 1: return balPlace();
			case 2: return defPlace();
			case 3: return walPlace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see Player#turn()
	 */
	@Override
	void turn() {
		switch (currentStrats[3]) {
			case 0: aggTurn();
			case 1: balTurn();
			case 2: defTurn();
			case 3: walTurn();
		}
		return;
	}
	
	int aggOccupy(Node from, Node to) {
		// move the max number of units to the newly captured node
		return from.getUnits()-1;
	}
	
	int balOccupy(Node from, Node to) {
		// keep half the units (round down)
		return from.getUnits() - (from.getUnits()/2);
	}
	
	int defOccupy(Node from, Node to) {
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
	
	int walOccupy(Node from, Node to) {
		// keep half the units (round down)
		return from.getUnits() - (from.getUnits()/2);
	}
	
	Node aggPlace() {
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
				
				// eliminate nodes not adjacent to enemy nodes
				int index = 0;
				while (index < nodes.size()) {
					boolean adjEnemy = false;
					
					// check all adjacent nodes
					for (Node node : nodes.get(index).getAdj()) {
						// if the node belongs to an enemy player, this node is valid
						if (node.getOwner() != this) {
							adjEnemy = true;
							break;
						}
					}
					
					// remove the node or check the next one
					if (adjEnemy)
						index++;
					else
						nodes.remove(index);
				}
				
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
	
	Node balPlace() {
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
	
	Node defPlace() {
		// determine if this is the first time we're placing a unit
		if (graph.getNumOwnedNodes(this) == 0) {
			// place the first unit down in the most defensive node (lowest degree)
			// choose the one with the least adjacency threat
			ArrayList<Node> degNodes = new ArrayList<Node>(0);
			
			for (int i = 2; i <= 6; i++) {
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
				
				// eliminate nodes not adjacent to enemy nodes
				int index = 0;
				while (index < nodes.size()) {
					boolean adjEnemy = false;
					
					// check all adjacent nodes
					for (Node node : nodes.get(index).getAdj()) {
						// if the node belongs to an enemy player, this node is valid
						if (node.getOwner() != this) {
							adjEnemy = true;
							break;
						}
					}
					
					// remove the node or check the next one
					if (adjEnemy)
						index++;
					else
						nodes.remove(index);
				}
				
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
	
	Node walPlace() {
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
				
				// prune nodes that aren't adjacent to the enemy
				int index = 0;
				while (index < nodes.size()) {
					// get the adjacent nodes
					ArrayList<Node> adjNodes = nodes.get(index).getAdj();
					
					// see if any of the adjacent nodes belong to the enemy
					boolean hasAdjEnemy = false;
					for (Node node : adjNodes) {
						if (node.getOwner() != this) {
							hasAdjEnemy = true;
							break;
						}
					}
					
					// keep the node if it does
					if (hasAdjEnemy)
						index++;
					else
						nodes.remove(index);
				}
				
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
	
	void aggTurn() {
		// get all owned nodes
		ArrayList<Node> nodes = graph.getOwnedNodes(this);
		
		/*
		 * ATTACK STEP
		 */
		
		// sort nodes in order of decreasing number of units
		boolean sorted = false;
		while (!sorted) {
			sorted = true;
			for (int i = 0; i < nodes.size() - 1; i++) {
				// compare this node to the next one
				if (nodes.get(i).getUnits() < nodes.get(i+1).getUnits()) {
					sorted = false;
					
					// move this node to the end of the list
					nodes.add(i+2, nodes.get(i));
					nodes.remove(i);
				}
			}
		}
		
		// iterate through the owned nodes and attack adjacent enemy nodes
		// start from own node with most units and attack enemy nodes in increasing order of number of units
		int index = 0;
		while (index < nodes.size()) {
			Node node = nodes.get(index); 
			
			// get the nodes adjacent to this one
			ArrayList<Node> adjNodes = node.getAdj();
			
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
			
			// sort nodes in order of increasing number of units
			sorted = false;
			while (!sorted) {
				sorted = true;
				for (int i = 0; i < adjNodes.size() - 1; i++) {
					// compare this node to the next one
					if (adjNodes.get(i).getUnits() > adjNodes.get(i+1).getUnits()) {
						sorted = false;
						
						// move this node to the end of the list
						adjNodes.add(i+2, adjNodes.get(i));
						adjNodes.remove(i);
					}
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
	
	void balTurn() {
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
	
	void defTurn() {
		// get all owned nodes
		ArrayList<Node> nodes = graph.getOwnedNodes(this);
		
		/*
		 * ATTACK STEP
		 */

		// sort nodes in order of increasing number of units
		boolean sorted = false;
		while (!sorted) {
			sorted = true;
			for (int i = 0; i < nodes.size() - 1; i++) {
				// compare this node to the next one
				if (nodes.get(i).getUnits() < nodes.get(i+1).getUnits()) {
					sorted = false;
					
					// move this node to the end of the list
					nodes.add(i+2, nodes.get(i));
					nodes.remove(i);
				}
			}
		}
		
		// iterate through the owned nodes and attack adjacent enemy nodes with fewer units
		int index = 0;
		while (index < nodes.size()) {
			Node node = nodes.get(index); 
			
			// get the nodes adjacent to this one
			ArrayList<Node> adjNodes = node.getAdj();
			
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
			
			// sort nodes in order of increasing number of units
			sorted = false;
			while (!sorted) {
				sorted = true;
				for (int i = 0; i < adjNodes.size() - 1; i++) {
					// compare this node to the next one
					if (adjNodes.get(i).getUnits() > adjNodes.get(i+1).getUnits()) {
						sorted = false;
						
						// move this node to the end of the list
						adjNodes.add(i+2, adjNodes.get(i));
						adjNodes.remove(i);
					}
				}
			}
			
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
	
	void walTurn() {
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