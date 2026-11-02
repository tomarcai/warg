package risk.AI.Modules.RoundPlanner;

import net.yura.domination.engine.core.Card;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Player;
import risk.AI.Data_Structures.*;
import risk.AI.Modules.RoundPlanner.Reinforce.*;
import java.util.*;
import risk.AI.Data_Structures.Module_Input.I_RP_CashCards;
import risk.AI.Data_Structures.Module_Input.I_RP_PlaceArmies;
import risk.AI.Data_Structures.Module_Output.*;

public class C_RP_Reinforce {  

	private Random rand = new Random();
	
	private C_RP_Reinforce_CashCards cashcards_module;
	private C_RP_Reinforce_PlaceArmies placearmies_module;

	private String output;
	private Player currentPlayer;
	private T_Game game;
	private T_TrainingExampleWriter trainingExample;
	
	// types[0]: type of cash_cards.
	// types[1]: type of place_armies.
	public C_RP_Reinforce(String[] types, T_Game game, Player currentPlayer, T_TrainingExampleWriter trainingExample) {
		this.currentPlayer = currentPlayer;
		this.game = game;
		cashcards_module = new C_RP_Reinforce_CashCards(types[0], currentPlayer, game, trainingExample);
		placearmies_module = new C_RP_Reinforce_PlaceArmies(types[1], game, currentPlayer, trainingExample);
		this.trainingExample = trainingExample;
	}
	
	public void runCashCards(T_RP_MergedPlanList planList) {
		// Do we trade cards?
		cashcards_module.run(new I_RP_CashCards(planList,currentPlayer.getExtraArmies(),currentPlayer.getCards()));
		if (cashcards_module.getOutput().getCashCards()) {
			output = doTradeCards(currentPlayer.getCards());
		} else {
			output = "endtrade";
		}
	}

	public void runPlaceArmy(T_RP_MergedPlanList planList, T_Board board, O_IG_NextMoveEstimate nextMove, O_MP_GoalDistribution goaldist) {
		// Where do we place the extra armies?
		placearmies_module.run(new I_RP_PlaceArmies(planList,currentPlayer.getExtraArmies(),board));
		if (placearmies_module.doSaveDefenseListTrainingData()) {
			this.trainingExample.saveModule(placearmies_module.getLastPlacement(), board, nextMove, goaldist, this.currentPlayer);
		}
		String countryName = String.valueOf(placearmies_module.getOutput().getCountry().getColor());
		output = "placearmies " + countryName +" 1";
	}
	
	public String getOutput() {
		return output;
	}
	
	// Finds the best combination 
	private String doTradeCards(Vector cards) {
		Card[] bestCombination = game.getBestCardCombination(cards);
		
		if (bestCombination == null) {
			System.out.println("Error in C_RP_Reinforce.doTradeCards(): It has been checked that a valid card combination exists, but the combination could not be found!");
			return null;
		}
		
		// Output to parser.
		String output = "trade ";
		if (bestCombination[0].getName().equals("wildcard")) { 
			output = output + bestCombination[0].getName(); 
		} else { 
			output = output + bestCombination[0].getCountry().getColor();
		}
		output = output+" ";
		if (bestCombination[1].getName().equals("wildcard")) { 
			output = output + bestCombination[1].getName(); 
		} else { 
			output = output + bestCombination[1].getCountry().getColor();
		}
		output = output+" ";
		if (bestCombination[2].getName().equals("wildcard")) { 
			output = output + bestCombination[2].getName(); 
		} else { 
			output = output + bestCombination[2].getCountry().getColor();
		}
		return output;
	}
}
