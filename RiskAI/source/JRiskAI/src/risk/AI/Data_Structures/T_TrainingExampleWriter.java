package risk.AI.Data_Structures;

import java.io.*;
import java.util.*;
import net.yura.domination.engine.ai.framework.AIPlayerFrameworkBest;
import net.yura.domination.engine.ai.framework.AIPlayerFrameworkCustom;
import net.yura.domination.engine.ai.framework.AIPlayerFrameworkScripted;

import net.yura.domination.engine.core.Card;
import net.yura.domination.engine.core.Continent;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.engine.core.Mission;
import risk.AI.Data_Structures.Module_Input.*;
import risk.AI.Data_Structures.Module_Output.*;
import risk.AI.AIPlayerFramework;

public class T_TrainingExampleWriter  {

	private final String[] module_names = {"ig_mission","ig_continent","ig_winning",
											"master_prioritizer",
											"rp_cashcards", "rp_placearmies", "rp_fortify",
											"initialplacement", "attack_plan", "defense_plan"};

	/*{"ig_mission","ig_continent","ig_winning", "ig_nextmove",
											"master_prioritizer", "initialplacement",
											"rp_ap_cost", "rp_ap_priority", "rp_makeattackplan", "rp_discardplan", "rp_de_cost", "rp_de_priority", 
											"rp_cashcards", "rp_placearmies", "rp_fortify", "rp_scoreattackplan", "rp_scoremergedplan"};*/
	private String baseDir = "ai-data/";
	private Random rand = new Random();
	private Vector trainingExamples = new Vector();
	private boolean recordTrainingData;
	private final RiskGame game;

	private T_AISettings settings;
	private String currentBoardFile;
	private T_TrainingExample currentBoardTrainingExample;
	private String currentGameFile;
	private boolean boardHasBeenUsed = true;
	private int indent = 0;
	private int modulesSaved = 0;
	private int boardsSaved = 0;
	private Player tmpPlayer = null; // used to check if a new player is selected.
	private int roundsPlayed = 0;
	private long startTime = 0;
	private T_BoardIntegrity boardIntegrity;
	private Vector savedTrainingExamples = new Vector();
	private C_TimingList[][] moduleRunTimes;// = new C_TimingList[][T_Game.MODULE_NAMES.length];
	private long[][] moduleLoadTimes;// = new long[T_Game.MODULE_NAMES.length];
	private C_Timing benchmarkTimer = new C_Timing();
	private Vector<C_Timing>[] frameworkTimers = new Vector[T_Game.NUMBER_OF_PLAYERS_MAX];
	private Player currentFrameworkTimer = null;
	
	/**
	 * Creates a new instance of T_TrainingExampleWriter
	 */
	public T_TrainingExampleWriter(RiskGame game, T_AISettings settings, boolean recordTrainingData, String baseDir) {
		this.recordTrainingData = recordTrainingData;
		this.game = game;
		this.settings = settings;
		this.boardIntegrity = new T_BoardIntegrity();
		if (!baseDir.endsWith("/")) {
			baseDir = baseDir + "/";
		}
		this.baseDir = baseDir;
		moduleRunTimes = new C_TimingList[T_Game.NUMBER_OF_PLAYERS_MAX][T_Game.MODULE_NAMES.length];
		moduleLoadTimes = new long[T_Game.NUMBER_OF_PLAYERS_MAX][T_Game.MODULE_NAMES.length];
		for (int p = 0; p < T_Game.NUMBER_OF_PLAYERS_MAX; p++) {
			frameworkTimers[p] = new Vector();
			for (int i = 0; i < T_Game.MODULE_NAMES.length; i++) {
				moduleRunTimes[p][i] = new C_TimingList();
			}			
		}
		doBenchmarkTest();
	}
	
	public void init(RiskGame game) {
		if (recordTrainingData) {
			this.currentGameFile = "game"+getUniqueString()+".xml";
			this.checkProperDirStructure();
			startTime = System.currentTimeMillis();
		}
	}
	
	public String getBaseDir() {
		return this.baseDir;
	}
	
	public void recordBoardState(T_Board board) {
		if (recordTrainingData) {
			if (!boardIntegrity.checkBoardIntegrity_returnResult(board)) {
				if (!boardHasBeenUsed) {
					deleteLastBoard(currentBoardTrainingExample);
				}
				boardHasBeenUsed = false;
				boardIntegrity.recordBoard(board);
				Vector data = new Vector();
				beginTag("boardstate", data);
				beginTag("game_file", data);
				addToData(this.currentGameFile, data);
				endTag("game_file", data);
				for (int i = 0; i < board.getCountryCount(); i++) {
					//addToData(T_Game.decentColorToString(board.getCountryByInt(i).getOwner().getColor())+","+board.getCountryByInt(i).getArmies()+";",data);
					beginTag("territory name=\""+board.getCountryByInt(i).getIdString()+"\"",data);
					beginTag("owner",data);
					addToData(T_Game.decentColorToString(board.getCountryByInt(i).getOwner().getColor()),data);
					endTag("owner",data);
					beginTag("armies",data);
					addToData(String.valueOf(board.getCountryByInt(i).getArmies()),data);
					endTag("armies",data);
					endTag("territory",data);
				}
				endTag("boardstate",data);
				this.currentBoardFile = "board"+getUniqueString()+".xml";
				currentBoardTrainingExample = saveFileToMem("boardstates","",currentBoardFile,data);
				this.boardsSaved++;
			}
		}
	}
	
	public void newTurn(Player p) {
		if (recordTrainingData) {
			saveTrainingExamples();
		}
		if (tmpPlayer == null) {
			tmpPlayer = p;
			roundsPlayed = 1;
		} else {
			if (p.equals(tmpPlayer)) {
				roundsPlayed++;
			}
		}
	}
	
	private void addToData(String str, Vector data) {
		if (recordTrainingData) {
			for (int i = 0; i < indent; i++) {
				str = "\t" + str;
			}
			data.add(str);
		}
	}
	
	private void endTag(String tag, Vector data) {
		indent--;
		addToData("</"+tag+">", data);
	}
	
	private void beginTag(String tag, Vector data) {
		addToData("<"+tag+">", data);
		indent++;
	}
	
	private void saveModuleToMem(String module_name, Vector data) {
		saveFileToMem("modules",module_name, module_name+getUniqueString()+".xml",data);
		this.modulesSaved++;
	}
	
	private T_TrainingExample saveFileToMem(String dir, String module_name, String filename, Vector data) {
		T_TrainingExample t = new T_TrainingExample(baseDir,dir,module_name,filename,data);
		this.trainingExamples.add(t);
		return t;
	}
	
	public void endAllFrameworkTimer() {
		for (int i = 0; i < game.getPlayers().size(); i++) {
			if (frameworkTimers[i].size() > 0) {
				C_Timing t = frameworkTimers[i].lastElement();
				if (!t.isStopped()) {
					t.endTimer();
				}
			}
		}
	}
	
	public void closeGameAndSave(Player winner) {
		if (recordTrainingData) {
			// Record game
			Vector data = new Vector();
			beginTag("game",data);
			for (int i = 0; i < game.getPlayers().size(); i++) {
				Player p = (Player)game.getPlayers().get(i);
				beginTag("player name=\""+p.getName()+"\" color=\""+T_Game.decentColorToString(p.getColor()) +"\" type=\""+playerTypeToString(p)+"\"",data);
				beginTag("mission",data);
				addToData(T_Game.decentMissionToString(p.getMission(),p),data);
				endTag("mission",data);
				endTag("player",data);
			}
			beginTag("winner",data);
			addToData(T_Game.decentColorToString(winner.getColor()),data);
			endTag("winner",data);
			beginTag("statistics",data);
			beginTag("time_used",data);
			addToData(String.valueOf((float)(System.currentTimeMillis() - this.startTime) / 1000.0f),data);
			endTag("time_used",data);
			beginTag("rounds_played",data);
			addToData(String.valueOf(this.roundsPlayed),data);
			endTag("rounds_played",data);
			beginTag("board_states_saved",data);
			addToData(String.valueOf(this.boardsSaved-1),data);
			endTag("board_states_saved",data);
			beginTag("module_runs_saved",data);
			addToData(String.valueOf(this.modulesSaved),data);
			endTag("module_runs_saved",data);
			endTag("statistics",data);
			endTag("game",data);
			this.saveFileToMem("","",currentGameFile, data);
			
			// If the last board is unused, then delete it
			if (!this.boardHasBeenUsed) {
				deleteLastBoard(this.currentBoardTrainingExample);
			}
			
			// Save all training examples
			saveTrainingExamples();
		}
	}
	
	public void saveWinnerFile(Player winner, String path) {
		try {
			File f = new File(path);
			if (!f.exists()) {
				f.mkdir();
			}
			FileWriter writer = new FileWriter(f.getPath()+"/game"+getUniqueString()+".dat");
			BufferedWriter out = new BufferedWriter(writer);
			
			out.write("winner: ");
			out.newLine();
			out.write(this.playerTypeToString(winner)+" ("+T_Game.decentColorToString(winner.getColor())+")");
			out.newLine();
			out.newLine();
			out.write("players: ");
			out.newLine();
			
			for (int i = 0; i < game.getPlayers().size(); i++) {
				Player p = (Player)game.getPlayers().get(i);
				out.write(this.playerTypeToString(p)+" ("+T_Game.decentColorToString(p.getColor())+")");
				out.newLine();
			}
			out.close();
			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void saveGameStats(Player winner, Map<Player, AIPlayerFramework> aiPlayers) {
		String path = "stats/";
		File f = new File(path);
		if (!f.exists()) {
			f.mkdir();
		}
		boolean tmp = recordTrainingData;
		recordTrainingData = true; // small hack - pretend we are recording training data.
		
		Vector data = new Vector();
		addToData("<?xml version=\"1.0\" encoding=\"UTF-8\"?>",data);
		beginTag("gamestats",data);
		beginTag("winner",data);
		this.addToData(T_Game.decentColorToString(winner.getColor()),data);
		endTag("winner", data);
		beginTag("benchmark_test",data);
		this.addToData(String.valueOf(this.benchmarkTimer.getTimer()),data);
		endTag("benchmark_test", data);
		beginTag("rounds_played",data);
		this.addToData(String.valueOf(this.roundsPlayed),data);
		endTag("rounds_played", data);
		for (int i = 0; i < game.getPlayers().size(); i++) {
			Player p = (Player)game.getPlayers().get(i);
			beginTag("player", data);
			beginTag("color", data);
			addToData(T_Game.decentColorToString(p.getColor()),data);
			endTag("color", data);
			beginTag("player_type", data);
			addToData(playerTypeToString(p),data);
			endTag("player_type", data);
			beginTag("mission", data);
			addToData(T_Game.decentMissionToString(p.getMission(),p),data);
			endTag("mission", data);
			if (p.getType() == AIPlayerFrameworkCustom.TYPE) { // Framework_custom
				beginTag("round_times", data);
				for (int r = 0; r < this.frameworkTimers[i].size(); r++) {
					addToData(String.valueOf(frameworkTimers[i].get(r).getTimer()),data);
				}
				endTag("round_times", data);
				String[] compo = aiPlayers.get(p).getAIComposition();
				for (int m = 0; m < T_Game.MODULE_NAMES.length; m++) {
					beginTag("module name=\""+T_Game.MODULE_NAMES[m]+"\"",data);
					beginTag("module_type", data);
					addToData(compo[m],data);
					endTag("module_type", data);
					beginTag("load_time",data);
					addToData(String.valueOf(this.moduleLoadTimes[i][m]),data);
					endTag("load_time",data);
					beginTag("run_time_average",data);
					addToData(String.valueOf(this.moduleRunTimes[i][m].getAverage()),data);
					endTag("run_time_average",data);
					beginTag("run_time_min",data);
					addToData(String.valueOf(this.moduleRunTimes[i][m].getMin()),data);
					endTag("run_time_min",data);
					beginTag("run_time_max",data);
					addToData(String.valueOf(this.moduleRunTimes[i][m].getMax()),data);
					endTag("run_time_max",data);
					beginTag("run_times",data);
					String str = "";
					for (int t = 0; t < moduleRunTimes[i][m].getTimingList().size(); t++) {
						str += String.valueOf(this.moduleRunTimes[i][m].getTimingList().get(t).longValue());
						if (t < moduleRunTimes[i][m].getTimingList().size()-1) {
							str += ",";
						}
					}
					addToData(str,data);						
					endTag("run_times",data);
					endTag("module", data);
				}
			}

			endTag("player", data);
		}			
		endTag("gamestats",data);
		recordTrainingData = tmp;
		try {
			
			FileWriter writer = new FileWriter(f.getPath()+"/gamestats"+getUniqueString()+".xml");
			BufferedWriter out = new BufferedWriter(writer);
			
			for (int i = 0; i < data.size(); i++) {
				out.write((String)data.get(i));
				out.newLine();
			}
			/*out.write("winner: ");
			out.newLine();
			out.write(T_Game.decentColorToString(winner.getColor()));
			out.newLine();
			out.newLine();
			out.write("players: ");
			out.newLine();
			
			for (int i = 0; i < game.getPlayers().size(); i++) {
				Player p = (Player)game.getPlayers().get(i);
				out.write(this.playerTypeToString(p)+" (color="+T_Game.decentColorToString(p.getColor())+",mission="+T_Game.decentMissionToString(p.getMission(),p));
				if (p.getType() == 7) { // Framework_custom
					String[] compo = ((AIPlayerFramework)p).getAIComposition();
					String type = "";
					String module = "";
					for (int j = 0; j < compo.length; j++) {
						if (!compo[j].equals("script")) {
							if (type.equals("")) {
								type = compo[j];
								module = T_Game.MODULE_NAMES[j];
							} else {
								throw new UnsupportedOperationException("More than one non-script module found!");
							}
						}
					}
					out.write(",type={"+module+","+type+"}");
				}
				out.write(")");
				out.newLine();
			}
			out.newLine();
			
			out.write("modulestats: ");
			out.newLine();
			out.write("benchmark: "+benchmarkTimer.getTimer());
			out.newLine();
			for (int i = 0; i < T_Game.MODULE_NAMES.length; i++) {
				out.write(T_Game.MODULE_NAMES[i]+": load="+moduleLoadTimes[i]+", run={"+moduleRunTimes[i].getFullString()+"}");
				out.newLine();
			}
			*/
			out.close();
			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private void saveTrainingExamples() {
		while (!this.trainingExamples.isEmpty()) {
			((T_TrainingExample)trainingExamples.firstElement()).saveExample();
			savedTrainingExamples.add(((T_TrainingExample)trainingExamples.firstElement()).getFullPath());
			trainingExamples.remove(0);
		}
	}
	
	public void deleteSavedTrainingExamples() {
		while (!this.savedTrainingExamples.isEmpty()) {
			File f = new File((String)savedTrainingExamples.firstElement());
			if (!f.delete()) {
				System.out.println("T_TrainingExampleWriter.deleteSavedTrainingExamples(): can not delete file ("+(String)savedTrainingExamples.firstElement()+")");
			}
			savedTrainingExamples.remove(0);
		}
	}
	
	private void checkProperDirStructure() {
		// Check proper dircetory structure
		File f = new File(baseDir);
		if (!f.exists()) {
			f.mkdir();
		}
		f = new File(baseDir+"training_examples");
		if (!f.exists()) {
			f.mkdir();
		}
		String dir = baseDir+"training_examples/modules";
		f = new File(dir);
		if (!f.exists()) {
			f.mkdir();
		}
		for (int i = 0; i < this.module_names.length; i++) {
			dir = baseDir+"training_examples/modules/"+this.module_names[i];
			f = new File(dir);
			if (!f.exists()) {
				f.mkdir();
			}
		}
		
		dir = baseDir+"training_examples/boardstates";
		f = new File(dir);
		if (!f.exists()) {
			f.mkdir();
		}
	}
	
	private String getUniqueString() {
		String str = Long.toHexString(System.currentTimeMillis());
		str += Integer.toHexString(rand.nextInt());
		return str;
	}
	
	private String playerTypeToString(Player p) {
		switch (p.getType()) {
			case Player.PLAYER_HUMAN: return "human";

			case Player.PLAYER_AI_CRAP: return "AI (Crap)";
			case Player.PLAYER_AI_EASY: return "AI (Easy)";
			case Player.PLAYER_AI_AVERAGE: return "AI (Average)";
			case Player.PLAYER_AI_HARD: return "AI (Hard)";

			case Player.PLAYER_NEUTRAL: return "Neutral";

			case AIPlayerFrameworkScripted.TYPE: return "AI (Framework)";
			case AIPlayerFrameworkCustom.TYPE: return "AI (Framework_custom)";
			case AIPlayerFrameworkBest.TYPE: return "AI (Framework_best)";

			default: return "unknown";
		}
	}
	
	private void deleteLastBoard(T_TrainingExample currentBoardTrainingExample) {
		if (!trainingExamples.remove(currentBoardTrainingExample)) {
			File f = new File(baseDir+"training_examples/boardstates/"+currentBoardFile);
			if (!f.delete()) {
				System.out.println("Error in trainingExample.deleteLastBoard(): could not find currentBoardTrainingExample! (T_TrainingExampleContainer.java)");
			} else {
				if (!this.savedTrainingExamples.remove(baseDir+"training_examples/boardstates/"+currentBoardFile)) {
					System.out.println("Error in trainingExample.deleteLastBoard(): could not find currentBoard in savedTrainingExamples! (T_TrainingExampleContainer.java)");
				}
			}
		}
		currentBoardTrainingExample = null;
		this.boardsSaved--;
	}
	
	// Methods for adding standard data to a training example:
	
	private void addToModuleData_Intro(Player player, Vector data) {
		beginTag("module_run",data);
		beginTag("player_reference",data);
		addToData(T_Game.decentColorToString(player.getColor()),data);
		endTag("player_reference",data);
		beginTag("game_file", data);
		addToData(this.currentGameFile, data);
		endTag("game_file", data);
		beginTag("current_round", data);
		addToData(String.valueOf(this.roundsPlayed), data);
		endTag("current_round", data);
	}
	
	private void addToModuleData_Board(Vector data) {
		beginTag("board_file",data);
		addToData(this.currentBoardFile,data);
		endTag("board_file",data);
		this.boardHasBeenUsed = true;
	}
	
	private void addToModuleData_OppRiskCards(T_Opp_RiskCards opp_riskcards, Vector data) {
		beginTag("opp_risk_cards",data);
		for (int i = 0; i < game.getPlayers().size(); i++) {
			beginTag("player color=\""+T_Game.decentColorToString(((Player)game.getPlayers().get(i)).getColor())+"\"",data);
			addToData(String.valueOf(opp_riskcards.getNumOfRiskCards(i)), data);
			endTag("player", data);
		}
		endTag("opp_risk_cards",data);
	}
	
	private void addToModuleData_Past(T_Past past, Vector data) {
		beginTag("past",data);
		Vector boardPast = past.getBoardPast();
		beginTag("board_past",data);
		for (int i = 0; i < boardPast.size(); i++) {
			addToData((String)boardPast.get(i),data);
		}
		endTag("board_past",data);
		Vector actionPast = past.getActionPast();
		beginTag("action_past",data);
		for (int i = 0; i < actionPast.size(); i++) {
			addToData((String)actionPast.get(i),data);
		}
		endTag("action_past",data);
		endTag("past",data);
	}
	
	private void addToModuleData_MissionEstimate(O_IG_MissionEstimate missionEstimate, Vector data) {
		beginTag("missions_estimates",data);
		for (int p = 0; p < game.getPlayers().size(); p++) {
			beginTag("player color=\""+T_Game.decentColorToString(((Player)game.getPlayers().get(p)).getColor())+"\"",data);
			for (int m = 0; m < game.getNoMissions(); m++) {
				beginTag("mission type=\""+T_Game.decentMissionToString((Mission)game.getMissions().get(m),null)+"\"",data);
				addToData(String.valueOf(missionEstimate.getEstimate(p,m)),data);
				endTag("mission", data);
			}
			endTag("player", data);
		}
		endTag("missions_estimates",data);
	}
	
	private void addToModuleData_ContinentEstimate(O_IG_ContinentEstimate continentEstimate, Vector data) {
		beginTag("continent_estimates", data);
		for (int p = 0; p < game.getPlayers().size(); p++) {
			beginTag("player color=\""+T_Game.decentColorToString(((Player)game.getPlayers().get(p)).getColor())+"\"",data);
			for (int c = 0; c < game.getContinents().length; c++) {
				beginTag("continent name=\""+game.getContinents()[c].getName()+"\"",data);
				for (int r = 0; r < settings.roundsToPredict; r++) {
					beginTag("round number=\""+r+"\"",data);
					addToData(String.valueOf(continentEstimate.getEstimate(p,c,r)),data);
					endTag("round",data);
				}
				endTag("continent",data);
			}
			endTag("player", data);
		}
		endTag("continent_estimates", data);
	}

	private void addToModuleData_OwnershipEstimate(O_IG_Ownership ownership, Vector data) {
		beginTag("ownership", data);
		for (int p = 0; p < game.getPlayers().size(); p++) {
			beginTag("player color=\""+T_Game.decentColorToString(((Player)game.getPlayers().get(p)).getColor())+"\"",data);
			for (int c = 0; c < game.getContinents().length; c++) {
				beginTag("continent name=\""+game.getContinents()[c].getName()+"\"",data);
				addToData(String.valueOf(ownership.getIsOwned(p,c)),data);
				endTag("continent",data);
			}
			endTag("player", data);
		}
		endTag("ownership", data);
	}
		
	private void addToModuleData_WinningEstimate(O_IG_WinningEstimate winningEstimate, Vector data) {
		beginTag("winning_estimates", data);
		for (int p = 0; p < game.getPlayers().size(); p++) {
			beginTag("player color=\""+T_Game.decentColorToString(((Player)game.getPlayers().get(p)).getColor())+"\"",data);
			for (int r = 0; r < settings.roundsToPredict; r++) {
				beginTag("round number=\""+r+"\"",data);
				addToData(String.valueOf(winningEstimate.getEstimate(p,r)),data);
				endTag("round",data);
			}
			endTag("player", data);
		}
		endTag("winning_estimate", data);
	}
	
	private void addToModuleData_NextMoveEstimate(O_IG_NextMoveEstimate nextMoveEstimate, Vector data) {
		beginTag("nextmove_estimate",data);
		Vector territories = nextMoveEstimate.getTerritoryInDangerList();
		for (int i = 0; i < territories.size(); i++) {
			addToData((String)((Country)territories.get(i)).getIdString(),data);
		}
		endTag("nextmove_estimate",data);
	}
	
	private void addToModuleData_GoalDistribution(O_MP_GoalDistribution goaldist, Vector data) {
		beginTag("goal_distribution",data);
		beginTag("conquer_and_defend_continent",data);
		for (int i = 0; i < game.getContinents().length; i++) {
			beginTag("continent name=\""+((Continent)game.getContinents()[i]).getName()+"\"",data);
			addToData(String.valueOf(goaldist.getDistribution_CD()[i]),data);
			endTag("continent",data);
		}
		endTag("conquer_and_defend_continent",data);
		beginTag("conquer_continent",data);
		for (int i = 0; i < game.getContinents().length; i++) {
			beginTag("continent name=\""+((Continent)game.getContinents()[i]).getName()+"\"",data);
			addToData(String.valueOf(goaldist.getDistribution_C()[i]),data);
			endTag("continent",data);
		}
		endTag("conquer_continent",data);
		beginTag("defend_continent",data);
		for (int i = 0; i < game.getContinents().length; i++) {
			beginTag("continent name=\""+((Continent)game.getContinents()[i]).getName()+"\"",data);
			addToData(String.valueOf(goaldist.getDistribution_D()[i]),data);
			endTag("continent",data);
		}
		endTag("defend_continent",data);
		beginTag("obstruct_continent",data);
		for (int i = 0; i < game.getContinents().length; i++) {
			beginTag("continent name=\""+((Continent)game.getContinents()[i]).getName()+"\"",data);
			addToData(String.valueOf(goaldist.getDistribution_O()[i]),data);
			endTag("continent",data);
		}
		endTag("obstruct_continent",data);
		beginTag("obstruct_and_defend_continent",data);
		for (int i = 0; i < game.getContinents().length; i++) {
			beginTag("continent name=\""+((Continent)game.getContinents()[i]).getName()+"\"",data);
			addToData(String.valueOf(goaldist.getDistribution_OD()[i]),data);
			endTag("continent",data);
		}
		endTag("obstruct_and_defend_continent",data);
		beginTag("attack_player",data);
		for (int i = 0; i < game.getPlayers().size(); i++) {
			beginTag("player name=\""+T_Game.decentColorToString(((Player)game.getPlayers().get(i)).getColor())+"\"",data);
			addToData(String.valueOf(goaldist.getDistribution_A()[i]),data);
			endTag("player",data);
		}
		endTag("attack_player",data);
		beginTag("territories_18",data);
		addToData(String.valueOf(goaldist.getDistribution_18()),data);
		endTag("territories_18",data);
		beginTag("territories_24",data);
		addToData(String.valueOf(goaldist.getDistribution_24()),data);
		endTag("territories_24",data);
		endTag("goal_distribution",data);
	}
	
	private void addToModuleData_AttackPlan(T_RP_AttackPlan attackPlan, Vector data) {
		beginTag("attack_plan", data);
		beginTag("territories", data);
		for (int i = 0; i < attackPlan.size(); i++) {
			addToData(attackPlan.getCountry(i).getIdString(),data);
		}
		endTag("territories", data);
		beginTag("armies_to_leave", data);
		for (int i = 0; i < attackPlan.size(); i++) {
			addToData(String.valueOf(attackPlan.getArmiesToLeave(i)),data);
		}
		endTag("armies_to_leave", data);
		endTag("attack_plan", data);
	}
	
	private void addToModuleData_Priority(float priority, Vector data) {
		beginTag("priority", data);
		addToData(String.valueOf(priority),data);
		endTag("priority", data);
	}	
	
	private void addToModuleData_ArmiesReceived(int armies, Vector data) {
		beginTag("armies_received", data);
		addToData(String.valueOf(armies),data);
		endTag("armies_received", data);		
	}
	
	private void addToModuleData_EstimatedCost(int cost, Vector data) {
		beginTag("cost_estimated", data);
		addToData(String.valueOf(cost),data);
		endTag("cost_estimated", data);		
	}

	private void addToModuleData_MinimumCost(int cost, Vector data) {
		beginTag("cost_minimum", data);
		addToData(String.valueOf(cost),data);
		endTag("cost_minimum", data);		
	}

	private void addToModuleData_AttackPlanListElement(T_RP_AttackPlanListElement attackPlan, Vector data) {
		beginTag("attack_plan_extended", data);
		this.addToModuleData_AttackPlan(attackPlan.getAttackPlan(),data);
		this.addToModuleData_EstimatedCost(attackPlan.getEstimatedCost(),data);
		this.addToModuleData_MinimumCost(attackPlan.getMinimumCost(),data);
		this.addToModuleData_Priority(attackPlan.getPriority(),data);
		this.addToModuleData_Score(attackPlan.getScore(),data);
		endTag("attack_plan_extended", data);
	}
	
	private void addToModuleData_RiskCards(Vector riskCards, Vector data) {
		beginTag("risk_cards", data);
		for (int i = 0; i < riskCards.size(); i++) {
			Card c = (Card)riskCards.get(i);
			beginTag("type",data);
			addToData(c.getName(),data);
			endTag("type",data);
			beginTag("card",data);
			this.addToModuleData_Territory(c.getCountry(),data);
			endTag("card",data);
		}
		endTag("risk_cards", data);
	}

	private void addToModuleData_DiscardAttackPlan(boolean discard, Vector data) {
		beginTag("discard_attack_plan",data);
		addToData(String.valueOf(discard),data);
		endTag("discard_attack_plan",data);
	}

	private void addToModuleData_CashCards(boolean cashcards, Vector data) {
		beginTag("cash_cards",data);
		addToData(String.valueOf(cashcards),data);
		endTag("cash_cards",data);
	}

	private void addToModuleData_Territory(Country territory, Vector data) {
		beginTag("territory",data);
		String name = "";
		if (territory == null) {
			name = "null";
		} else {
			name = territory.getIdString();
		}
		addToData(name,data);
		endTag("territory",data);		
	}
	
	private void addToModuleData_AttackerArmiesLeft(int armies, Vector data) {
		beginTag("attacker_armies_left", data);
		addToData(String.valueOf(armies),data);
		endTag("attacker_armies_left", data);		
	}
	
	private void addToModuleData_IsDefensePlan(boolean isDefensePlan, Vector data) {
		beginTag("is_defense_plan", data);
		addToData(String.valueOf(isDefensePlan),data);
		endTag("is_defense_plan", data);		
	}
	
	private void addToModuleData_MergedPlanElement(T_RP_MergedPlanElement planElement, Vector data) {
		beginTag("merged_plan_element",data);
		this.addToModuleData_Territory(planElement.getCountry(),data);
		this.addToModuleData_EstimatedCost(planElement.getEstimatedCost(),data);
		this.addToModuleData_MinimumCost(planElement.getMinimumCost(),data);
		this.addToModuleData_Priority(planElement.getPriority(),data);
		this.addToModuleData_AttackerArmiesLeft(planElement.getAttackerArmiesLeft(),data);
		this.addToModuleData_IsDefensePlan(planElement.getIsDefensePlan(),data);
		this.addToModuleData_Score(planElement.getScore(),data);
		endTag("merged_plan_element",data);
	}

	private void addToModuleData_MergedPlanList(T_RP_MergedPlanList planList, Vector data) {
		beginTag("merged_plan_list",data);
		for (int i = 0; i < planList.size(); i++) {
			 this.addToModuleData_MergedPlanElement((T_RP_MergedPlanElement)planList.get(i),data);
		}
		endTag("merged_plan_list",data);
	}
	
	private void addToModuleData_ArmiesLeft(int armies, Vector data) {
		beginTag("armies_left", data);
		addToData(String.valueOf(armies),data);
		endTag("armies_left", data);		
	}
	
	private void addToModuleData_Score(float score, Vector data) {
		beginTag("score", data);
		addToData(String.valueOf(score),data);
		endTag("score", data);
	}		

	private void addToModuleData_BeginningOfTurn(boolean beginningOfTurn, Vector data) {
		beginTag("beginning_of_turn",data);
		addToData(String.valueOf(beginningOfTurn),data);
		endTag("beginning_of_turn",data);
	}
	
	private void addToModuleData_InfluenceMap(T_InfluenceMap influenceMap, Vector data) {
		beginTag("influence_map",data);
		for (int i = 0; i < influenceMap.size(); i++) {
			addToData(String.valueOf(influenceMap.getValue(i)),data);
		}
		endTag("influence_map",data);
	}
	
	private void addToModuleData_DefenseList(T_RP_DefenseList defenseList, Vector data) {
		beginTag("defense_list",data);
		for (int i = 0; i < defenseList.size(); i++) {
			this.addToModuleData_DefenseListElement(defenseList.get(i),data);
		}
		endTag("defense_list",data);
	}

	private void addToModuleData_DefenseListElement(T_RP_DefenseListElement defenseListElement, Vector data) {
		beginTag("defense_list_element",data);
		this.addToModuleData_EstimatedCost(defenseListElement.getCost(),data);
		this.addToModuleData_Territory(defenseListElement.getCountry(),data);
		this.addToModuleData_Priority(defenseListElement.getPriority(),data);
		this.addToModuleData_AttackerArmiesLeft(defenseListElement.getAttackerArmiesLeft(),data);
		endTag("defense_list_element",data);
	}
	
	private void addToModuleData_Armies(int armies, Vector data) {
		beginTag("armies",data);
		addToData(String.valueOf(armies),data);
		endTag("armies",data);
	}
	
	// Methods for adding input and output data to training examples - one method for each module:
	
	public void saveModule(I_IG_MissionEstimate input, O_IG_MissionEstimate output, Player player) {
		if (recordTrainingData) {
			Vector data = new Vector();
			this.addToModuleData_Intro(player,data);
			
			// input
			beginTag("input",data);
			this.addToModuleData_Past(input.getPast(),data);
			endTag("input",data);
			
			//output
			beginTag("output",data);
			this.addToModuleData_MissionEstimate(output,data);
			endTag("output",data);
			endTag("module_run",data);
			this.saveModuleToMem(this.module_names[0],data);
		}
	}
	
	public void saveModule(I_IG_ContinentEstimate input, O_IG_ContinentEstimate output, Player player) {
		if (recordTrainingData) {
			boardIntegrity.checkBoardIntegrity(this.getClass().getSimpleName(),"",input.getBoard());
			Vector data = new Vector();
			this.addToModuleData_Intro(player,data);
			
			// input
			beginTag("input",data);
			this.addToModuleData_Board(data);
			this.addToModuleData_OppRiskCards(input.getOppRiskCards(),data);
			this.addToModuleData_BeginningOfTurn(input.getBeginningOfTurn(),data);
			endTag("input",data);
			
			//output
			beginTag("output",data);
			this.addToModuleData_ContinentEstimate(output, data);
			endTag("output",data);
			endTag("module_run",data);
			this.saveModuleToMem(this.module_names[1],data);
		}
	}
	
	public void saveModule(I_IG_WinningEstimate input, O_IG_WinningEstimate output, Player player) {
		if (recordTrainingData) {
			boardIntegrity.checkBoardIntegrity(this.getClass().getSimpleName(),"",input.getBoard());
			Vector data = new Vector();
			this.addToModuleData_Intro(player,data);
			
			// input
			beginTag("input",data);
			this.addToModuleData_Board(data);
			this.addToModuleData_MissionEstimate(input.getMissionEstimate(),data);
			this.addToModuleData_OppRiskCards(input.getOppRiskCards(),data);
			this.addToModuleData_BeginningOfTurn(input.getBeginningOfTurn(),data);
			this.addToModuleData_ContinentEstimate(input.getContinentEstimate(),data);
			endTag("input",data);
			
			//output
			beginTag("output",data);
			this.addToModuleData_WinningEstimate(output,data);
			endTag("output",data);
			endTag("module_run",data);
			this.saveModuleToMem(this.module_names[2],data);
		}
	}
	
	public void saveModule(I_MP_GoalDistribution input, O_MP_GoalDistribution output, Player player) {
		if (recordTrainingData) {
			Vector data = new Vector();
			this.addToModuleData_Intro(player,data);
			
			// input
			beginTag("input",data);
			this.addToModuleData_MissionEstimate(input.getMissionEstimate(),data);
			this.addToModuleData_ContinentEstimate(input.getContinentEstimate(),data);
			this.addToModuleData_WinningEstimate(input.getWinningEstimate(),data);
			this.addToModuleData_OwnershipEstimate(input.getOwnership(),data);
			this.addToModuleData_NextMoveEstimate(input.getNextMoveEstimate(),data);
			endTag("input",data);
			
			//output
			beginTag("output",data);
			this.addToModuleData_GoalDistribution(output,data);
			endTag("output",data);
			endTag("module_run",data);
			this.saveModuleToMem(this.module_names[3],data);
		}
	}
	
	/*public void saveModule(I_RP_AttackPlanPriority input, O_RP_AttackPlanPriority output, Player player) {
		if (recordTrainingData) {
			if (!boardIntegrity.checkBoardIntegrity(this.getClass().getSimpleName(),"",input.getBoard())) {
				boardIntegrityFailed();
			}
			Vector data = new Vector();
			this.addToModuleData_Intro(player,data);
			
			// input
			beginTag("input",data);
			this.addToModuleData_AttackPlan(input.getAttackPlan(),data);
			this.addToModuleData_Board(data);
			this.addToModuleData_GoalDistribution(input.getGoalDistribution(),data);
			endTag("input",data);
			
			//output
			beginTag("output",data);
			this.addToModuleData_Priority(output.getPriority(),data);
			endTag("output",data);
			endTag("module_run",data);
			this.saveModuleToMem(this.module_names[4],data);
		}
	}*/

	/*
	public void saveModule(I_RP_DiscardAttackPlan input, O_RP_DiscardAttackPlan output, Player player) {
		if (recordTrainingData) {
			if (!boardIntegrity.checkBoardIntegrity(this.getClass().getSimpleName(),"",input.getBoard())) {
				boardIntegrityFailed();
			}
			Vector data = new Vector();
			this.addToModuleData_Intro(player,data);
			
			// input
			beginTag("input",data);
			this.addToModuleData_Board(data);
			this.addToModuleData_ArmiesReceived(input.getArmiesReceived(),data);
			this.addToModuleData_AttackPlanListElement(input.getPlan(),data);
			this.addToModuleData_RiskCards(input.getRiskCards(),data);
			this.addToModuleData_BeginningOfTurn(input.getBeginningOfTurn(),data);
			endTag("input",data);
			
			//output
			beginTag("output",data);
			this.addToModuleData_DiscardAttackPlan(output.getDiscard(),data);
			endTag("output",data);
			endTag("module_run",data);
			this.saveModuleToMem(this.module_names[5],data);
		}
	}*/
	/*
	public void saveModule(I_RP_DefenseCost input, O_RP_DefenseCost output, Player player) {
		if (recordTrainingData) {
			boardIntegrity.checkBoardIntegrity(this.getClass().getSimpleName(),"",input.getBoard());
			Vector data = new Vector();
			this.addToModuleData_Intro(player,data);
			
			// input
			beginTag("input",data);
			this.addToModuleData_Board(data);
			this.addToModuleData_Territory(input.getTerritory(),data);
			this.addToModuleData_AttackerArmiesLeft(input.getAttackerArmiesLeft(),data);
			endTag("input",data);
			
			//output
			beginTag("output",data);
			this.addToModuleData_EstimatedCost(output.getCost(),data);
			endTag("output",data);
			endTag("module_run",data);
			this.saveModuleToMem(this.module_names[6],data);
		}		
	}

	public void saveModule(I_RP_DefensePriority input, O_RP_DefensePriority output, Player player) {
		if (recordTrainingData) {
			boardIntegrity.checkBoardIntegrity(this.getClass().getSimpleName(),"",input.getBoard());
			Vector data = new Vector();
			this.addToModuleData_Intro(player,data);
			
			// input
			beginTag("input",data);
			this.addToModuleData_Board(data);
			this.addToModuleData_GoalDistribution(input.getGoalDistribution(),data);
			this.addToModuleData_NextMoveEstimate(input.getNextMoveEstimate(),data);
			this.addToModuleData_Territory(input.getTerritory(),data);
			endTag("input",data);
			
			//output
			beginTag("output",data);
			this.addToModuleData_Priority(output.getPriority(),data);
			endTag("output",data);
			endTag("module_run",data);
			this.saveModuleToMem(this.module_names[7],data);
		}				
	}

	*/
	public void saveModule(I_RP_CashCards input, O_RP_CashCards output, Player player) {
		if (recordTrainingData) {
			Vector data = new Vector();
			this.addToModuleData_Intro(player,data);
			
			// input
			beginTag("input",data);
			this.addToModuleData_ArmiesReceived(input.getArmiesReceived(),data);
			this.addToModuleData_RiskCards(input.getOwnRiskCards(),data);
			this.addToModuleData_MergedPlanList(input.getPlanList(),data);
			endTag("input",data);
			
			//output
			beginTag("output",data);
			this.addToModuleData_CashCards(output.getCashCards(),data);
			endTag("output",data);
			endTag("module_run",data);
			this.saveModuleToMem(this.module_names[4],data);
		}				
	}

	public void saveModule(I_RP_PlaceArmies input, O_RP_PlaceArmies output, Player player) {
		if (recordTrainingData) {
			Vector data = new Vector();
			this.addToModuleData_Intro(player,data);
			
			// input
			beginTag("input",data);
			this.addToModuleData_Board(data);
			this.addToModuleData_ArmiesLeft(input.getArmies_left(),data);
			this.addToModuleData_MergedPlanList(input.getPlanList(),data);
			endTag("input",data);
			
			//output
			beginTag("output",data);
			this.addToModuleData_Territory(output.getCountry(),data);
			endTag("output",data);
			endTag("module_run",data);
			this.saveModuleToMem(this.module_names[5],data);
		}		
	}

	/*
	public void saveModule(I_RP_ScoreAttackPlan input, O_RP_ScoreAttackPlan output, Player player) {
		if (recordTrainingData) {
			Vector data = new Vector();
			this.addToModuleData_Intro(player,data);
			
			// input
			beginTag("input",data);
			this.addToModuleData_Priority(input.getPriority(),data);
			this.addToModuleData_EstimatedCost(input.getCost(),data);
			endTag("input",data);
			
			//output
			beginTag("output",data);
			this.addToModuleData_Score(output.getScore(),data);
			endTag("output",data);
			endTag("module_run",data);
			this.saveModuleToMem(this.module_names[10],data);
		}		
	}*/

	public void saveModule(I_RP_FortifyTransfer input, O_RP_FortifyTransfer output, Player player) {
		if (recordTrainingData) {
			boardIntegrity.checkBoardIntegrity(this.getClass().getSimpleName(),"",input.getBoard());
			Vector data = new Vector();
			this.addToModuleData_Intro(player,data);
			
			// input
			beginTag("input",data);
			this.addToModuleData_Board(data);
			this.addToModuleData_GoalDistribution(input.getGoalDistribution(),data);
			this.addToModuleData_DefenseList(input.getDefenseList(),data);
			endTag("input",data);
			
			//output
			beginTag("output",data);
			beginTag("from",data);
			this.addToModuleData_Territory(output.getFromCountry(),data);
			endTag("from",data);
			beginTag("to",data);
			this.addToModuleData_Territory(output.getToCountry(),data);
			endTag("to",data);
			this.addToModuleData_Armies(output.getNumOfArmies(),data);
			endTag("output",data);
			endTag("module_run",data);
			this.saveModuleToMem(this.module_names[6],data);
		}				
	}

	public void saveModule(I_InitialPlacement input, O_RP_PlaceArmies output, Player player) {
		if (recordTrainingData) {
			boardIntegrity.checkBoardIntegrity(this.getClass().getSimpleName(),"",input.getBoard());
			Vector data = new Vector();
			this.addToModuleData_Intro(player,data);
			
			// input
			beginTag("input",data);
			this.addToModuleData_Board(data);
			this.addToModuleData_MissionEstimate(input.getMissionEstimate(),data);
			this.addToModuleData_InfluenceMap(input.getAttackerArmiesLeftMap(),data);
			endTag("input",data);
			
			//output
			beginTag("output",data);
			this.addToModuleData_Territory(output.getCountry(),data);
			endTag("output",data);
			endTag("module_run",data);
			this.saveModuleToMem(this.module_names[7],data);
		}				
	}

	public void saveModule(T_RP_AttackPlanListElement attack_plan, T_Board board, O_MP_GoalDistribution goaldist, Player player) {
		if (recordTrainingData) {
			boardIntegrity.checkBoardIntegrity(this.getClass().getSimpleName(),"",board);
			Vector data = new Vector();
			// Special intro for this training data:
			beginTag("attack_plan",data);
			beginTag("player_reference",data);
			addToData(T_Game.decentColorToString(player.getColor()),data);
			endTag("player_reference",data);
			beginTag("game_file", data);
			addToData(this.currentGameFile, data);
			endTag("game_file", data);
			beginTag("current_round", data);
			addToData(String.valueOf(this.roundsPlayed), data);
			endTag("current_round", data);

			beginTag("data_used",data);
			this.addToModuleData_Board(data);
			this.addToModuleData_GoalDistribution(goaldist,data);
			endTag("data_used",data);
			
			this.addToModuleData_AttackPlanListElement(attack_plan, data);

			endTag("attack_plan",data);
			this.saveModuleToMem(this.module_names[8],data);
		}			
	}

	public void saveModule(T_RP_MergedPlanElement plan, T_Board board, O_IG_NextMoveEstimate nextMove, O_MP_GoalDistribution goaldist, Player player) {
		if (recordTrainingData) {
			boardIntegrity.checkBoardIntegrity(this.getClass().getSimpleName(),"",board);
			Vector data = new Vector();
			// Special intro for this training data:
			beginTag("defense_plan",data);
			beginTag("player_reference",data);
			addToData(T_Game.decentColorToString(player.getColor()),data);
			endTag("player_reference",data);
			beginTag("game_file", data);
			addToData(this.currentGameFile, data);
			endTag("game_file", data);
			beginTag("current_round", data);
			addToData(String.valueOf(this.roundsPlayed), data);
			endTag("current_round", data);

			beginTag("data_used",data);
			this.addToModuleData_Board(data);
			this.addToModuleData_NextMoveEstimate(nextMove,data);
			this.addToModuleData_GoalDistribution(goaldist,data);
			endTag("data_used",data);
			
			this.addToModuleData_MergedPlanElement(plan,data);

			endTag("defense_plan",data);
			this.saveModuleToMem(this.module_names[9],data);
		}					
	}

	public void addModuleRunTime(int playerIndex, String module, long time) {
		this.moduleRunTimes[playerIndex][moduleNameToIndex(module)].addTime(time);
	}
	
	public void setModuleLoadTime(int playerIndex, String module, long time) {
		this.moduleLoadTimes[playerIndex][moduleNameToIndex(module)] = time;
	}

	private int moduleNameToIndex(String module) {
		int i = T_Game.MODULE_NAMES.length-1;
		while ((i > -1) && (!T_Game.MODULE_NAMES[i].equals(module))) {
			i--;
		}
		if (i == -1) {
			throw new UnsupportedOperationException("Module not found: "+module);
		}
		return i;
	}

	private void doBenchmarkTest() {
		benchmarkTimer.startTimer();
		Random r = new Random();
		for (int i = 0; i < 10000; i++) {
			double s = Math.sqrt(r.nextDouble()*i);
		}
		benchmarkTimer.endTimer();
	}

/*	public Vector<C_Timing> getFrameworkTimer(int playerIndex) {
		return frameworkTimers[playerIndex];
	}
*/	
	public void stopFrameworkTimer(Player p) {
		C_Timing t = frameworkTimers[this.game.getPlayers().indexOf(p)].lastElement();
		if (!t.isStopped()) {
			t.endTimer();
		}
	}
	
	public void startFrameworkTimer(Player p) {
		currentFrameworkTimer = p;
		Vector<C_Timing> t = frameworkTimers[this.game.getPlayers().indexOf(p)];
		t.add(new C_Timing());
		t.lastElement().startTimer();
	}
}