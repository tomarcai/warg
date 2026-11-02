package risk.AI.Data_Structures.Module_Input;

import risk.AI.Data_Structures.*;
import risk.AI.Data_Structures.Module_Output.O_MP_GoalDistribution;
import risk.AI.Data_Structures.Module_Output.O_IG_NextMoveEstimate;
import risk.AI.Techniques.Pathfinding.AStar;

public class I_RP_FortifyTransfer {
	
	T_RP_DefenseList defenseList;
	O_MP_GoalDistribution goaldist;
	T_Board board;
	
	/** Creates a new instance of I_RP_FortifyTransfer */
	public I_RP_FortifyTransfer(T_RP_DefenseList defenseList, O_MP_GoalDistribution goals, T_Board board) {
		this.defenseList = defenseList;
		this.goaldist = goals;
		this.board = board;
	}

	public T_RP_DefenseList getDefenseList() {
		return defenseList;
	}

	public O_MP_GoalDistribution getGoalDistribution() {
		return goaldist;
	}

	public T_Board getBoard() {
		return board;
	}
	

}
