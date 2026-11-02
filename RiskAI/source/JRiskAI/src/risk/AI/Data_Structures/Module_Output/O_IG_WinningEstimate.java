package risk.AI.Data_Structures.Module_Output;

import risk.AI.Data_Structures.*;

public class O_IG_WinningEstimate {

	private float[][] winning_estimate;
	
	public O_IG_WinningEstimate() {
		this.winning_estimate = new float[T_Game.NUMBER_OF_PLAYERS_MAX][T_Game.NUMBER_OF_ROUNDS_PREDICTED];
	}
	
	public float getEstimate(int player, int round) {
		return winning_estimate[player][round];
	}	
	
	public void setEstimate(int player, int round, float estimate) {
		winning_estimate[player][round] = estimate;
	}
	
}