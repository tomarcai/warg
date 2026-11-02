package risk.AI.Data_Structures.Module_Output;

import risk.AI.Data_Structures.*;

public class O_IG_ContinentEstimate {
	
	private float[][][] continent_estimate;
	private int numOfPlayers;
	private int numOfContinents;
	
	
	public O_IG_ContinentEstimate(int numOfPlayers, int numOfContinents) {
		continent_estimate = new float[T_Game.NUMBER_OF_PLAYERS_MAX][T_Game.NUMBER_OF_CONTINENTS][T_Game.NUMBER_OF_ROUNDS_PREDICTED];
		/* removed for speed - should not be necessary to set initial values to zero.
		for (int playerIndex = 0; playerIndex < T_Game.NUMBER_OF_PLAYERS_MAX; playerIndex++) {
			for (int continentIndex = 0; continentIndex < T_Game.NUMBER_OF_CONTINENTS; continentIndex++) {
				for (int roundIndex = 0; roundIndex < T_Game.NUMBER_OF_ROUNDS_PREDICTED; roundIndex++) {
					continent_estimate[playerIndex][continentIndex][roundIndex] = 0;
				}
			}
		}*/ 
	    this.numOfPlayers = numOfPlayers;
	    this.numOfContinents = numOfContinents;
	}
	
	public float getEstimate(int player, int continent, int round) {
	    return continent_estimate[player][continent][round];
	}

        public void setEstimate(int player, int continent, int round, float estimate) {
            continent_estimate[player][continent][round] = estimate;
        }
	
	public int getNumOfPlayers() {
	    return numOfPlayers;
	}
        
	public int getNumOfContinents() {
	    return numOfContinents;
	}
        
}