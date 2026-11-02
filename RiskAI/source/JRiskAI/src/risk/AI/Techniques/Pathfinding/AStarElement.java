package risk.AI.Techniques.Pathfinding;

import net.yura.domination.engine.core.Country;

public class AStarElement {
	
	private int heuristic = 0;
	private Country parent = null;
	private int score = 0;
	private int stepCost = 0;
	
	public AStarElement() {
		
	}
	
	public int getHeuristic() {
		return this.heuristic;
	}
	
	public Country getParent() {
		return this.parent;
	}
	
	public int getScore() {
		return this.score;
	}
	
	public int getStepCost() {
		return this.stepCost;
	}
	
	public void setHeuristic(int value) {
		this.heuristic = value;
	}
	
	public void setParent(Country country) {
		this.parent = country;
	}
	
	public void updateScore() {
		this.score = this.heuristic + this.stepCost;
	}
	
	public void setStepCost(int value) {
		this.stepCost = value;
	}
	
	public void resetValues() {
		int heuristic = 0;
		Country parent = null;
		int score = 0;
		int stepCost = 0;
	}
}