package risk.AI.Modules.InformationGivers;

import net.yura.domination.engine.core.Continent;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Mission;
import net.yura.domination.engine.core.Player;
import risk.AI.Data_Structures.C_Timing;
import risk.AI.Data_Structures.Module_Output.O_IG_ContinentEstimate;
import risk.AI.Data_Structures.Module_Output.O_IG_MissionEstimate;
import risk.AI.Data_Structures.Module_Output.O_IG_WinningEstimate;
import risk.AI.Data_Structures.Module_Input.I_IG_WinningEstimate;
import risk.AI.Data_Structures.T_Board;
import risk.AI.Data_Structures.T_Game;
import risk.AI.Data_Structures.T_Opp_RiskCards;
import risk.AI.Data_Structures.T_TrainingExampleWriter;
import risk.AI.Modules.RoundPlanner.Planner.C_RP_Planner_AP_Cost;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import risk.AI.Techniques.Pathfinding.AStar;
import trainer.trainers.WekaTrainer;
import trainingdataconverter.trainingdata.GameData;
import trainingdataconverter.trainingdata.IG_WinningData;
import neuralnetwork.FeedForwardNetwork;
import Bayes.BayesLoader;
import trainingdataconverter.trainingdata.gamedata.WinningEstimateAttribute;

public class C_IG_Winning {
	
	private String type;
	private T_Game game;
	private Random rand = new Random();
	private AStar aStar;
	private T_TrainingExampleWriter trainingExample;
	private Player currentPlayer;
	private C_RP_Planner_AP_Cost costModule;
	private FeedForwardNetwork neuralNetwork = null;
	private WekaTrainer[] wekaModels = null;
	private int currentPlayerIndex;
	private O_IG_WinningEstimate output;
	private C_Timing timer = new C_Timing();
	private BayesLoader bn = null;
	
	public C_IG_Winning(String type, T_Game game, AStar aStar, T_TrainingExampleWriter trainingExample, Player currentPlayer, C_RP_Planner_AP_Cost costModule) {
		this.type = type;
		this.game = game;
		this.aStar = aStar;
		this.trainingExample = trainingExample;
		this.currentPlayer = currentPlayer;
		this.costModule = costModule;
		this.currentPlayerIndex = game.getPlayerIndex(currentPlayer);
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
		trainingExample.setModuleLoadTime(game.getPlayerIndex(currentPlayer), "ig_winning", loadTime);
	}
	
	private void doLoadModel() {
		if (type.equals("dt") || type.equals("nb")) {
			wekaModels = new WekaTrainer[T_Game.NUMBER_OF_ROUNDS_PREDICTED];
			for(int roundIndex = 0; roundIndex < T_Game.NUMBER_OF_ROUNDS_PREDICTED; roundIndex++) {
				WekaTrainer model = new WekaTrainer(type);
				String filename = game.getAISettings().trainedAIModelsDirectory + "ig_winning/" + model.getModelDirectory() + "/" + IG_WinningData.makeARFFRelationName(roundIndex) + model.getModelFilenameExtension();
				model.loadModel(filename);
				wekaModels[roundIndex] = model;
				//System.out.println(ProgressStats.getText(roundIndex, T_Game.NUMBER_OF_ROUNDS_PREDICTED) + " Loaded the Weka model (" + type + ") for round " + roundIndex + ".");
			}
		} else if (type.equals("nn")) {
			neuralNetwork = new FeedForwardNetwork(game.getAISettings().trainedAIModelsDirectory + "ig_winning/nn/data.nnmodel");
		} else if (type.equals("nn_wo")) {
			neuralNetwork = new FeedForwardNetwork(game.getAISettings().trainedAIModelsDirectory + "ig_winning/nn_wo/data.nnmodel");
		}else if (type.equals("bn")){
		    String fileName = "closeToWinBN";
		    bn = new BayesLoader(fileName);
		    int i = 0;
		}
	}
	
	// numOfPlayers: retrieved by game.getNoPlayers().
	public void run(I_IG_WinningEstimate input) {
		if (type.equals("random")) {
			random_run(game.getPlayerCount());
		} else {
			timer.startTimer();
			if (type.equals("script")) {
				script_run(input.getBoard(), input.getOppRiskCards(), input.getMissionEstimate(), input.getBeginningOfTurn(), input.getContinentEstimate());
			} else if (type.equals("dt") || type.equals("nb")) {
				weka_run(input.getBoard(), input.getOppRiskCards(), input.getMissionEstimate(), input.getBeginningOfTurn(), input.getContinentEstimate());
			} else if (type.equals("nn") || type.equals("nn_wo")) {
				nn_run(input.getBoard(), input.getOppRiskCards(), input.getMissionEstimate(), input.getBeginningOfTurn(), input.getContinentEstimate());
			} else if (type.equals("bn")) {
				bn_run(input.getBoard(), input.getOppRiskCards(), input.getMissionEstimate(), input.getBeginningOfTurn(), input.getContinentEstimate());
			}
			timer.endTimer();
			trainingExample.addModuleRunTime(game.getPlayerIndex(currentPlayer), "ig_winning", timer.getTimer());
			this.trainingExample.saveModule(input,output,currentPlayer);
		}
	}
	
	public O_IG_WinningEstimate getOutput() {
		return output;
	}
	
	// Random
	
	private void random_run(int numOfPlayers) {
		output = new O_IG_WinningEstimate();
		for (int x = 0; x < numOfPlayers; x++) {
			output.setEstimate(x,0,rand.nextFloat());
			for (int i = 1; i < 5; i++) {
				float r = rand.nextFloat() + output.getEstimate(x,i-1);
				if (r > 1.0f) { r = 1.0f; }
				output.setEstimate(x,i,r);
			}
		}
	}
	
	private void script_run(T_Board board, T_Opp_RiskCards opp_riskcards, O_IG_MissionEstimate opp_missionestimate, boolean beginningOfTurn, O_IG_ContinentEstimate continentEstimate) {
		output = new O_IG_WinningEstimate();
		Mission mission = null;
		Player player = null, targetPlayer = null;
		boolean targetPlayerAlive;
		int continentCount = 0;
		int roundsToPredict = game.getAISettings().roundsToPredict;
		float missionCertainty;
		
		for(int playerIndex= 0; playerIndex < game.getPlayerCount(); playerIndex++) {
			player = (Player)game.getPlayers().get(playerIndex);
			if (player == currentPlayer) {
				mission = player.getMission();
				missionCertainty = 1.0f;
			} else {
				mission = opp_missionestimate.getMostLikelyMission(playerIndex, game.getMissions());
				missionCertainty = opp_missionestimate.getMostLikelyMissionEstimate(playerIndex) / 0.3f; // if the certainty is 0.3f then we are as sure of the mission as we can get
				missionCertainty = Math.min(1.0f, missionCertainty);
			}
			
			// "Conquer continents"
			if (mission.getContinent1() != null) {
				if (mission.getContinent3() == null) {
					continentCount = 2;
				} else {
					continentCount = 3;
				}
				int continent1 = board.getContinents().indexOf(mission.getContinent1());
				int continent2 = board.getContinents().indexOf(mission.getContinent2());
				int continent3 = -1;
				if (continentCount == 3) {
					continent3 = board.getContinents().indexOf(mission.getContinent3());
				}
				// Calculate winning estimate
				float estimate;
				for(int round = 0; round < roundsToPredict; round++) {
					estimate = continentEstimate.getEstimate(playerIndex, continent1, round);
					estimate *= continentEstimate.getEstimate(playerIndex, continent2, round);
					if (continentCount == 3) {
						// Find the continent that is easiest to conquer
						float bestEstimate = 0.0f, tempEstimate;
						for(int continentIndex = 0; continentIndex < board.getContinents().size(); continentIndex++) {
							if ((continentIndex != continent1) && (continentIndex != continent2)) {
								tempEstimate = continentEstimate.getEstimate(playerIndex, continentIndex, round);
								if (tempEstimate > bestEstimate) {
									bestEstimate = tempEstimate;
								}
							}
						}
						estimate *= bestEstimate;
					}
					setEstimate(playerIndex, round, estimate, missionCertainty);
				}
				int a = 0;
			} else {
				targetPlayer = mission.getPlayer();
				if (targetPlayer != null) {
					targetPlayerAlive = mission.getPlayer().getNoTerritoriesOwned() > 0;
				} else {
					targetPlayerAlive = false;
				}
				// "Kill player"
				if ((targetPlayer != null) && targetPlayerAlive && (targetPlayer != player)) {
					//calcKillPlayerEstimate(board, playerIndex, player, targetPlayer);
					Country targetPlayerTerritory, neighborTerritory;
					Vector targetPlayerTerritories = targetPlayer.getTerritoriesOwned();
					Vector neighborTerritories;
					int otherArmies = 0;
					// Count armies in territories neighboring the target player's, and neither belong to the player we are looking at, nor the target player
					for(int territoryIndex = 0; territoryIndex < targetPlayerTerritories.size(); territoryIndex++) {
						neighborTerritories = ((Country)targetPlayerTerritories.get(territoryIndex)).getNeighbours();
						for(int neighborIndex = 0; neighborIndex < neighborTerritories.size(); neighborIndex++) {
							neighborTerritory = (Country) neighborTerritories.get(neighborIndex);
							if ((neighborTerritory.getOwner() != player) && (neighborTerritory.getOwner() != targetPlayer)) {
								otherArmies += neighborTerritory.getArmies();
							}
						}
					}
					// Calculate winning estimate
					float estimate = 0;
					int playerArmies, targetArmies;
					for(int round = 0; round < roundsToPredict; round++) {
						playerArmies = player.getNoArmies() + game.getNumberOfReinforcements(player) * (round + 1) - player.getNoTerritoriesOwned(); // "- player.getNoTerritoriesOwned()" because one army needs to be left behind
						targetArmies = targetPlayer.getNoArmies() + game.getNumberOfReinforcements(targetPlayer) * (round + 1);
						estimate = game.getBattleOutcomeProbTable().getBattleProbability(playerArmies, Math.round(targetArmies + otherArmies * game.getAISettings().otherArmiesFactor));
						setEstimate(playerIndex, round, estimate, missionCertainty);
					}
				} else {
					// "Conquer territories"
					if ((mission.getNoofcountries() > 0) && (!targetPlayerAlive || targetPlayer == player))	{
						// 24: antal lande der mangler = 24 - #occupied
						// 18: antal lande der mangler = 18 - #occupied m. 2 p� hver
						// Hvor t�t er han p� at tage de lande der mangler?
						List<Country> territoryList = board.getCountries();
						Vector<Integer> playerArmies = new Vector();
						Vector<Integer> enemyArmies = new Vector();
						Vector<Boolean> playerOwns = new Vector();

						List<Integer> numberOfPlayerReinforcements = game.calcFutureReinforcements(player, roundsToPredict);
						Vector<Integer> numberOfEnemyReinforcements = calcNumberOfEnemyReinforcements(player, roundsToPredict);
						
						float estimate;
						int numberOfEnemyReinforcementsPerTerritory;
						for(int round = 0; round < roundsToPredict; round++) {
							// Base all calculations on the board as it looks now
							initEstimateTerritories(player, territoryList, playerArmies,enemyArmies, playerOwns);
							// !!!Fjenden kan f� for f� reinforcements pga. afrunding, giv dem derfor altid mindst 1!!!
							numberOfEnemyReinforcementsPerTerritory = Math.max(1, Math.round((float)numberOfEnemyReinforcements.get(round) / (float)(board.getCountries().size() - player.getNoTerritoriesOwned())));
							estimate = calcEstimateTerritories(player, mission, numberOfPlayerReinforcements.get(round), numberOfEnemyReinforcementsPerTerritory, territoryList, playerArmies, enemyArmies, playerOwns);
							setEstimate(playerIndex, round, estimate, missionCertainty);
						}
					}
				}
			}
		}
	}
	
	private void setEstimate(int player, int round, float estimate, float missionCertainty) {
		// Multiply estimate with how certain we are on the mission estimate
		estimate *= missionCertainty;
		if (round > 0) {
			float lastEstimate = output.getEstimate(player, round - 1);
			// Don't let the estimate decrease compared to last round
			if (estimate < lastEstimate) {
				estimate = lastEstimate;
			}
		}
		output.setEstimate(player, round, estimate);
	}
	
	private void initEstimateTerritories(Player player, List<Country> territoryList, Vector<Integer> playerArmies, Vector<Integer> enemyArmies, Vector<Boolean> playerOwns) {
		playerArmies.clear();
		enemyArmies.clear();
		playerOwns.clear();
		// Initialize lists of armies used as a "board" to keep track of the simulated attacks
		boolean owned;
		for(int territoryIndex = 0; territoryIndex < territoryList.size(); territoryIndex++) {
			owned = (territoryList.get(territoryIndex).getOwner() == player);
			playerOwns.add(owned);
			if (owned) {
				playerArmies.add(territoryList.get(territoryIndex).getArmies());
				enemyArmies.add(0);
			} else {
				playerArmies.add(0);
				enemyArmies.add(territoryList.get(territoryIndex).getArmies());
			}
		}
	}
	
	private Vector<Integer> calcNumberOfEnemyReinforcements(Player player, int roundsToPredict) {
		// Calculate the number of reinforcements the enemy players get as a whole
		// These will then be placed evenly on all enemy territories on the board
		Vector<Integer> numberOfEnemyReinforcements = new Vector();
		// Set all values to 0
		for(int i = 0; i < roundsToPredict; i++) {
			numberOfEnemyReinforcements.add(0);
		}
		// For each enemy player, add his number of reinforcements to the total number of enemy reinforcements
		List<Integer> reinforcements = new Vector();
		Player enemy = null;
		for(int enemyIndex = 0; enemyIndex < game.getPlayerCount(); enemyIndex++) {
			enemy = ((Player)game.getPlayers().get(enemyIndex));
			if (enemy != player) {
				reinforcements = game.calcFutureReinforcements(enemy, roundsToPredict);
				for(int i = 0; i < numberOfEnemyReinforcements.size(); i++) {
					numberOfEnemyReinforcements.set(i, numberOfEnemyReinforcements.get(i) + reinforcements.get(i));
				}
			}
		}
		return numberOfEnemyReinforcements;
	}
	
	private float calcEstimateTerritories(Player player, Mission mission, int numberOfPlayerReinforcements, int numberOfEnemyReinforcements, List<Country> territoryList, Vector<Integer> playerArmies, Vector<Integer> enemyArmies, Vector<Boolean> playerOwns) {
		float accumulatedBattleProb = 1.0f;
		// How many armies that should be left when moving armies into a newly conquered territory
		int armiesToLeave = mission.getNoofarmies();
		int territoriesOwned = player.getNoTerritoriesOwned();
		int territoriesNeeded = mission.getNoofcountries() - territoriesOwned;
		
		boolean placeReinforcements = true;
		boolean canAttack = true;
		int attackingArmies;
		while ((territoriesOwned < territoriesNeeded) && canAttack) {
			Vector<Country> neighborTerritories;
			int bestSourceIndex = -1, bestTargetIndex = -1;
			float battleProb, bestBattleProb = 0.0f;
			// Find the enemy territory that is easiest to conquer
			for(int sourceIndex = 0; sourceIndex < playerArmies.size(); sourceIndex++) {
				// The source territory must be owned AND there must enough armies to leave behind if an enemy territory was conquered from here
				if ((playerOwns.get(sourceIndex)) && (playerArmies.get(sourceIndex) > armiesToLeave)) {
					neighborTerritories = (Vector<Country>)territoryList.get(sourceIndex).getNeighbours();
					// Find weakest neighbor
					for(int neighborIndex = 0; neighborIndex < neighborTerritories.size(); neighborIndex++) {
						int targetIndex = territoryList.indexOf(neighborTerritories.get(neighborIndex));
						if (!playerOwns.get(targetIndex)) {
							attackingArmies = playerArmies.get(sourceIndex) - armiesToLeave;
							if (attackingArmies == 0) {
								int aa = 0;
							}
							if (enemyArmies.get(targetIndex) == 0) {
								int aa = 0;
							}
							battleProb = game.getBattleOutcomeProbTable().getBattleProbability(attackingArmies, enemyArmies.get(targetIndex));
							if (battleProb > bestBattleProb) {
								bestSourceIndex = sourceIndex;
								bestTargetIndex = targetIndex;
								bestBattleProb = battleProb;
							}
						}
					}
				}
			}
			// Simulate the target territory getting conquered from the source territory
			if ((bestBattleProb > 0.0) && (bestSourceIndex > -1) && (bestTargetIndex > -1)) {
				// Place player and enemy reinforcements
				if (placeReinforcements) {
					// !!!Reinforcements for fjenden bliver bare spredt ud over boardet, mens playerens bliver placeret i start-territoriet!!!
					playerArmies.set(bestSourceIndex, playerArmies.get(bestSourceIndex) + numberOfPlayerReinforcements);
					for (int enemyArmiesIndex = 0; enemyArmiesIndex < enemyArmies.size(); enemyArmiesIndex++) {
						enemyArmies.set(enemyArmiesIndex, enemyArmies.get(enemyArmiesIndex) + numberOfEnemyReinforcements);
					}
					placeReinforcements = false;
				}
				int armiesLost = Math.round((float)game.calcEstimatedArmyCost(enemyArmies.get(bestTargetIndex)));
				// Target is now occupied by the player
				playerOwns.set(bestTargetIndex, true);
				// Set number of armies in target territory
				playerArmies.set(bestTargetIndex, playerArmies.get(bestSourceIndex) - armiesToLeave);
				enemyArmies.set(bestTargetIndex, 0);
				// Set number of armies in source territory
				playerArmies.set(bestSourceIndex, armiesToLeave);
				accumulatedBattleProb *= bestBattleProb;
				territoriesOwned++;
			} else {
				// No territories can be attacked - abort
				accumulatedBattleProb = 0.0f;
				canAttack = false;
			}
		}
		if (accumulatedBattleProb == 1.0f) {
			int a = 0;
		}
		return accumulatedBattleProb;
	}
	
	private void weka_run(T_Board board, T_Opp_RiskCards opp_riskcards, O_IG_MissionEstimate opp_missionestimate, boolean beginningOfTurn, O_IG_ContinentEstimate continentEstimate) {
		GameData input = new GameData(game.getPlayers(), game.getPlayerIndex(currentPlayer));
		input.setBoard(board);
		input.setOpponentCards(opp_riskcards);
		input.setBeginningOfTurn(beginningOfTurn);
		input.setMissionEstimates(opp_missionestimate);
		input.setContinentEstimates(continentEstimate);
		
		IG_WinningData data = new IG_WinningData();
		data.setData(input);
		
		String outputText =
				"IG_Winning estimates:\r\n" +
				"---------------------\r\n";
		int unknownCount = 0;
		output = new O_IG_WinningEstimate();
		for(int playerIndex = 0; playerIndex < game.getPlayerCount(); playerIndex++) {
			input.setCurrentPlayer(playerIndex);
			outputText += "  " + game.decentColorToString(((Player) game.getPlayers().get(playerIndex)).getColor()) + ":\t  ";
			for(int roundIndex = 0; roundIndex < T_Game.NUMBER_OF_ROUNDS_PREDICTED; roundIndex++) {
				WekaTrainer model = wekaModels[roundIndex];
				String arffString = data.makeARFFString(roundIndex);
				int queryResult = model.query(arffString);
				float estimate = 0.0f;
				if (queryResult == -1) {
					unknownCount++;
				} else {
					estimate = WinningEstimateAttribute.getEstimateFromARFFState(queryResult);
				}
				outputText += "(r:" + roundIndex + ")=" + estimate + "(\"" + queryResult + "\") ";
				output.setEstimate(playerIndex, roundIndex, estimate);
			}
			outputText += "\r\n";
		}
		outputText +=
				"  Unknown states: " + unknownCount + "/" + T_Game.NUMBER_OF_ROUNDS_PREDICTED * game.getPlayerCount() + "\r\n" +
				"---------------------\r\n";
		//System.out.print(outputText);
		int a = 0;
	}
	
	private void nn_run(T_Board board, T_Opp_RiskCards opp_riskcards, O_IG_MissionEstimate opp_missionestimate, boolean beginningOfTurn, O_IG_ContinentEstimate continentEstimate) {
		GameData data = new GameData(game.getPlayers(), game.getPlayerIndex(currentPlayer));
		//Player currentPlayer = (Player) game.getPlayers().get(currentPlayerIndex);
		data.setBoard(board);
		data.setOpponentCards(opp_riskcards);
		data.setBeginningOfTurn(beginningOfTurn);
		data.setMissionEstimates(opp_missionestimate);
		data.setContinentEstimates(continentEstimate);
		float[] nnInput = new float[neuralNetwork.getNumberOfInputNodes()];
		output = new O_IG_WinningEstimate();
		for (int playerIndex = 0; playerIndex < game.getPlayerCount(); playerIndex++) {
			data.setCurrentPlayer(playerIndex);
			for (int roundIndex = 0; roundIndex < T_Game.NUMBER_OF_ROUNDS_PREDICTED; roundIndex++) {
				// Build input
				int i = 0;
				nnInput[i] = (float)roundIndex/4f;
				i++;
				for (int territoryIndex = 0; territoryIndex < T_Game.NUMBER_OF_TERRITORIES; territoryIndex++) {
					nnInput[i] = Float.parseFloat(data.getBoard()[territoryIndex].getNNValue());
					i++;
				}
				for (int opponentCardIndex = 0; opponentCardIndex < T_Game.NUMBER_OF_PLAYERS_MAX; opponentCardIndex++) {
					nnInput[i] = Float.parseFloat(data.getOpponentCards()[opponentCardIndex].getNNValue());
					i++;
				}
				// mission
				for (int missionIndex = 0; missionIndex < T_Game.NUMBER_OF_MISSIONS; missionIndex++) {
					nnInput[i] = Float.parseFloat(data.getMissionEstimate(missionIndex).getNNValue());
					i++;
				}
				// continent
				for (int continentIndex = 0; continentIndex < T_Game.NUMBER_OF_CONTINENTS; continentIndex++) {
					nnInput[i] = Float.parseFloat(data.getContinentEstimate(continentIndex, roundIndex).getNNValue());
					i++;
				}
				
				nnInput[i] = Float.parseFloat(data.getBeginningOfTurn().getNNValue());
				i++;
				// Validate number of inputs
				if (i != (nnInput.length)) {
					throw new RuntimeException("The length of nnInput does not match the number of values written to nnInput!");
				}
				// Query network
				float[] out = this.neuralNetwork.calcOutput(nnInput);
				// Build output
				output.setEstimate(playerIndex, roundIndex, out[0]);
			}
		}
	}
	
	private void bn_run(T_Board board, T_Opp_RiskCards opp_riskcards, O_IG_MissionEstimate opp_missionestimate, boolean beginningOfTurn, O_IG_ContinentEstimate continentEstimate){
		
		GameData data = new GameData(game.getPlayers(), this.currentPlayerIndex);
		data.setBoard(board);
		data.setOpponentCards(opp_riskcards);
		data.setMissionEstimates(opp_missionestimate);
		data.setContinentEstimates(continentEstimate);
		data.setBeginningOfTurn(beginningOfTurn);
		data.initBnWinningValueCalc();
		output = new O_IG_WinningEstimate();

		// output = new O_IG_WinningEstimate();
		//String[] goalStateList = {"0.0;0.0", "0.0;0.2", "0.2;0.4", "0.4;0.6", "0.6;0.8", "0.8;1.0", "1.0;1.0"};
		///AttributeStates goalStates = new AttributeStates(goalStateList);
		//String[] missionStateList = {"AF_SA", "AS_AF", "NA_AF", "EU_AU_ANY", "NA_AU", "EU_SA_ANY", "Black", "Red", "White", "Green", "Blue", "Yellow",  "24", "18" };
		//AttributeStates missionStates = new AttributeStates(missionStateList);
		
		for(int pn = 0; pn < game.getPlayerCount(); pn++){
			data.setCurrentPlayer(pn);
			Vector<String> listOfNames = new Vector(17);
			listOfNames.add("NorthAmerica");
			listOfNames.add("SouthAmerica");
			listOfNames.add("Europe");
			listOfNames.add("Africa");
			listOfNames.add("Asia");
			listOfNames.add("Australia");
			listOfNames.add("AnyContinent1");
			listOfNames.add("AnyContinent2");
			listOfNames.add("Kill_Player1");
			listOfNames.add("Kill_Player2");
			listOfNames.add("Kill_Player3");
			listOfNames.add("Kill_Player4");
			listOfNames.add("Kill_Player5");
			listOfNames.add("Kill_Player6");
			listOfNames.add("Conq18");
			listOfNames.add("Conq24");
			listOfNames.add("Observed_Mission");
			
			Vector<Integer> listOfStates = new Vector(17);
			
			
			Vector<Country> territoryList = (Vector<Country>)board.getCountries();
			Vector<Integer> playerArmies = new Vector();
			Vector<Integer> enemyArmies = new Vector();
			Vector<Boolean> playerOwns = new Vector();
			for(int rn = 0; rn < 5; rn++){
				listOfStates.add(0);
				listOfStates.add(0);
				listOfStates.add(0);
				listOfStates.add(0);
				listOfStates.add(0);
				listOfStates.add(0);
				listOfStates.add(0);
				listOfStates.add(0);
				listOfStates.add(0);
				listOfStates.add(0);
				listOfStates.add(0);
				listOfStates.add(0);
				listOfStates.add(0);
				listOfStates.add(0);
				listOfStates.add(0);
				listOfStates.add(0);
				listOfStates.add(0);
		
				for(int cn = 0; cn < 6; cn++){
					if(((Continent)board.getContinents().get(cn)).getName().toUpperCase().equals("NORTH-AMERICA")){
						listOfStates.set(0, data.getBnWinningValueCalc().calcCurrentPlayerCloseToContinentStateIndex(rn, cn, pn));
					}
					if(((Continent)board.getContinents().get(cn)).getName().toUpperCase().equals("SOUTH-AMERICA")){
						listOfStates.set(1, data.getBnWinningValueCalc().calcCurrentPlayerCloseToContinentStateIndex(rn, cn, pn));
					}
					if(((Continent)board.getContinents().get(cn)).getName().toUpperCase().equals("EUROPE")){
						listOfStates.set(2, data.getBnWinningValueCalc().calcCurrentPlayerCloseToContinentStateIndex(rn, cn, pn));
					}
					if(((Continent)board.getContinents().get(cn)).getName().toUpperCase().equals("AFRICA")){
						listOfStates.set(3, data.getBnWinningValueCalc().calcCurrentPlayerCloseToContinentStateIndex(rn, cn, pn));
					}
					if(((Continent)board.getContinents().get(cn)).getName().toUpperCase().equals("ASIA")){
						listOfStates.set(4, data.getBnWinningValueCalc().calcCurrentPlayerCloseToContinentStateIndex(rn, cn, pn));
					}
					if(((Continent)board.getContinents().get(cn)).getName().toUpperCase().equals("AUSTRALIA")){
						listOfStates.set(5, data.getBnWinningValueCalc().calcCurrentPlayerCloseToContinentStateIndex(rn, cn, pn));
					}
					
				}
				
				 listOfStates.set(6, data.getBnWinningValueCalc().calcCurrentPlayerMostLikelyAnyContinentStateIndex(pn, rn, 3));
				 listOfStates.set(7, data.getBnWinningValueCalc().calcCurrentPlayerMostLikelyAnyContinentStateIndex(pn, rn, 1));
				
				for(int opn = 0; opn < game.getPlayerCount(); opn++){
		    if(opn == 0){
			listOfStates.set(8, data.getBnWinningValueCalc().calcCurrentPlayerCloseToKillPlayerStateIndex(opn, rn));
		    }
		    if(opn == 1){
			listOfStates.set(9, data.getBnWinningValueCalc().calcCurrentPlayerCloseToKillPlayerStateIndex(opn, rn));
		    }
		    if(opn == 2){
			listOfStates.set(10, data.getBnWinningValueCalc().calcCurrentPlayerCloseToKillPlayerStateIndex(opn, rn));
		    }
		    if(opn == 3){
			listOfStates.set(11, data.getBnWinningValueCalc().calcCurrentPlayerCloseToKillPlayerStateIndex(opn, rn));
		    }
		    if(opn == 4){
			listOfStates.set(12, data.getBnWinningValueCalc().calcCurrentPlayerCloseToKillPlayerStateIndex(opn, rn));
		    }
		    if(opn == 5){
			listOfStates.set(13, data.getBnWinningValueCalc().calcCurrentPlayerCloseToKillPlayerStateIndex(opn, rn));
		    }
		}
		
		listOfStates.set(14, data.getBnWinningValueCalc().calcCurrentPlayerCloseToConq18StateIndex(rn));
		listOfStates.set(15, data.getBnWinningValueCalc().calcCurrentPlayerCloseToConq24StateIndex(rn));
		listOfStates.set(16, data.getBnWinningValueCalc().calcCurrentPlayerMissionStateIndex(pn));
	    
				
				if(!game.isPlayerDead(((Player)game.getPlayers().get(pn)))){
                                bn.enterEvidence(bn.getDomain(), listOfNames, listOfStates);
				float estimate = WinningEstimateAttribute.getBNValueFromState(bn.getResult("winning"));
                               // System.out.println("Winning: Result for player " + ((Player)game.getPlayers().get(pn)).getName() + "," + "Round " + rn + ":" + estimate);
                                output.setEstimate(pn, rn, estimate);
				}else{
				// System.out.println("Winning: Result for player " + ((Player)game.getPlayers().get(pn)).getName() + "," + "Round " + rn + ":" + "0");   
                                 output.setEstimate(pn, rn, 0.0f);
				}
				listOfStates.clear();
			}
		}
		
	}
}
