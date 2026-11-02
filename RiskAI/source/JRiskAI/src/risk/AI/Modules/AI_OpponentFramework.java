/*
 * AI_OpponentFramework.java
 *
 * Created on 20. februar 2006, 15:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package risk.AI.Modules;

import net.yura.domination.engine.core.Player;
import risk.AI.Modules.InformationGivers.C_IG_NextMove;
import risk.AI.Data_Structures.Module_Input.*;
import risk.AI.Data_Structures.*;

/**
 *
 * @author Administrator
 */
public class AI_OpponentFramework extends AI_Framework {
	
	/** Creates a new instance of AI_OpponentFramework */
	
	public AI_OpponentFramework(Player opponent, AI_Framework currentFramework) {
		super(null, opponent, currentFramework.getMostLikelyMission(opponent), new T_TrainingExampleWriter(null, null, false, currentFramework.getTrainingDataBaseDir()));
		
		String[] newTypes = currentFramework.getTypes().clone();
		newTypes[2] = "none"; // set IG_NextMove type to "none", otherwise, the IG_NextMove would call itself, which would call itself and so on.
		
		this.types = newTypes;
		this.game = currentFramework.getGame();
		this.opp_risk_cards = currentFramework.getOpponentRiskCards();
		this.board = currentFramework.getBoard();
		this.past = currentFramework.getPast();
		this.attackerArmiesLeftMap = new T_InfluenceMap(board, game);
		
		this.igMission_module = currentFramework.getIGMissionModule();
		this.igWinning_module = currentFramework.getIGWinningModule();
		this.igNextMove_module = new C_IG_NextMove(types[2], currentFramework.getGame(), currentFramework.getPathfinder(), opponent, this.trainingExample);
		this.igContinent_module = currentFramework.getIGContinentModule();
		this.igOwnership_module = currentFramework.getIGOwnershipModule();
		
		//this.mp_module = new C_MasterPrioritizer(newTypes[4], game.getPlayerIndex(opponent), ownMission, board.getContinents(), game, this.trainingExample);
		this.mp_module = currentFramework.getMasterPrioritizerModule();
		//String[] planner_types = {newTypes[5], newTypes[6], newTypes[7], newTypes[8], newTypes[9], newTypes[10]};
		//this.rpPlanner_module = new C_RP_Planner(planner_types, game, currentFramework.getPathfinder(), opponent, this.trainingExample);
		this.rpPlanner_module = currentFramework.getPlannerModule();
		
		//this.rpAttack_module = new C_RP_Attack(newTypes[13],game, opponent, ownMission, this.trainingExample);
		this.rpAttack_module = currentFramework.getAttackModule();
	}

	public T_RP_AttackPlanListElement getAttackPlan() {
		return rpAttack_module.calcBestAttackPlan(rpPlanner_module.getOutput().getAttackPlanList());
	}

	public void run() {
		// Run all IGs and the MP.
		//igMission_module.run(new I_IG_MissionEstimate(past));
		//igWinning_module.run(new I_IG_WinningEstimate(board,this.opp_risk_cards,igMission_module.getOutput()));
		igNextMove_module.run(new I_IG_NextMoveEstimate(board,this));
		//igContinent_module.run(new I_IG_ContinentEstimate(board,this.opp_risk_cards));
		mp_module.run(new I_MP_GoalDistribution(igMission_module.getOutput(),igNextMove_module.getOutput(),igContinent_module.getOutput(),igWinning_module.getOutput(), igOwnership_module.getOutput()), true);
		// Run the planner
		attackerArmiesLeftMap.calcAttackerArmiesLeft();
		rpPlanner_module.run(mp_module.getOutput(),board,this.opp_risk_cards,igNextMove_module.getOutput(), attackerArmiesLeftMap, true, false, true);
	}
	
}

