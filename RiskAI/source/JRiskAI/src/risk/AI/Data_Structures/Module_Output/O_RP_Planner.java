package risk.AI.Data_Structures.Module_Output;

import risk.AI.Data_Structures.*;

public class O_RP_Planner {
	
	T_RP_AttackPlanList attack_plan;
	T_RP_DefenseList defense_list;
	
	/*public O_RP_Planner(T_RP_AttackPlanList attack_plan, T_RP_DefenseList defense_list) {
		this.attack_plan = attack_plan;
		this.defense_list = defense_list;
	}*/
	
	public T_RP_AttackPlanList getAttackPlanList() {
		return attack_plan;
	}	
	
	public T_RP_DefenseList getDefenseList() {
		return defense_list;
	}
	
	public void setAttackPlanList(T_RP_AttackPlanList attackPlanList) {
		this.attack_plan = attackPlanList;
	}
	
	public void setDefenseList(T_RP_DefenseList defense_list) {
		this.defense_list = defense_list;
	}
	
}