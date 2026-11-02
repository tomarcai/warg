package risk.AI.Data_Structures.Module_Input;

import risk.AI.Data_Structures.Module_Output.O_IG_MissionEstimate;
import risk.AI.Data_Structures.T_Board;
import risk.AI.Data_Structures.T_InfluenceMap;

public class I_InitialPlacement {
	
	T_Board board;
	O_IG_MissionEstimate missionEstimate;
	T_InfluenceMap attackerArmiesLeftMap;
	
	/** Creates a new instance of I_InitialPlacement */
	public I_InitialPlacement(T_Board board, O_IG_MissionEstimate missionEstimate, T_InfluenceMap attackerArmiesLeftMap) {
		this.board = board;
		this.missionEstimate = missionEstimate;
		this.attackerArmiesLeftMap = attackerArmiesLeftMap;
	}

	public T_Board getBoard() {
		return board;
	}

	public O_IG_MissionEstimate getMissionEstimate() {
		return missionEstimate;
	}

	public T_InfluenceMap getAttackerArmiesLeftMap() {
		return attackerArmiesLeftMap;
	}

}
