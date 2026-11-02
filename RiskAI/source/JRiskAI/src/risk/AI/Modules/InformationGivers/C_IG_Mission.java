package risk.AI.Modules.InformationGivers;

import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Mission;
import net.yura.domination.engine.core.Player;
import risk.AI.Data_Structures.Module_Output.O_IG_MissionEstimate;
import risk.AI.Data_Structures.Module_Input.I_IG_MissionEstimate;
import risk.AI.Data_Structures.*;
import java.util.*;
import neuralnetwork.FeedForwardNetwork;
import trainingdataconverter.trainingdata.GameData;
import trainingdataconverter.trainingdata.IG_MissionData;
import trainer.trainers.WekaTrainer;
import trainingdataconverter.trainingdata.gamedata.MissionEstimateAttribute;

public class C_IG_Mission {
	
	private String type;
	private T_Game game;
	private Random rand = new Random();
	private T_TrainingExampleWriter trainingExample;
	private Player currentPlayer;
	private FeedForwardNetwork neuralNetwork;
	private WekaTrainer[] models = null;
	
	private O_IG_MissionEstimate output;
	private C_Timing timer = new C_Timing();
	
	public C_IG_Mission(String type, T_Game game, T_TrainingExampleWriter trainingExample, Player currentPlayer) {
		this.type = type;
		this.game = game;
		this.trainingExample = trainingExample;
		this.currentPlayer = currentPlayer;
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
		trainingExample.setModuleLoadTime(game.getPlayerIndex(currentPlayer), "ig_mission", loadTime);
	}
	
	private void doLoadModel() {
		if (type.equals("dt") || type.equals("nb")) {
			// Since we are using the same DT for all kill player missions, we need 9 trees
			models = new WekaTrainer[9];
			for(int missionIndex = 0; missionIndex < 9; missionIndex++) {
				WekaTrainer model = new WekaTrainer(type);
				String filename = game.getAISettings().trainedAIModelsDirectory + "ig_mission/" + model.getModelDirectory() + "/" + IG_MissionData.makeARFFRelationName(missionIndex) + model.getModelFilenameExtension();
				model.loadModel(filename);
				models[missionIndex] = model;
				//System.out.println(ProgressStats.getText(missionIndex, 9) + " Loaded the Weka model (" + type + ") for mission " + game.MISSION_NAMES[missionIndex] + ".");
			}
		} else if (type.equals("nn")) {
			neuralNetwork = new FeedForwardNetwork(game.getAISettings().trainedAIModelsDirectory + "ig_mission/nn/data.nnmodel");
		} else if (type.equals("nn_wo")) {
			neuralNetwork = new FeedForwardNetwork(game.getAISettings().trainedAIModelsDirectory + "ig_mission/nn_wo/data.nnmodel");
		}
	}
	
	public void run(I_IG_MissionEstimate input) {
		if (type.equals("random")) {
			random_run(game.getPlayerCount(), game.getMissionCount());
		} else {
			timer.startTimer();
			if (type.equals("script")) {
				script_run(input.getPast());
			} else if (type.equals("dt") || type.equals("nb")) {
				weka_run(input.getPast());
			} else if (type.equals("nn") || type.equals("nn_wo")) {
				nn_run(input.getPast());
			}
			timer.endTimer();
			this.trainingExample.addModuleRunTime(game.getPlayerIndex(currentPlayer), "ig_mission", timer.getTimer());
			this.trainingExample.saveModule(input,output,currentPlayer);
		}
	}
	
	public O_IG_MissionEstimate getOutput() {
		return output;
	}
	
	// Random
	
	private void random_run(int numOfPlayers, int numOfMissions) {
		output = new O_IG_MissionEstimate();
		for (int x = 0; x < numOfPlayers; x++) {
			for (int y = 0; y < numOfMissions; y++) {
				output.setEstimate(x,y,rand.nextFloat());
			}
		}
	}
	
	private void script_run(T_Past past) {
		int numOfPlayers = game.getPlayerCount();
		List missions = game.getMissions();
		output = new O_IG_MissionEstimate();
		for (int i = 0; i < numOfPlayers; i++) {
			double totalAttacks = (double)past.getTotalAttacksByPlayer(i);
			double totalContinentPoints = (double)past.getTotalInterestPoints(i);
			for (int j = 0; j < missions.size(); j++) {
				Mission m = (Mission)missions.get(j);
				//double est18 = (1.0d/(6.0d+(double)numOfPlayers))*((double)getNumberOfTerritoriesWithMoreThat18Armies((Player)game.getPlayers().get(i))/18.0d);
				//double est24 = (1.0d/(6.0d+(double)numOfPlayers))*((double)((Player)game.getPlayers().get(i)).getNoTerritoriesOwned()/24.0d);
				double est18 = (1.0d/3.0d)*((double)getNumberOfTerritoriesWithMoreThat18Armies((Player)game.getPlayers().get(i))/18.0d);
				double est24 = (1.0d/3.0d)*((double)((Player)game.getPlayers().get(i)).getNoTerritoriesOwned()/24.0d);
				double est = 0.0d;
				if (m.getContinent1() != null) {// Continent mission
					if (past.getTotalInterestPoints(i) > 0) {
						est = 0.25d*(((double)past.getPlayersInterestInContinent(i,m.getContinent1()) / totalContinentPoints) +
							((double)past.getPlayersInterestInContinent(i,m.getContinent2()) / totalContinentPoints)) +
							(6.0d-(double)numOfPlayers)/72.0d - ((double)(est18+est24))/(6.0d+(double)numOfPlayers);
					} else {
						est = 0.0d;
					}
				} else if (m.getPlayer() != null) { // Kill player mission
					if (past.getTotalAttacksByPlayer(i) > 0) {
						est = ((double)numOfPlayers/6.0d)*((double)past.getNumOfAttacksOnPlayer(i,m.getPlayer())/(2.0d*(double)totalAttacks)) - ((double)(est18+est24))/(6.0d+(double)numOfPlayers);
					} else {
						est = 0.0d;
					}
				} else if (m.getNoofcountries() == 18) { // 18 territories w/ 2 armies on each mission
					est = est18;
				} else if (m.getNoofcountries() == 24) { // 24 territories w/ 1 army on each mission
					est = est24;
				}
				output.setEstimate(i,j,(float)est);
			}
			// If a player is estimated of having "kill X" as the most likely mission, but X is dead, then the player has the "24" mission.
			if (output.getMostLikelyMission(i,game.getMissions()).getPlayer() != null) {
				if (game.isPlayerDead(output.getMostLikelyMission(i,game.getMissions()).getPlayer())) {
					int missionIndex = game.getMissions().indexOf(output.getMostLikelyMission(i,game.getMissions()));
					float killPlayerEstimate = output.getEstimate(i,missionIndex);
					output.setEstimate(i,missionIndex,0.0f);
					for (int mi = 0; mi < game.getMissionCount(); mi++) {
						if ((((Mission)game.getMissions().get(mi)).getPlayer() == null) &&
							(((Mission)game.getMissions().get(mi)).getNoofcountries() == 24)) {
							float new_est = killPlayerEstimate + output.getEstimate(i,mi);
							if (new_est > 1) {
								new_est = 1.0f;
							}
							output.setEstimate(i,mi,new_est);
							break;
						}
					}
				}
			}
		}
	}
	
	private int getNumberOfTerritoriesWithMoreThat18Armies(Player p) {
		int n = 0;
		for (int i = 0; i < p.getNoTerritoriesOwned(); i++) {
			if (((Country)p.getTerritoriesOwned().get(i)).getArmies() > 1) {
				n++;
			}
		}
		return n;
	}
	
	/* Weka model: Decision tree or na�ve Bayes */
	private void weka_run(T_Past past) {
		GameData input = new GameData(game.getPlayers(), game.getPlayerIndex(currentPlayer));
		input.setPast(past);
		
		IG_MissionData data = new IG_MissionData();
		data.setData(input);
		
		output = new O_IG_MissionEstimate();
		String outputText =
			"IG_Mission estimates:\r\n" +
			"---------------------\r\n";
		int unknownCount = 0;
		for (int missionIndex = 0; missionIndex < game.NUMBER_OF_MISSIONS; missionIndex++) {
			WekaTrainer model;
			if (missionIndex > 8) {
				model = models[8]; // Use the same model for all kill player missions
			} else {
				model = models[missionIndex];
			}
			for (int playerIndex = 0; playerIndex < game.getPlayerCount(); playerIndex++) {
				input.setCurrentPlayer(playerIndex);
				String arffString = data.makeARFFString(missionIndex);
				int queryResult = model.query(arffString);
				float estimate = 0.0f;
				if (queryResult == -1) {
					unknownCount++;
				} else {
					estimate = MissionEstimateAttribute.getARFFAttributeStates().getValueFromIndex(queryResult);
				}
				/*
				if ((missionIndex != 7) && (estimate != 0.0f)) {
					System.out.println("DET VIRKER!! IKKE ALLE HAR 24-MISSIONEN!!! HURRA");
					int a = 0;
				}
				 */
				outputText += "(p:" + playerIndex + ",m:" + missionIndex + ")=" + estimate + "(\"" + queryResult + "\") ";
				output.setEstimate(playerIndex, missionIndex, estimate);
			}
			outputText += "\r\n";
		}
		//System.out.print(outputText);
		int a = 0;
		// Set estimate to zero for Kill Player X missions when there are less than X players
		for (int playerIndex = 0; playerIndex < game.getPlayerCount(); playerIndex++) {
			for (int killPlayerIndex = game.getPlayerCount(); killPlayerIndex < T_Game.NUMBER_OF_PLAYERS_MAX; killPlayerIndex++) {
				output.setEstimate(playerIndex, T_Game.NUMBER_OF_MISSIONS - (T_Game.NUMBER_OF_PLAYERS_MAX - killPlayerIndex), 0.0f);
			}
		}
	}
	
	private void nn_run(T_Past past) {
		GameData data = new GameData(game.getPlayers(), game.getPlayerIndex(currentPlayer));
		data.setPast(past);
		float[] nnInput = new float[neuralNetwork.getNumberOfInputNodes()];
		output = new O_IG_MissionEstimate();
		for (int playerIndex = 0; playerIndex < game.getPlayerCount(); playerIndex++) {
			data.setCurrentPlayer(playerIndex);
			// Build input
			String[] in = data.getPastAttribute().getNNValue().split(",");
			if (in.length == nnInput.length) {
				for (int i = 0; i < in.length; i++) {
					nnInput[i] = Float.parseFloat(in[i]);
				}
				// Query network
				float[] out = this.neuralNetwork.calcOutput(nnInput);
				// Build output
				for (int missionIndex = 0; missionIndex < out.length; missionIndex++) {
					output.setEstimate(playerIndex, missionIndex, out[missionIndex]);
				}
				// Set estimate to zero for Kill Player X missions when there are less than X players
				for (int i = game.getPlayerCount(); i < T_Game.NUMBER_OF_PLAYERS_MAX; i++) {
					output.setEstimate(playerIndex, T_Game.NUMBER_OF_MISSIONS-(T_Game.NUMBER_OF_PLAYERS_MAX-i), 0.0f);
				}
			} else {
				throw new RuntimeException("Number of input values does not match the expected length (length: "+nnInput.length+")");
			}
		}
	}
}