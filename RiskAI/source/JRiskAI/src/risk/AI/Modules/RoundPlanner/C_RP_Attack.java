package risk.AI.Modules.RoundPlanner;

import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Mission;
import net.yura.domination.engine.core.Player;
import risk.AI.Data_Structures.*;
import risk.AI.Data_Structures.Module_Output.O_IG_WinningEstimate;
import risk.AI.Data_Structures.Module_Input.I_RP_ScoreAttackPlan;
import risk.AI.Modules.RoundPlanner.Attack.*;

public class C_RP_Attack {  

	private C_RP_Attack_DoAttack doAttack_module;
	private C_RP_Attack_ScoreAttackPlan scorePlan_module;
	
	private T_RP_AttackPlanListElement currentAttackPlan = null;
	private T_Game game;
	private boolean reviseAttackPlanNow;
	
	private String output;
	
	public C_RP_Attack(String score_type, T_Game game, Player currentPlayer, Mission ownMission, T_TrainingExampleWriter trainingExample) {
		this.game = game;
		this.scorePlan_module = new C_RP_Attack_ScoreAttackPlan(score_type, game, currentPlayer, trainingExample);
		this.doAttack_module = new C_RP_Attack_DoAttack(game, currentPlayer,ownMission);
	}
	
	public boolean doesAttackPlanNeedRevising() {
		return reviseAttackPlanNow;
	}
	
	public String getOutput() {
		return output;
	}
	
	/**
	 * @param attack_plans
	 * @param winningEstimate
	 * @param revisedAttackPlan Flag stating whether or not we are following the same attack plan. If the attack plan has been revised, then this flag is set to true.
	 */
	public void run(T_RP_AttackPlanList attack_plans, O_IG_WinningEstimate winningEstimate, boolean revisedAttackPlan) {
		if (revisedAttackPlan) {
			currentAttackPlan = calcBestAttackPlan(attack_plans);
		}
		if (currentAttackPlan != null) {
			this.doAttack_module.run(currentAttackPlan,winningEstimate,revisedAttackPlan);
			output = doAttack_module.getOutput();
			reviseAttackPlanNow = doAttack_module.doesAttackPlanNeedRevising();
		} else {
			this.reviseAttackPlanNow = true;
		}
	}
        
	public Country getAttackingCountry() {
		return doAttack_module.getAttackingCountry();
	}

	public int getAttackingCountry_ArmiesToLeave() {
		return doAttack_module.getAttackingCountry_ArmiesToLeave();
	}
	
	public T_RP_AttackPlanListElement getCurrentAttackPlan() {
		return this.currentAttackPlan;
	}
	
	public T_RP_AttackPlanListElement calcBestAttackPlan(T_RP_AttackPlanList attack_plans) {
		if (attack_plans.size() > 0) {
			int bestAttackPlanIndex = 0;
			float bestAttackPlanScore = 0;
			for (int i = 1; i < attack_plans.size(); i++) {
				scorePlan_module.run(new I_RP_ScoreAttackPlan(attack_plans.get(i).getPriority(),attack_plans.get(i).getEstimatedCost()));
				float score = scorePlan_module.getOutput().getScore();
				attack_plans.get(i).setScore(score);
				if (score > bestAttackPlanScore) {
					bestAttackPlanScore = score;
					bestAttackPlanIndex = i;
				}
			}
			return attack_plans.get(bestAttackPlanIndex);
		} else {
			return null;
		}
	}
}