/*
 * O_RP_MakeSimpleAttackPlan.java
 *
 * Created on 8. marts 2006, 13:58
 */

package risk.AI.Data_Structures.Module_Output;

import risk.AI.Data_Structures.T_RP_AttackPlan;

public class O_RP_MakeSimpleAttackPlan {
	
	private T_RP_AttackPlan plan;
	
	/** Creates a new instance of O_RP_MakeSimpleAttackPlan */
	public O_RP_MakeSimpleAttackPlan(T_RP_AttackPlan plan) {
		this.plan = plan;
	}
	
	public T_RP_AttackPlan getAttackPlan() {
		return plan;
	}

}
