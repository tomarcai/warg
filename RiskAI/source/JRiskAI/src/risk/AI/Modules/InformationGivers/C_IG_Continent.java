package risk.AI.Modules.InformationGivers;

import net.yura.domination.engine.core.Continent;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Player;
import neuralnetwork.FeedForwardNetwork;
import risk.AI.Data_Structures.*;
import java.util.*;
import risk.AI.Data_Structures.Module_Input.I_IG_ContinentEstimate;
import risk.AI.Data_Structures.Module_Output.O_IG_ContinentEstimate;
import risk.AI.Data_Structures.T_RP_AttackPlan;
import risk.AI.Modules.RoundPlanner.Planner.C_RP_Planner_AP_Cost;
import risk.AI.Data_Structures.Module_Input.I_RP_AttackPlanCost;
import risk.AI.Techniques.Pathfinding.AStar;
import trainer.trainers.WekaTrainer;
import trainingdataconverter.trainingdata.IG_ContinentData;
import trainingdataconverter.trainingdata.GameData;
import trainingdataconverter.trainingdata.gamedata.ContinentEstimateAttribute;
import Bayes.BayesLoader;

public class C_IG_Continent {
	
	private String type;
	private T_Game game;
	private Random rand = new Random();
	private int currentPlayerIndex;
	private O_IG_ContinentEstimate output;
	private AStar shortestPath;
	private T_RP_AttackPlan attack_plan;
	private C_RP_Planner_AP_Cost costModule;
	private T_TrainingExampleWriter trainingExample;
	private T_RP_AttackPlanListElement bestAttackPlan;
	private WekaTrainer[][] wekaModels = null;
	private FeedForwardNetwork neuralNetwork = null;
	private C_Timing timer = new C_Timing();
	private BayesLoader bn = null;
	
	public C_IG_Continent(String type, T_Game game, int currentPlayerIndex, AStar shortestPath, C_RP_Planner_AP_Cost costModule, T_TrainingExampleWriter trainingExample) {
		this.type = type;
		this.game = game;
		this.currentPlayerIndex = currentPlayerIndex;
		this.shortestPath = shortestPath;
		this.costModule = costModule;
		this.trainingExample = trainingExample;
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
		trainingExample.setModuleLoadTime(currentPlayerIndex, "ig_continent", loadTime);
	}
	
	private void doLoadModel() {
		if (type.equals("dt") || type.equals("nb")) {
			wekaModels = new WekaTrainer[T_Game.NUMBER_OF_CONTINENTS][T_Game.NUMBER_OF_ROUNDS_PREDICTED];
			for(int continentIndex = 0; continentIndex < T_Game.NUMBER_OF_CONTINENTS; continentIndex++) {
				for(int roundIndex = 0; roundIndex < T_Game.NUMBER_OF_ROUNDS_PREDICTED; roundIndex++) {
					WekaTrainer model = new WekaTrainer(type);
					String relationName;
					if ((continentIndex == 5) && (roundIndex == 4)) {
						// Australia: Use the model for round 3 for predicting round 4 (the model is missing for round 4)
						relationName = IG_ContinentData.makeARFFRelationName(continentIndex, 3);
					} else {
						relationName = IG_ContinentData.makeARFFRelationName(continentIndex, roundIndex);
					}
					String filename = game.getAISettings().trainedAIModelsDirectory + "ig_continent/" + model.getModelDirectory() + "/" + relationName + model.getModelFilenameExtension();
					model.loadModel(filename);
					wekaModels[continentIndex][roundIndex] = model;
					//System.out.println(ProgressStats.getText((continentIndex * (T_Game.NUMBER_OF_ROUNDS_PREDICTED)) + roundIndex, T_Game.NUMBER_OF_CONTINENTS * T_Game.NUMBER_OF_ROUNDS_PREDICTED) + " Loaded the Weka model (" + type + ") for " + T_Game.CONTINENT_NAMES[continentIndex] + " round " + roundIndex + ".");
				}
			}
		} else if (type.equals("nn")) {
			neuralNetwork = new FeedForwardNetwork(game.getAISettings().trainedAIModelsDirectory + "ig_continent/nn/data.nnmodel");
		} else if (type.equals("nn_wo")) {
			neuralNetwork = new FeedForwardNetwork(game.getAISettings().trainedAIModelsDirectory + "ig_continent/nn_wo/data.nnmodel");
		} else if (type.equals("bn")){
		    String fileName = "closeToContinent_one";
		    bn = new BayesLoader(fileName);
		    int i = 0;
		}
	}
	
	public void run(I_IG_ContinentEstimate input) {
		if (type.equals("random")) {
			random_run(game.getPlayerCount());
		} else {
			timer.startTimer();
			if(type.equals("script")){
				script_run(game.getPlayerCount(), input.getBoard(), input.getOppRiskCards(), input.getBeginningOfTurn());
			} else if (type.equals("dt") || type.equals("nb")) {
				weka_run(game.getPlayerCount(), input.getBoard(), input.getOppRiskCards(), input.getBeginningOfTurn());
			} else if (type.equals("nn") || type.equals("nn_wo")) {
				nn_run(game.getPlayerCount(), input.getBoard(), input.getOppRiskCards(), input.getBeginningOfTurn());
			} else if (type.equals("bn")){
				bn_run(game.getPlayerCount(), input.getBoard(), input.getOppRiskCards(), input.getBeginningOfTurn());
			} 
			timer.endTimer();
			trainingExample.addModuleRunTime(currentPlayerIndex, "ig_continent", timer.getTimer());
			trainingExample.saveModule(input,output,(Player)game.getPlayers().get(this.currentPlayerIndex));
		}
	}
	// float[][] is a 2d array of percentages. Float[x][y] is the percentage of how close player x is to owning continent y.
	public O_IG_ContinentEstimate getOutput() {
		return output;
	}
	
	// Random
	
	private void random_run(int numOfPlayers) {
		output = new O_IG_ContinentEstimate(numOfPlayers,6);
		for (int x = 0; x < numOfPlayers; x++) {
			for (int y = 0; y < 6; y++) {
				output.setEstimate(x,y,0,rand.nextFloat());
				for (int i = 1; i < 5; i++) {
					float r = rand.nextFloat() + output.getEstimate(x,y,i-1);
					if (r > 1.0f) { r = 1.0f; }
					output.setEstimate(x,y,i,r);
				}
			}
		}
	}
	
	// Script
	
	private void script_run(int numOfPlayers, T_Board board, T_Opp_RiskCards opp_riskcards, boolean beginningOfTurn){
		output = new O_IG_ContinentEstimate(numOfPlayers,6);
		calcContinentEstimate(numOfPlayers, board, opp_riskcards, 1, output, beginningOfTurn);
	}
	
	public void calcContinentEstimate(int numOfPlayers, T_Board board, T_Opp_RiskCards opp_riskcards, int numberOfContinents, O_IG_ContinentEstimate output, boolean beginningOfRound){
		for (int pn = 0; pn < game.getPlayerCount(); pn++){
			for(int cn = 0; cn < 6; cn++){
				float outcome = 0;
				int assistance = 0;
				int numberOfReinforcements;
				int targetsArmiesInContinent = 0;
				int targetsTerritoriesInContinent = 0;
				int hisOpponentsArmiesInContinent = 0;
				int hisOpponentsTerritoriesInContinent = 0;
				int numberOfTerritoriesLeft = 0;
				int fakeRn = 0;
				int numberOfOppReinforcement = 0;
				Continent continent = (Continent)board.getContinents().get(cn);
				
				int reinforcementsFromCards = 0;
				int numberOfRiskCards = ((Player)game.getPlayers().get(pn)).getCards().size();
				numberOfTerritoriesLeft = continent.getTerritoriesContained().size();
				while(numberOfTerritoriesLeft > 0){
					for(int tn = 0; tn < continent.getTerritoriesContained().size(); tn++){
						Country territory = (Country)continent.getTerritoriesContained().get(tn);
						
						if(territory.getOwner() != game.getPlayers().get(pn)){
							hisOpponentsArmiesInContinent += territory.getArmies();
							hisOpponentsTerritoriesInContinent += 1;
							numberOfTerritoriesLeft -= 1;
						} else {
							targetsArmiesInContinent += territory.getArmies();
							targetsTerritoriesInContinent += 1;
							numberOfTerritoriesLeft -= 1;
						}
					}
				}
				assistance = calcOutsideReinforcements(board, continent, (Player)game.getPlayers().get(pn))/numberOfContinents;
				for(int rn = 0; rn < 5; rn++){
					if(beginningOfRound){
						fakeRn = rn+1;
					}else{
						fakeRn = rn;
					}
					if(numberOfRiskCards >= 3){
						switch(numberOfRiskCards){
							case 3: reinforcementsFromCards += game.calcReinforcementsFromCards(3);
							numberOfRiskCards -= 2;
							break;
							case 4: reinforcementsFromCards += game.calcReinforcementsFromCards(4);
							numberOfRiskCards -= 2;
							break;
							case 5: reinforcementsFromCards += game.calcReinforcementsFromCards(5);
							numberOfRiskCards -= 2;
							break;
							default: reinforcementsFromCards += 10;
							numberOfRiskCards -= 2;
							break;
						}
					}
					numberOfRiskCards = 1;
					numberOfReinforcements = fakeRn*game.getNumberOfReinforcements((Player)game.getPlayers().get(pn))/numberOfContinents;
					numberOfOppReinforcement = Math.round(((float)0.15*(fakeRn*(14 - game.getNumberOfReinforcements((Player)game.getPlayers().get(pn))))));
					if(numberOfOppReinforcement < 0){
						numberOfOppReinforcement = 0;
					}
					int attackNumber = (targetsArmiesInContinent+assistance+numberOfReinforcements+reinforcementsFromCards-hisOpponentsTerritoriesInContinent-numberOfOppReinforcement);
					if(attackNumber <= 0){
						outcome = 0;
					} else
						if(hisOpponentsArmiesInContinent <= 0){
						outcome = 1;
						} else {
						outcome = game.getBattleOutcomeProbTable().getBattleProbability(attackNumber, hisOpponentsArmiesInContinent);
						}
					numberOfRiskCards +=1;
					output.setEstimate(pn, cn, rn, outcome);
				}
				
			}
		}
	}
	
	private int calcOutsideReinforcements(T_Board board, Continent continent, Player player){
		Vector borderTerritories = new Vector();
		Vector planContainer = new Vector();
		Vector path = new Vector();
		Vector occupiedWorldTerritories = new Vector();
		Vector bestPath = new Vector();
		if(board.getBorderTerritories(continent).size() > 0){
			borderTerritories = (Vector)board.getBorderTerritories(continent).clone();
		}
		if(player.getTerritoriesOwned().size() > 0){
			occupiedWorldTerritories = (Vector)player.getTerritoriesOwned().clone();
		}
		Vector leaveArmiesVector = new Vector();
		T_RP_AttackPlanListElement attack_plan_w_cost;
		int armiesInStart;
		int armiesInPath;
		int pathOutcome;
		int bestPathOutcome = 0;
		T_RP_AttackPlanListElement[] armiesAtBorders;
		//remove occupied territories from the border list.
		for(int count = borderTerritories.size()-1; count >= 0; count--){
			if(((Country)borderTerritories.get(count)).getOwner() == game.getPlayers().get(game.getPlayerIndex(player))){
				borderTerritories.remove(count);
			}
		}
		
		
		//Maintain the list of territories occupied by the player.
		//Removing territories inside the continent in question.
		
		for(int count = occupiedWorldTerritories.size()-1; count >= 0; count--){
			if(((Country)occupiedWorldTerritories.get(count)).getContinent().equals(continent) || ((Country)occupiedWorldTerritories.get(count)).getArmies() < 2){
				occupiedWorldTerritories.remove(count);
			}
		}
		
		
		//remove occupied territories from the list.
		if(!borderTerritories.isEmpty()){
			for(int count = 0; count < borderTerritories.size(); count++){
				if(((Country)borderTerritories.get(count)).getOwner() == game.getPlayers().get(game.getPlayerIndex(player))){
					borderTerritories.remove(count);
				}
			}
			
			//Maintain the list of territories occupied by the player.
			//Removing territories inside the continent in question.
			if(!occupiedWorldTerritories.isEmpty()){
				for(int count = 0; count < occupiedWorldTerritories.size(); count++){
					if(((Country)occupiedWorldTerritories.get(count)).getContinent() == continent){
						occupiedWorldTerritories.remove(count);
					}
				}
				
				//Do A* search between border territories and territories occupied by the player.
				for(int borders = 0; borders < borderTerritories.size(); borders++){
					armiesInStart = 0;
					pathOutcome = 0;
					armiesInPath = 0;
					planContainer.add(new Vector());
					for(int occupied = 0; occupied < occupiedWorldTerritories.size(); occupied++){
						path = shortestPath.getShortestPath((Country)occupiedWorldTerritories.get(occupied), ((Country)borderTerritories.get(borders)),(Player)game.getPlayers().get(game.getPlayerIndex(player)));
						if(path != null){
							attack_plan_w_cost = new T_RP_AttackPlanListElement();
							T_RP_AttackPlan attack_plan = new T_RP_AttackPlan();
							path.add(0, ((Country)occupiedWorldTerritories.get(occupied)));
							armiesInStart = ((Country)occupiedWorldTerritories.get(occupied)).getArmies();
							for(int k = 0; k < path.size(); k++){
								leaveArmiesVector.add(1);
							}
							attack_plan.addCountries(path);
							attack_plan.addArmiesToLeave(leaveArmiesVector);
							costModule.run(new I_RP_AttackPlanCost(attack_plan), true);
							//The pathOutcome is set to the cost of the number of armies in the starting territory minus the cost of the plan.
							//This number is the number of armies the attacker will have left when reaching the border territory.
							pathOutcome = armiesInStart - costModule.getOutput().getEstimatedCost();
							attack_plan_w_cost.setAttackPlan(attack_plan);
							attack_plan_w_cost.setEstimatedCost(pathOutcome);
							//The plan is only added if the outcome is possitive, since plans that below one means
							//than the attacker would not reach the border.
							if(pathOutcome > 0){
								((Vector)planContainer.lastElement()).add(attack_plan_w_cost);
							}
						}
					}
				}
			}
		}
		
		
		
		int theBorderTerritoryNumber = 0;
		int thePlanNumber = 0;
		armiesAtBorders = new T_RP_AttackPlanListElement[borderTerritories.size()];
		for(int init = 0; init < borderTerritories.size(); init++){
			armiesAtBorders[init] = null;
		}
		cleanPlanContainer(planContainer);
		while(!isNull(planContainer)){
			boolean hasBeenRemoved = false;
			T_RP_AttackPlanListElement bestAttackPlan = findBestAttackPlan(planContainer);
			for(int i = 0; i < armiesAtBorders.length; i++){
				// Hvis en attack plans startland er i en allerede fundet attack plan s� smides den v�k og der findes en ny.
				if(armiesAtBorders[i] != null && armiesAtBorders[i].getAttackPlan().getCountry(0).equals(bestAttackPlan.getAttackPlan().getCountry(0))){
					for(int borders = 0; borders < planContainer.size(); borders++){
						if(planContainer.get(borders) != null){
							if(((T_RP_AttackPlanListElement)((Vector)planContainer.get(borders)).get(0)).getAttackPlan().getLastCountry().equals(bestAttackPlan.getAttackPlan().getLastCountry())){
								((Vector)planContainer.get(borders)).remove(bestAttackPlan);
								hasBeenRemoved = true;
								break;
							}
						}
					}
					break;
				}
			}
			if(!hasBeenRemoved){
				for(int borders = 0; borders < planContainer.size(); borders++){
					if(planContainer.get(borders) != null){
						if(((T_RP_AttackPlanListElement)((Vector)planContainer.get(borders)).get(0)).getAttackPlan().getLastCountry().equals(bestAttackPlan.getAttackPlan().getLastCountry())){
							armiesAtBorders[borders] = bestAttackPlan;
							planContainer.set(borders, null);
							break;
						}
					}
				}
			} else{
				cleanPlanContainer(planContainer);
			}
		}
		for(int i = 0; i < armiesAtBorders.length; i++){
			if(armiesAtBorders[i] != null){
				bestPathOutcome += armiesAtBorders[i].getEstimatedCost();
			}
		}
		return bestPathOutcome;
	}
	
	private void cleanPlanContainer(Vector planContainer){
		for(int j=planContainer.size()-1; j>=0; j--){
			if((planContainer.get(j) != null) && ((Vector)planContainer.get(j)).size()==0){
				planContainer.set(j, null);
			}
		}
	}
	
	private T_RP_AttackPlanListElement findBestAttackPlan(Vector planContainer){
		if(!isNull(planContainer)){
			T_RP_AttackPlanListElement bestAttackPlan = null;
			for(int i = 0; i < planContainer.size(); i++){
				if(planContainer.get(i) != null){
					bestAttackPlan = ((T_RP_AttackPlanListElement)((Vector)planContainer.get(i)).get(0));
					break;
				}
			}
			for(int theBorderTerritoryNumber = 0; theBorderTerritoryNumber < planContainer.size();theBorderTerritoryNumber++){
				if(planContainer.get(theBorderTerritoryNumber) != null){
					for(int thePlanNumber = ((Vector)planContainer.get(theBorderTerritoryNumber)).size()-1; thePlanNumber >= 0; thePlanNumber--){
						T_RP_AttackPlanListElement attackPlanInVector = ((T_RP_AttackPlanListElement)((Vector)planContainer.get(theBorderTerritoryNumber)).get(thePlanNumber));
						if(attackPlanInVector.getEstimatedCost() > bestAttackPlan.getEstimatedCost()){
							bestAttackPlan = attackPlanInVector;
						}
					}
				}
			}
			return bestAttackPlan;
		} else {
			return null;
		}
	}
	
	private boolean isNull(Vector planContainer){
		for(int i = 0; i < planContainer.size(); i++){
			if(planContainer.get(i) != null){
				return false;
			}
		}
		return true;
	}
	
	/* Weka model: Decision tree or na�ve Bayes */
	private void weka_run(int numOfPlayers, T_Board board, T_Opp_RiskCards opp_riskcards, boolean beginningOfTurn) {
		// Run script to fill out the output structure with values, since the decision tree only covers Africa so far
		//script_run(numOfPlayers, board, opp_riskcards, beginningOfTurn);
		
		GameData input = new GameData(game.getPlayers(), currentPlayerIndex);
		input.setNumberOfPlayers(numOfPlayers);
		input.setBoard(board);
		input.setOpponentCards(opp_riskcards);
		input.setBeginningOfTurn(beginningOfTurn);
		
		IG_ContinentData data = new IG_ContinentData();
		data.setData(input);
		
		String outputText =
			"IG_Continent estimates:\r\n" +
			"-----------------------\r\n";
		int unknownCount = 0;
		output = new O_IG_ContinentEstimate(numOfPlayers, T_Game.NUMBER_OF_CONTINENTS);
		for(int continentIndex = 0; continentIndex < T_Game.NUMBER_OF_CONTINENTS; continentIndex++) {
			outputText += "  " + T_Game.CONTINENT_NAMES[continentIndex] + ":\t  ";
			for(int roundIndex = 0; roundIndex < T_Game.NUMBER_OF_ROUNDS_PREDICTED; roundIndex++) {
				WekaTrainer model = wekaModels[continentIndex][roundIndex];
				for(int playerIndex = 0; playerIndex < numOfPlayers; playerIndex++) {
					input.setCurrentPlayer(playerIndex);
					String arffString = data.makeARFFString(continentIndex, roundIndex);
					int queryResult = model.query(arffString);
					float estimate = 0.0f;
					if (queryResult == -1) {
						unknownCount++;
					} else {
						estimate = ContinentEstimateAttribute.getEstimateFromARFFState(queryResult);
					}
					outputText += "(p:" + playerIndex + ",r:" + roundIndex + ")=" + estimate + "(\"" + queryResult + "\") ";
					output.setEstimate(playerIndex, continentIndex, roundIndex, estimate);
				}
			}
			outputText += "\r\n";
		}
		outputText +=
			"  Unknown states: " + unknownCount + "/" + T_Game.NUMBER_OF_CONTINENTS * T_Game.NUMBER_OF_ROUNDS_PREDICTED * numOfPlayers + "\r\n" +
			"-----------------------\r\n";
		//System.out.print(outputText);
		int a = 0;
	}
	
	private void nn_run(int numOfPlayers, T_Board board, T_Opp_RiskCards opp_riskcards, boolean beginningOfTurn) {
		GameData data = new GameData(game.getPlayers(), this.currentPlayerIndex);
		data.setNumberOfPlayers(numOfPlayers);
		data.setBoard(board);
		data.setOpponentCards(opp_riskcards);
		data.setBeginningOfTurn(beginningOfTurn);
		float[] nnInput = new float[neuralNetwork.getNumberOfInputNodes()];
		output = new O_IG_ContinentEstimate(numOfPlayers, T_Game.NUMBER_OF_CONTINENTS);
		for (int playerIndex = 0; playerIndex < numOfPlayers; playerIndex++) {
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
				nnInput[i] = Float.parseFloat(data.getBeginningOfTurn().getNNValue());
				i++;
				// Validate number of inputs
				if (i != (nnInput.length)) {
					throw new RuntimeException("The length of nnInput does not match the number of values written to nnInput!");
				}
				// Query network
				float[] out = this.neuralNetwork.calcOutput(nnInput);
				// Build output
				for (int continentIndex = 0; continentIndex < out.length; continentIndex++) {
					output.setEstimate(playerIndex, continentIndex, roundIndex, out[continentIndex]);
				}
				
			}
		}
	}
	
	private void bn_run(int numOfPlayers, T_Board board, T_Opp_RiskCards opp_riskcards, boolean beginningOfTurn){
		GameData data = new GameData(game.getPlayers(), this.currentPlayerIndex);
		data.setBoard(board);
		data.setOpponentCards(opp_riskcards);
		data.setBeginningOfTurn(beginningOfTurn);
		data.initBnContinentValueCalc();
		
		output = new O_IG_ContinentEstimate(numOfPlayers,6);
                
		
		for (int pn = 0; pn < game.getPlayerCount(); pn++){
			data.setCurrentPlayer(pn);
			for(int cn = 0; cn < board.getContinents().size(); cn++){
				for(int rn = 0; rn < 5; rn++){
					Vector<String> listOfNames = new Vector();
					Vector<Integer> listOfStates = new Vector();
					listOfNames.add("Armies_X_Cont");
					listOfNames.add("Armies_Opp_Cont");
					listOfNames.add("Armies_X_Close");
					listOfNames.add("Armies_Opp_Close");
					
					listOfStates.add(data.getBnContinentValueCalc().calcCurrentPlayerArmiesInContinentStateIndex(cn, rn));
					listOfStates.add(data.getBnContinentValueCalc().calcOpponentPlayersArmiesInContinentStateIndex(cn,rn));
					listOfStates.add(data.getBnContinentValueCalc().calcCurrentPlayerArmiesOutsideContinentStateIndex(cn, rn));
					listOfStates.add(data.getBnContinentValueCalc().calcOpponentPlayersArmiesOutsideContinentStateIndex(cn,rn));
					
					if(!game.isPlayerDead(((Player)game.getPlayers().get(pn)))){
                                bn.enterEvidence(bn.getDomain(), listOfNames, listOfStates);
                                float estimate = ContinentEstimateAttribute.getBNValueFromState(bn.getResult("continent"));
				//System.out.println("Continent: Result for " + ((Continent)board.getContinents().get(cn)).getName() + ": player " + ((Player)game.getPlayers().get(pn)).getName() + "," + "Round " + rn + ":" + estimate);
                                output.setEstimate(pn, cn, rn, estimate);
				}else{
				// System.out.println("Continent: Result for " + ((Continent)board.getContinents().get(cn)).getName() + ": player " + ((Player)game.getPlayers().get(pn)).getName() + "," + "Round " + rn + ":" + "0");   
                                 output.setEstimate(pn, cn, rn, 0.0f);
				}
                                listOfStates.clear();
				}
			}
		}
	}
	
	
}

