package risk.AI.Modules.RoundPlanner.Reinforce;

import net.yura.domination.engine.core.Card;
import net.yura.domination.engine.core.Player;
import risk.AI.Data_Structures.Module_Output.O_RP_CashCards;
import risk.AI.Data_Structures.*;
import java.util.*;
import risk.AI.Data_Structures.Module_Input.I_RP_CashCards;

public class C_RP_Reinforce_CashCards {

	private String type;
	private Random rand = new Random();
	private T_TrainingExampleWriter trainingExample;
	private T_Game game;
	private Player player;
	private C_Timing timer = new C_Timing();
	private O_RP_CashCards output;
	
	public C_RP_Reinforce_CashCards(String type, Player player, T_Game game, T_TrainingExampleWriter trainingExample) {
		this.type = type;
		this.game = game;
		this.player = player;
		this.trainingExample = trainingExample;
		trainingExample.setModuleLoadTime(game.getPlayerIndex(player), "rp_cashcards", 0);
	}
	
	public void run(I_RP_CashCards input) {
		if (type.equals("random")) {
			random_run();
		} else
		if (type.equals("script")) {
			timer.startTimer();
			script_run(input.getPlanList(),input.getArmiesReceived(),input.getOwnRiskCards());
			timer.endTimer();
			trainingExample.addModuleRunTime(game.getPlayerIndex(player), "rp_cashcards", timer.getTimer());
			trainingExample.saveModule(input,output,player);
		}
	}
	
	public O_RP_CashCards getOutput() {
		return output;
	}
	
	// Random
	
	private void random_run() {
		output = new O_RP_CashCards();
		output.setCashCards(rand.nextBoolean());
	}
	
	// Script
	
	private void script_run(T_RP_MergedPlanList planList, int armies_recieved, Vector riskCards) {
		boolean output = false;
		if (riskCards.size() > 4) {
			output = true;
		} else {
			output = false;
			Card[] bestCombi = game.getBestCardCombination(riskCards);
			if (bestCombi != null) {
				int rMin = armies_recieved;
				int cardValue = game.getCardCombinationScore(bestCombi[0],bestCombi[1],bestCombi[2]);
				int rMax = cardValue + rMin;
				if (rMax != rMin) { // Best card combination yields 0 - should not happen, since it has been checked that cards can be traded.
					if (cardValue == 10) { // If the cards yields 10 armies, trade right away.
						output = true;
					} else {
						if (planList.size() > 0) {
							T_RP_MergedPlanList sorted_list = planList.getListSortedByScore();
							T_RP_MergedPlanElement e = sorted_list.get(0);
							if (e.getPriority() > 0.9) {
								if (e.getEstimatedCost() < (e.getCountry().getArmies()+rMax)) { // Can cash cards pay the best plan's cost?
									output = true;
								} 
							}
						}
						// See if a better combination could come later:
						if (!output) {
							if (riskCards.size() == 3) {
								if ((cardValue == 6) || (cardValue == 8)) {
									output = true;
								} else 
								if (riskCards.size() == 4) {
									Card c1 = (Card)riskCards.get(0);
									Card c2 = (Card)riskCards.get(1);
									Card c3 = (Card)riskCards.get(2);
									Card c4 = (Card)riskCards.get(3);
									if ((c1.getName().equals(c2.getName())) && 
										(c2.getName().equals(c3.getName())) && 
										(c3.getName().equals(c4.getName()))) {
										output = true;
									}
								}
							}

						}
					}
				}
			} 
		}
		this.output = new O_RP_CashCards();
		this.output.setCashCards(output);
	}

}