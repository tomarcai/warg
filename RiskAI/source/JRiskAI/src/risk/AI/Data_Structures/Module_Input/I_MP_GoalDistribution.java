package risk.AI.Data_Structures.Module_Input;

import risk.AI.Data_Structures.*;
import risk.AI.Data_Structures.Module_Output.O_IG_MissionEstimate;
import risk.AI.Data_Structures.Module_Output.O_IG_WinningEstimate;
import risk.AI.Data_Structures.Module_Output.O_IG_NextMoveEstimate;
import risk.AI.Data_Structures.Module_Output.O_IG_ContinentEstimate;
import risk.AI.Data_Structures.Module_Output.O_IG_Ownership;
import risk.AI.Modules.AI_Framework;
/**
 *
 * @author Administrator
 */
public class I_MP_GoalDistribution {
	
	O_IG_MissionEstimate missionEstimate;
	O_IG_NextMoveEstimate nextmoveEstimate;
	O_IG_ContinentEstimate continentEstimate;
	O_IG_WinningEstimate winningEstimate;
	O_IG_Ownership ownership;
	
	/** Creates a new instance of I_IG_MissionEstimate */
	public I_MP_GoalDistribution(O_IG_MissionEstimate missionEstimate, O_IG_NextMoveEstimate nextmoveEstimate, O_IG_ContinentEstimate continentEstimate, O_IG_WinningEstimate winningEstimate, O_IG_Ownership ownership) {
		this.missionEstimate = missionEstimate;
		this.winningEstimate = winningEstimate;
		this.nextmoveEstimate = nextmoveEstimate;
		this.continentEstimate = continentEstimate;
		this.ownership = ownership;
	}
	
	public O_IG_MissionEstimate getMissionEstimate() {
		return missionEstimate;
	}
	
	public O_IG_NextMoveEstimate getNextMoveEstimate() {
		return nextmoveEstimate;
	}
	
	public O_IG_ContinentEstimate getContinentEstimate() {
		return continentEstimate;
	}
	
	public O_IG_WinningEstimate getWinningEstimate() {
		return winningEstimate;
	}
	
	public O_IG_Ownership getOwnership(){
		return ownership;
	}
	
}
