
package risk.AI.Data_Structures.Module_Output;

import risk.AI.Data_Structures.*;

public class O_IG_Ownership {
	
	private boolean[][] continent_owner;
	
	public O_IG_Ownership(){
		continent_owner = new boolean[T_Game.NUMBER_OF_PLAYERS_MAX][T_Game.NUMBER_OF_CONTINENTS];
	}
	
	public boolean getIsOwned(int player, int continent) {
		return continent_owner[player][continent];
	}
	
	public void setIsOwned(int player, int continent, boolean owned) {
		continent_owner[player][continent] = owned;
	}
	
}
