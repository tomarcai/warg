package risk.AI.Data_Structures.Module_Input;

import java.util.Vector;
import risk.AI.Data_Structures.T_RP_MergedPlanList;

public class I_RP_CashCards {
	
	T_RP_MergedPlanList planList;
	int armies_recieved;
	Vector ownRiskCards;
	
	/** Creates a new instance of I_RP_CashCards */
	public I_RP_CashCards(T_RP_MergedPlanList planList, int armies_recieved, Vector ownRiskCards) {
		this.planList = planList;
		this.armies_recieved = armies_recieved; 
		this.ownRiskCards = ownRiskCards;
	}

	public int getArmiesReceived() {
		return armies_recieved;
	}

	public Vector getOwnRiskCards() {
		return ownRiskCards;
	}

	public T_RP_MergedPlanList getPlanList() {
		return planList;
	}

}
