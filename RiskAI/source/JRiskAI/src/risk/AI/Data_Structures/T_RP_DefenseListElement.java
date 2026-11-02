package risk.AI.Data_Structures;

import net.yura.domination.engine.core.Country;

public class T_RP_DefenseListElement {
	
	private Country c;
	private float priority;
	private int cost;
	private int attackerArmiesLeft = -1;
	
	public Country getCountry() {
		return c;
	}
	
	public float getPriority() {
		return priority;
	}
	
	public int getCost() {
		return cost;
	}
	
	public void setCountry(Country c) {
		this.c = c;
	}
	
	public void setPriority(float priority) {
		this.priority = priority;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	/**
	 * Used in training data only.
	 * @return returns the armies an attacker would have left in this territory.
	 */
	public int getAttackerArmiesLeft() {
		return attackerArmiesLeft;
	}

	/**
	 * Used in training data only.
	 */
	public void setAttackerArmiesLeft(int attackerArmiesLeft) {
		this.attackerArmiesLeft = attackerArmiesLeft;
	}

}