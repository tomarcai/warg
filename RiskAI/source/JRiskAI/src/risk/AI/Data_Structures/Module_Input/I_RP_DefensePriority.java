package risk.AI.Data_Structures.Module_Input;

import net.yura.domination.engine.core.Country;
import risk.AI.Data_Structures.Module_Output.O_IG_NextMoveEstimate;
import risk.AI.Data_Structures.Module_Output.O_MP_GoalDistribution;
import risk.AI.Data_Structures.T_Board;

public class I_RP_DefensePriority {
	
	Country territory;
	T_Board board;
	O_IG_NextMoveEstimate nextMoveEstimate;
	O_MP_GoalDistribution goaldist;
	
	/** Creates a new instance of I_RP_DefensePriority */
	public I_RP_DefensePriority(Country territory, T_Board board, O_IG_NextMoveEstimate nextMoveEstimate, O_MP_GoalDistribution goaldist) {
		this.territory = territory;
		this.board = board;
		this.nextMoveEstimate = nextMoveEstimate;
		this.goaldist = goaldist;
	}

	public T_Board getBoard() {
		return board;
	}

	public O_IG_NextMoveEstimate getNextMoveEstimate() {
		return nextMoveEstimate;
	}

	public O_MP_GoalDistribution getGoalDistribution() {
		return goaldist;
	}

	public Country getTerritory() {
		return territory;
	}

}
