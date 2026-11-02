package risk.AI.Modules.InformationGivers;

import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Player;
import risk.AI.Data_Structures.*;
import java.util.*;
import risk.AI.Data_Structures.Module_Output.O_IG_NextMoveEstimate;
import risk.AI.Data_Structures.Module_Input.I_IG_NextMoveEstimate;
import risk.AI.Data_Structures.T_RP_AttackPlan;
import risk.AI.Modules.AI_Framework;
import risk.AI.Modules.AI_OpponentFramework;
import risk.AI.Techniques.Pathfinding.*;
import neuralnetwork.FeedForwardNetwork;
import trainingdataconverter.trainingdata.GameData;
import trainingdataconverter.trainingdata.gamedata.NextMoveEstimateAttribute;

public class C_IG_NextMove {

	private String type;
	private T_Game game;
	private Random rand = new Random();
	private O_IG_NextMoveEstimate output;
	private T_TrainingExampleWriter trainingExample;
	private Player currentPlayer;
	private Pathfinder pathfinder;
	private C_Timing timer = new C_Timing();
	private FeedForwardNetwork neuralNetwork = null;
	
	public C_IG_NextMove(String type, T_Game game,Pathfinder pathfinder, Player currentPlayer, T_TrainingExampleWriter trainingExample) {
		this.type = type;
		this.game = game;
		this.currentPlayer = currentPlayer;
		this.pathfinder = pathfinder;
		this.trainingExample = trainingExample;
		if (!type.equals("none")) {
			trainingExample.setModuleLoadTime(game.getPlayerIndex(currentPlayer), "ig_nextmove",0);
		}
		loadModel();
	}

	private void loadModel() {
		long loadTime = 0;
		if (type.equals("nn_wo")) {
			doLoadModel();
			timer.startTimer();
			doLoadModel();
			timer.endTimer();
			loadTime = timer.getTimer();
		}
		trainingExample.setModuleLoadTime(game.getPlayerIndex(currentPlayer), "ig_nextmove", loadTime);
	}

	private void doLoadModel() {
		if (type.equals("nn")) {
			//neuralNetwork = new FeedForwardNetwork(game.getAISettings().trainedAIModelsDirectory + "ig_winning/nn/data.nnmodel");
		} else if (type.equals("nn_wo")) {
			neuralNetwork = new FeedForwardNetwork(game.getAISettings().trainedAIModelsDirectory + "ig_nextmove/nn_wo/data.nnmodel");
		}
	}

	
	// String[] is a array of actions.
	
	public void run(I_IG_NextMoveEstimate input) {
		if (type.equals("random")) {
			random_run(input.getBoard());			
		} else {
			if (type.equals("script")) {
				timer.startTimer();
				script_run(input.getFramework());
				timer.endTimer();
				this.trainingExample.addModuleRunTime(game.getPlayerIndex(currentPlayer), "ig_nextmove", timer.getTimer());
			} else if (type.equals("nn_wo")) {
				timer.startTimer();
				nn_run(input.getFramework());
				timer.endTimer();
				this.trainingExample.addModuleRunTime(game.getPlayerIndex(currentPlayer), "ig_nextmove", timer.getTimer());
			} else if (type.equals("none")) {
				none_run();
			}
		}
	}
	
	public O_IG_NextMoveEstimate getOutput() {
		return output;
	}
	
	// None

	private void none_run() {
		output = new O_IG_NextMoveEstimate(null);
	}
	
	// Random
	
	private void random_run(T_Board board) {
		T_RP_AttackPlanList resultingAttackPlans = new T_RP_AttackPlanList();
		for (int k = 0; k < game.getPlayerCount(); k++) {
			Player p = (Player)game.getPlayers().get(k);
			if (p.getNoTerritoriesOwned() > 0) { // is the player alive?
				if (k != game.getPlayerIndex(currentPlayer)) {
					Vector attackPlanList = new Vector();
					// Generate all possible attack plans:
					for (int i = 0; i < board.getCountryCount(); i++) {
						if ((board.getCountryByInt(i).getOwner().equals(p)) && (board.getCountryByInt(i).getArmies() > 1)) {
							for (int j = 0; j < board.getCountryCount(); j++) {
								if (!board.getCountryByInt(j).getOwner().equals(p)) {
									Vector paths = pathfinder.findPaths(board.getCountryByInt(i), board.getCountryByInt(j), p);
									for (int l = 0; l < paths.size(); l++) {
										attackPlanList.add((Vector)paths.get(l));
									}
								}
							}
						}
					}
					if (attackPlanList.size() > 0) {
						T_RP_AttackPlan ap = new T_RP_AttackPlan();
						ap.addCountries((Vector)attackPlanList.get(rand.nextInt(attackPlanList.size())));
						T_RP_AttackPlanListElement attackPlanListElement = new T_RP_AttackPlanListElement();
						attackPlanListElement.setAttackPlan(ap);
						resultingAttackPlans.add(attackPlanListElement);
					}
				}
			} 
		}
		output = AttackPlansToEstimate(resultingAttackPlans);
	}
		
	// Script

	private void script_run(AI_Framework framework) {
		// Instantiate an AI with all modules of same type as this
		// then use this AI to collect the territories the opponents want to attack
		AI_OpponentFramework opponentFramework = null;
		T_RP_AttackPlanListElement bestPlan = null;
		T_RP_AttackPlanList attackPlanList = new T_RP_AttackPlanList();
		for(int playerIndex = 0; playerIndex < game.getPlayerCount(); playerIndex++) {
			Player p = (Player)game.getPlayers().get(playerIndex);
			if ((playerIndex != game.getPlayerIndex(currentPlayer)) && (p.getNoTerritoriesOwned() > 0)) {
				// Make AI for the opponent
				//opponentFramework = framework.getOpponentFramework((Player)board.getPlayers().get(playerIndex));
				opponentFramework = new AI_OpponentFramework((Player)game.getPlayers().get(playerIndex),framework);
				
				opponentFramework.getMasterPrioritizerModule().setNewCurrentPlayer(playerIndex,framework.getMostLikelyMission(p));		
				opponentFramework.getPlannerModule().setNewCurrentPlayer(playerIndex);
				
				opponentFramework.run();
				// Collect the best attack plan
				bestPlan = opponentFramework.getAttackPlan();
				if (bestPlan != null) {
					attackPlanList.add(bestPlan);
				}

				opponentFramework.getMasterPrioritizerModule().setNewCurrentPlayer(game.getPlayerIndex(currentPlayer),framework.getMission());		
				opponentFramework.getPlannerModule().setNewCurrentPlayer(game.getPlayerIndex(currentPlayer));
			
			}
		}
		output = AttackPlansToEstimate(attackPlanList);
	}
	
	private O_IG_NextMoveEstimate AttackPlansToEstimate(T_RP_AttackPlanList attackPlans) {
		// Return a list of the territories contained in the attack plans
		Vector<Country> result = new Vector();
		T_RP_AttackPlan plan = null;
		Country territory = null;
		for(int planIndex = 0; planIndex < attackPlans.size(); planIndex++) {
			plan = attackPlans.get(planIndex).getAttackPlan();
			for(int territoryIndex = 1; territoryIndex < plan.size(); territoryIndex++) {
				territory = plan.getCountry(territoryIndex);
				if (!(result.contains(territory))) {
					result.add(territory);
				}
			}
		}	
		return new O_IG_NextMoveEstimate(result);
	}

	private void nn_run(AI_Framework framework) {
		GameData data = new GameData(game.getPlayers(), game.getPlayerIndex(currentPlayer));
		data.setMissionEstimates(framework.getIGMissionModule().getOutput());
		data.setContinentEstimates(framework.getIGContinentModule().getOutput());
		data.setWinningEstimates(framework.getIGWinningModule().getOutput());
		data.setOwnership(framework.getIGOwnershipModule().getOutput());
		float[] nnInput = new float[neuralNetwork.getNumberOfInputNodes()];
		int i = 0;
		// Build the input
		for (int playerIndex = 0; playerIndex < game.NUMBER_OF_PLAYERS_MAX; playerIndex++) {
			data.setCurrentPlayer(playerIndex);
			for (int roundIndex = 0; roundIndex < T_Game.NUMBER_OF_ROUNDS_PREDICTED; roundIndex++) {
				nnInput[i] = Float.parseFloat(data.getWinningEstimate(roundIndex).getNNValue());
				i++;
			}
		}
		for (int playerIndex = 0; playerIndex < game.NUMBER_OF_PLAYERS_MAX; playerIndex++) {
			data.setCurrentPlayer(playerIndex);
			for (int missionIndex = 0; missionIndex < T_Game.NUMBER_OF_MISSIONS; missionIndex++) {
				nnInput[i] = Float.parseFloat(data.getMissionEstimate(missionIndex).getNNValue());
				i++;
			}		
		}
		for (int playerIndex = 0; playerIndex < game.NUMBER_OF_PLAYERS_MAX; playerIndex++) {
			data.setCurrentPlayer(playerIndex);
			for (int roundIndex = 0; roundIndex < T_Game.NUMBER_OF_ROUNDS_PREDICTED; roundIndex++) {
				for (int continentIndex = 0; continentIndex < T_Game.NUMBER_OF_CONTINENTS; continentIndex++) {
					nnInput[i] = Float.parseFloat(data.getContinentEstimate(continentIndex,roundIndex).getNNValue());
					i++;
				}
			}
		}
		for (int playerIndex = 0; playerIndex < game.NUMBER_OF_PLAYERS_MAX; playerIndex++) {
			data.setCurrentPlayer(playerIndex);
			for (int continentIndex = 0; continentIndex < T_Game.NUMBER_OF_CONTINENTS; continentIndex++) {
				nnInput[i] = Float.parseFloat(data.getOwnership(continentIndex).getNNValue());
				i++;
			}
		}
		// Validate number of inputs
		if (i != (nnInput.length)) {
			throw new RuntimeException("The length of nnInput does not match the number of values written to nnInput!");
		}
		// Query network
		float[] out = this.neuralNetwork.calcOutput(nnInput);
		// Build the output
		Vector<Country> output_vector = new Vector();
		for (int j = 0; j < framework.getBoard().getCountryCount(); j++) {
			if (NextMoveEstimateAttribute.getNNValueFromNNOutput(out[j])) {
				output_vector.add(framework.getBoard().getCountryByInt(j));
			}
		}
		output = new O_IG_NextMoveEstimate(output_vector);
			
	}

}