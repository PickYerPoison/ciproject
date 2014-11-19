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

	protected
		Graph graph = null;
		String name = "Default";
		int range = 3;
		int telemetrySize = 9;
		int[] telemetry = new int[telemetrySize];
		final int ATTACKS = 0;
		final int NODES_WON = 1;
		final int NODES_LOST = 2;
		final int PLAYERS_KILLED = 3;
		final int UNITS_KILLED = 4;
		final int UNITS_LOST = 5;
		final int TURNS = 6;
		final int GAMES_WON = 7;
		final int GAMES_LOST = 8;
	
	/**
	 * Zero argument constructor for the Player class. 
	 */
	public Player() {
		Arrays.fill(telemetry, 0);
	}
	
	/**
	 * Constructor for the Player class that sets the name. 
	 * @param n The name to give the player.
	 */
	public Player(String n) {
		Arrays.fill(telemetry, 0);
		name = n;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String out = "";
		for (int i = 0; i < telemetrySize; i++) {
			double totalGames = telemetry[GAMES_WON] + telemetry[GAMES_LOST];
			switch (i) {
				case ATTACKS: 		out += "       Attacks made: " + (telemetry[i]/totalGames) + "\n"; break;
				case NODES_WON: 	out += "          Nodes won: " + (telemetry[i]/totalGames) + "\n"; break;
				case NODES_LOST:	out += "         Nodes lost: " + (telemetry[i]/totalGames) + "\n"; break;
				case PLAYERS_KILLED:out += "     Players killed: " + (telemetry[i]/totalGames) + "\n"; break;
				case UNITS_KILLED: 	out += "       Units killed: " + (telemetry[i]/totalGames) + "\n"; break;
				case UNITS_LOST:	out += "         Units lost: " + (telemetry[i]/totalGames) + "\n"; break;
				case TURNS: 		out += "     Turns survived: " + ((telemetry[i]-1)/totalGames) + "\n"; break;
				case GAMES_WON: {	double ratio = (telemetry[GAMES_WON]/(double)(telemetry[GAMES_LOST]));
									ratio *= 100;
									int r = (int)(ratio);
									out += "          W/L ratio: " + (r/100.0) + "\n";
									break; }
			}
		}
		return out;
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
	 * Gets a piece of telemetry data.
	 * @param index Integer index of the data to get.
	 * @return The requested telemetry data.
	 */
	public int getTelemetry(int index) {
		// don't run off the array if a bad value is given
		if (0 < index && index < telemetrySize)
			return telemetry[index];
		else
			return 0;
	}
	
	/**
	 * Sets the player's name.
	 * @param n The player's new name.
	 */
	public void setName(String n) {
		name = n;
		return;
	}
	
	/**
	 * Sets the player's associated graph.
	 * @param g The graph to associate the player with. 
	 */
	public void setGraph(Graph g) {
		graph = g;
		return;
	}
	
	/**
	 * Allows telemetry data to be set.
	 * @param index Integer index of the data to set.
	 * @param value The value to set the data to.
	 */
	public void setTelemetry(int index, int value) {
		telemetry[index] = value;
		return;
	}
	
	/**
	 * Outputs an error message to the console.
	 * @param error A string containing the error message, or null for no message.
	 */
	public void showError(String error) {
		if (error != null)
			System.out.println("Player " + name + " says " + error);
		return;
	}
	
	/**
	 * Determines if this player has lost.
	 * @return True if the player has lost, false if they haven't.
	 */
	public boolean hasLost() {
		if (graph.getNumOwnedNodes(this) == 0)
			return true;
		else
			return false;
	}
	
	/**
	 * Attacks an unowned node using units from an owned one. Returns a boolean
	 * indicating if an error occurred. Call this any number of times from turn(). 
	 * @param from The node the attack is originating from.
	 * @param to The node the attack is being made towards.
	 * @param num The number of units being used to attack.
	 * @return True if there was an error, false if there wasn't.
	 */
	public boolean attack(Node from, Node to, int num) {
		boolean error = true;
		// from node is null
		if (from == null)
			showError("ATTACK ERROR: Tried to attack from null instead of a node!");
		// to node is null
		else if (to == null)
			showError("ATTACK ERROR: Tried to attack null instead of a node!");
		// from node isn't owned by this player
		else if (from.getOwner() != this)
			showError("ATTACK ERROR: Tried to attack from an unowned node!");
		// to node is owned by this player
		else if (to.getOwner() == this)
			showError("ATTACK ERROR: Tried to attack an owned node!");
		// more units are being transferred than are available or allowed
		else if (num >= from.getUnits() || num > 3)
			showError("ATTACK ERROR: Tried to attack with too many units!");
		// no units being used to attack
		else if (num == 0)
			showError("ATTACK ERROR: Tried to attack with zero units!");
		// negative number of units are being used to attack
		else if (num < 0)
			showError("ATTACK ERROR: Tried to attack with a negative number of units!");
		// nodes aren't adjacent
		else if (from.getAdj().contains(to) == false)
			showError("ATTACK ERROR: Tried to attack a non-adjacent node!");
		// no errors - request that the defender defend
		else {
			// get the number of defending units
			int defense = to.getOwner().defend(from, to, num);
			
			// too many units being used to defend
			if (defense > 2 || defense > to.getUnits())
				showError("DEFEND ERROR: Tried to defend with too many units!");
			// no units being used to defend
			else if (defense == 0)
				showError("DEFEND ERROR: Tried to defend with zero units!");
			// negative number of units being used to defend
			else if (defense < 0)
				showError("DEFEND ERROR: Tried to defend with a negative number of units!");
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
					// subtract defender wins from units available to move
					from.addUnits(-defenderWins);
					
					// transfer ownership
					Player oldOwner = to.getOwner();
					to.setOwner(this);
					
					// check how many units to move
					int move = occupy(from, to);
					
					// reverse changes, in case an error occurs
					from.addUnits(defenderWins);
					to.setOwner(oldOwner);
					
					// too many units being moved
					if (move >= from.getUnits())
						showError("OCCUPY ERROR: Tried to move too many units!");
					// no units being moved
					else if (move == 0)
						showError("OCCUPY ERROR: Tried to move zero units!");
					// negative number of units being moved
					else if (move < 0)
						showError("OCCUPY ERROR: Tried to move a negative number of units!");
					// no errors - exchange ownership and units
					else {
						telemetry[ATTACKS]++;
						telemetry[NODES_WON]++;
						telemetry[UNITS_KILLED] += attackerWins;
						telemetry[UNITS_LOST] += defenderWins;
						to.getOwner().telemetry[NODES_LOST]++;
						to.getOwner().telemetry[UNITS_KILLED] += defenderWins;
						to.getOwner().telemetry[UNITS_LOST] += attackerWins;
						if (graph.getNumOwnedNodes(to.getOwner()) == 1)
							telemetry[PLAYERS_KILLED]++;
						to.setOwner(this);
						to.setUnits(0);
						from.addUnits(-defenderWins);
						graph.moveUnits(from, to, move);
						error = false;
					}
				}
				// otherwise, just process unit losses
				else {
					telemetry[ATTACKS]++;
					telemetry[UNITS_KILLED] += attackerWins;
					telemetry[UNITS_LOST] += defenderWins;
					to.getOwner().telemetry[UNITS_KILLED] += defenderWins;
					to.getOwner().telemetry[UNITS_LOST] += attackerWins;
					from.addUnits(-defenderWins);
					to.addUnits(-attackerWins);
					error = false;
				}
			}
		}
		return error;
	}
	
	/**
	 * Fortifies one node using units from another. Returns a boolean indicating
	 * if there was an error. Call this function once at the end of turn().
	 * @param from Node to transfer from.
	 * @param to Node to transfer to.
	 * @param num Number of units to transfer.
	 * @return True if there was an error, false if there wasn't.
	 */
	public boolean fortify(Node from, Node to, int num) {
		boolean error = true;
		// one of the nodes is null
		if (from == null)
			showError("FORTIFY ERROR: Tried to transfer from null instead of a node!");
		else if (to == null)
			showError("FORTIFY ERROR: Tried to transfer to null instead of a node!");
		// one of the nodes isn't owned by this player
		else if (from.getOwner() != this)
			showError("FORTIFY ERROR: Tried to transfer from an unowned node!");
		else if (to.getOwner() != this)
			showError("FORTIFY ERROR: Tried to transfer to an unowned node!");
		// the two nodes are the same
		if (to == from)
			showError("FORTIFY ERROR: Tried to transfer units to the same node!");
		// more units being transferred than are available
		else if (num >= from.getUnits())
			showError("FORTIFY ERROR: Tried to transfer too many units!");
		// no units being transferred
		else if (num == 0)
			showError("FORTIFY ERROR: Tried to transfer zero units!");
		// negative number of units being transferred
		else if (num < 0)
			showError("FORTIFY ERROR: Tried to transfer a negative number of units!");
		// nodes aren't adjacent
		else if (from.getAdj().contains(to) == false)
			showError("FORTIFY ERROR: Tried to transfer to a non-adjacent node!");
		// no errors - move the units
		else {
			graph.moveUnits(from, to, num);
			error = false;
		}
		
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