package risk.AI.Data_Structures.Module_Input;

import risk.AI.Data_Structures.*;
/**
 *
 * @author Administrator
 */
public class I_IG_MissionEstimate {
	
	T_Past past;
	
	/** Creates a new instance of I_IG_MissionEstimate */
	public I_IG_MissionEstimate(T_Past past) {
		this.past = past;
	}
	
	public T_Past getPast() {
		return past;
	}
	
}
