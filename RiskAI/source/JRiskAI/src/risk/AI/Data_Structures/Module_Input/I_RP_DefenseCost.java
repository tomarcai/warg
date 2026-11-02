package risk.AI.Data_Structures.Module_Input;

import net.yura.domination.engine.core.Country;
import risk.AI.Data_Structures.T_Board;

public class I_RP_DefenseCost {
	
	Country territory;
	T_Board board;
	int attackerArmiesLeft;
	/** Creates a new instance of I_RP_DefenseCost */
	public I_RP_DefenseCost(Country territory, T_Board board, int attackerArmiesLeft) {
		this.territory = territory;
		this.board = board;
		this.attackerArmiesLeft = attackerArmiesLeft;
	}

	public T_Board getBoard() {
		return board;
	}

	public Country getTerritory() {
		return territory;
	}

	public int getAttackerArmiesLeft() {
		return attackerArmiesLeft;
	}
	
}
