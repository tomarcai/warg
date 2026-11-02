package risk.AI.Modules.RoundPlanner.Fortify;

import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Player;
import risk.AI.Data_Structures.Module_Output.O_MP_GoalDistribution;
import risk.AI.Data_Structures.Module_Output.O_RP_FortifyTransfer;
import risk.AI.Data_Structures.Module_Input.I_RP_FortifyTransfer;
import risk.AI.Techniques.Pathfinding.AStar;
import risk.AI.Data_Structures.*;
import java.util.*;

public class C_RP_Fortify_Transfer {
	
	private String type;
	private Random rand = new Random();
	private Player player;
	private T_Game game;
	private AStar pathFinder;
	private T_TrainingExampleWriter trainingExample;
	private C_Timing timer = new C_Timing();
	private O_RP_FortifyTransfer output;
	
	public C_RP_Fortify_Transfer(String type, Player currentPlayer, T_TrainingExampleWriter trainingExample, T_Game game, AStar pathFinder) {
		this.type = type;
		this.player = currentPlayer;
		this.trainingExample = trainingExample;
		this.game = game;
		this.pathFinder = pathFinder;
		trainingExample.setModuleLoadTime(game.getPlayerIndex(player), "rp_fortify", 0);
	}
	
	public void run(I_RP_FortifyTransfer input) {
		if (type.equals("random")) {
			random_run(input.getBoard());
		}
		if (type.equals("script")) {
			timer.startTimer();
			script_run(input.getDefenseList(), input.getGoalDistribution(), input.getBoard());
			timer.endTimer();
			trainingExample.addModuleRunTime(game.getPlayerIndex(player), "rp_fortify", timer.getTimer());
			trainingExample.saveModule(input,output,player);
		}
	}
	
	public O_RP_FortifyTransfer getOutput() {
		return output;
	}
	
	// Random
	
	private void random_run(T_Board board) {
		// Make a list of possible from-to pair of countries, where both countries are owned by the current player, and the source country has more than 1 army.
		Vector countryPairs_fromCountry = new Vector();
		Vector countryPairs_toCountry = new Vector();
		for (int i = 0; i < board.getCountryCount(); i++) {
			Country c = board.getCountryByInt(i);
			if ((c.getOwner().equals(player)) && (c.getArmies() > 1)) {
				for (int j = 0; j < c.getNeighbours().size(); j++) {
					Country n = (Country)c.getNeighbours().get(j);
					if (n.getOwner().equals(player)) {
						countryPairs_fromCountry.add(c);
						countryPairs_toCountry.add(n);
					}
				}
			}
		}
		// Select a random pair of the list of pairs and a random amount of armies.
		output = new O_RP_FortifyTransfer();
		if (countryPairs_fromCountry.size() > 0) {
			int r = rand.nextInt(countryPairs_fromCountry.size());
			output.setFromCountry((Country)countryPairs_fromCountry.get(r));
			output.setToCountry((Country)countryPairs_toCountry.get(r));
			output.setNumOfArmies(rand.nextInt(output.getFromCountry().getArmies()));
		} else {
			output.setFromCountry(null);
			output.setToCountry(null);
			output.setNumOfArmies(0);
		}
	}
	
	private void script_run(T_RP_DefenseList defenseList, O_MP_GoalDistribution goals, T_Board board) {
		int armiesToLeave;
		if (goals.getDistribution_18() > game.getAISettings().importanceThreshold_18w2) {
			armiesToLeave = 2;
		} else {
			armiesToLeave = 1;
		}
		
		Vector<Country> sourceTerritories = new Vector();
		Vector<Country> targetTerritories = new Vector();
		
		findSourceAndTargetTerritories(sourceTerritories, targetTerritories, game, goals, armiesToLeave);
		Vector<Country> path = findBestPath(sourceTerritories, targetTerritories, defenseList, board);
		
		output = new O_RP_FortifyTransfer();
		if (path != null) {
			output.setFromCountry(path.get(0));
			output.setToCountry(path.get(1));
			output.setNumOfArmies(path.get(0).getArmies() - armiesToLeave);
		} else {
			output.setNumOfArmies(0);
		}
	}
	
	private void findSourceAndTargetTerritories(Vector<Country> sourceTerritories, Vector<Country> targetTerritories, T_Game game, O_MP_GoalDistribution goals, int armiesToLeave) {
		Country territory;
		boolean allNeighborsOccupied;
		Vector<Country> neighbors;
		for(int i = 0; i < player.getNoTerritoriesOwned(); i++) {
			territory = (Country) player.getTerritoriesOwned().get(i);
			// Are all neighbors occupied?
			neighbors = (Vector<Country>) territory.getNeighbours();
			allNeighborsOccupied = true;
			for(int neighborIndex = 0; (allNeighborsOccupied) && (neighborIndex < neighbors.size()); neighborIndex++) {
				allNeighborsOccupied = (neighbors.get(neighborIndex).getOwner() == player);
			}
			if (allNeighborsOccupied) {
				if (territory.getArmies() > armiesToLeave) {
					sourceTerritories.add(territory);
				}
			} else {
				targetTerritories.add(territory);
			}
		}
	}
	
	private Vector<Country> findBestPath(Vector<Country> sourceTerritories, Vector<Country> targetTerritories, T_RP_DefenseList defenseList, T_Board board) {
		Vector<Country> path, bestPath = null;
		float score, bestScore = 0.0f;
		for(int sourceIndex = 0; sourceIndex < sourceTerritories.size(); sourceIndex++) {
			for(int targetIndex = 0; targetIndex < targetTerritories.size(); targetIndex++) {
				path = pathFinder.getShortestOccupiedPath(sourceTerritories.get(sourceIndex), targetTerritories.get(targetIndex), player);
				if (path == null) {
					Country source = sourceTerritories.get(sourceIndex);
					Country target = targetTerritories.get(targetIndex);
					int a = 0;
				}
				if (path != null) {
					// Put source territory in list since this is not included by the path finder
					path.add(0, sourceTerritories.get(sourceIndex));
					score = calcPathScore(path, defenseList, board);
					if (score > bestScore) {
						bestScore = score;
						bestPath = path;
					}
				}
			}
		}
		return bestPath;
	}
	
	private float calcPathScore(Vector<Country> path, T_RP_DefenseList defenseList, T_Board board) {
		float priority = 1.0f;
		// Find target territory in defense list
		for(int i = 0; (priority == 1.0f) && (i < defenseList.size()); i++) {
			if (defenseList.get(i).getCountry() == path.lastElement()) {
				priority += defenseList.get(i).getPriority();
			}
		}
		float multiplier = 1.0f;
		if (board.isBorderTerritory(path.firstElement()) || (isNeighborToOwnedBorder((Country)path.firstElement(),board))) {
			multiplier += 0.1f;
		}
		float sourceArmies = (float)path.firstElement().getArmies();
		float totalArmies = (float)player.getNoArmies();
		float pathLength = (float)(path.size() - 1);
		float result = ((priority * (sourceArmies / totalArmies)) / (pathLength * pathLength)) * multiplier;
		return result;
	}

	private boolean isNeighborToOwnedBorder(Country country, T_Board board) {
		for (int i = 0; i < country.getNeighbours().size(); i++) {
			Country c = (Country)country.getNeighbours().get(i);
			if ((board.isBorderTerritory(c)) && (c.getContinent().isOwned(this.player))) {
				return true;
			}
		}
		return false;
	}

}
