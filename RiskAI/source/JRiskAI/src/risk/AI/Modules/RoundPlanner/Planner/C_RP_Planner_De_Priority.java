package risk.AI.Modules.RoundPlanner.Planner;

import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Mission;
import net.yura.domination.engine.core.Player;
import risk.AI.Data_Structures.*;
import risk.AI.Data_Structures.Module_Output.O_IG_NextMoveEstimate;
import risk.AI.Data_Structures.Module_Output.O_MP_GoalDistribution;
import risk.AI.Data_Structures.Module_Output.O_RP_DefensePriority;
import java.util.*;
import risk.AI.Data_Structures.Module_Input.I_RP_DefensePriority;
import trainingdataconverter.trainingdata.GameData;
import trainingdataconverter.trainingdata.RP_De_PriorityData;
import trainingdataconverter.trainingdata.gamedata.PriorityAttribute;
import neuralnetwork.FeedForwardNetwork;
import trainer.trainers.WekaTrainer;

public class C_RP_Planner_De_Priority {
	
	private String type;
	private T_Game game;
	private T_TrainingExampleWriter trainingExample;
	private Player player;
	private Random rand = new Random();
	private FeedForwardNetwork neuralNetwork = null;	
	private C_Timing timer = new C_Timing();
	private WekaTrainer wekaModel = null;
	
	private O_RP_DefensePriority output;
	
	public C_RP_Planner_De_Priority(String type, Player player, T_Game game, T_TrainingExampleWriter trainingExample) {
		this.type = type;
		this.game = game;
		this.player = player;
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
		trainingExample.setModuleLoadTime(game.getPlayerIndex(player), "rp_de_priority", loadTime);
	}
	
	private void doLoadModel() {
		if (type.equals("dt") || type.equals("nb")) {
			wekaModel = new WekaTrainer(type);
			String filename = game.getAISettings().trainedAIModelsDirectory + "rp_de_priority/" + wekaModel.getModelDirectory() + "/" + RP_De_PriorityData.makeARFFRelationName() + wekaModel.getModelFilenameExtension();
			wekaModel.loadModel(filename);
			//System.out.println(ProgressStats.getText(0, 1) + " Loaded the Weka model (" + type + ") for RP_De_Priority.");
		} else if (type.equals("nn")) {
			neuralNetwork = new FeedForwardNetwork(game.getAISettings().trainedAIModelsDirectory + "rp_de_priority/nn/data.nnmodel");
		} else if (type.equals("nn_wo")) {
			neuralNetwork = new FeedForwardNetwork(game.getAISettings().trainedAIModelsDirectory + "rp_de_priority/nn_wo/data.nnmodel");
		}		
	}
	
	public void run(I_RP_DefensePriority input, boolean isOpponentFramework) {
		if (type.equals("random")) {
			random_run();
		} else {
			timer.startTimer();
			if (type.equals("script")) {
				script_run(input.getTerritory(), input.getBoard(), input.getNextMoveEstimate(), input.getGoalDistribution());
			} else if (type.equals("dt") || type.equals("nb")) {
				weka_run(input.getTerritory(), input.getBoard(), input.getNextMoveEstimate(), input.getGoalDistribution());
			} else if (type.equals("nn") || type.equals("nn_wo")) {
				nn_run(input.getTerritory(), input.getBoard(), input.getNextMoveEstimate(), input.getGoalDistribution());
			}				
			timer.endTimer();
			if (!isOpponentFramework) {
				trainingExample.addModuleRunTime(game.getPlayerIndex(player), "rp_de_priority", timer.getTimer());
			}
			//trainingExample.saveModule(input,output,player);
		}
	}
	
	public O_RP_DefensePriority getOutput() {
		return output;
	}
	
	// Random
	
	private void random_run() {
		output = new O_RP_DefensePriority();
		output.setPriority(rand.nextFloat());
	}
	
	// Script
	
	private void script_run(Country territory, T_Board board, O_IG_NextMoveEstimate oppNextMove, O_MP_GoalDistribution goals) {
		// 1. Will occupying the territory help satisfy an MP goal?
		//    a) Conquer continent
		//    b) Defend continent
		//    c) Kill player
		//    d) Conquer 24
		//    e) Conquer 18 with 2
		// 2. Will the territory be attacked by an opponent this round?
		Player player = territory.getOwner();
		Mission mission = player.getMission();
		Country neighbor = null;
		
		/*// Init variables used to determine if the mission is "Kill player"
		boolean targetPlayerAlive;*/
		Player targetPlayer = mission.getPlayer();
		/*if (targetPlayer != null) {
			targetPlayerAlive = mission.getPlayer().getNoTerritoriesOwned() > 0;
		} else {
			targetPlayerAlive = false;
		}
		*/
		
		// 1. Will occupying the territory help satisfy an MP goal?
		float conquerPriority = 0.0f;
		float defendPriority = 0.0f;
		float killPlayerPriority = 0.0f;
		float conquer24Priority = 0.0f;
		float conquer18Priority = 0.0f;

		float tempPriority = 0.0f;
		Vector<Integer> neighborContinentsChecked = new Vector();
		int thisContinentIndex = board.getContinents().indexOf(territory.getContinent());
		int neighborContinentIndex = -1;

		for(int neighborIndex = 0; neighborIndex < territory.getNeighbours().size(); neighborIndex++) {
			neighbor = (Country)territory.getNeighbours().get(neighborIndex);
			neighborContinentIndex = board.getContinents().indexOf(neighbor.getContinent());

			// 1 a) MP goal: Conquer continent
			if (!(neighborContinentsChecked.contains(neighborContinentIndex)) &&
				(thisContinentIndex != neighborContinentIndex)) {
				// Give priority for neighboring a continent that should be conquered
				conquerPriority += goals.getDistribution_C()[neighborContinentIndex];
				conquerPriority += goals.getDistribution_CD()[neighborContinentIndex];
				conquerPriority += goals.getDistribution_O()[neighborContinentIndex];
				conquerPriority += goals.getDistribution_OD()[neighborContinentIndex];
				neighborContinentsChecked.add(neighborContinentIndex);
			}

			// 1 c) MP goal: Kill player
			if ((killPlayerPriority == 0.0f) && (targetPlayer != null)/* && targetPlayerAlive*/) {
				if (neighbor.getOwner() == targetPlayer) {
					killPlayerPriority = goals.getDistribution_A()[game.getPlayerIndex(targetPlayer)];
				}
			}

			// 1 d) MP goal: Conquer 24
			if (neighbor.getOwner() != player) {
				tempPriority = goals.getDistribution_24() * game.getBattleOutcomeProbTable().getBattleProbability(territory.getArmies(), neighbor.getArmies());
				if (tempPriority > conquer24Priority) {
					conquer24Priority = tempPriority;
				}
			}
		}

		// 1 b) MP goal: Defend continent
		if ((territory.getContinent().isOwned(player)) && (board.isBorderTerritory(territory))){
			defendPriority = goals.getDistribution_D()[thisContinentIndex]*1.5f;
		}

		// 1 e) MP goal: Conquer 18 with 2
		conquer18Priority += 0.2 * conquer24Priority * goals.getDistribution_18();
		if (territory.getArmies() == 1) {
			conquer18Priority += 0.8 * goals.getDistribution_18();
		}

		// 2. Will the territory be attacked by an opponent this round?
		float nextMoveMultiplier = 0.75f;
		if ((oppNextMove != null) && (oppNextMove.getTerritoryInDangerList() != null) && (oppNextMove.getTerritoryInDangerList().contains(territory))) {
			nextMoveMultiplier = 1.0f;
		}
		
		// Compute the total priority
		output = new O_RP_DefensePriority();
		output.setPriority(nextMoveMultiplier * (conquerPriority + defendPriority + conquer24Priority + conquer18Priority));
	}

	private void weka_run(Country territory, T_Board board, O_IG_NextMoveEstimate oppNextMove, O_MP_GoalDistribution goals) {
		GameData input = new GameData(game.getPlayers(), game.getPlayerIndex(this.player));
		input.setMergedPlanTerritory(territory.getIdString());
		input.setBoard(board);
		input.setNextMove(oppNextMove);
		input.setGoalDistribution(goals);
		input.setPriority(0.0f);
		
		RP_De_PriorityData data = new RP_De_PriorityData();
		data.setData(input);
		
		String outputText =
			"RP_De_Priority estimates:\r\n" +
			"-------------------------\r\n";
		int unknownCount = 0;
		output = new O_RP_DefensePriority();
		outputText += "  Priority:\t  ";
		String arffString = data.makeARFFString();
		int queryResult = wekaModel.query(arffString);
		float estimate = 0.0f;
		if (queryResult == -1) {
			unknownCount++;
		} else {
			estimate = PriorityAttribute.getARFFValueFromState(queryResult);
		}
		outputText += estimate + "(\"" + queryResult + "\") ";
		output.setPriority(estimate);
		outputText += "\r\n";
		outputText +=
			"  Unknown states: " + unknownCount + "/" + 1 + "\r\n" +
			"-------------------------\r\n";
		//System.out.print(outputText);
		int a = 0;
	}

	private void nn_run(Country territory, T_Board board, O_IG_NextMoveEstimate oppNextMove, O_MP_GoalDistribution goals) {
		GameData data = new GameData(game.getPlayers(), game.getPlayerIndex(this.player));
		data.setMergedPlanTerritory(territory.getIdString());
		data.setBoard(board);
		data.setNextMove(oppNextMove);
		data.setGoalDistribution(goals);
		float[] nnInput = new float[neuralNetwork.getNumberOfInputNodes()];
		output = new O_RP_DefensePriority();
		int i = 0;
		// territory
		nnInput[i] = Float.parseFloat(data.getMergedPlanTerritory().getNNValue());
		i++;
		// board
		for (int territoryIndex = 0; territoryIndex < T_Game.NUMBER_OF_TERRITORIES; territoryIndex++) {
			nnInput[i] = Float.parseFloat(data.getBoard()[territoryIndex].getNNValue());
			i++;
		}
		// next move
		for (int territoryIndex = 0; territoryIndex < T_Game.NUMBER_OF_TERRITORIES; territoryIndex++) {
			nnInput[i] = Float.parseFloat(data.getNextMoveAttribute()[territoryIndex].getNNValue());
			i++;
		}
		// goal dist
		for (int goalIndex = 0; goalIndex < T_Game.NUMBER_OF_GOALS; goalIndex++) {
			nnInput[i] = Float.parseFloat(data.getGoalDistribution()[goalIndex].getNNValue());
			i++;
		}
		// Validate number of inputs
		if (i != (nnInput.length)) {
			throw new RuntimeException("The length of nnInput does not match the number of values written to nnInput!");
		}
		// Query Network
		float[] out = this.neuralNetwork.calcOutput(nnInput);
		// Build output
		output.setPriority(Math.round(PriorityAttribute.getNNValueFromNNOutput(out[0])));		
	}

	public void setNewCurrentPlayer(int playerIndex) {
		this.player = (Player)game.getPlayers().get(playerIndex);
	}


}