package risk.AI.Data_Structures.Module_Output;

import risk.AI.Data_Structures.*;

public class O_RP_AttackPlanCost {
	
	// content[] is a size 2 array of integers. content[0] is the minimum_cost and content[1] is the estimated_cost
	int estimated_cost;
    int minimum_cost;
	
	public O_RP_AttackPlanCost() {
	}
	
	public int getEstimatedCost() {
		return estimated_cost;
	}	
	
	public int getMinimumCost() {
		return minimum_cost;
	}	
	
	public void setEstimatedCost(int cost) {
		estimated_cost = cost;
	}	
	
	public void setMinimumCost(int cost) {
		minimum_cost = cost;
	}	
}