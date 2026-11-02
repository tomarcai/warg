package risk.AI.Modules.RoundPlanner.Reinforce;

import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Player;
import risk.AI.Data_Structures.Module_Output.O_RP_PlaceArmies;
import risk.AI.Data_Structures.*;
import java.util.*;
import risk.AI.Data_Structures.Module_Input.I_RP_PlaceArmies;

public class C_RP_Reinforce_PlaceArmies {

	private String type;
	private Random rand = new Random();
	private Player player;
	private T_Game game;
	private T_RP_MergedPlanElement lastPlacement = null;
	private boolean saveDefenseListTrainingData = false;
	private C_Timing timer = new C_Timing();
	private T_TrainingExampleWriter trainingExample;
	private O_RP_PlaceArmies output;
	
	public C_RP_Reinforce_PlaceArmies(String type, T_Game game, Player player, T_TrainingExampleWriter trainingExample) {
		this.type = type;
		this.game = game;
		this.player = player;
		this.trainingExample = trainingExample;
		trainingExample.setModuleLoadTime(game.getPlayerIndex(player), "rp_placearmies", 0);
	}
	
	public void run(I_RP_PlaceArmies input) {
		if (type.equals("random")) {
			random_run(input.getPlanList());
		} else 
		if (type.equals("script")) {
			timer.startTimer();
			script_run(input.getPlanList(),input.getBoard());
			timer.endTimer();
			trainingExample.addModuleRunTime(game.getPlayerIndex(player), "rp_placearmies", timer.getTimer());
			this.trainingExample.saveModule(input,output,player);
		} 
	}
	
	public O_RP_PlaceArmies getOutput() {
		return output;
	}
	
	// Random
	
	private void random_run(T_RP_MergedPlanList planList) {
		Vector c = new Vector();
		for (int i = 0; i < planList.size(); i++) {
			if (planList.get(i).getCountry().getOwner().equals(player)) {
				c.add(planList.get(i).getCountry());
			}
		}
		output = new O_RP_PlaceArmies();
		if (c.size() == 0) {
			// Place an army in a random territory owned by the player.
			c = player.getTerritoriesOwned();
		}
		output.setCountry((Country)c.get(rand.nextInt(c.size())));
	}
	
	// Script

	private void script_run(T_RP_MergedPlanList planList, T_Board board) {
		for(int i = 0; i < planList.size(); i++) {
			Country t = planList.get(i).getCountry();
			if (t.getOwner() != player) {
				int a = 0;
			}
		}
		Country territory = null;
		saveDefenseListTrainingData = false;
		if (planList.size() > 0) {
			T_RP_MergedPlanList sortedPlanList = planList.getListSortedByScore();
			T_RP_MergedPlanElement place = sortedPlanList.get(0);
			territory = place.getCountry();
			// Will the army placed pay the cost?
			if (territory.getArmies()+1 >= place.getEstimatedCost()) {
				planList.remove(place);
			}
			if ((lastPlacement != place) && (place != null)) {
				lastPlacement = place;
				if (place.getIsDefensePlan()) {
					saveDefenseListTrainingData = true;				
				}
			}
		} else {
			// First check if the player owns any continents.
			Vector ownedBorders = new Vector();
			for (int i = 0; i < player.getTerritoriesOwned().size(); i++) {
				if ((board.isBorderTerritory((Country)player.getTerritoriesOwned().get(i))) && (((Country)player.getTerritoriesOwned().get(i)).getContinent().isOwned(player))) {
					ownedBorders.add((Country)player.getTerritoriesOwned().get(i));
				}
			}
			Vector territories;
			if (ownedBorders.size() > 0) {
				territories = ownedBorders;
			} else {
				territories = player.getTerritoriesOwned();
			}
			// Place an army in a random territory owned by the player.
			territory = (Country)territories.get(rand.nextInt(territories.size()));
		}
		output = new O_RP_PlaceArmies();
		output.setCountry(territory);
	}

	public T_RP_MergedPlanElement getLastPlacement() {
		return lastPlacement;
	}

	public boolean doSaveDefenseListTrainingData() {
		return saveDefenseListTrainingData;
	}

}