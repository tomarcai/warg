// This contains a list of attack plans.

package risk.AI.Data_Structures;

import java.util.*;

public class T_RP_AttackPlanList {
	
	Vector attackPlanList = new Vector();
	
	public void add(T_RP_AttackPlanListElement attack_plan) {
	    attackPlanList.add(attack_plan);
	}
	
	public T_RP_AttackPlanListElement get(int index) {
	    return (T_RP_AttackPlanListElement)attackPlanList.get(index);
	}
	
	public int size() {
	    return attackPlanList.size();
	}
	
	public void remove(int index) {
		attackPlanList.remove(index);
	}
}