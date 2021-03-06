import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.Formula;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * @author Graham
 *
 */
public class Main {
	/**
	 * @param args
	 */
	public static void main(String[] args) throws BiffException, IOException, WriteException {
		Game game = new Game();

		// set up the game map
		int[][] riskMap = { {1, 3, 23}, {0, 2, 3, 4}, {1, 4, 5, 13}, {0, 1, 4, 6}, {1, 2, 3, 5, 7}, 
				{2, 4, 7}, {3, 7, 8}, {4, 5, 6, 8}, {6, 7, 9}, {8, 10, 11}, {9, 11, 12}, {9, 10, 12, 33}, 
				{10, 11}, {2, 14, 15}, {13, 15, 16, 17}, {13, 14, 16, 18}, {14, 15, 17, 18, 19}, 
				{14, 16, 19, 20, 25, 28}, {15, 16, 19, 33}, {16, 17, 18, 28, 32, 33}, {17, 21, 25, 29}, 
				{20, 22, 24, 26, 29}, {21, 23, 24}, {0, 22, 24, 26, 27}, {21, 22, 23, 26}, {17, 20, 28, 29, 30}, 
				{21, 23, 24, 27, 29}, {23, 26}, {17, 19, 25, 30, 32, 34}, {20, 21, 25, 26, 30, 31}, 
				{25, 28, 29, 31}, {29, 30, 38}, {19, 28, 33, 34}, {11, 18, 19, 32, 34, 35}, {28, 32, 33, 35, 37}, 
				{33, 34, 36}, {35, 37}, {34, 36}, {31, 39, 40}, {38, 40, 41}, {38, 39, 41}, {39, 40} };
		
		game.setupMap(riskMap);
		
		// do each mutation/learning rate thing
		// L = 0.01, M from 3 to 10 (25 runs)
		// M = 3, L from 0.12 to 3 (25 runs)
		// also, record it
		
		// open the spreadsheet
		WritableWorkbook alleleWorkbook;
		alleleWorkbook = Workbook.createWorkbook(new File("alleles.xls"));
		
		// create a new sheet
		WritableSheet awsheet = alleleWorkbook.createSheet("Data", 0);
		
		// add the column headers
		awsheet.addCell(new Label(0, 0, "M"));
		awsheet.addCell(new Label(1, 0, "Games"));
		awsheet.addCell(new Label(2, 0, "Wins"));
		awsheet.addCell(new Label(3, 0, "L"));
		awsheet.addCell(new Label(4, 0, "Games"));
		awsheet.addCell(new Label(5, 0, "Wins"));
		
		int yy = 0;
		
		for (double m = 0; m <= 5.2; m += 5/25.0) {
			game.setTelemetry(0, 0);
			System.out.println("m = " + m);
			// Add the players (between 3 and 6)
			game.addPlayer(new DefensivePlayer("Defensive"));
			game.addPlayer(new AggressivePlayer("Aggressive"));
			game.addPlayer(new BalancedPlayer("Balanced"));
			game.addPlayer(new WallPlayer("Wall"));
			EvolvingPlayer e = new EvolvingPlayer("Evolving");
			game.addPlayer(e);
			
			e.learningRate = 0.01;
			e.mutationRate = m;
			
			yy++;
			awsheet.addCell(new Number(0, yy, m));
			
			for (int i = 0; i < 10000; i++) {
				game.randomizeStart();
				game.runGame();
				
				// break out of the game if the evolving player has fully evolved
				boolean fullyEvolved = true;
				for (double[] strat : e.masterStrats) {
					double highest = 0;
					double lowest = 0;
					
					// find the highest and lowest values
					for (double val : strat) {
						if (val > highest)
							highest = val;
						else if (val >= 0 && val < lowest)
							lowest = val;
					}
					
					// if the highest and the lowest are equal, it is not fully evolved
					if (highest == lowest) {
						fullyEvolved = false;
						break;
					}
					
					// see if it exceeds all of the others by the mutation rate
					for (double val : strat) {
						if (val < highest && val >= 0 && val + e.mutationRate >= highest) {
							fullyEvolved = false;
							break;
						}
					}
					
					// break out of the loop if we're able to
					if (!fullyEvolved)
						break;
				}
				
				// finish the game if necessary
				if (fullyEvolved)
					break;
			}
			
			awsheet.addCell(new Number(1, yy, game.getTelemetry(0)));
			awsheet.addCell(new Number(2, yy, e.getTelemetry("GAMES_WON")));
			game.clearPlayers();
		}
		
		yy = 0;
		
		for (double l = 0; l <= 3.1; l += 3/25.0) {
			game.setTelemetry(0, 0);
			System.out.println("l = " + l);
			// Add the players (between 3 and 6)
			game.addPlayer(new DefensivePlayer("Defensive"));
			game.addPlayer(new AggressivePlayer("Aggressive"));
			game.addPlayer(new BalancedPlayer("Balanced"));
			game.addPlayer(new WallPlayer("Wall"));
			EvolvingPlayer e = new EvolvingPlayer("Evolving");
			game.addPlayer(e);
			
			e.learningRate = l;
			e.mutationRate = 3;
			
			yy++;
			awsheet.addCell(new Number(3, yy, l));
			
			for (int i = 0; i < 10000; i++) {
				game.randomizeStart();
				game.runGame();
				
				// break out of the game if the evolving player has fully evolved
				boolean fullyEvolved = true;
				for (double[] strat : e.masterStrats) {
					double highest = 0;
					double lowest = 0;
					
					// find the highest and lowest values
					for (double val : strat) {
						if (val > highest)
							highest = val;
						else if (val >= 0 && val < lowest)
							lowest = val;
					}
					
					// if the highest and the lowest are equal, it is not fully evolved
					if (highest == lowest) {
						fullyEvolved = false;
						break;
					}
					
					// see if it exceeds all of the others by the mutation rate
					for (double val : strat) {
						if (val < highest && val >= 0 && val + e.mutationRate >= highest) {
							fullyEvolved = false;
							break;
						}
					}
					
					// break out of the loop if we're able to
					if (!fullyEvolved)
						break;
				}
				
				// finish the game if necessary
				if (fullyEvolved)
					break;
			}
			
			awsheet.addCell(new Number(4, yy, game.getTelemetry(0)));
			awsheet.addCell(new Number(5, yy, e.getTelemetry("GAMES_WON")));
			game.clearPlayers();
		}
		
		alleleWorkbook.write();
		alleleWorkbook.close();
		
		// get the list of players
		ArrayList<Player> players = game.getPlayers();
		
		// arrange the players - alphabetical by name
		boolean sorted = false;
		while (sorted == false) {
			sorted = true;
			for (int i = 0; i < players.size() - 1; i++) {
				// for strings, "A" < "B"
				int compare = players.get(i).getName().compareToIgnoreCase(players.get(i+1).getName());
				
				if (compare > 0) {
					sorted = false;
					players.add(i+2, players.get(i));
					players.remove(i);
				}
			}
		}
		
		System.out.print(game);
		
		// write to a spreadsheet
		
		// open the spreadsheet
		WritableWorkbook workbook;
		workbook = Workbook.createWorkbook(new File("output.xls"));
		
		// create a new sheet
		WritableSheet wsheet = workbook.createSheet("Data", 0);
		
		// create arrays and references for adding data
		String[] labels = { "Attacks per turn", "Nodes won per game",  "Nodes won per turn",
						  "Nodes lost per game", "Nodes lost per turn", "Players killed per game", 
						  "Units killed per turn",  "Units lost per turn", "Turns survived per game", "Win percentage" };
		
		// add the vertical labels (statistics)
		int x = 0;
		int y = 1;
		for (String l : labels) {
			Label label = new Label(x, y, l);
			wsheet.addCell(label);
			y++;
		}
		
		// add the data for each player
		for (x = 1; x <= players.size(); x++) {
			Player player = players.get(x-1);
			// add the player's name
			Label label = new Label(x, 0, player.getName());
			wsheet.addCell(label);
			
			// add their data
			for (y = 1;  y <= labels.length; y++) {
				
				double totalGames = player.get("GAMES_WON") + player.get("GAMES_LOST");
				double turns = (double)(player.get("TURNS"));
				double out = 0;
				
				switch (y) {
					case 1: 	out = (player.get("ATTACKS")/turns);	break;
					case 2: 	out = (player.get("NODES_WON")/totalGames); break;
					case 3: 	out = (player.get("NODES_WON")/turns); break;
					case 4:		out = (player.get("NODES_LOST")/totalGames); break;
					case 5: 	out = (player.get("NODES_LOST")/turns); break;
					case 6:		out = (player.get("PLAYERS_KILLED")/totalGames); break;
					case 7: 	out = (player.get("UNITS_KILLED")/turns); break;
					case 8:		out = (player.get("UNITS_LOST")/turns); break;
					case 9: 	out = (player.get("TURNS")/totalGames); break;
					case 10:	out = player.get("GAMES_WON")/totalGames; break;
				}
				
				// add the data point
				Number number = new Number(x, y, out);
				wsheet.addCell(number);
			}
		}
		
		// add the formulas
		int width = x;
		
		// copy in vertical statistics
		for (y = 1; y <= labels.length; y++) {
			Formula formula = new Formula(x, y, "A" + (y + 1));
			wsheet.addCell(formula);
		}
		
		// copy or calculate in player data
		for (int i = 0; i < players.size(); i++) {
			// increment the x value and reset the y value
			x++;
			y = 0;
			
			// copy player name
			Formula formula = new Formula(x, y, String.valueOf(Character.toChars('A' + (x - width))) + "1");
			wsheet.addCell(formula);
			
			// add normalized statistics
			for (y = 1; y <= labels.length; y++) {
				formula = new Formula(x, y, String.valueOf(Character.toChars('A' + (x - width))) + (y + 1) +
						"/MAX(B" + (y + 1) + ":" + String.valueOf(Character.toChars('A' + width)) + (y + 1) + ")");
				wsheet.addCell(formula);
			}
		}
		
		workbook.write();
		workbook.close();
	}
}
