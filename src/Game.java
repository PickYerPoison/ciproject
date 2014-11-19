import java.util.ArrayList;

/**
 * @author Graham Netherton
 *
 */
public class Game {

	private
		Graph graph;
		ArrayList<Player> players;
	
	/**
	 * Constructor for the Game class.
	 */
	public Game() {
		graph = new Graph();
		players = new ArrayList<Player>(0);
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
	public void runGame(boolean unloadPlayers, boolean unloadMap) {
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
			for (Player player : players) {
				System.out.println("Player " + player.getName() + " placing unit " + (i + 1) + ".");
				graph.placeUnit(player.place(), player);
			}
		}
		
		// turns in game thus far
		int turns = 0;
		int maxTurns = 500;
		
		// cycle through players
		while (inPlay > 1) {
			// Output to console
			System.out.println("Starting turn " + turns + "...");
			
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
					player.setTelemetry(6, player.getTelemetry(6)+1);
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
			if (turns > maxTurns) {
				System.out.println("Maximum turns exceeded. Ending game...");
				break;
			}
		}

		System.out.println("The game took " + (turns - 1) + " turns.");
		
		// declare the winner if no error occurred
		if (inPlay == 1) {
			for (Player player : players) {
				if (player.hasLost() == false) {
					System.out.println("The winner of the match is " + player.getName() + ".");
				}
			}
		}
		else
			System.out.println(inPlay + " players remain. The match is a tie.");
		
		// output summaries for each player
		for (Player player : players) {
			System.out.println("-------------------");
			System.out.println("Player " + player.getName() + (player.hasLost() ? " LOST." : " SURVIVED."));
			System.out.print(player);
			if (!player.hasLost()) {
				System.out.println("        Owned nodes: " + graph.getNumOwnedNodes(player));
				int totalUnits = 0;
				for (Node node : graph.getOwnedNodes(player))
					totalUnits += node.getUnits();
				System.out.println("         Units left: " + totalUnits);
			}
		}
		
		// unload any specified assets
		if (unloadPlayers) {
			players.clear();
			players.trimToSize();
		}
		if (unloadMap) {
			graph.clear();
		}
	}
	
	/**
	 * Overloaded runGame() that doesn't unload anything afterwards.
	 */
	public void runGame() {
		runGame(false, false);
	}
}