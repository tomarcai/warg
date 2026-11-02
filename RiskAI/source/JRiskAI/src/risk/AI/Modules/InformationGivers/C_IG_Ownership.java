package risk.AI.Modules.InformationGivers;

import net.yura.domination.engine.core.Continent;
import net.yura.domination.engine.core.Player;
import risk.AI.Data_Structures.*;
import risk.AI.Data_Structures.Module_Input.I_IG_Ownership;
import risk.AI.Data_Structures.Module_Output.O_IG_Ownership;

public class C_IG_Ownership {
	private O_IG_Ownership output;
	private T_Game game;
	private T_Board board;
	
	public C_IG_Ownership(T_Game game, T_Board board) {
		this.game = game;
		this.board = board;
	}
	
	public void run(I_IG_Ownership input){
		int players = game.getPlayerCount();
		int continents = board.getContinents().size();
		output = new O_IG_Ownership();
		for(int pn = 0; pn < game.getPlayerCount(); pn++){
			for(int cn=0; cn< board.getContinents().size(); cn++){
				if(((Continent)(board.getContinents().get(cn))).isOwned(((Player)game.getPlayers().get(pn)))){
					output.setIsOwned(pn, cn, true);
				}else{
					output.setIsOwned(pn, cn, false);
				}
			}
		}
	}
	
	public O_IG_Ownership getOutput() {
		return output;
	}
}
