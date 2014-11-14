/**
 * @author Graham Netherton
 *
 */
public abstract class Player {

	private
		Graph graph;
	
	/**
	 * Constructor for the Player class.
	 */
	public Player(Graph g) {
		graph = g;
	}
	
	public boolean attack(Node from, Node to, int num) {
		
		return true;
	}
	
	/**
	 * Fortifies one node using units from another. Returns a string containing
	 * the error if something went wrong, or null if nothing did.
	 * @param from Node to transfer from.
	 * @param to Node to transfer to.
	 * @param num Number of units to transfer.
	 */
	public String fortify(Node from, Node to, int num) {
		String error = null;
		// one of the nodes isn't owned by this player
		if (from.getPlayer() != this)
			error = "FORTIFY ERROR: Tried to transfer from an unowned node!";
		else if (to.getPlayer() != this)
			error = "FORTIFY ERROR: Tried to transfer to an unowned node!";
		// more units are being transferred than are available
		else if (num >= from.getUnits())
			error = "FORTIFY ERROR: Tried to transfer too many units!";
		// a negative number of units are being transferred
		else if (num < 0)
			error = "FORTIFY ERROR: Tried to transfer a negative number of units!";
		// the nodes aren't adjacent
		else if (from.getAdjacent().contains(to) == false)
			error = "FORTIFY ERROR: Tried to transfer to a non-adjacent node!";
		// no errors - move the units
		else
			graph.moveUnits(from, to, num);
		
		return error;
	}
	
	/**
	 * To be implemented by each individual AI. This function should contain
	 * the AI's actual decision-making process.
	 */
	abstract void turn();
}