package risk.AI.Modules;

import net.yura.domination.engine.core.Continent;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Mission;
import net.yura.domination.engine.core.Player;
import risk.AI.Data_Structures.Module_Input.I_IG_ContinentEstimate;
import risk.AI.Data_Structures.Module_Input.I_IG_MissionEstimate;
import risk.AI.Data_Structures.Module_Input.I_IG_NextMoveEstimate;
import risk.AI.Data_Structures.Module_Input.I_IG_Ownership;
import risk.AI.Data_Structures.Module_Input.I_IG_WinningEstimate;
import risk.AI.Data_Structures.Module_Input.I_InitialPlacement;
import risk.AI.Data_Structures.Module_Input.I_MP_GoalDistribution;
import risk.AI.Data_Structures.Module_Input.I_RP_ScoreMergedPlans;
import risk.AI.Data_Structures.Module_Output.O_IG_Ownership;
import risk.AI.Data_Structures.Module_Output.O_IG_WinningEstimate;
import risk.AI.Data_Structures.T_Board;
import risk.AI.Data_Structures.T_Game;
import risk.AI.Data_Structures.T_InfluenceMap;
import risk.AI.Data_Structures.T_Opp_RiskCards;
import risk.AI.Data_Structures.T_Past;
import risk.AI.Data_Structures.T_RP_AttackPlanList;
import risk.AI.Data_Structures.T_RP_AttackPlanListElement;
import risk.AI.Data_Structures.T_RP_DefenseList;
import risk.AI.Data_Structures.T_RP_DefenseListElement;
import risk.AI.Data_Structures.T_RP_MergedPlanElement;
import risk.AI.Data_Structures.T_RP_MergedPlanList;
import risk.AI.Data_Structures.T_TrainingExampleWriter;
import risk.AI.Modules.InformationGivers.C_IG_Continent;
import risk.AI.Modules.InformationGivers.C_IG_Mission;
import risk.AI.Modules.InformationGivers.C_IG_NextMove;
import risk.AI.Modules.InformationGivers.C_IG_Ownership;
import risk.AI.Modules.InformationGivers.C_IG_Winning;
import risk.AI.Modules.MasterPrioritizer.C_MasterPrioritizer;
import risk.AI.Modules.RoundPlanner.C_RP_Attack;
import risk.AI.Modules.RoundPlanner.C_RP_Fortify;
import risk.AI.Modules.RoundPlanner.C_RP_InitPlacement;
import risk.AI.Modules.RoundPlanner.C_RP_Planner;
import risk.AI.Modules.RoundPlanner.C_RP_Reinforce;
import risk.AI.Modules.RoundPlanner.C_RP_ScoreMergedPlans;
import risk.AI.Techniques.Pathfinding.Pathfinder;
import java.util.Random;
import java.util.Vector;

public class AI_Framework {

	private Random rand = new Random(); // used in selectCapital only.

	private final boolean debugging = false;
	private final boolean outputLog = false;

	protected C_IG_Mission igMission_module;
	protected C_IG_Winning igWinning_module;
	protected C_IG_NextMove igNextMove_module;
	protected C_IG_Ownership igOwnership_module;
	protected C_IG_Continent igContinent_module;
	protected C_MasterPrioritizer mp_module;
	protected C_RP_Planner rpPlanner_module;
	private C_RP_Reinforce rpReinforce_module;
	protected C_RP_Attack rpAttack_module;
	private C_RP_Fortify rpFortify_module;
	private C_RP_InitPlacement rpInitPlacement_module;
	private C_RP_ScoreMergedPlans rpScoreMergedPlans_module;
	//private C_RP_MakeSimpleAttackPlan rpMakeSimpleAttackPlan_module;

	protected T_Past past;
	protected T_Board board;
	protected T_Opp_RiskCards opp_risk_cards;
	protected T_Game game;
	protected T_TrainingExampleWriter trainingExample;
	private O_IG_Ownership continentDistribution; // Bruges til at finde ud af om man lige pludselig ejer et continent.
	private int lastBattleArmiesLost = 0;
	private boolean beginningOfTurn;

	private Pathfinder pathfinder;
	protected Player player;
	protected Mission ownMission;
	protected T_InfluenceMap attackerArmiesLeftMap;

	protected String[] types;

	private Vector frameworkLog = new Vector();

	private boolean newTurn = true;

	private T_RP_MergedPlanList mergedPlanList = null;
	private boolean reviseAttackPlanNow = true;
	private boolean isFirstRoundEver;

	/** Are we following a simple attack plan just to gain a Risk card? */
	//private boolean isAttackingSimple;

	/**
	 * types[0]: type of igMission<br>
	 * types[1]: type of igWinning<br>
	 * types[2]: type of igNextMove<br>
	 * types[3]: type of igContinent<br>
	 * types[4]: type of mp<br>
	 * types[5]: type of rpPlanner_MakeAttackPlan<br>
	 * types[6]: type of rpPlanner_APCost<br>
	 * types[7]: type of rpPlanner_APPriority<br>
	 * types[8]: type of rpPlanner_ScorePlans<br>
	 * types[9]: type of rpPlanner_DeCost<br>
	 * types[10]: type of rpPlanner_DePriority<br>
	 * types[11]: type of rpReinforce_CashCards<br>
	 * types[12]: type of rpReinforce_PlaceArmies<br>
	 * types[13]: type of rpScorePlan<br>
	 * types[14]: type of rpDoAttack<br>
	 * types[15]: type of rpFortify<br>
	 * types[16]: type of rpInitPlacement<br>
	 * types[17]: type of rpPlanner_MakeSimpleAttackPlan<br>
	 */
	public AI_Framework() {
	}

	public AI_Framework(String[] types, Player player, Mission ownMission, T_TrainingExampleWriter trainingExample) {
		this.types = types;
		this.player = player;
		this.ownMission = ownMission; // Overridden by init(). But igNextMove does not call init() and should not call init()!.
		this.trainingExample = trainingExample;
	}

	public void init(T_Past past, T_Game game, T_Board board) {
		this.past = past;
		this.board = board;
		this.game = game;
		this.opp_risk_cards = new T_Opp_RiskCards(game);
		this.pathfinder = new Pathfinder(board);
		this.ownMission = player.getMission();
		this.attackerArmiesLeftMap = new T_InfluenceMap(board, game);
		
		igMission_module = new C_IG_Mission(types[0],game,this.trainingExample, player);
		igNextMove_module = new C_IG_NextMove(types[2], game, pathfinder, player, this.trainingExample);
		igOwnership_module = new C_IG_Ownership(game, board);
		mp_module = new C_MasterPrioritizer(types[4], game.getPlayerIndex(player), ownMission, board.getContinents(), game, this.trainingExample);
		String[] planner_types = {types[5],types[6],types[7],types[8],types[9],types[10]};
		rpPlanner_module = new C_RP_Planner(planner_types, game, pathfinder, player, this.trainingExample);
		// Needs to be create after the Planner since it uses the De_Cost module
		igContinent_module = new C_IG_Continent(types[3], game, game.getPlayerIndex(player), pathfinder.getAStarPathFinder(), rpPlanner_module.getAP_Cost_Module(),this.trainingExample);
		igWinning_module = new C_IG_Winning(types[1], game, pathfinder.getAStarPathFinder(),this.trainingExample, player, rpPlanner_module.getAP_Cost_Module());
		String[] reinforce_types = {types[11],types[12]};
		rpReinforce_module = new C_RP_Reinforce(reinforce_types, game, player, this.trainingExample);
		rpAttack_module = new C_RP_Attack(types[13], game, player, ownMission, this.trainingExample);
		rpFortify_module = new C_RP_Fortify(types[14], player, this.trainingExample, game, pathfinder.getAStarPathFinder());
		rpInitPlacement_module = new C_RP_InitPlacement(types[15], player, this.trainingExample, game);
		//rpMakeSimpleAttackPlan_module = new C_RP_MakeSimpleAttackPlan(types[17], game, player);
		rpScoreMergedPlans_module = new C_RP_ScoreMergedPlans(types[16], game, player, this.trainingExample);
		
		isFirstRoundEver = true;
	}
	
	/**
	 * Checks to see if a new turn has begun. Runs all IGs, the MP and the Planner.
	 */
	private void checkNewTurn() {
		if (newTurn) {
			beginningOfTurn = true;
			trainingExample.newTurn(this.player);
			// Run all IGs and the MP.
			runIGs();
			runMP();
			// Run the planner
			
			runRP_Planner();
			
			newTurn = false;
			
			reviseAttackPlanNow = true;
			isFirstRoundEver = false;
		}
	}
	
	private void runIGs() {
		igMission_module.run(new I_IG_MissionEstimate(past));
		igContinent_module.run(new I_IG_ContinentEstimate(board,this.opp_risk_cards, this.beginningOfTurn));
		igWinning_module.run(new I_IG_WinningEstimate(board,this.opp_risk_cards,igMission_module.getOutput(), this.beginningOfTurn, this.igContinent_module.getOutput()));
		// Don't trust the WinningEstimate in the first round of the game
		if (isFirstRoundEver) {
			O_IG_WinningEstimate estimate = igWinning_module.getOutput();
			for(int playerIndex = 0; playerIndex < game.getPlayerCount(); playerIndex++) {
				estimate.setEstimate(playerIndex, 0, estimate.getEstimate(playerIndex, 0) * 0.75f);
			}
		}
		igOwnership_module.run(new I_IG_Ownership(board));
		igNextMove_module.run(new I_IG_NextMoveEstimate(board,this));
		
		// Copy the continent ownership - used to see if the AI suddenly owns a continent -> run the IGs and MP again.
		continentDistribution = new O_IG_Ownership();
		for (int i = 0; i < game.getPlayerCount(); i++)  {
			for (int j = 0; j < board.getContinents().size(); j++) {
				continentDistribution.setIsOwned(i,j, igOwnership_module.getOutput().getIsOwned(i,j));
			}
		}
		
		addIGsToLog(frameworkLog);
	}
	
	private void runMP() {
		mp_module.run(new I_MP_GoalDistribution(igMission_module.getOutput(),igNextMove_module.getOutput(),igContinent_module.getOutput(),igWinning_module.getOutput(), igOwnership_module.getOutput()), false);
		addMPToLog(frameworkLog);
		// Save log here
		outputLog(frameworkLog);
	}
	
	public String tradeCards() {
		debug("tradeCards");
		trainingExample.recordBoardState(board);
		checkNewTurn();
		rpReinforce_module.runCashCards(this.mergedPlanList);
		return rpReinforce_module.getOutput();
	}
	
	public String placeArmies(boolean initialArmyPlacement) {
		debug("placeArmies");
		trainingExample.recordBoardState(board);
		if (initialArmyPlacement) {
			igMission_module.run(new I_IG_MissionEstimate(past));
			attackerArmiesLeftMap.calcAttackerArmiesLeft();
			rpInitPlacement_module.run(new I_InitialPlacement(board, igMission_module.getOutput(), attackerArmiesLeftMap));
			return rpInitPlacement_module.getOutput();
		} else {
			checkNewTurn();
			rpReinforce_module.runPlaceArmy(this.mergedPlanList, this.board, this.igNextMove_module.getOutput(), this.mp_module.getOutput());
			beginningOfTurn = false;
			return rpReinforce_module.getOutput();
		}
	}
	
	public String attack() {
		debug("attack");
		trainingExample.recordBoardState(board);
		
		// Has the AI just conquered a continent?
		for (int i = 0; i < board.getContinents().size(); i++) {
			if (((Continent)board.getContinents().get(i)).isOwned(player) != continentDistribution.getIsOwned(game.getPlayerIndex(player),i)) {
				this.runIGs();
				this.runMP();
				reviseAttackPlanNow = true;
				break;
			}
		}
		String out = doAttack();
		if (out == null) {
			// revise attack plan
			//runRP_Planner(); // Moved to doAttack.
			// try again
			out = doAttack();
			if (out == null) {
				out = "endattack";
			}
		}
		// If we have run out of attack plans, but still need a Risk card
		/*
		if (isAttackingSimple) {
			isAttackingSimple = false;
		} else {
			if (out.equals("endattack") && !game.riskCardIsReceivedThisRound(player)) {
				isAttackingSimple = true;
				rpMakeSimpleAttackPlan_module.run(new I_RP_MakeSimpleAttackPlan(mp_module.getOutput(), board));
				T_RP_AttackPlan plan = rpMakeSimpleAttackPlan_module.getOutput().getAttackPlan();
				if (plan != null) {
					out = "attack " + plan.getFirstCountry().getName() + " " + plan.getLastCountry().getName();
				}
				int a = 0;
			}
		}*/
		return out;
	}
	
	private String doAttack() {
		//boolean revised = (currentAttackPlan == null);
		boolean revised = reviseAttackPlanNow;
		if (reviseAttackPlanNow) {
			runRP_Planner();
			if (rpPlanner_module.getOutput().getAttackPlanList().size() == 0) {
				return "endattack";
			} else {
				//currentAttackPlan = rpPlanner_module.getOutput().getAttackPlanList().getBestAttackPlan();
				reviseAttackPlanNow = false;
			}
		}
		rpAttack_module.run(rpPlanner_module.getOutput().getAttackPlanList(),igWinning_module.getOutput(),revised);
		if (rpAttack_module.doesAttackPlanNeedRevising()) {
			reviseAttackPlanNow = true;
			//currentAttackPlan = null;
			return null;
		} else {
			// Set continent ownership
			for (int i = 0; i < board.getContinents().size(); i++) {
				this.continentDistribution.setIsOwned(game.getPlayerIndex(player),i,((Continent)board.getContinents().get(i)).isOwned(player));
			}
			if (revised) {
				// Attack plan has begun
				T_RP_AttackPlanListElement plan = rpAttack_module.getCurrentAttackPlan();
				if (outputLog) {
					System.out.println(plan);
					int a = 0;
				}
			}
			this.trainingExample.saveModule(rpAttack_module.getCurrentAttackPlan(),this.board, this.mp_module.getOutput(), this.player);
		}
		return rpAttack_module.getOutput();
	}

	/**
	 * Attacking roll
	 */
	public String roll() {
		int numdice;
		/*if (isAttackingSimple) {
			T_RP_AttackPlan plan = rpMakeSimpleAttackPlan_module.getOutput().getAttackPlan();
			int attackingArmies = plan.getCountry(0).getArmies() - (Integer) plan.getArmiesToLeave(0);
			if (attackingArmies > 0) {
				return "roll " + Math.min(3, attackingArmies);
			} else {
				return "retreat";
			}
		} else {*/
			this.rpAttack_module.getCurrentAttackPlan().addCostToCostSpent(game.getLastBattle_ArmiesLost());
			boolean attackPlanTooExpensive = false;
			//System.out.println("AP cost so far: "+this.rpAttack_module.getCurrentAttackPlan().getCostSpent());
			//System.out.println("AP estim. cost: "+this.rpAttack_module.getCurrentAttackPlan().getEstimatedCost());
			if ((this.rpAttack_module.getCurrentAttackPlan().getEstimatedCost() < (this.rpAttack_module.getCurrentAttackPlan().getCostSpent()/(1+this.game.getAISettings().attackEstimatedCostExceed)))) {
				attackPlanTooExpensive = true;
			}
			numdice = this.rpAttack_module.getAttackingCountry().getArmies() - rpAttack_module.getAttackingCountry_ArmiesToLeave();
			if (numdice > 3) {
				numdice = 3;
			}
			if ((numdice < 1) || (attackPlanTooExpensive)) {
				reviseAttackPlanNow = true;
				game.resetLastBattle_ArmiesLost();
				//currentAttackPlan = null;
				return "retreat";
			}
			return "roll "+numdice;
		//}
	}
	
	public String move() {
		trainingExample.recordBoardState(board);
		/*if (isAttackingSimple) {
			T_RP_AttackPlan plan = rpMakeSimpleAttackPlan_module.getOutput().getAttackPlan();
			int armiesToMove = plan.getCountry(0).getArmies() - (Integer) plan.getArmiesToLeave(0);
			if (armiesToMove > 0) {
				return "move " + armiesToMove;
			} else {
				return "nomove";
			}
		} else {*/
			int armies = this.rpAttack_module.getAttackingCountry().getArmies() - rpAttack_module.getAttackingCountry_ArmiesToLeave();
			if (armies < 1) {
				return "nomove";
			} else {
				this.rpAttack_module.getCurrentAttackPlan().addCostToCostSpent(rpAttack_module.getAttackingCountry_ArmiesToLeave());
				return "move "+armies;
			}
		//}
	}
	
	public String fortify() {
		trainingExample.recordBoardState(board);
		runIGs();
		runMP();
		rpPlanner_module.makeDefenseList(board, this.igNextMove_module.getOutput(), mp_module.getOutput(), attackerArmiesLeftMap, false);
		rpFortify_module.run(rpPlanner_module.getOutput().getDefenseList(), mp_module.getOutput(), board);
		newTurn = true; // Should be in endTurn() - but that might not be runned!
		return rpFortify_module.getOutput();
	}
	
	public String selectCapital() {
		String output = "";
		Vector t = player.getTerritoriesOwned();
		output = "capital " + ((Country)t.elementAt( rand.nextInt(t.size()) )).getColor();
		return output;
	}
	
	public String defend(int defending_armies) {
		if (defending_armies > 2) {
			defending_armies = 2;
		}
		return "roll "+defending_armies;
	}
	
	public String endTurn() {
		return "endgo";
	}
	
	private void debug(String s) {
		if (debugging) {
			System.out.println(s);
		}
	}
	
	private void runRP_Planner() {
		// Calculate influence map
		game.resetLastBattle_ArmiesLost();
		attackerArmiesLeftMap.calcAttackerArmiesLeft();
		rpPlanner_module.run(mp_module.getOutput(),board,this.opp_risk_cards,igNextMove_module.getOutput(), attackerArmiesLeftMap, this.beginningOfTurn, game.riskCardIsReceivedThisRound(player), false);
		// Construct a merged planlist.
		mergedPlanList = new T_RP_MergedPlanList();
		T_RP_AttackPlanList ap = rpPlanner_module.getOutput().getAttackPlanList();
		//this.attackPlanList = ap;
		for (int i = 0; i < ap.size(); i++) {
			boolean add = true;
			for (int j = 0; j < mergedPlanList.size(); j++) {
				// If the start country of an attack plan is already present in the mergedList, then it should only be added, if its priority is greater than the present one.
				if (((T_RP_MergedPlanElement)mergedPlanList.get(j)).getCountry().equals(((T_RP_AttackPlanListElement)ap.get(i)).getAttackPlan().getCountry(0))) {
					if (((T_RP_AttackPlanListElement)ap.get(i)).getPriority() > ((T_RP_MergedPlanElement)mergedPlanList.get(j)).getPriority()) {
						mergedPlanList.remove(j);
					} else {
						add = false;
					}
					break;
				}
			}
			if (add) {
				T_RP_MergedPlanElement e = new T_RP_MergedPlanElement();
				e.setCountry(((T_RP_AttackPlanListElement)ap.get(i)).getAttackPlan().getCountry(0));
				e.setEstimatedCost(((T_RP_AttackPlanListElement)ap.get(i)).getEstimatedCost());
				e.setMinimumCost(((T_RP_AttackPlanListElement)ap.get(i)).getMinimumCost());
				e.setPriority(((T_RP_AttackPlanListElement)ap.get(i)).getPriority());
				e.setIsDefensePlan(false);
				mergedPlanList.add(e);
			}
		}
		
		T_RP_DefenseList de = rpPlanner_module.getOutput().getDefenseList();
		for (int i = 0; i < de.size(); i++) {
			T_RP_MergedPlanElement e = new T_RP_MergedPlanElement();
			e.setCountry(((T_RP_DefenseListElement)de.get(i)).getCountry());
			e.setEstimatedCost(((T_RP_DefenseListElement)de.get(i)).getCost());
			e.setMinimumCost(0);
			e.setPriority(((T_RP_DefenseListElement)de.get(i)).getPriority());
			e.setIsDefensePlan(true);
			e.setAttackerArmiesLeft(((T_RP_DefenseListElement)de.get(i)).getAttackerArmiesLeft());
			mergedPlanList.add(e);
		}
		
		for (int i = 0; i < mergedPlanList.size(); i++) {
			T_RP_MergedPlanElement e = (T_RP_MergedPlanElement)mergedPlanList.get(i);
			this.rpScoreMergedPlans_module.run(new I_RP_ScoreMergedPlans(e.getPriority(),e.getEstimatedCost(), e.getIsDefensePlan()));
			e.setScore(this.rpScoreMergedPlans_module.getOutput().getScore());
		}
		
		/*if (this.beginningOfTurn && this.debugging) {
			T_RP_MergedPlanList mList = mergedPlanList.getListSortedByPriority();
			System.out.println("Merged lists:");
			for (int i = 0; i < mList.size(); i++) {
				String str = " " + mList.get(i).getCountry().getName() + " (c:"+mList.get(i).getEstimatedCost()+",p:"+mList.get(i).getPriority()+")";
				if (mList.get(i).getIsDefensePlan()) {
					str = str + " (Defense)";
				}
				System.out.println(str);
			}
			int a = 0;
		}*/
	}
	
	public String[] getTypes() {
		return types;
	}
	
	public T_Opp_RiskCards getOpponentRiskCards() {
		return this.opp_risk_cards;
	}
	
	public T_Past getPast() {
		return past;
	}
	
	public T_Board getBoard() {
		return board;
	}
	
	public C_IG_Mission getIGMissionModule() {
		return this.igMission_module;
	}
	
	public C_IG_Ownership getIGOwnershipModule(){
		return this.igOwnership_module;
	}
	
	public C_IG_Continent getIGContinentModule() {
		return this.igContinent_module;
	}
	
	public C_IG_Winning getIGWinningModule() {
		return this.igWinning_module;
	}

	public C_MasterPrioritizer getMasterPrioritizerModule() {
		return this.mp_module;
	}
	
	public C_RP_Planner getPlannerModule() {
		return this.rpPlanner_module;
	}
	
	public C_RP_Attack getAttackModule() {
		return this.rpAttack_module;
	}
	
	public Pathfinder getPathfinder() {
		return this.pathfinder;
	}
	
	public Mission getMostLikelyMission(Player p) {
		return this.igMission_module.getOutput().getMostLikelyMission(game.getPlayers().indexOf(p),game.getMissions());
	}
	
	public T_Game getGame() {
		return this.game;
	}
	
	public String getTrainingDataBaseDir() {
		return this.trainingExample.getBaseDir();
	}
	
	private void addIGsToLog(Vector log) {
		log.add("===========================================================");
		log.add("Player: "+this.player.getName()+" | Color: "+T_Game.decentColorToString(player.getColor()));
		log.add("  Mission Estimate:");
		log.add("  ---------------------------------------------------------");
		for (int i = 0; i < game.getPlayerCount(); i++) {
			log.add("    "+T_Game.decentColorToString(((Player)game.getPlayers().get(i)).getColor()));
			for (int j = 0; j < game.getMissionCount(); j++) {
				String str = "";
				if (((Mission)game.getMissions().get(j)).equals(this.igMission_module.getOutput().getMostLikelyMission(i,game.getMissions()))) {
					str = " (most likely)";
				}
				String str2 = " ";
				if (((Mission)game.getMissions().get(j)).equals(((Player)game.getPlayers().get(i)).getMission())) {
					str2 = "*";
				}
				log.add("     "+str2+T_Game.decentMissionToString((Mission)game.getMissions().get(j),null)+": "+String.valueOf(this.igMission_module.getOutput().getEstimate(i,j))+str);
			}
		}
		log.add("  Winning Estimate:");
		log.add("  ---------------------------------------------------------");
		for (int i = 0; i < game.getPlayerCount(); i++) {
			String str = "";
			for (int j = 0; j < game.getAISettings().roundsToPredict; j++) {
				str = str + String.valueOf(this.igWinning_module.getOutput().getEstimate(i,j));
				if (j < game.getAISettings().roundsToPredict-1) {
					str = str + ", ";
				}
			}
			log.add("    "+T_Game.decentColorToString(((Player)game.getPlayers().get(i)).getColor())+": "+str);
		}
		log.add("  Continent Estimate:");
		log.add("  ---------------------------------------------------------");
		for (int i = 0; i < game.getPlayerCount(); i++) {
			log.add("    "+T_Game.decentColorToString(((Player)game.getPlayers().get(i)).getColor()));
			for (int k = 0; k < board.getContinents().size(); k++) {
				String str = "";
				for (int j = 0; j < game.getAISettings().roundsToPredict; j++) {
					str = str + String.valueOf(this.igContinent_module.getOutput().getEstimate(i,k,j));
					if (j < game.getAISettings().roundsToPredict-1) {
						str = str + ", ";
					}
				}
				log.add("      "+((Continent)board.getContinents().get(k)).getName()+": "+str);
			}
		}
		log.add("  NextMove Estimate:");
		log.add("  ---------------------------------------------------------");
		Vector t = this.igNextMove_module.getOutput().getTerritoryInDangerList();
		for (int i = 0; i < t.size(); i++) {
			log.add("    "+((Country)t.get(i)).getIdString()+" (owner: "+T_Game.decentColorToString(((Country)t.get(i)).getOwner().getColor())+")");
		}
	}
	
	private void addMPToLog(Vector log) {
		log.add("  Goal Distribution:");
		log.add("  ---------------------------------------------------------");
		log.add("    Conquer and defend continent:");
		for (int i = 0; i < board.getContinents().size(); i++) {
			log.add("      "+((Continent)board.getContinents().get(i)).getName()+": "+String.valueOf(this.mp_module.getOutput().getDistribution_CD()[i]));
		}
		log.add("    Conquer continent:");
		for (int i = 0; i < board.getContinents().size(); i++) {
			log.add("      "+((Continent)board.getContinents().get(i)).getName()+": "+String.valueOf(this.mp_module.getOutput().getDistribution_C()[i]));
		}
		log.add("    Defend continent:");
		for (int i = 0; i < board.getContinents().size(); i++) {
			log.add("      "+((Continent)board.getContinents().get(i)).getName()+": "+String.valueOf(this.mp_module.getOutput().getDistribution_D()[i]));
		}
		log.add("    Obstruct continent:");
		for (int i = 0; i < board.getContinents().size(); i++) {
			log.add("      "+((Continent)board.getContinents().get(i)).getName()+": "+String.valueOf(this.mp_module.getOutput().getDistribution_O()[i]));
		}
		log.add("    Obstruct and defend continent:");
		for (int i = 0; i < board.getContinents().size(); i++) {
			log.add("      "+((Continent)board.getContinents().get(i)).getName()+": "+String.valueOf(this.mp_module.getOutput().getDistribution_OD()[i]));
		}
		log.add("    Attack Player:");
		for (int i = 0; i < game.getPlayers().size(); i++) {
			log.add("      "+T_Game.decentColorToString(((Player)game.getPlayers().get(i)).getColor())+": "+String.valueOf(this.mp_module.getOutput().getDistribution_A()[i]));
		}
		log.add("    18:"+String.valueOf(this.mp_module.getOutput().getDistribution_18()));
		log.add("    24:"+String.valueOf(this.mp_module.getOutput().getDistribution_24()));
		log.add("===========================================================");
	}
	
	private void outputLog(Vector log) {
		if (outputLog) {
			for (int i = 0; i < log.size(); i++) {
				System.out.println((String)log.get(i));
			}
		}
		log.clear();
	}
	
	public Mission getMission() {
		return this.ownMission;
	}
}
