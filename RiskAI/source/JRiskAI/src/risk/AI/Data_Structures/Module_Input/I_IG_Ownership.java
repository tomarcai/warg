
package risk.AI.Data_Structures.Module_Input;

import risk.AI.Data_Structures.T_Board;
import risk.AI.Data_Structures.Module_Output.O_IG_Ownership;

public class I_IG_Ownership {
	T_Board board;

	public I_IG_Ownership(T_Board board) {
		this.board = board;
	}
	
	public T_Board getBoard() {
		return board;
	}
	
}
