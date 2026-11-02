package risk.AI.Data_Structures;

import net.yura.domination.engine.core.Player;

public class T_Opp_RiskCards {
	
	private T_Game game;
	
	public T_Opp_RiskCards(T_Game game) {
		this.game = game;
	}
	
	public int getNumOfRiskCards(int player) {
		return ((Player)game.getPlayers().get(player)).getCards().size();
	}
	
}