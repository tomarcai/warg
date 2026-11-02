package risk.AI.Modules.RoundPlanner;

import net.yura.domination.engine.core.Continent;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Mission;
import net.yura.domination.engine.core.Player;
import risk.AI.Data_Structures.*;
import java.util.*;
import risk.AI.Data_Structures.Module_Output.O_IG_MissionEstimate;
import risk.AI.Data_Structures.Module_Output.O_RP_PlaceArmies;
import risk.AI.Data_Structures.Module_Input.I_InitialPlacement;
import trainer.trainers.WekaTrainer;
import trainingdataconverter.trainingdata.GameData;
import neuralnetwork.FeedForwardNetwork;
import trainingdataconverter.trainingdata.InitialPlacementData;

public class C_RP_InitPlacement {
	
	private final boolean printDebugLog = false;
	private Vector<String> debugLog = new Vector();
	
	private Random rand = new Random();
	
	private String type;
	private String output;
	private Player player;
	private T_TrainingExampleWriter trainingExample;
	private T_Game game;
	private FeedForwardNetwork neuralNetwork = null;
	private WekaTrainer wekaModel = null;
	private C_Timing timer = new C_Timing();
	
	public C_RP_InitPlacement(String type, Player player, T_TrainingExampleWriter trainingExample, T_Game game) {
		this.player = player;
		this.type = type;
		this.trainingExample = trainingExample;
		this.game = game;
		loadModel();
	}
	
	/**
	 * Loads the model for the chosen AI technique and measures the time it takes to do it.
	 * To rule out disk access time the model will be loaded twice, and only the load time
	 * for the second time will be measured. This will give the time it takes to build the
	 * model structure in memory.
	 */
	private void loadModel() {
		long loadTime = 0;
		if (type.equals("bn") || type.equals("dt") || type.equals("nb") || type.equals("nn") || type.equals("nn_wo")) {
			doLoadModel();
			timer.startTimer();
			doLoadModel();
			timer.endTimer();
			loadTime = timer.getTimer();
		}
		trainingExample.setModuleLoadTime(game.getPlayerIndex(player), "initialplacement", loadTime);
	}
	
	private void doLoadModel() {
		if (type.equals("dt") || type.equals("nb")) {
			wekaModel = new WekaTrainer(type);
			String filename = game.getAISettings().trainedAIModelsDirectory + "initialplacement/" + wekaModel.getModelDirectory() + "/" + InitialPlacementData.makeARFFRelationName() + wekaModel.getModelFilenameExtension() + ".gz";
			wekaModel.loadModel(filename);
			//System.out.println(ProgressStats.getText(0, 1) + " Loaded the Weka model (" + type + ") for initial placement.");
		} else if (type.equals("nn")) {
			neuralNetwork = new FeedForwardNetwork(game.getAISettings().trainedAIModelsDirectory + "initialplacement/nn/data.nnmodel");
		} else if (type.equals("nn_wo")) {
			neuralNetwork = new FeedForwardNetwork(game.getAISettings().trainedAIModelsDirectory + "initialplacement/nn_wo/data.nnmodel");
		}
	}
	
	public void run(I_InitialPlacement input) {
		O_RP_PlaceArmies place = new O_RP_PlaceArmies();
		if (type.equals("random")) {
			place.setCountry(random_run(input.getBoard()));
		} else {
			timer.startTimer();
			if (type.equals("script")) {
				place.setCountry(script_run(input.getBoard(), input.getMissionEstimate(), input.getAttackerArmiesLeftMap()));
			} else if (type.equals("dt") || type.equals("nb")) {
				place.setCountry(weka_run(input.getBoard(), input.getMissionEstimate(), input.getAttackerArmiesLeftMap()));
			} else if (type.equals("nn") || type.equals("nn_wo")) {
				place.setCountry(nn_run(input.getBoard(), input.getMissionEstimate(), input.getAttackerArmiesLeftMap()));
			}
			timer.endTimer();
			trainingExample.addModuleRunTime(game.getPlayerIndex(player), "initialplacement", timer.getTimer());
			trainingExample.saveModule(input,place,player);
		}
		output = "placearmies "+place.getCountry().getColor() + " 1";
	}
	
	public String getOutput() {
		return output;
	}
	
	// Random
	
	private Country random_run(T_Board board) {
		// Make list of occupied countries:
		Vector c = new Vector();
		for (int i = 0; i < board.getCountryCount(); i++) {
			if (board.getCountryByInt(i).getOwner().equals(player)) {
				c.add(board.getCountryByInt(i));
			}
		}
		// Pick a random country from that list:
		return (Country)c.get(rand.nextInt(c.size()));
	}
	
	// Script
	
	private Country script_run(T_Board board, O_IG_MissionEstimate missionEstimate, T_InfluenceMap attackerArmiesLeftMap) {
		debugLog.clear();
		Continent continent;
		Country territory;
		List continentList = board.getContinents();
		// Count number of armies in each continent
		int[] enemyArmiesInContinent = new int[board.getContinents().size()];
		int[] playerArmiesInContinent = new int[board.getContinents().size()];
		boolean[] continentHasAnOwnedBorder = new boolean[board.getContinents().size()];
		int[] armiesToLeaveInContinent = new int[board.getContinents().size()];
		for(int continentIndex = 0; continentIndex < continentList.size(); continentIndex++) {
			continent = (Continent) continentList.get(continentIndex);
			// Count number of enemy armies
			enemyArmiesInContinent[continentIndex] = 0;
			playerArmiesInContinent[continentIndex] = 0;
			continentHasAnOwnedBorder[continentIndex] = false;
			armiesToLeaveInContinent[continentIndex] = 0;
			for(int territoryIndex = 0; territoryIndex < continent.getTerritoriesContained().size(); territoryIndex++) {
				territory = (Country) continent.getTerritoriesContained().get(territoryIndex);
				if (territory.getOwner() != player) {
					enemyArmiesInContinent[continentIndex] += territory.getArmies();
				} else {
					playerArmiesInContinent[continentIndex] += territory.getArmies();
					if (board.isBorderTerritory(territory)) {
						continentHasAnOwnedBorder[continentIndex] = true;
					}
					armiesToLeaveInContinent[continentIndex]++;
				}
			}
		}
		
		Mission mission = player.getMission();
		Continent c1 = mission.getContinent1();
		Continent c2 = mission.getContinent2();
		Player targetPlayer = mission.getPlayer();
		Country neighbor, bestTerritory = null;
		float bestScore = -100.0f;
		float multiplier, score;
		boolean isInContinentsMission, isInKillPlayerMission;
		int continentIndex;
		int armiesPlacedThereAlready;
		int attackerArmies;
		int playerArmies;
		for(int territoryIndex = 0; territoryIndex < player.getNoTerritoriesOwned(); territoryIndex++) {
			territory = (Country) player.getTerritoriesOwned().get(territoryIndex);
			
			// Check if the territory is a part of the player's mission:
			// "Conquer continents"
			continent = territory.getContinent();
			isInContinentsMission = ((continent == c1) || (continent == c2));
			// "Kill player"
			isInKillPlayerMission = false;
			if (!isInContinentsMission) {
				for(int neighborIndex = 0; (!isInKillPlayerMission) && (neighborIndex < territory.getNeighbours().size()); neighborIndex++) {
					neighbor = (Country) territory.getNeighbours().get(neighborIndex);
					isInKillPlayerMission = (neighbor.getOwner() == targetPlayer);
				}
			}
			// "Conquer territories" is not taken into account - it is too early to try to fulfill that mission
			
			// Score the territory
			multiplier = 1.0f;
			score = 0.0f;
			// Give a higher score if part of a mission
			if (isInContinentsMission) {
				multiplier += 0.2f;
			}
			if (isInKillPlayerMission) {
				multiplier += 0.05f;
			}
			// Give a higher score if the AI has placed armies there before
			armiesPlacedThereAlready = territory.getArmies() - 1;
			if (armiesPlacedThereAlready > 0) {
				multiplier += 0.01f;
			}
			// If the biggest enemy army attacked, what is the probability he wins (and we lose the territory)?
			attackerArmies = attackerArmiesLeftMap.getValue(territory);
			float territoryLostProb = 0.0f;
			if (attackerArmies > 0) {
				territoryLostProb = game.getBattleOutcomeProbTable().getBattleProbability(attackerArmies, territory.getArmies());
			}
			multiplier += territoryLostProb / 20; // max = 0.05
			// How big is the player's number of armies compared to the others in the continent
			continent = territory.getContinent();
			continentIndex = continentList.indexOf(continent);
			// Don't place any more armies if the continent can be conquered easily
			playerArmies = 1 + playerArmiesInContinent[continentIndex] - armiesToLeaveInContinent[continentIndex];
			float conquerProb = 0.0f;
			if (playerArmies < 1) {
				conquerProb = 0;
			} else if (enemyArmiesInContinent[continentIndex] < 1) {
				conquerProb = 1.0f;
			} else {
				conquerProb = game.getBattleOutcomeProbTable().getBattleProbability(playerArmies, enemyArmiesInContinent[continentIndex]);
			}
			if (conquerProb > 0.95) {
				score += -0.5f;
			} else {
				score += conquerProb;
			}
			// Give points according to the number of reinforcements that will be received if the continent was conquered
			score += (float)continent.getArmyValue() / 700; // max = 7/700 = 0.01
			// Prefer placement in border territories, if any is owned in the continent
			if ((continentHasAnOwnedBorder[continentIndex]) && (board.isBorderTerritory(territory))) {
				score += 0.04f;
			}
			// It is good if enemies can be attacked from the territory, and even better if they are in the same continent
			int enemyNeighborCount = 0;
			int enemyNeighborInContinentCount = 0;
			for(int neighborIndex = 0; neighborIndex < territory.getNeighbours().size(); neighborIndex++) {
				neighbor = ((Country) territory.getNeighbours().get(neighborIndex));
				if (neighbor.getOwner() != player) {
					enemyNeighborCount++;
					if (neighbor.getContinent() == territory.getContinent()) {
						enemyNeighborInContinentCount++;
					}
				}
			}
			if (enemyNeighborInContinentCount > 0) {
				multiplier += 0.05f;
			} else if (enemyNeighborCount > 0) {
				multiplier += 0.01f;
			}
			
			// Calculate total score
			score *= multiplier;
			if (score > bestScore) {
				bestScore = score;
				bestTerritory = territory;
			}
			if (territoryIndex == player.getNoTerritoriesOwned() - 1) {
				String name = bestTerritory.getIdString();
				int a = 0;
			}
			debugLog.add("***********************");
			debugLog.add(territory.getIdString());
			debugLog.add("-----------------------");
			debugLog.add("isInContinentsMission = " + isInContinentsMission);
			debugLog.add("isInKillPlayerMission = " + isInKillPlayerMission);
			debugLog.add("armiesPlacedThereAlready = " + armiesPlacedThereAlready);
			debugLog.add("territoryLostProb = " + territoryLostProb);
			debugLog.add("conquerProb = " + conquerProb);
			debugLog.add("continentHasAnOwnedBorder && isBorderTerritory = " + ((continentHasAnOwnedBorder[continentIndex]) && (board.isBorderTerritory(territory))));
			debugLog.add("enemyNeighborCount = " + enemyNeighborCount);
			debugLog.add("-----------------------");
			debugLog.add("score = " + score);
		}
		debugLog.add("***********************");
		debugLog.add("mission = " + game.decentMissionToString(mission, player));
		debugLog.add("bestTerritory = " + bestTerritory.getIdString());
		printDebugLog();
		return bestTerritory;
	}
	
	private void printDebugLog() {
		if (printDebugLog) {
			for(int i = 0; i < debugLog.size(); i++) {
				System.out.println(debugLog.get(i));
			}
			debugLog.clear();
		}
	}
	
	private Country weka_run(T_Board board, O_IG_MissionEstimate missionEstimate, T_InfluenceMap influenceMap) {
		GameData input = new GameData(game.getPlayers(), game.getPlayerIndex(this.player));
		input.setInfluenceMap(influenceMap);
		input.setMissionEstimates(missionEstimate);
		
		InitialPlacementData data = new InitialPlacementData();
		data.setData(input);
		
		String outputText =
				"InitialPlacement estimates:\r\n" +
				"--------------------------\r\n";
		int unknownCount = 0;
		int notOwnedCount = 0;
		Country result = null;
		outputText += "  Territory:\t  ";
		String arffString = data.makeARFFString();
		int queryResult = wekaModel.query(arffString);
		if (queryResult == -1) {
			unknownCount++;
		} else {
			result = (Country) board.getCountryByInt(queryResult);
		}
		// Check if the territory is owned by the player
		boolean ownedByPlayer = false;
		if (result != null) {
			for (int territoryIndex = 0; (!ownedByPlayer) && (territoryIndex < player.getNoTerritoriesOwned()); territoryIndex++) {
				ownedByPlayer = ((Country) player.getTerritoriesOwned().get(territoryIndex)).getIdString().equals(result.getIdString());
			}
		}
		// A territory owned by the player was picked
		if ((result != null) && (ownedByPlayer)) {
			outputText += result.getIdString() + "(\"" + queryResult + "\") ";
		} else if ((result != null) && (!ownedByPlayer)) {
			// A territory owned by someone else than the player was picked
			outputText += result.getIdString() + "(\"" + queryResult + "\") is NOT owned by the player - random pick: ";
			result = random_run(board);
			outputText += result.getIdString();
			notOwnedCount++;
		} else if (result == null) {
			// No territory was picked
			outputText += "NULL(\"" + queryResult + "\") - random pick: ";
			result = random_run(board);
			outputText += result.getIdString();
		}
		outputText += "\r\n";
		outputText +=
				"  Not owned: " + notOwnedCount + "/1\r\n" +
				"  Unknown states: " + unknownCount + "/1\r\n" +
				"--------------------------\r\n";
		//System.out.print(outputText);
		int a = 0;
		return result;
	}
	
	private Country nn_run(T_Board board, O_IG_MissionEstimate missionEstimate, T_InfluenceMap influenceMap) {
		GameData data = new GameData(game.getPlayers(), game.getPlayerIndex(this.player));
		data.setBoard(board);
		data.setMissionEstimates(missionEstimate);
		data.setInfluenceMap(influenceMap);
		float[] nnInput = new float[neuralNetwork.getNumberOfInputNodes()];
		int i = 0;
		// board
		for (int territoryIndex = 0; territoryIndex < T_Game.NUMBER_OF_TERRITORIES; territoryIndex++) {
			nnInput[i] = Float.parseFloat(data.getBoard()[territoryIndex].getNNValue());
			i++;
		}
		// mission
		for (int playerIndex = 0; playerIndex < T_Game.NUMBER_OF_PLAYERS_MAX; playerIndex++) {
			data.setCurrentPlayer(playerIndex);
			for (int missionIndex = 0; missionIndex < T_Game.NUMBER_OF_MISSIONS; missionIndex++) {
				nnInput[i] = Float.parseFloat(data.getMissionEstimate(missionIndex).getNNValue());
				i++;
			}
		}
		// influence map
		for (int territoryIndex = 0; territoryIndex < T_Game.NUMBER_OF_TERRITORIES; territoryIndex++) {
			nnInput[i] = Float.parseFloat(data.getInfluenceMap(territoryIndex).getNNValue());
			i++;
		}
		// Validate number of inputs
		if (i != (nnInput.length)) {
			throw new RuntimeException("The length of nnInput does not match the number of values written to nnInput!");
		}
		// Query network
		float[] out = this.neuralNetwork.calcOutput(nnInput);
		// Build output
		Vector<Country> occupiedTerritories = new Vector(); // could have used player.getTerritoriesOwned(), but the NN has been trained with a list of territories sorted by an index number based on the loaction on the board.
		for (int j = 0; j < board.getCountryCount(); j++) {
			if (board.getCountryByInt(j).getOwner().equals(player)) {
				occupiedTerritories.add(board.getCountryByInt(j));
			}
		}
		return occupiedTerritories.get(Math.round(out[0]*(occupiedTerritories.size()-1)));
	}
	
}