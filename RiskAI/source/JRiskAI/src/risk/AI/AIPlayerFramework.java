/*
 * AIPlayerFramework.java
 *
 * Created on 11. februar 2006, 16:22
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package risk.AI;

import java.io.*;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.ai.framework.AIPlayerFrameworkAbstract;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import risk.AI.Modules.AI_Framework;
import risk.AI.Data_Structures.*;

public class AIPlayerFramework {
	
	private AIPlayerFrameworkAbstract.AIPlayerFrameworkGlobal frameworkGame;
	private Player player;
	private AI_Framework framework;
	private T_Board board;
	private T_BoardIntegrity boardIntegrity;
	private boolean newTurn = true;
	private boolean pureScripted;
	private String winnerStatsDir;
	private T_TrainingExampleWriter trainingExample;
	private boolean timerStarted = false;
	
	/** Creates a new instance of AIPlayerFramework */
	public AIPlayerFramework(Player player, AIPlayerFrameworkAbstract.AIPlayerFrameworkGlobal g, String promptMsg, String translatorMoveMsg, T_Past past, T_TrainingExampleWriter trainingExample, boolean pureScripted, int type) {
		this.player = player;
		frameworkGame = g;
		this.pureScripted = pureScripted;
		this.trainingExample = trainingExample;
		String[] ai_types = makeAIComposition(T_Game.MODULE_NAMES);
		framework = new AI_Framework(ai_types,player, null,trainingExample); // The mission is also set in init(), so no problem. But it is used by igNextMove - so don't remove!
		boardIntegrity = new T_BoardIntegrity();
	}
	
	public String[] getAIComposition() {
		return this.framework.getTypes();
	}
	
	/**
	 * type[0]: type of igMission<br>
	 * type[1]: type of igWinning<br>
	 * type[2]: type of igNextMove<br>
	 * type[3]: type of igContinent<br>
	 * type[4]: type of mp<br>
	 * type[5]: type of rpPlanner_MakeAttackPlan<br>
	 * type[6]: type of rpPlanner_APCost<br>
	 * type[7]: type of rpPlanner_APPriority<br>
	 * type[8]: type of rpPlanner_DiscardPlans<br>
	 * type[9]: type of rpPlanner_DeCost<br>
	 * type[10]: type of rpPlanner_DePriority<br>
	 * type[11]: type of rpReinforce_CashCards<br>
	 * type[12]: type of rpReinforce_PlaceArmies<br>
	 * type[13]: type of rpScorePlan<br>
	 * type[14]: type of rpDoAttack<br>
	 * type[15]: type of rpFortify<br>
	 * type[16]: type of rpInitPlacement<br>
	 * type[17]: type of rpPlanner_MakeSimpleAttackPlan<br>
	 */
	private String[] makeAIComposition(String[] module_names) {
		/*String[] module_names = {"ig_mission", "ig_winning", "ig_nextmove", "ig_continent", "master_prioritizer", 
								 "rp_makeattackplan", "rp_ap_cost", "rp_ap_priority", "rp_discardattackplan", 
								 "rp_de_cost", "rp_de_priority", "rp_cashcards", "rp_placearmies", "rp_scoreattackplan", 
								 "rp_fortify", "initialplacement", "rp_scoremergedplan"};*/
		String[] type = new String[17];
		if (this.pureScripted) {
			type[0] = "script"; // igMission
			type[1] = "script"; // igWinning
			type[2] = "script"; // igNextMove
			type[3] = "script"; // igContinent
			type[4] = "script"; // mp
			type[5] = "script"; // rpPlanner_MakeAttackPlan
			type[6] = "script"; // rpPlanner_APCost
			type[7] = "script"; // rpPlanner_APPriority
			type[8] = "script"; // rpPlanner_DiscardPlans
			type[9] = "script"; // rpPlanner_DeCost
			type[10] = "script"; // rpPlanner_DePriority
			type[11] = "script"; // rpReinforce_CashCards
			type[12] = "script"; // rpReinforce_PlaceArmies
			type[13] = "script"; // rpScorePlan
			type[14] = "script"; // rpFortify
			type[15] = "script"; // rpInitPlacement
			type[16] = "script"; // rpScoreMergedPlans
			winnerStatsDir = "gameData/";
		} else {
			if (AIPlayerFrameworkAbstract.aiComposition.size() > 0) {
				type = AIPlayerFrameworkAbstract.aiComposition.remove(0);
				winnerStatsDir = "gameData/";
			} else {
				// Load AIComposition rfom "custom_framework.txt"
				File f = new File("custom_framework.txt");
				if (f.exists()) {
					try {
						int counter = 0;
						BufferedReader reader = new BufferedReader(new FileReader(f.getPath()));
						String line = reader.readLine();
						while (line != null) {
							int commentIndex = line.indexOf("//");
							if (commentIndex != -1)  {
								line = line.substring(0,commentIndex);
							}
							if (counter < type.length) {
								type[counter] = line.trim();
							}
							counter++;
							line = reader.readLine();
						}
						reader.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
					for (int i = 0; i < type.length; i++) {
						if (!type[i].equals("script")) {
							winnerStatsDir = "gameData_"+module_names[i];
							break;
						}
					}
				} else {
					System.out.println("custom_framework.txt not found - using pure scripted AI.");
					this.pureScripted = true;
					return makeAIComposition(module_names);
				}
			}
		}
		frameworkGame.setWinnerStatsDir(winnerStatsDir);
		return type;
	}

	public void init(T_Past past, T_Game game, T_Board board) {
		framework.init(past, game, board);
		this.board = board;
	}

	@Deprecated
	public void play(Risk risk) {
		risk.parser(play());
	}

	public String play() {
		RiskGame game = frameworkGame.getGame();
		String output = "";
		boolean mayContinue = true;
		if (this.newTurn && game.getSetupDone()) {
			for (int i = 0; i < game.getCountries().length; i++) {
				if ((game.getCountries()[i]).getArmies() > 1000) {
					output = "Fatal error: "+game.getCountryInt(i).getIdString()+" contains more than 1000 armies ("+(game.getCountries()[i]).getArmies()+").";
					mayContinue = false;
					// Save a report
					int j = 0;
					File f = null;
					do {
						f = new File("ArmyOverflow"+j+".reported");
						j++;
					} while (f.exists());
					try {
						f.createNewFile();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
					frameworkGame.getTrainingExampleWriter().deleteSavedTrainingExamples();
					break;
				}
			}
		}
		if (mayContinue) {
			boardIntegrity.recordBoard(board);
			Player p = game.getCurrentPlayer();
			int gameState = game.getState();
			switch (gameState) {
				// trade cards
				case RiskGame.STATE_TRADE_CARDS:
					output = framework.tradeCards();
					newTurn = false;
					if (!timerStarted) {
						trainingExample.startFrameworkTimer(player);
						timerStarted = true;
					}
					break;
				// place armies
				case RiskGame.STATE_PLACE_ARMIES:
					output = framework.placeArmies(!game.getSetupDone());
					newTurn = false;
					if (!timerStarted) {
						trainingExample.startFrameworkTimer(player);
						timerStarted = true;
					}
					break;
				// attacking
				case RiskGame.STATE_ATTACKING:
					output = framework.attack();
					break;
				// rolling
				case RiskGame.STATE_ROLLING:
					output = framework.roll();
					break;
				// moving
				case RiskGame.STATE_BATTLE_WON:
					output = framework.move();
					break;
				// fortifying
				case RiskGame.STATE_FORTIFYING:
					output = framework.fortify();
					newTurn = true;
					break;
				// endturn - might not be runned!
				case RiskGame.STATE_END_TURN:
					output = framework.endTurn();
					newTurn = true;
					break;
				// select capital
				case RiskGame.STATE_SELECT_CAPITAL:
					output = framework.selectCapital();
					break;
				// defend yourself!
				case RiskGame.STATE_DEFEND_YOURSELF:
					output = framework.defend(game.getDefender().getArmies());
					break;
			}
			boardIntegrity.checkBoardIntegrity(this.getClass().getSimpleName(),"play",board);
		}
		if (newTurn) {
			this.trainingExample.stopFrameworkTimer(player);
			timerStarted = false;
		}
		return output;
	}
}
