package risk.AI.Data_Structures.Module_Input;

import risk.AI.Data_Structures.T_Board;
import risk.AI.Data_Structures.T_Opp_RiskCards;

public class I_IG_ContinentEstimate {
	
	private T_Board board;
	private T_Opp_RiskCards opp_riskcards;
	private boolean beginningOfTurn;
	
	/** Creates a new instance of I_IG_ContinentEstimate */
	public I_IG_ContinentEstimate(T_Board board, T_Opp_RiskCards opp_riskcards, boolean beginningOfTurn) {
		this.board = board;
		this.opp_riskcards = opp_riskcards;
		this.beginningOfTurn = beginningOfTurn;
	}

	public T_Board getBoard() {
		return board;
	}

	public T_Opp_RiskCards getOppRiskCards() {
		return opp_riskcards;
	}
	
	public boolean getBeginningOfTurn(){
		return beginningOfTurn;
	}

}
