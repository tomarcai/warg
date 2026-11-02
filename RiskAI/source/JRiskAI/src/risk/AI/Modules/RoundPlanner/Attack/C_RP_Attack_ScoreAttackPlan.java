package risk.AI.Modules.RoundPlanner.Attack;

import net.yura.domination.engine.core.Player;
import risk.AI.Data_Structures.*;
import java.util.*;
import risk.AI.Data_Structures.Module_Output.O_RP_ScoreAttackPlan;
import risk.AI.Data_Structures.Module_Input.I_RP_ScoreAttackPlan;
import trainingdataconverter.trainingdata.GameData;
import trainingdataconverter.trainingdata.RP_ScoreAttackPlanData;
import trainingdataconverter.trainingdata.gamedata.ScoreAttribute;
import neuralnetwork.FeedForwardNetwork;
import trainer.trainers.WekaTrainer;

public class C_RP_Attack_ScoreAttackPlan {
	
	private String type;
	private T_Game game;
	private Random rand = new Random();
	private Player player;
	private FeedForwardNetwork neuralNetwork = null;
	private WekaTrainer wekaModel = null;
	private T_TrainingExampleWriter trainingExample;
	private C_Timing timer = new C_Timing();
	
	private O_RP_ScoreAttackPlan output;
	
	public C_RP_Attack_ScoreAttackPlan(String type, T_Game game, Player player, T_TrainingExampleWriter trainingExample) {
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
		trainingExample.setModuleLoadTime(game.getPlayerIndex(player), "rp_scoreattackplan", loadTime);
	}
	
	private void doLoadModel() {
		if (type.equals("dt") || type.equals("nb")) {
			wekaModel = new WekaTrainer(type);
			String filename = game.getAISettings().trainedAIModelsDirectory + "rp_scoreattackplan/" + wekaModel.getModelDirectory() + "/" + RP_ScoreAttackPlanData.makeARFFRelationName() + wekaModel.getModelFilenameExtension();
			wekaModel.loadModel(filename);
			//System.out.println(ProgressStats.getText(0, 1) + " Loaded the Weka model (" + type + ") for RP_ScoreAttackPlan.");
		} else if (type.equals("nn")) {
			neuralNetwork = new FeedForwardNetwork(game.getAISettings().trainedAIModelsDirectory + "rp_scoreattackplan/nn/data.nnmodel");
		} else if (type.equals("nn_wo")) {
			neuralNetwork = new FeedForwardNetwork(game.getAISettings().trainedAIModelsDirectory + "rp_scoreattackplan/nn_wo/data.nnmodel");
		}
	}
	
	// armies_recieved could be found by calling Player.getExtraArmies, but we might not be in the "reinforce phase" when this module is runned. So it should be calculated manually!
	public void run(I_RP_ScoreAttackPlan input) {
		if (type.equals("random")) {
			random_run();
		} else {
			timer.startTimer();
			if (type.equals("script")) {
				script_run(input.getPriority(),input.getCost());
			} else if (type.equals("dt") || type.equals("nb")) {
				weka_run(input.getPriority(),input.getCost());
			} else if (type.equals("nn") || type.equals("nn_wo")) {
				nn_run(input.getPriority(),input.getCost());
			} else if (type.equals("bn")) {
				bn_run(input.getPriority(),input.getCost());
			}
			timer.endTimer();
			trainingExample.addModuleRunTime(game.getPlayerIndex(player), "rp_scoreattackplan", timer.getTimer());
			//trainingExample.saveModule(input,output,player);
		}
		// Discard the plan if the attack plan's score is below the threshold
	}
	
	/**
	 * The output from the module
	 * @return Returns <CODE>true</CODE> if the attack plan should be discarded
	 */
	public O_RP_ScoreAttackPlan getOutput() {
		return output;
	}
	
	// Random
	
	private void random_run() {
		// Give the plan a random score
		output = new O_RP_ScoreAttackPlan();
		output.setScore(rand.nextFloat());
	}
	
	// Script
	
	private void script_run(float priority, int cost) {
		// tallet 100 skal m�ske s�ttes ned til en 20-50 stykker. Test me! :)
		// Desuden kan der m�ske bruges noget kode fra designet af "discard attack plan".
		// - "attack-planer der m�ske ikke kan udf�res i denne runde m� gerne f� lidt oprustninger alligevel".
		output = new O_RP_ScoreAttackPlan();
		output.setScore((float)(Math.pow((float)priority,10.0f)/(float)cost));
	}
	
	private void weka_run(float priority, int cost) {
		GameData input = new GameData(game.getPlayers(), game.getPlayerIndex(this.player));
		input.setEstimatedCost(cost);
		input.setPriority(priority);
		
		RP_ScoreAttackPlanData data = new RP_ScoreAttackPlanData();
		data.setData(input);
		
		String outputText =
			"RP_ScoreAttackPlan estimates:\r\n" +
			"-----------------------------\r\n";
		int unknownCount = 0;
		output = new O_RP_ScoreAttackPlan();
		outputText += "  Score:\t  ";
		String arffString = data.makeARFFString();
		int queryResult = wekaModel.query(arffString);
		float score = 0.0f;
		if (queryResult == -1) {
			unknownCount++;
		} else {
			score = ScoreAttribute.getARFFValueFromState(queryResult);
		}
		outputText += score + "(\"" + queryResult + "\") ";
		output.setScore(score);
		outputText += "\r\n";
		outputText +=
			"  Unknown states: " + unknownCount + "/" + 1 + "\r\n" +
			"---------------------\r\n";
		//System.out.print(outputText);
		int a = 0;
	}
	
	private void nn_run(float priority, int cost) {
		GameData data = new GameData(game.getPlayers(), this.game.getPlayerIndex(player));
		data.setEstimatedCost(cost);
		data.setPriority(priority);
		float[] nnInput = new float[neuralNetwork.getNumberOfInputNodes()];
		output = new O_RP_ScoreAttackPlan();
		int i = 0;
		nnInput[i] = Float.parseFloat(data.getEstimatedCost().getNNValue());
		i++;
		nnInput[i] = Float.parseFloat(data.getPriority().getNNValue());
		i++;
		// Validate number of inputs
		if (i != (nnInput.length)) {
			throw new RuntimeException("The length of nnInput does not match the number of values written to nnInput!");
		}
		// Query network
		float[] out = this.neuralNetwork.calcOutput(nnInput);
		// Build output
		output.setScore(ScoreAttribute.getNNValueFromNNOutput(out[0]));
	}
	
	private void bn_run(float priority, int cost) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
}