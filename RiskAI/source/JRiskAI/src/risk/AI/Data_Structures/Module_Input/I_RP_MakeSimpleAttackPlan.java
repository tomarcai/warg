/*
 * I_RP_MakeSimpleAttackPlan.java
 *
 * Created on 8. marts 2006, 14:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package risk.AI.Data_Structures.Module_Input;

import risk.AI.Data_Structures.Module_Output.O_MP_GoalDistribution;
import risk.AI.Data_Structures.T_Board;

/**
 *
 * @author Administrator
 */
public class I_RP_MakeSimpleAttackPlan {
	
	private O_MP_GoalDistribution goalDistribution;
	private T_Board board;
	
	/** Creates a new instance of I_RP_MakeSimpleAttackPlan */
	public I_RP_MakeSimpleAttackPlan(O_MP_GoalDistribution goalDistribution, T_Board board) {
		this.goalDistribution = goalDistribution;
		this.board = board;
	}

	public T_Board getBoard() {
		return board;
	}

	public O_MP_GoalDistribution getGoalDistribution() {
		return goalDistribution;
	}

}
