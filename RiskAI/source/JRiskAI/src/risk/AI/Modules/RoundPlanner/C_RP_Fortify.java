package risk.AI.Modules.RoundPlanner;

import net.yura.domination.engine.core.Player;
import risk.AI.Data_Structures.*;
import risk.AI.Data_Structures.Module_Output.O_MP_GoalDistribution;
import risk.AI.Data_Structures.Module_Output.O_RP_FortifyTransfer;
import risk.AI.Data_Structures.Module_Input.I_RP_FortifyTransfer;
import risk.AI.Modules.RoundPlanner.Fortify.*;
import risk.AI.Techniques.Pathfinding.AStar;

public class C_RP_Fortify {  

	private C_RP_Fortify_Transfer transfer_module;
	private Player player;
	
	private String output;
	
	public C_RP_Fortify(String type, Player currentPlayer, T_TrainingExampleWriter trainingExample, T_Game game, AStar pathFinder) {
		transfer_module = new C_RP_Fortify_Transfer(type, currentPlayer, trainingExample, game, pathFinder);
		this.player = player;
	}
	
	public String getOutput() {
		return output;
	}
	
	public void run(T_RP_DefenseList defenseList, O_MP_GoalDistribution goaldist, T_Board board) {
		transfer_module.run(new I_RP_FortifyTransfer(defenseList, goaldist, board));
		O_RP_FortifyTransfer out = transfer_module.getOutput();
		if (out.getNumOfArmies() > 0) {
			output = "movearmies " + out.getFromCountry().getColor() + " " + out.getToCountry().getColor() + " " + out.getNumOfArmies();
		} else {
			output = "nomove";
		}
	}

}