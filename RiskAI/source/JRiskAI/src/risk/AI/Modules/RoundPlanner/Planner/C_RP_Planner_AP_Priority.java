package risk.AI.Modules.RoundPlanner.Planner;

import net.yura.domination.engine.core.Continent;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Player;
import risk.AI.Data_Structures.*;
import risk.AI.Data_Structures.Module_Output.O_MP_GoalDistribution;
import risk.AI.Data_Structures.T_RP_AttackPlan;
import risk.AI.Data_Structures.Module_Output.O_RP_AttackPlanPriority;
import risk.AI.Data_Structures.Module_Input.I_RP_AttackPlanPriority;
import java.util.*;
import trainingdataconverter.trainingdata.GameData;
import trainingdataconverter.trainingdata.gamedata.PriorityAttribute;
import neuralnetwork.FeedForwardNetwork;

public class C_RP_Planner_AP_Priority {
	
	private String type;
	private Random rand = new Random();
	private Player currentPlayer;
	private T_Game game;
	private int[] memBoard_armies;
	private Player[] memBoard_owners;
	private FeedForwardNetwork neuralNetwork = null;
	private T_TrainingExampleWriter trainingExample;
	private C_Timing timer = new C_Timing();
	
	private float[] lastPriorityTable; // to be removed!
	
	private O_RP_AttackPlanPriority output;
	
	public C_RP_Planner_AP_Priority(String type, T_Game game, Player player, T_TrainingExampleWriter trainingExample) {
		this.type = type;
		this.game = game;
		this.currentPlayer = player;
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
		trainingExample.setModuleLoadTime(game.getPlayerIndex(currentPlayer), "rp_ap_priority", loadTime);
	}
	
	private void doLoadModel() {
		if (type.equals("dt")) {
			// To do..
		} else if (type.equals("nn")) {
			neuralNetwork = new FeedForwardNetwork(game.getAISettings().trainedAIModelsDirectory + "rp_ap_priority/nn/data.nnmodel");
		} else if (type.equals("nn_wo")) {
			neuralNetwork = new FeedForwardNetwork(game.getAISettings().trainedAIModelsDirectory + "rp_ap_priority/nn_wo/data.nnmodel");
		}
	}
	
	public void run(I_RP_AttackPlanPriority input, boolean isOpponentFramework) {
		if (type.equals("random")) {
			random_run();
		} else {
			timer.startTimer();
			if (type.equals("script")) {
	 			script_run(input.getAttackPlan(), input.getBoard(), input.getGoalDistribution());
			} else if (type.equals("dt")) {
				dt_run(input.getAttackPlan(), input.getBoard(), input.getGoalDistribution());
			} else if (type.equals("nn") || type.equals("nn_wo")) {
				nn_run(input.getAttackPlan(), input.getBoard(), input.getGoalDistribution());
			}
			timer.endTimer();
			if (!isOpponentFramework) {
				trainingExample.addModuleRunTime(game.getPlayerIndex(currentPlayer), "rp_ap_priority", timer.getTimer());
			}
		}
		//trainingExample.saveModule(input,output,player);
	}
	
	public O_RP_AttackPlanPriority getOutput() {
		return output;
	}
	
	// Random
	
	private void random_run() {
		output = new O_RP_AttackPlanPriority();
		output.setPriority(rand.nextFloat());
	}

	private void script_run(T_RP_AttackPlan attack_plan, T_Board board, O_MP_GoalDistribution goaldist) {
		this.memBoard_armies = new int[board.getCountries().size()];
		this.memBoard_owners = new Player[board.getCountries().size()];
		// priorityTable: holds information on how well the attack pland satisfies each goal in the goal distribution.
		float[] priorityTable = new float[goaldist.getFullDistribution().length];
		// Satisfying Conquer and defend goals:
		float[] out = this.calcPriorityForOffensiveGoal(attack_plan, board.getContinents(), goaldist.getDistribution_CD());
		for (int i = 0; i < out.length; i++) {
			priorityTable[i] = out[i];
		}
		// Satisfying Conquer goals:
		out = this.calcPriorityForOffensiveGoal(attack_plan, board.getContinents(), goaldist.getDistribution_C());
		for (int i = 0; i < out.length; i++) {
			priorityTable[i+6] = out[i];
		}
		// (Dis)satisfying Defend goals:
		
		// Hvis Country(0) har et border-territorie som nabo i et andet continent, og man skal defend det continent -> penalty!
		Country c = attack_plan.getCountry(0);
		boolean disBorderCountry = false;
		for (int i = 0; i < c.getNeighbours().size(); i++) {
			Continent con = ((Country)c.getNeighbours().get(i)).getContinent();
			int cont_i = board.getContinents().indexOf(con);
			if ((!con.equals(c.getContinent())) && (goaldist.getDistribution_D()[cont_i] > 0)) {
				disBorderCountry = true;
				break;
			}
		}
		// eller hvis Country(0) starter i et border-territorie..
		int cont_index = board.getContinents().indexOf(c.getContinent());
		if ((goaldist.getDistribution_D()[cont_index] > 0) || (disBorderCountry)) {
			priorityTable[cont_index+12] = -goaldist.getDistribution_D()[cont_index];
		}
		// Satisfying Obstruct goals:
		//out = this.calcPriorityForOffensiveGoal(attack_plan, board.getContinents(), goaldist.getDistribution_O());
		out = this.calcPriorityForObstruct(attack_plan, board.getContinents(), goaldist.getDistribution_O());
		for (int i = 0; i < out.length; i++) {
			priorityTable[i+18] = out[i];
		}
		// Satisfying Obstruct and defend goals:
		//out = this.calcPriorityForOffensiveGoal(attack_plan, board.getContinents(), goaldist.getDistribution_OD());
		out = this.calcPriorityForObstruct(attack_plan, board.getContinents(), goaldist.getDistribution_OD());
		for (int i = 0; i < out.length; i++) {
			priorityTable[i+24] = out[i];
		}
		// Satisfying Attack Player goals:
		for (int i = 0; i < game.getPlayerCount(); i++) {
			Player o = (Player)game.getPlayers().get(i);
			Vector tTarget = o.getTerritoriesOwned(); 
			Vector tTarget_intersect_tPlan = new Vector();
			for (int j = 1; j < attack_plan.getCountries().size(); j++) {
				if (tTarget.contains(attack_plan.getCountry(j))) {
					tTarget_intersect_tPlan.add(attack_plan.getCountry(j));
				}
			}
			int aTarget = o.getNoArmies();
			int aPlan = 0;
			for (int j = 1; j < attack_plan.getCountries().size(); j++) {
				if (attack_plan.getCountry(j).getOwner().equals(o)) {
					aPlan += attack_plan.getCountry(j).getArmies();
				}
			}
			if ((aTarget > 0) && (tTarget.size() >0)) {
				priorityTable[i+30] = (((((float)aPlan) / (float)aTarget) + ((float)tTarget_intersect_tPlan.size() / (float)tTarget.size())) / 2) * goaldist.getDistribution_A()[i];
			} else {
				priorityTable[i+30] = 0.0f;
			}
		}
		// Satisfying 18 goal:
		float a = 0; // number of territories, where more than 1 army is left behind.
		for (int i = 0; i < attack_plan.getCountries().size(); i++) {
			if (attack_plan.getArmiesToLeave(i) > 1) {
				a++;
			}
		}
		priorityTable[36] = (((((float)attack_plan.getCountries().size()-1.0f)) + currentPlayer.getNoTerritoriesOwned()) / (18.0f)) * (a / ((float)attack_plan.getCountries().size())) * goaldist.getDistribution_18();
		if 	(priorityTable[36] > 1) {
			priorityTable[36] = 1.0f;
		}
		
		// Satisfying 24 goal:
		priorityTable[37] = (((((float)attack_plan.getCountries().size()-1.0f)) + (float)currentPlayer.getNoTerritoriesOwned()) / (24.0f)) * goaldist.getDistribution_24();
		if 	(priorityTable[37] > 1) {
			priorityTable[37] = 1.0f;
		}

		/*for (int i = 0; i < goaldist.getFullDistribution().length; i++) {
			if ((goaldist.getFullDistribution()[i] == 0.0f) && priorityTable[i] > 0.0f) {
				int p = 0;
			}
		}*/
		
		
		
		// Simulating attackplans
		simulateAttackPlan(attack_plan, board);
		// Conquer and defend goal - can the continent be defended?
		for (int i = 0; i < out.length; i++) {
			/*if (canContinentBeDefended(attack_plan,board, i)) {
				priorityTable[i] *= 2;
			}*/
			if (!canContinentBeDefended(attack_plan,board, i)) {
				priorityTable[i] = 0;
			}
		}
		// Obstruct and defend goal - can the obstruction be defended?
		for (int i = 0; i < out.length; i++) {
			/*if (canObstructionBeDefended(attack_plan,board, i)) {
				priorityTable[i+24] *= 2;
			}*/
			if (!canObstructionBeDefended(attack_plan,board, i)) {
				priorityTable[i+24] = 0;
			}
		}		
		revertBoard(board);
		
		// Summing all together:
		float res = 0;
		for (int i = 0; i < priorityTable.length; i++) {
			res += priorityTable[i];
		}
		
		int num = doesAttackPlanConquerContinent(attack_plan, board);
		if (num > 0) {
			res *= (0.3*num)+1;
		}
		
		this.output = new O_RP_AttackPlanPriority();
		this.output.setPriority(Math.max(res,0));
		
		this.lastPriorityTable = priorityTable;
	}
	
	private float[] calcPriorityForOffensiveGoal(T_RP_AttackPlan attack_plan, List continents, float[] goalDistForGoalType) {
		float[] result = new float[continents.size()];
		for (int i = 0; i < continents.size(); i++) {
			Continent c = (Continent)continents.get(i);
			Vector tGoal = new Vector(); // the set of territories in the given continent not owned by the player.
			for (int j = 0; j < c.getTerritoriesContained().size(); j++) {
				if (!((Country)c.getTerritoriesContained().get(j)).getOwner().equals(currentPlayer)) {
					tGoal.add(c.getTerritoriesContained().get(j));
				}
			}
			Vector tGoal_intersect_tPlan = new Vector();
			for (int j = 1; j < attack_plan.getCountries().size(); j++) {
				if (tGoal.contains(attack_plan.getCountry(j))) {
					tGoal_intersect_tPlan.add(attack_plan.getCountry(j));
				}
			}
			if (tGoal.size() > 0) {
				result[i] = ((float)tGoal_intersect_tPlan.size() / (float)tGoal.size()) * goalDistForGoalType[i];
			} else {
				result[i] = 0.0f;
			}
		}
		return result;
	}

	private float[] calcPriorityForObstruct(T_RP_AttackPlan attack_plan, List continents, float[] goalDistForGoalType) {
		float[] result = new float[continents.size()];
		for (int i = 0; i < continents.size(); i++) {
			Continent c = (Continent)continents.get(i);
			boolean doesObstruct = false;
			
			for (int j = 1; j < attack_plan.getCountries().size(); j++) {
				if (c.getTerritoriesContained().contains(attack_plan.getCountry(j))) {
					doesObstruct = true;
					break;
				}
			}
			if (doesObstruct) {
				result[i] = goalDistForGoalType[i];
			} else {
				result[i] = 0.0f;
			}
		}
		return result;
	}

	/** returns how many continents the AP conquers. */
	private int doesAttackPlanConquerContinent(T_RP_AttackPlan attack_plan, T_Board board) {
		int res = 0;
		for (int i = 0; i < board.getContinents().size(); i++) {
			Continent c = (Continent)board.getContinents().get(i);
			Vector territoriesNotOwnedByPlayer = new Vector();
			for (int j = 0; j < c.getTerritoriesContained().size(); j++) {
				if (!((Country)c.getTerritoriesContained().get(j)).getOwner().equals(currentPlayer)) {
					territoriesNotOwnedByPlayer.add(((Country)c.getTerritoriesContained().get(j)));
				}
			}
			if ((territoriesNotOwnedByPlayer.size() > 0) && (attack_plan.containsAllCountries(territoriesNotOwnedByPlayer)))  {
				res++;
				if (((Continent)board.getContinents().get(i)).equals(currentPlayer.getMission().getContinent1()) || 
					((Continent)board.getContinents().get(i)).equals(currentPlayer.getMission().getContinent2())) {
					res++;
				}
			}
		}
		return res;
	}

	private void simulateAttackPlan(T_RP_AttackPlan attack_plan, T_Board board) {
		// save board
		for (int i = 0; i < board.getCountries().size(); i++) {
			this.memBoard_armies[i] = board.getCountryByInt(i).getArmies();
			this.memBoard_owners[i] = board.getCountryByInt(i).getOwner();
		}
		// simulate attack plan
		for (int i = 0; i < attack_plan.size()-1; i++) {
			int defending_armies = attack_plan.getCountry(i+1).getArmies();
			int armies_to_lose = Math.round((float)game.calcEstimatedArmyCost(defending_armies)); 
			int armies_possible_to_leave = attack_plan.getCountry(i).getArmies() - armies_to_lose;
			if (armies_possible_to_leave >= attack_plan.getArmiesToLeave(i)) {
				BadUtils.setArmies(attack_plan.getCountry(i+1), attack_plan.getCountry(i).getArmies() - armies_to_lose - attack_plan.getArmiesToLeave(i));
				BadUtils.setArmies(attack_plan.getCountry(i), attack_plan.getArmiesToLeave(i));
				attack_plan.getCountry(i).setOwner(this.currentPlayer);
			} else {
				break;
			}			 
		}
	}

	private boolean canContinentBeDefended(T_RP_AttackPlan attack_plan, T_Board board, int continentIndex) {
		boolean canDefend = true;
		T_InfluenceMap imap = new T_InfluenceMap(board,game);
		imap.calcAttackerArmiesLeft();
		Continent c = ((Continent)board.getContinents().get(continentIndex));
		for (int i = 0; i < c.getTerritoriesContained().size(); i++) {
			if (board.isBorderTerritory((Country)c.getTerritoriesContained().get(i))) {
				if (((Country)c.getTerritoriesContained().get(i)).getArmies() < imap.getValue(((Country)c.getTerritoriesContained().get(i)))) {
					canDefend = false;
				}
			}
		}
		return canDefend;
	}

	private void revertBoard(T_Board board) {
		for (int i = 0; i < board.getCountries().size(); i++) {
			 BadUtils.setArmies(board.getCountryByInt(i), this.memBoard_armies[i]);
			 board.getCountryByInt(i).setOwner(this.memBoard_owners[i]);
		}
	}

	private boolean canObstructionBeDefended(T_RP_AttackPlan attack_plan, T_Board board, int continentIndex) {
		boolean canDefend = false;
		T_InfluenceMap imap = new T_InfluenceMap(board,game);
		imap.calcAttackerArmiesLeft();
		Continent c = ((Continent)board.getContinents().get(continentIndex));
		for (int i = 0; i < c.getTerritoriesContained().size(); i++) {
			if ((((Country)c.getTerritoriesContained().get(i)).getOwner().equals(currentPlayer))) {
				if (((Country)c.getTerritoriesContained().get(i)).getArmies() > imap.getValue(((Country)c.getTerritoriesContained().get(i)))) {
					canDefend = true;
					break;
				}
			}
		}
		return canDefend;
	}

	public float[] getLastPriorityTable() {
		return lastPriorityTable;
	}
	
	// dt
	private void dt_run(T_RP_AttackPlan t_RP_AttackPlan, T_Board t_Board, O_MP_GoalDistribution o_MP_GoalDistribution) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	// nn
	public void nn_run(T_RP_AttackPlan attack_plan, T_Board board, O_MP_GoalDistribution goaldist) {
		GameData data = new GameData(game.getPlayers(), game.getPlayerIndex(currentPlayer));
		data.setAttackPlan(attack_plan);
		data.setBoard(board);
		data.setGoalDistribution(goaldist);
		float[] nnInput = new float[neuralNetwork.getNumberOfInputNodes()];
		output = new O_RP_AttackPlanPriority();
		int i = 0;
		// attack plan
		for (int territoryIndex = 0; territoryIndex < T_Game.NUMBER_OF_TERRITORIES; territoryIndex++) {
			if (data.getAttackPlan()[territoryIndex] == null) {
				nnInput[i] = 0.0f;
			} else {
				nnInput[i] = Float.parseFloat(data.getAttackPlan()[territoryIndex].getNNValue());
			}
			i++;
		}
		// board
		for (int territoryIndex = 0; territoryIndex < T_Game.NUMBER_OF_TERRITORIES; territoryIndex++) {
			nnInput[i] = Float.parseFloat(data.getBoard()[territoryIndex].getNNValue());
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
		output.setPriority(PriorityAttribute.getNNValueFromNNOutput(out[0]));
		
	}

	public void setNewCurrentPlayer(int playerIndex) {
		this.currentPlayer = (Player)game.getPlayers().get(playerIndex);
	}
	
}