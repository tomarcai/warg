package risk.AI.Data_Structures.Module_Input;

import risk.AI.Data_Structures.*;
import risk.AI.Modules.AI_Framework;
/**
 *
 * @author Administrator
 */
public class I_IG_NextMoveEstimate {
	
	T_Board board;
	AI_Framework framework;
	
	/** Creates a new instance of I_IG_MissionEstimate */
	public I_IG_NextMoveEstimate(T_Board board, AI_Framework framework) {
		this.board = board;
		this.framework = framework;
	}
	
	public T_Board getBoard() {
		return board;
	}
	
	public AI_Framework getFramework() {
		return framework;
	}
}
