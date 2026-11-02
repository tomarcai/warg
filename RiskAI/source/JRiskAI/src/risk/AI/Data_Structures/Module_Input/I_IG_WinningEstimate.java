package risk.AI.Data_Structures.Module_Input;

import risk.AI.Data_Structures.*;
import risk.AI.Data_Structures.Module_Output.O_IG_ContinentEstimate;
import risk.AI.Data_Structures.Module_Output.O_IG_MissionEstimate;
import risk.AI.Modules.AI_Framework;
/**
 *
 * @author Administrator
 */
public class I_IG_WinningEstimate {
	
	private T_Board board;
	private T_Opp_RiskCards opp_riskcards;
	private O_IG_MissionEstimate missionestimate;
	private boolean beginningOfTurn;
	private O_IG_ContinentEstimate continentEstimate;
	
	/** Creates a new instance of I_IG_MissionEstimate */
	public I_IG_WinningEstimate(T_Board board, T_Opp_RiskCards opp_riskcards, O_IG_MissionEstimate missionestimate, boolean beginningOfTurn, O_IG_ContinentEstimate continentEstimate) {
		this.board = board;
		this.opp_riskcards = opp_riskcards;
		this.missionestimate = missionestimate;
		this.beginningOfTurn = beginningOfTurn;
		this.continentEstimate = continentEstimate;
	}
	
	public T_Board getBoard() {
		return board;
	}
	
	public T_Opp_RiskCards getOppRiskCards() {
		return opp_riskcards;
	}
	
	public O_IG_MissionEstimate getMissionEstimate() {
		return missionestimate;
	}
	
	public O_IG_ContinentEstimate getContinentEstimate() {
		return continentEstimate;
	}
	
	public boolean getBeginningOfTurn() {
		return beginningOfTurn;
	}
}
