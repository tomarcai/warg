package risk.AI.Data_Structures.Module_Input;

import java.util.Vector;
import risk.AI.Data_Structures.T_Board;
import risk.AI.Data_Structures.T_RP_AttackPlanListElement;

public class I_RP_DiscardAttackPlan {
	
	private T_RP_AttackPlanListElement plan;
	private T_Board board;
	private int armies_received;
	private Vector riskCards;
	private boolean beginningOfTurn;
	
	/** Creates a new instance of I_RP_DiscardAttackPlan */
	public I_RP_DiscardAttackPlan(T_RP_AttackPlanListElement plan, T_Board board, int armies_received, Vector riskCards, boolean beginningOfTurn) {
		this.plan = plan;
		this.board = board;
		this.armies_received = armies_received;
		this.riskCards = riskCards;
		this.beginningOfTurn = beginningOfTurn;
	}

	public int getArmiesReceived() {
		return armies_received;
	}

	public T_RP_AttackPlanListElement getPlan() {
		return plan;
	}

	public T_Board getBoard() {
		return board;
	}

	public Vector getRiskCards() {
		return riskCards;
	}

	public boolean getBeginningOfTurn() {
		return beginningOfTurn;
	}
	
}
