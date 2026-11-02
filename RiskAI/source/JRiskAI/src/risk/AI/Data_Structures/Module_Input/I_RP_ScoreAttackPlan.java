package risk.AI.Data_Structures.Module_Input;

public class I_RP_ScoreAttackPlan {
	
	private float priority;
	private int cost;
	
	/** Creates a new instance of I_RP_ScoreAttackPlan */
	public I_RP_ScoreAttackPlan(float priority, int cost) {
		this.cost = cost;
		this.priority = priority;
	}
	
	public float getPriority() {
		return priority;
	}
	
	public int getCost() {
		return cost;
	}
	
}
