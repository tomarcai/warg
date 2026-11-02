package risk.AI.Data_Structures.Module_Input;

import risk.AI.Data_Structures.T_RP_AttackPlan;
import risk.AI.Data_Structures.Module_Output.O_MP_GoalDistribution;
import risk.AI.Data_Structures.*;

public class I_RP_AttackPlanPriority {
	
	T_RP_AttackPlan attack_plan;
	T_Board board;
	O_MP_GoalDistribution goaldist;
	
	/** Creates a new instance of I_RP_AttackPlanPriority */
	public I_RP_AttackPlanPriority(T_RP_AttackPlan attack_plan, T_Board board, O_MP_GoalDistribution goaldist) {
		this.attack_plan = attack_plan;
		this.board = board;
		this.goaldist = goaldist;
	}
	
	public T_RP_AttackPlan getAttackPlan() {
		return this.attack_plan;
	}
	
	public T_Board getBoard() {
		return this.board;
	}
	
	public O_MP_GoalDistribution getGoalDistribution() {
		return this.goaldist;
	}

}
