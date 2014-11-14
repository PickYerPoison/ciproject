import java.util.Random;
import java.util.Arrays;

/**
 * The Player class represents the players in the game. The game iterates through
 * the players and calls the turn() function of each.
 * 
 * @author Graham Netherton
 *
 */
public abstract class Player {

	private
		Graph graph;
		String name = "Default";
	
	/**
	 * Constructor for the Player class.
	 * @param g The graph to link this player to.
	 */
	public Player(Graph g) {
		graph = g;
	}
	
	/**
	 * Extended constructor for the Player class.
	 * @param g The graph to link this player to.
	 * @param n The player's name.
	 */
	public Player(Graph g, String n) {
		graph = g;
		name = n;
	}
	
	/**
	 * Returns the AI's personal name. Used for identifying what code each AI is
	 * running.
	 * @return The AI's personal name as a string.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Attacks an unowned node using units from an owned one. Returns a string
	 * containing the error if something went wrong, or null if nothing did. Call
	 * this any number of times from turn(). 
	 * @param from The node the attack is originating from.
	 * @param to The node the attack is being made towards.
	 * @param num The number of units being used to attack.
	 * @return A string containing the error message, or null if no error.
	 */
	public String attack(Node from, Node to, int num) {
		String error = null;
		// from node isn't owned by this player
		if (from.getPlayer() != this)
			error = "ATTACK ERROR: Tried to attack from an unowned node!";
		// to node is owned by this player
		else if (to.getPlayer() == this)
			error = "ATTACK ERROR: Tried to attack an owned node!";
		// more units are being transferred than are available or allowed
		else if (num >= from.getUnits() || num > 3)
			error = "ATTACK ERROR: Tried to attack with too many units!";
		// no units being used to attack
		else if (num == 0)
			error = "ATTACK ERROR: Tried to attack with zero units!";
		// negative number of units are being used to attack
		else if (num < 0)
			error = "ATTACK ERROR: Tried to attack with a negative number of units!";
		// nodes aren't adjacent
		else if (from.getAdjacent().contains(to) == false)
			error = "ATTACK ERROR: Tried to attack a non-adjacent node!";
		// no errors - request that the defender defend
		else {
			// get the number of defending units
			int defense = to.getPlayer().defend(from, to, num);
			
			// too many units being used to defend
			if (defense > 2 || defense > to.getUnits())
				error = "DEFEND ERROR: Tried to defend with too many units!";
			// no units being used to defend
			else if (defense == 0)
				error = "DEFEND ERROR: Tried to defend with zero units!";
			// negative number of units being used to defend
			else if (defense < 0)
				error = "DEFEND ERROR: Tried to defend with a negative number of units!";
			else {
				// create the RNG for rolling dice
				Random rand = new Random();
				
				// generate rolls
				int[] defendRolls = new int[defense];
				int[] attackRolls = new int[num];
				for (int i = 0; i < defense; i++) {
					defendRolls[i] = rand.nextInt(6);
				}
				Arrays.sort(defendRolls);
				for (int i = 0; i < num; i++) {
					attackRolls[i] = rand.nextInt(6);
				}
				Arrays.sort(attackRolls);
				
				// compare rolls
				int attackerWins = 0;
				int defenderWins = 0;
				
				for (int i = 0; i < Math.min(defense, num); i++) {
					// on a tie, defender wins
					if (attackRolls[i] > defendRolls[i])
						attackerWins++;
					else
						defenderWins++;
				}
				
				// see if the attacker won
				if (to.getUnits() - attackerWins <= 0) {
					// check how many units to move
					int move = occupy(from, to);
					
					// too many units being moved
					if (move >= from.getUnits())
						error = "OCCUPY ERROR: Tried to move too many units!";
					// no units being moved
					else if (move == 0)
						error = "OCCUPY ERROR: Tried to move zero units!";
					// negative number of units being moved
					else if (move < 0)
						error = "OCCUPY ERROR: Tried to move a negative number of units!";
					// no errors - exchange ownership and units
					else {
						to.setPlayer(this);
						to.addUnits(-to.getUnits());
						from.addUnits(-defenderWins);
						graph.moveUnits(from, to, move);
					}
				}
				// otherwise, just process unit losses
				else {
					from.addUnits(-defenderWins);
					to.addUnits(-attackerWins);
				}
			}
		}
		return error;
	}
	
	/**
	 * Fortifies one node using units from another. Returns a string containing
	 * the error if something went wrong, or null if nothing did. Call this
	 * function once at the end of turn().
	 * @param from Node to transfer from.
	 * @param to Node to transfer to.
	 * @param num Number of units to transfer.
	 * @return A string containing the error message, or null if no error.
	 */
	public String fortify(Node from, Node to, int num) {
		String error = null;
		// one of the nodes isn't owned by this player
		if (from.getPlayer() != this)
			error = "FORTIFY ERROR: Tried to transfer from an unowned node!";
		else if (to.getPlayer() != this)
			error = "FORTIFY ERROR: Tried to transfer to an unowned node!";
		// more units being transferred than are available
		else if (num >= from.getUnits())
			error = "FORTIFY ERROR: Tried to transfer too many units!";
		// no units being transferred
		else if (num == 0)
			error = "FORTIFY ERROR: Tried to transfer zero units!";
		// negative number of units being transferred
		else if (num < 0)
			error = "FORTIFY ERROR: Tried to transfer a negative number of units!";
		// nodes aren't adjacent
		else if (from.getAdjacent().contains(to) == false)
			error = "FORTIFY ERROR: Tried to transfer to a non-adjacent node!";
		// no errors - move the units
		else
			graph.moveUnits(from, to, num);
		
		return error;
	}
	
	/**
	 * To be implemented by each individual AI. This function should contain
	 * the AI's logic for deciding how many units to use when defending against
	 * an attack. 
	 * @param from The node the attack is originating from.
	 * @param to The node the attack is being made towards.
	 * @param num The number of units being used to attack.
	 * @return The number of units being used to defend.
	 */
	abstract int defend(Node from, Node to, int num);
	
	/**
	 * To be implemented by each individual AI. This function should contain
	 * the AI's logic for deciding how many units to move into a new node that
	 * is gained from a successful attack.
	 * @param from The node the units are moving from.
	 * @param to The node the units are moving to.
	 * @return The number of units to move.
	 */
	abstract int occupy(Node from, Node to);
	
	/**
	 * To be implemented by each individual AI. This function should contain
	 * the AI's logic for deciding where to place new units. It should be able
	 * to function in all of these conditions: no units in any of the nodes,
	 * units in some of the nodes, and units in all of the nodes.
	 * @return The node where the unit should be placed.
	 */
	abstract Node place();
	
	/**
	 * To be implemented by each individual AI. This function should contain
	 * the AI's actual decision-making process. The player should never call
	 * this function by itself; it will always be called externally.
	 */
	abstract void turn();
}