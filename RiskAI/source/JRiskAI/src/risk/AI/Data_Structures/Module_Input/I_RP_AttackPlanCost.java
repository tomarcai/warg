package risk.AI.Data_Structures.Module_Input;

import risk.AI.Data_Structures.T_RP_AttackPlan;

/**
 *
 * @author Administrator
 */
public class I_RP_AttackPlanCost {
	
	T_RP_AttackPlan attack_plan;
	
	/** Creates a new instance of I_RP_AttackPlanCost */
	public I_RP_AttackPlanCost(T_RP_AttackPlan attack_plan) {
		this.attack_plan = attack_plan;
	}
	
	public T_RP_AttackPlan getAttackPlan() {
		return attack_plan;
	}
	
}
