package risk.AI.Data_Structures;

import net.yura.domination.engine.core.Country;

public class T_RP_MergedPlanElement {
	
	private Country c;
	private float priority;
	private int estimated_cost;
	private int minimum_cost;
	private float score;
	private boolean isDefensePlan;
	private int attackerArmiesLeft = -1;
	
	public Country getCountry() {
		return c;
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
	
	public void setCountry(Country c) {
	    this.c = c;
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
	
	public void setIsDefensePlan(boolean isDefensePlan) {
		this.isDefensePlan = isDefensePlan;
	}
	
	public boolean getIsDefensePlan() {
		return isDefensePlan;
	}

	/**
	 * Used for training data only.
	 * @return returns the number of armies an attacker would have left on this territory.
	 */
	public int getAttackerArmiesLeft() {
		return attackerArmiesLeft;
	}

	/**
	 * Used for training data only.
	 */
	public void setAttackerArmiesLeft(int attackerArmiesLeft) {
		this.attackerArmiesLeft = attackerArmiesLeft;
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

}