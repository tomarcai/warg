package risk.AI.Modules.RoundPlanner.Planner;

import net.yura.domination.engine.core.Player;
import risk.AI.Data_Structures.*;
import risk.AI.Data_Structures.Module_Output.O_RP_AttackPlanCost;
import risk.AI.Data_Structures.Module_Input.I_RP_AttackPlanCost;

public class C_RP_Planner_AP_Cost {
	
	private String type;
	private T_Game game;
	
	private O_RP_AttackPlanCost output;
	private C_Timing timer = new C_Timing();
	private T_TrainingExampleWriter trainingExample;
	private Player player;
	
	public C_RP_Planner_AP_Cost(String type, T_Game game, Player player, T_TrainingExampleWriter trainingExample) {
		this.type = type;
		this.game = game;
		this.player = player;
		this.trainingExample = trainingExample;
		trainingExample.setModuleLoadTime(game.getPlayerIndex(player), "rp_ap_cost", 0);
	}
	
	public void run(I_RP_AttackPlanCost input, boolean isOpponentFramework) {
		if (type.equals("random") || (type.equals("script"))) {
			timer.startTimer();
			script_run(input.getAttackPlan());
			timer.endTimer();
			if (!isOpponentFramework) {
				trainingExample.addModuleRunTime(game.getPlayerIndex(player), "rp_ap_cost", timer.getTimer());
			}
		}
	}
	
	public O_RP_AttackPlanCost getOutput() {
		return output;
	}
	
	// Script

	private void script_run(T_RP_AttackPlan attack_plan) {
		output = new O_RP_AttackPlanCost();
		output.setMinimumCost(calcMinimumCost(attack_plan));
		output.setEstimatedCost(calcEstimatedCost(attack_plan));
	}
	
	// Common methods
	
	private int calcMinimumCost(T_RP_AttackPlan attack_plan) {
		// Armies to leave behind
		int defenseCost = 0; 
		for(int i = 0; i < attack_plan.size() - 1; i++) {
			defenseCost += attack_plan.getArmiesToLeave(i);
		}
		return defenseCost;
	}

	private int calcEstimatedCost(T_RP_AttackPlan attack_plan) {
		double attackCost = 0.0;
		int numDefenders;
		// Armies lost by attacking the territories
		for(int i = 1; i < attack_plan.size(); i++) {
			numDefenders = attack_plan.getCountry(i).getArmies();
			attackCost += game.calcEstimatedArmyCost(numDefenders);
		}
		return (int)attackCost + output.getMinimumCost();
	}
	
	public void setNewCurrentPlayer(int playerIndex) {
		this.player = (Player)game.getPlayers().get(playerIndex);
	}

}