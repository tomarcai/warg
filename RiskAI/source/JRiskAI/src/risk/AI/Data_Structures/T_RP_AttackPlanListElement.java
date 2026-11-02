// An element in the AttackPlanList. This is in fact a single attack plan, with country, priority, estimated and minimum cost.

package risk.AI.Data_Structures;

public class T_RP_AttackPlanListElement {
	
	private T_RP_AttackPlan attack_plan;
	private float priority;
	private int estimated_cost;
	private int minimum_cost;
	private int cost_so_far = 0;
	private float score;
	
	private float[] priorityTable; // to be removed!!
	
	public T_RP_AttackPlan getAttackPlan() {
		return attack_plan;
	}
	
	public float getPriority() {
		return priority;
	}
	
	public int getEstimatedCost() {
		return estimated_cost;
	}
	
	public int getMinimumCost() {
		return minimum_cost;
	}
	
	public float getScore() {
		return score;
	}
	
	public void setAttackPlan(T_RP_AttackPlan attack_plan) {
		this.attack_plan = attack_plan;
	}
	
	public void setPriority(float priority) {
		this.priority = priority;
	}
	
	public void setEstimatedCost(int estimated_cost) {
		this.estimated_cost = estimated_cost;
	}
	
	public void setMinimumCost(int minimum_cost) {
		this.minimum_cost = minimum_cost;
	}
	
	public void addCostToCostSpent(int cost) {
		this.cost_so_far += cost;
	}
	
	public int getCostSpent() {
		return this.cost_so_far;
	}
	
	public void setScore(float score) {
		this.score = score;
	}
	
	public String toString() {
		return "Attack Plan: (c:" + getMinimumCost() + "/" + getEstimatedCost() + ",p:" + getPriority() + "): \n" + attack_plan + priorityTableToString();
	}

	public float[] getPriorityTable() {
		return priorityTable;
	}

	public void setPriorityTable(float[] priorityTable) {
		this.priorityTable = priorityTable;
	}

	private String priorityTableToString() {
		String str = "\n";
		str = str + "C&D:[";
		for (int i = 0; i < 6; i++) {
			str = str + String.valueOf(this.priorityTable[i]);
			if (i != 5) {
				str = str + ",";
			}
		}
		str = str + "] C:[";
		for (int i = 0; i < 6; i++) {
			str = str + String.valueOf(this.priorityTable[i+6]);
			if (i != 5) {
				str = str + ",";
			}
		}
		str = str + "] D:[";
		for (int i = 0; i < 6; i++) {
			str = str + String.valueOf(this.priorityTable[i+12]);
			if (i != 5) {
				str = str + ",";
			}
		}
		str = str + "] O:[";
		for (int i = 0; i < 6; i++) {
			str = str + String.valueOf(this.priorityTable[i+18]);
			if (i != 5) {
				str = str + ",";
			}
		}
		str = str + "] O&D:[";
		for (int i = 0; i < 6; i++) {
			str = str + String.valueOf(this.priorityTable[i+24]);
			if (i != 5) {
				str = str + ",";
			}
		}
		str = str + "] A:[";
		for (int i = 0; i < 6; i++) {
			str = str + String.valueOf(this.priorityTable[i+30]);
			if (i != 5) {
				str = str + ",";
			}
		}
		str = str + "] 18:["+String.valueOf(this.priorityTable[36]);
		str = str + "] 24:["+String.valueOf(this.priorityTable[37])+"]";
		return str;
	}
	
}