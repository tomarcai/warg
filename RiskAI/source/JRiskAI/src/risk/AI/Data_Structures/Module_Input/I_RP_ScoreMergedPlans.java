package risk.AI.Data_Structures.Module_Input;

public class I_RP_ScoreMergedPlans {
	
	private float priority;
	private int cost;
	private boolean isDefensePlan;
	
	/** Creates a new instance of I_ScoreMergedPlans */
	public I_RP_ScoreMergedPlans(float priority, int cost, boolean isDefensePlan) {
		this.cost = cost;
		this.priority = priority;
		this.isDefensePlan = isDefensePlan;
	}
	
	public float getPriority() {
		return priority;
	}
	
	public int getCost() {
		return cost;
	}

	public boolean getIsDefensePlan() {
		return isDefensePlan;
	}

}
