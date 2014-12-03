import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * @author Graham Netherton
 *
 */
public class Game {
	public
		final int TOTAL_GAMES = 0;
		final int TOTAL_TURNS = 1;
		final int MIN_TURNS = 2;
		final int MAX_TURNS = 3;
		final int TIES = 4;
	
	private
		Graph graph;
		ArrayList<Player> players;
		int telemetrySize = 5;
		int[] telemetry = new int[telemetrySize];
	
	/**
	 * Constructor for the Game class.
	 */
	public Game() {
		graph = new Graph();
		players = new ArrayList<Player>(0);
		Arrays.fill(telemetry, 0);
		telemetry[MIN_TURNS] = 99999999;
	}
	
	public Graph getGraph() {
		return graph;
	}
	
	public ArrayList<Player> getPlayers() {
		return players;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String out = "";
		
		out += "GAME STATISTICS\n";
		
		for (int i = 0; i < telemetrySize; i++) {
			switch (i) {
				case TOTAL_GAMES:	out += "   Total games: " + telemetry[i] + "\n"; break;
				case TOTAL_TURNS:	out += "Avg turns/game: " + (telemetry[i]/telemetry[TOTAL_GAMES]) + "\n"; break;
				case MIN_TURNS: 	out += " Shortest game: " + telemetry[i] + " turns\n"; break;
				case MAX_TURNS:		out += "  Longest game: " + telemetry[i] + " turns\n"; break;
				case TIES:			out += " Game timeouts: " + telemetry[i] + "\n"; break;
			}
		}
		
		// output summaries for each player
		out += "------------------------------\n";
		out += "     AVERAGE PLAYER STATS\n";
		for (Player player : players) {
			out += "------------------------------\n";
			out += "  Player " + player.getName() + "\n";
			out += player;
		}
		
		return out;
	}
	
	/**
	 * Adds a player to the game.
	 * @param player The player to add.
	 */
	public void addPlayer(Player player) {
		players.add(player);
		player.setGraph(graph);
		return;
	}
	
	public void randomizeStart() {
		// create rng
		Random r = new Random();
		
		// get the new starting player
		Player startPlayer = players.get(r.nextInt(players.size()));
		
		// move all players to be behind the starting player
		while (players.get(0) != startPlayer) {
			// move the player at the start of the turn order to the back
			players.add(players.get(0));
			players.remove(0);
		}
		
		// trim the playerlist's size
		players.trimToSize();
		
		return;
	}
	
	/**
	 * Sets up the Risk map for use by connecting nodes until there are a specified
	 * number of nodes of each degree (specifying degrees 2 through 6).
	 * @param map Integer array of integer arrays specifying what to link each node to.
	 */
	public void setupMap(int[][] map) {
		// add all the nodes
		for (@SuppressWarnings("unused") int[] x : map) {
			graph.addNode();
		}
		
		// link the nodes
		int index = 0;
		for (Node node : graph.getNodes()) {
			for (int x : map[index])
				node.addAdj(graph.getNodes().get(x));
			index++;
		}
	}

	/**
	 * Runs the game. The game map and the player list should already be set up!
	 * @param unloadPlayers Boolean specifying whether the list of players should be reset afterwards.
	 * @param unloadMap Boolean specifying whether the graph of nodes should be reset afterwards.
	 */
	public void runGame(int maxTurns, boolean unloadPlayers, boolean unloadMap) {
		// add players. if there's an error somewhere, inPlay is set to 0.
		int inPlay = players.size();
		
		// determine how many units each player will start with
		int units = 0;
		switch (inPlay) {
			case 3: units = 35; break;
			case 4: units = 30; break;
			case 5: units = 25; break;
			case 6: units = 20; break;
			default: inPlay = 0; System.out.println("SETUP ERROR: Invalid number of players!");
		}
		
		// have the players distribute their units
		for (int i = 0; i < units; i++) {
			for (Player player : players)
				graph.placeUnit(player.place(), player);
		}
		
		// turns in game thus far
		int turns = 0;
		
		// cycle through players
		while (inPlay > 1) {			
			// iterate through the players
			for (Player player : players) {
				// don't execute a player's turn if they've lost
				if (player.hasLost() == false) {
					// have the player place new units
					int newUnits = graph.getNumOwnedNodes(player)/3;
					for (int i = 0; i < newUnits; i++)
						graph.placeUnit(player.place(), player);
					
					// call the player's turn function
					player.turn();

					// increase the player's turn counter
					player.set("TURNS", player.get("TURNS") + 1);
				}
			}
			
			// see how many players are still in play
			inPlay = 0;
			for (Player player : players)
				if (!player.hasLost())
					inPlay++;
			
			// increase the turn counter
			turns++;
			
			// break out of the loop if maxTurns was exceeded
			if (turns > maxTurns)
				break;
		}
		
		telemetry[TOTAL_GAMES]++;
		telemetry[TOTAL_TURNS] += (turns - 1);
		if (turns > telemetry[MAX_TURNS])
			telemetry[MAX_TURNS] = turns-1;
		if (turns < telemetry[MIN_TURNS])
			telemetry[MIN_TURNS] = turns-1;
		
		// update winner telemetry
		if (inPlay == 1) {
			for (Player player : players) {
				if (player.hasLost() == false)
					player.set("GAMES_WON", player.get("GAMES_WON")+1);
			}
		}
		else
			telemetry[TIES]++;
		
		// update player telemetry
		for (Player player : players) {
			if (player.hasLost())
				player.set("GAMES_LOST", player.get("GAMES_LOST") + 1);
		}
		
		// unload any specified assets
		if (unloadPlayers) {
			players.clear();
			players.trimToSize();
		}
		if (unloadMap)
			graph.clear();
		else
			graph.restore();
	}
	
	/**
	 * Overloaded runGame() that doesn't unload anything afterwards.
	 */
	public void runGame() {
		runGame(500, false, false);
	}
}