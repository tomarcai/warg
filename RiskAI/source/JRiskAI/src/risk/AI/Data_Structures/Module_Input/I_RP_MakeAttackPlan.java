package risk.AI.Data_Structures.Module_Input;

import net.yura.domination.engine.core.Country;
import risk.AI.Data_Structures.Module_Output.*;
import risk.AI.Data_Structures.T_Board;
import risk.AI.Data_Structures.T_InfluenceMap;

public class I_RP_MakeAttackPlan {
	
	Country startCountry;
	Country targetCountry;
	O_MP_GoalDistribution goaldist;
	T_Board board;
	T_InfluenceMap attackerArmiesLeftMap;
		
	/** Creates a new instance of I_RP_MakeAttackPlan */
	public I_RP_MakeAttackPlan(Country startCountry, Country targetCountry, O_MP_GoalDistribution goaldist, T_Board board, T_InfluenceMap attackerArmiesLeftMap) {
		this.startCountry = startCountry;
		this.targetCountry = targetCountry;
		this.goaldist = goaldist;
		this.board = board;
		this.attackerArmiesLeftMap = attackerArmiesLeftMap;
	}

	public T_Board getBoard() {
		return board;
	}

	public T_InfluenceMap getAttackerArmiesLeftMap() {
		return attackerArmiesLeftMap;
	}

	public O_MP_GoalDistribution getGoalDistribution() {
		return goaldist;
	}

	public Country getStartCountry() {
		return startCountry;
	}

	public Country getTargetCountry() {
		return targetCountry;
	}
	
}
