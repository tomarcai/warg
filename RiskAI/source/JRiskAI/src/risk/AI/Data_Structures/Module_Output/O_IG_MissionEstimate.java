package risk.AI.Data_Structures.Module_Output;
import java.util.*;

import net.yura.domination.engine.core.Mission;
import risk.AI.Data_Structures.*;

public class O_IG_MissionEstimate {
	
	private float[][] mission_estimate;
	
	public O_IG_MissionEstimate() {
		mission_estimate = new float[T_Game.NUMBER_OF_PLAYERS_MAX][T_Game.NUMBER_OF_MISSIONS];
	}
	
	public float getEstimate(int player, int mission) {
		return mission_estimate[player][mission];
	}
	
	public void setEstimate(int player, int mission, float estimate) {
		mission_estimate[player][mission] = estimate;
	}
	
	private int calcHighestEstimateIndex(int player) {
		int highestEstimateIndex = 0;
		for(int i =1; i < mission_estimate[player].length; i++){
			if(mission_estimate[player][i] > mission_estimate[player][highestEstimateIndex]){
				highestEstimateIndex = i;
			}
		}
		return highestEstimateIndex;
	}
	
	public Mission getMostLikelyMission(int player, List missionList) {
		return (Mission)missionList.get(calcHighestEstimateIndex(player));
	}
	
	public float getMostLikelyMissionEstimate(int player) {
		return mission_estimate[player][calcHighestEstimateIndex(player)];
	}
}