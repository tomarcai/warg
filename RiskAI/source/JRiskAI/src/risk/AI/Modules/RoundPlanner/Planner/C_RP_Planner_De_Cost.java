package risk.AI.Modules.RoundPlanner.Planner;

import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Player;
import risk.AI.Data_Structures.*;
import risk.AI.Data_Structures.Module_Input.I_RP_DefenseCost;
import risk.AI.Data_Structures.Module_Output.O_RP_DefenseCost;
import trainingdataconverter.trainingdata.GameData;
import trainingdataconverter.trainingdata.RP_De_CostData;
import trainingdataconverter.trainingdata.gamedata.CostAttribute;
import neuralnetwork.FeedForwardNetwork;
import trainer.trainers.WekaTrainer;

public class C_RP_Planner_De_Cost {
	
	private T_Game game;
	private String type;
	private Player player;
	private T_TrainingExampleWriter trainingExample;
	private FeedForwardNetwork neuralNetwork = null;	
	private C_Timing timer = new C_Timing();
	private WekaTrainer wekaModel = null;
	
	private O_RP_DefenseCost output;

	public C_RP_Planner_De_Cost(String type, Player player, T_Game game, T_TrainingExampleWriter trainingExample) {
		this.type = type;
		this.player = player;
		this.game = game;
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
		trainingExample.setModuleLoadTime(game.getPlayerIndex(player), "rp_de_cost", loadTime);
	}
	
	private void doLoadModel() {
		if (type.equals("dt") || type.equals("nb")) {
			wekaModel = new WekaTrainer(type);
			String filename = game.getAISettings().trainedAIModelsDirectory + "rp_de_cost/" + wekaModel.getModelDirectory() + "/" + RP_De_CostData.makeARFFRelationName() + wekaModel.getModelFilenameExtension();
			wekaModel.loadModel(filename);
			//System.out.println(ProgressStats.getText(0, 1) + " Loaded the Weka model (" + type + ") for RP_De_Cost.");
		} else if (type.equals("nn")) {
			neuralNetwork = new FeedForwardNetwork(game.getAISettings().trainedAIModelsDirectory + "rp_de_cost/nn/data.nnmodel");
		} else if (type.equals("nn_wo")) {
			neuralNetwork = new FeedForwardNetwork(game.getAISettings().trainedAIModelsDirectory + "rp_de_cost/nn_wo/data.nnmodel");
		}
	}
	
	/**
	 * Calculates the estimated cost of defending a territory this round.
	 * @param board The territory
	 * @param board The board
	 */
	public void run(I_RP_DefenseCost input, boolean isOpponentFramework) {
		if (type.equals("random")) {
			random_run(input.getBoard());
		} else {
			timer.startTimer();
			if (type.equals("script")) {
				script_run(input.getTerritory(), input.getBoard(), input.getAttackerArmiesLeft());
			} else if (type.equals("dt") || type.equals("nb")) {
				weka_run(input.getTerritory(), input.getBoard(), input.getAttackerArmiesLeft());
			} else if (type.equals("nn") || type.equals("nn_wo")) {
				nn_run(input.getTerritory(), input.getBoard(), input.getAttackerArmiesLeft());
			}
			timer.endTimer();
			if (!isOpponentFramework) {
				trainingExample.addModuleRunTime(game.getPlayerIndex(player), "rp_de_cost", timer.getTimer());
			}
			//trainingExample.saveModule(input, output, player);
		}
	}
	
	public O_RP_DefenseCost getOutput() {
		return output;
	}
	
	// Random
	
	private void random_run(T_Board board) {
		// The estimated cost of defending a territory is the amount of armies in the opponent territory with most armies.
		int mostArmies = 0;
		Country c;
		for (int i = 1; i < board.getCountryCount(); i++) {
			c = board.getCountryByInt(i);
			if (c.getOwner() != player) {
				if (c.getArmies() > mostArmies) {
					c = board.getCountryByInt(i);
					mostArmies = c.getArmies();
				}
			}
		}
		output = new O_RP_DefenseCost();
		output.setCost(mostArmies);
	}

	// Script
	
	private void script_run(Country territory, T_Board board, int attackerArmiesLeft) {
		// Count how many armies the player has in the neighbor territories
		int neighborArmies = 0;
		Country neighbor;
		for(int neighborIndex = 0; neighborIndex < territory.getNeighbours().size(); neighborIndex++) {
			neighbor = ((Country)territory.getNeighbours().get(neighborIndex));
			if (neighbor.getOwner() == territory.getOwner()) {
				neighborArmies += neighbor.getArmies() - 1; // -1 because one army needs to be left behind when attacking
			}
		}
		output = new O_RP_DefenseCost();
		// Make the neighbor armies count a little as defense force
		output.setCost(Math.max(0, Math.round((float) attackerArmiesLeft - neighborArmies * 0.1f)));
	}

	private void weka_run(Country territory, T_Board board, int attackerArmiesLeft) {
		GameData input = new GameData(game.getPlayers(), game.getPlayerIndex(this.player));
		input.setMergedPlanTerritory(territory.getIdString());
		input.setBoard(board);
		input.setAttackerArmiesLeft(attackerArmiesLeft);
		input.setEstimatedCost(1);
		
		RP_De_CostData data = new RP_De_CostData();
		data.setData(input);
		
		String outputText =
			"RP_De_Cost estimates:\r\n" +
			"---------------------\r\n";
		int unknownCount = 0;
		output = new O_RP_DefenseCost();
		outputText += "  Cost:\t  ";
		String arffString = data.makeARFFString();
		int queryResult = wekaModel.query(arffString);
		float cost = 0.0f;
		if (queryResult == -1) {
			unknownCount++;
		} else {
			cost = CostAttribute.getARFFValueFromState(queryResult);
		}
		outputText += cost + "(\"" + queryResult + "\") ";
		output.setCost(Math.round(cost));
		outputText += "\r\n";
		outputText +=
			"  Unknown states: " + unknownCount + "/" + 1 + "\r\n" +
			"---------------------\r\n";
		//System.out.print(outputText);
		int a = 0;
	}

	private void nn_run(Country territory, T_Board board, int attackerArmiesLeft) {
		GameData data = new GameData(game.getPlayers(), game.getPlayerIndex(this.player));
		data.setMergedPlanTerritory(territory.getIdString());
		data.setBoard(board);
		data.setAttackerArmiesLeft(attackerArmiesLeft);
		float[] nnInput = new float[neuralNetwork.getNumberOfInputNodes()];
		output = new O_RP_DefenseCost();
		int i = 0;
		// territory
		nnInput[i] = Float.parseFloat(data.getMergedPlanTerritory().getNNValue());
		i++;
		// board
		for (int territoryIndex = 0; territoryIndex < T_Game.NUMBER_OF_TERRITORIES; territoryIndex++) {
			nnInput[i] = Float.parseFloat(data.getBoard()[territoryIndex].getNNValue());
			i++;
		}
		// attackerArmiesLeft
		nnInput[i] = Float.parseFloat(data.getAttackerArmiesLeft().getNNValue());
		i++;
		// Validate number of inputs
		if (i != (nnInput.length)) {
			throw new RuntimeException("The length of nnInput does not match the number of values written to nnInput!");
		}
		// Query Network
		float[] out = this.neuralNetwork.calcOutput(nnInput);
		// Build output
		output.setCost(Math.round(CostAttribute.getNNValueFromNNOutput(out[0])));
	}

	public void setNewCurrentPlayer(int playerIndex) {
		this.player = (Player)game.getPlayers().get(playerIndex);
	}


}