package risk.AI.Modules.RoundPlanner.Planner;

import java.util.*;

import net.yura.domination.engine.core.Card;
import net.yura.domination.engine.core.Player;
import risk.AI.Data_Structures.Module_Input.I_RP_DiscardAttackPlan;
import risk.AI.Data_Structures.Module_Output.O_RP_DiscardAttackPlan;
import risk.AI.Data_Structures.*;

public class C_RP_Planner_DiscardAttackPlan {
	
	private String type;
	private Random rand = new Random();
	private T_RP_AttackPlanListElement attack_plan;
	private O_RP_DiscardAttackPlan output;
	private int numberOfRecinforcements;
	private int numberOfRiskCards;
	private T_Game game;
	private Player player;
	private T_TrainingExampleWriter trainingExample;
	private C_Timing timer = new C_Timing();	
	
	public C_RP_Planner_DiscardAttackPlan(String type, Player player, T_Game game, T_TrainingExampleWriter trainingExample) {
		this.type = type;
		this.player = player;
		this.game = game;
		this.trainingExample = trainingExample;
		trainingExample.setModuleLoadTime(game.getPlayerIndex(player), "rp_discardattackplan", 0);
	}
	
	// armies_recieved could be found by calling Player.getExtraArmies, but we might not be in the "reinforce phase" when this module is runned. So it should be calculated manually!
	public void run(I_RP_DiscardAttackPlan input, boolean isOpponentFramework) {
		if (type.equals("random")) {
			random_run();
		}
		if (type.equals("script")){
			timer.startTimer();
			script_run(input.getPlan().getPriority(), input.getPlan().getMinimumCost(), input.getPlan().getEstimatedCost(), input.getArmiesReceived(), input.getRiskCards(), input.getPlan().getAttackPlan().getCountry(0).getArmies()-1, input.getBeginningOfTurn());
			timer.endTimer();
			if (!isOpponentFramework) {
				trainingExample.addModuleRunTime(game.getPlayerIndex(player), "rp_discardattackplan", timer.getTimer());
			}
			//trainingExample.saveModule(input,output,player);
		}
	}
	
	/**
	 * The output from the module
	 * @return Returns <CODE>true</CODE> if the attack plan should be discarded
	 */
	public O_RP_DiscardAttackPlan getOutput() {
		return output;
	}
	
	// Random
	
	private void random_run() {
		// Give the plan a random score
		output = new O_RP_DiscardAttackPlan();
		output.setDiscard(rand.nextBoolean());
	}
	
	// Script
	private void script_run(float priority, float minimumCost, float estimatedCost, int reinforcements, Vector riskCards, int armiesInStart, boolean beginningOfTurn){
		output = new O_RP_DiscardAttackPlan();
		if (beginningOfTurn) {
			Card[] bestCards;
			if(!riskCards.isEmpty()){
				bestCards = game.getBestCardCombination(riskCards);
			}else{
				bestCards = null;
			}
			Card bestCard1 = null;
			Card bestCard2 = null;
			Card bestCard3 = null;
			if(bestCards != null){
				bestCard1 = bestCards[0];
				bestCard2 = bestCards[1];
				bestCard3 = bestCards[2];
			}
			if(bestCard1 != null && bestCard2 != null && bestCard3 != null){
				reinforcements = reinforcements + game.getCardCombinationScore(bestCard1, bestCard2, bestCard3);
			}
		} else {
			reinforcements = 0;
		}
		if (estimatedCost <= (armiesInStart + reinforcements)){
			output.setDiscard(false);
		} else {
			if ((minimumCost <= (armiesInStart + reinforcements)) && (estimatedCost*1.1 <= reinforcements)) {
				output.setDiscard(false);
			} else {
				int futureReinforcements = 0;
				if (beginningOfTurn) {
					futureReinforcements = ((int)game.calcFutureReinforcements(player, 3).lastElement());
				}
				if ((priority >= 0.9) && (estimatedCost <= (armiesInStart + futureReinforcements))){
					output.setDiscard(false);
				} else {
					output.setDiscard(true);
				}
			}
		}
		if (priority < game.getAISettings().discardAttackPlanPriority) {
			output.setDiscard(true);
		}
		/*
		if (output.getDiscard()) {
			System.out.println("Plan discarded:");
			System.out.println("  - Priority = " + priority);
			System.out.println("  - EstCost = " + estimatedCost);
		}
		*/
	}

	public void setNewCurrentPlayer(int playerIndex) {
		this.player = (Player)game.getPlayers().get(playerIndex);
	}

}
