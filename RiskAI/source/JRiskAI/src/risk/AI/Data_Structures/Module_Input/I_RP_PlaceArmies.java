package risk.AI.Data_Structures.Module_Input;

import risk.AI.Data_Structures.T_RP_MergedPlanList;
import risk.AI.Data_Structures.T_Board;

public class I_RP_PlaceArmies {
	
	private T_RP_MergedPlanList planList;
	private int armies_left;
	private T_Board board;
		
	/** Creates a new instance of I_RP_PlaceArmies */
	public I_RP_PlaceArmies(T_RP_MergedPlanList planList, int armies_left, T_Board board) {
		this.planList = planList;
		this.armies_left = armies_left;
		this.board = board;
	}

	public int getArmies_left() {
		return armies_left;
	}

	public T_RP_MergedPlanList getPlanList() {
		return planList;
	}

	/**
	 * The only information used is info on whether or not a territory is a border territory.
	 */
	public T_Board getBoard() {
		return board;
	}

}
