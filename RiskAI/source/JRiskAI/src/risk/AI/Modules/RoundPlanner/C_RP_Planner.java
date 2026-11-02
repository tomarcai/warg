package risk.AI.Modules.RoundPlanner;

import net.yura.domination.engine.core.Continent;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Player;
import risk.AI.Data_Structures.*;
import risk.AI.Data_Structures.Module_Output.*;
import risk.AI.Data_Structures.Module_Input.*;
import risk.AI.Data_Structures.T_RP_AttackPlan;
import risk.AI.Modules.RoundPlanner.Planner.*;
import risk.AI.Techniques.Pathfinding.*;
import java.util.*;

public class C_RP_Planner {
	
	private C_RP_Planner_AP_Cost ap_cost_module;
	private C_RP_Planner_AP_Priority ap_priority_module;
	private C_RP_Planner_De_Cost de_cost_module;
	private C_RP_Planner_De_Priority de_priority_module;
	private C_RP_Planner_DiscardAttackPlan discard_module;
	private C_RP_Planner_MakeAttackPlan makeattackplan_module;

	private O_RP_Planner output;
	private Player currentPlayer;
	private T_Game game;
	
	/**
	 * @param types types[0]: type of makeAttackPlan.<br>
	 * types[1]: type of AP_Cost.<br>
	 * types[2]: type of AP_Priority.<br>
	 * types[3]: type of ScoreAttackPlan.<br>
	 * types[4]: type of De_Cost.<br>
	 * types[5]: type of De_Priority.<br>
	 */
	public C_RP_Planner(String[] types, T_Game game, Pathfinder pathfinder, Player currentPlayer, T_TrainingExampleWriter trainingExample) {
		this.currentPlayer = currentPlayer;
		this.game = game;
		discard_module = new C_RP_Planner_DiscardAttackPlan(types[3], currentPlayer, game, trainingExample);
		de_cost_module = new C_RP_Planner_De_Cost(types[4], currentPlayer, game, trainingExample);
		de_priority_module = new C_RP_Planner_De_Priority(types[5], currentPlayer, game, trainingExample);
		ap_cost_module = new C_RP_Planner_AP_Cost(types[1], game, currentPlayer, trainingExample);
		ap_priority_module = new C_RP_Planner_AP_Priority(types[2], game, currentPlayer, trainingExample);
		makeattackplan_module = new C_RP_Planner_MakeAttackPlan(types[0], game, de_cost_module, pathfinder, trainingExample, currentPlayer);
	}
	
	public O_RP_Planner getOutput() {
		return output;
	}
	
	public void run(O_MP_GoalDistribution gd, T_Board board, T_Opp_RiskCards opp_riskcards, O_IG_NextMoveEstimate opp_nextmove, T_InfluenceMap attackerArmiesLeftMap, boolean beginningOfTurn, boolean hasReceivedCard, boolean isOpponentFramework) {
		// First update if any of the goals already have been fulfilled.
		updateGoalDistribution(board, gd, attackerArmiesLeftMap);
		// Make attack plans.
		Vector attack_plans = new Vector();
		for (int i = 0; i < board.getCountryCount(); i++) {
			if ((beginningOfTurn) || (board.getCountryByInt(i).getOwner().equals(currentPlayer)) && (board.getCountryByInt(i).getArmies() > 1)) {
				for (int j = 0; j < board.getCountryCount(); j++) {
					if (!board.getCountryByInt(j).getOwner().equals(currentPlayer)) {
						// First an attack plan with pure A* is made.
						makeattackplan_module.run(new I_RP_MakeAttackPlan(board.getCountryByInt(i), board.getCountryByInt(j), gd, board, attackerArmiesLeftMap), isOpponentFramework);
						for (int k = 0; k < makeattackplan_module.getOutput().size(); k++) {
							attack_plans.add(makeattackplan_module.getOutput().getAttackPlan(k));
						}
					}
				}
			}
		}
		// Calculate cost for each attack plan
		Vector attack_plan_cost = new Vector();
		for (int i = 0; i < attack_plans.size(); i++) {
			ap_cost_module.run(new I_RP_AttackPlanCost((T_RP_AttackPlan)attack_plans.get(i)), isOpponentFramework);
			attack_plan_cost.add(ap_cost_module.getOutput());
		}
		// Calculate priority for each attack plan
		Vector attack_plan_priority = new Vector();
		Vector attack_plan_priorityTable = new Vector(); // to be removed!! // to be removed!!
		for (int i = 0; i < attack_plans.size(); i++) {
			ap_priority_module.run(new I_RP_AttackPlanPriority((T_RP_AttackPlan)attack_plans.get(i),board,gd), isOpponentFramework);
			attack_plan_priority.add(ap_priority_module.getOutput().getPriority());
			attack_plan_priorityTable.add(ap_priority_module.getLastPriorityTable());
		}
		// Attack plan + cost + priority is combined
		// T_RP_AttackPlanListElement[] attack_plans_array = new T_RP_AttackPlanListElement[attack_plans.size()];
		T_RP_AttackPlanList combined_attack_plans = new T_RP_AttackPlanList();
		// In case all plans are discarded, then a simple attack plan is needed (if the player has not received any Risk cards yet).
		T_RP_AttackPlanList simpleAttackPlans = new T_RP_AttackPlanList();
		for (int i = 0; i < attack_plans.size(); i++) {
			//attack_plans_array[i] = new T_RP_AttackPlanListElement((T_RP_AttackPlan)attack_plans.get(i),(Float)attack_plan_priority.get(i),est_cost,min_cost);
			T_RP_AttackPlanListElement p = new T_RP_AttackPlanListElement();
			p.setAttackPlan((T_RP_AttackPlan)attack_plans.get(i));
			p.setEstimatedCost(((O_RP_AttackPlanCost)attack_plan_cost.get(i)).getEstimatedCost());
			p.setMinimumCost(((O_RP_AttackPlanCost)attack_plan_cost.get(i)).getMinimumCost());
			p.setPriority((Float)attack_plan_priority.get(i));
			
			p.setPriorityTable((float[])attack_plan_priorityTable.get(i)); // to be removed!!

			if (p.getAttackPlan().size() == 2) {
				simpleAttackPlans.add(p);
			}
			
			combined_attack_plans.add(p);
		}
		
		//Math.max((int)(currentPlayer.getNoTerritoriesOwned()/3),3)+calcContinentReinforcements(board);
		
		/*
		for (int i = 0; i < combined_attack_plans.size(); i++) {
			System.out.print(this.currentPlayer.getName() + ", Attack Plan: (c:"+combined_attack_plans.get(i).getEstimatedCost()+",p:"+combined_attack_plans.get(i).getPriority()+"): ");
			String str = "";
			for (int j = 0; j < combined_attack_plans.get(i).getAttackPlan().size(); j++) {
				str = str + combined_attack_plans.get(i).getAttackPlan().getCountry(j).getName() + ",";
			}
			System.out.println(str);
		}
		System.out.println("-----------------");
		*/
		
		
		
		// The combined attack_plans are sent to the discard attack plan module
		Vector risk_cards = currentPlayer.getCards();
		int armies_received = game.getNumberOfReinforcements(currentPlayer);
		int planIndex = 0;
		while (planIndex < combined_attack_plans.size()) {
			discard_module.run(new I_RP_DiscardAttackPlan(combined_attack_plans.get(planIndex), board, armies_received, risk_cards, beginningOfTurn), isOpponentFramework);
			if (discard_module.getOutput().getDiscard()) {
				// Discard plan
				combined_attack_plans.remove(planIndex);
			} else {
				planIndex++;
			}
		}
		/*
		for (int i = 0; i < combined_attack_plans.size(); i++) {
			System.out.print(this.currentPlayer.getName() + ", Attack Plan: (c:"+combined_attack_plans.get(i).getEstimatedCost()+",p:"+combined_attack_plans.get(i).getPriority()+"): ");
			String str = "";
			for (int j = 0; j < combined_attack_plans.get(i).getAttackPlan().size(); j++) {
				str = str + combined_attack_plans.get(i).getAttackPlan().getCountry(j).getName() + "(" + combined_attack_plans.get(i).getAttackPlan().getArmiesToLeave(j) + ")" + ",";
			}
			System.out.println(str);
		}
		System.out.println("-----------------");
		*/
		if ((combined_attack_plans.size() == 0) && (!hasReceivedCard)) {
			combined_attack_plans = simpleAttackPlans;
		}
		
		output = new O_RP_Planner();
		output.setAttackPlanList(combined_attack_plans);
		makeDefenseList(board, opp_nextmove, gd, attackerArmiesLeftMap, isOpponentFramework);
	}
	
	private void updateGoalDistribution(T_Board board, O_MP_GoalDistribution gd, T_InfluenceMap attackerArmiesLeftMap) {
		// Conquer and defend, Conquer, Defend.
		float[] gd_cd = gd.getDistribution_CD();
		float[] gd_c = gd.getDistribution_C();
		float[] gd_d = gd.getDistribution_D();
		for (int i = 0; i < board.getContinents().size(); i++) {
			if (((Continent)board.getContinents().get(i)).isOwned(currentPlayer)) {
				// If a continent has been conquered, then all importance is moved to the defense of that continent (but only if the importance of CD is larger than D).
				if (gd_cd[i] > gd_d[i]) {
					gd_d[i] = gd_cd[i];
				}
				// The Conquer and defend goal has been fulfilled.
				gd_cd[i] = 0;
				// The Conquer goal has also been fulfilled.
				gd_c[i] = 0;
			}
		}
		float[] gd_a = gd.getDistribution_A();
		for (int i = 0; i < game.getPlayers().size(); i++) {
			if (((Player)game.getPlayers().get(i)).getNoTerritoriesOwned() == 0) {
				gd_a[i] = 0;
			}
		}
		// Obstruct, Obstruct and Defend.
		float[] gd_o = gd.getDistribution_O();
		float[] gd_od = gd.getDistribution_OD();
		for (int i = 0; i < board.getContinents().size(); i++) {
			Continent c = (Continent)board.getContinents().get(i);
			int j = 0;
			while ((j < c.getTerritoriesContained().size()) &&
				(!((Country)c.getTerritoriesContained().get(j)).getOwner().equals(currentPlayer))) {
				j++;
			}
			if (j < c.getTerritoriesContained().size()) {
				// If the player occupies a territory in the given continent, then the obstruct goal for that continent has been fulfilled.
				gd_o[i] = 0;
				Country co = (Country)c.getTerritoriesContained().get(j);
				de_cost_module.run(new I_RP_DefenseCost(co, board, attackerArmiesLeftMap.getValue(co)), true);
				// If the player can defend that continent, then the obstruct and defend goal has been fulfilled.
				if (de_cost_module.getOutput().getCost() < co.getArmies()) {
					gd_od[i] = 0;
				}
			}
			
		}
	}

	public void makeDefenseList(T_Board board, O_IG_NextMoveEstimate opp_nextmove, O_MP_GoalDistribution gd, T_InfluenceMap attackerArmiesLeftMap, boolean isOpponentFramework) {
		T_RP_DefenseList defense_list = new T_RP_DefenseList();
		
		// Calculate cost and priority for each owned territory
		for (int i = 0; i < currentPlayer.getNoTerritoriesOwned(); i++) {
			T_RP_DefenseListElement element = new T_RP_DefenseListElement();
			Country c = (Country)currentPlayer.getTerritoriesOwned().get(i);

			element.setCountry(c);
			
			de_cost_module.run(new I_RP_DefenseCost(c, board, attackerArmiesLeftMap.getValue(c)), isOpponentFramework);
			int cost = de_cost_module.getOutput().getCost();
			if (cost > 0) {
				element.setCost(cost);
				de_priority_module.run(new I_RP_DefensePriority(c, board, opp_nextmove, gd), isOpponentFramework);
				float priority = de_priority_module.getOutput().getPriority();
				if (priority > 0) {
					element.setPriority(priority);
					element.setAttackerArmiesLeft(attackerArmiesLeftMap.getValue(c));
					defense_list.add(element);
				}
			}
		}
		output.setDefenseList(defense_list);
	}

	public C_RP_Planner_AP_Cost getAP_Cost_Module() {
		return ap_cost_module;
	}

	public void setNewCurrentPlayer(int playerIndex) {
		this.currentPlayer = (Player)game.getPlayers().get(playerIndex);
		this.ap_cost_module.setNewCurrentPlayer(playerIndex);
		this.ap_priority_module.setNewCurrentPlayer(playerIndex);
		this.de_cost_module.setNewCurrentPlayer(playerIndex);
		this.de_priority_module.setNewCurrentPlayer(playerIndex);
		this.discard_module.setNewCurrentPlayer(playerIndex);
		this.makeattackplan_module.setNewCurrentPlayer(playerIndex);
	}
	
}