package risk.AI.Modules.RoundPlanner.Planner;

import net.yura.domination.engine.core.Continent;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Player;
import risk.AI.Data_Structures.*;
import risk.AI.Data_Structures.Module_Output.O_MP_GoalDistribution;
import risk.AI.Data_Structures.Module_Output.O_RP_MakeAttackPlan;
import risk.AI.Techniques.Pathfinding.*;
import java.util.*;
import risk.AI.Data_Structures.Module_Input.I_RP_DefenseCost;
import risk.AI.Data_Structures.Module_Input.I_RP_MakeAttackPlan;

public class C_RP_Planner_MakeAttackPlan {
	
	private String type;
	private C_RP_Planner_De_Cost de_cost_module;
	private T_Game game;
	private Random rand = new Random();
	
	private O_RP_MakeAttackPlan output;
	private Player player;
	private Pathfinder pathfinder;
	private T_TrainingExampleWriter trainingExample;
	private C_Timing timer = new C_Timing();
	
	public C_RP_Planner_MakeAttackPlan(String type, T_Game game, C_RP_Planner_De_Cost de_cost_module, Pathfinder pathfinder, T_TrainingExampleWriter trainingExample, Player player) {
		this.type = type;
		this.game = game;
		this.de_cost_module = de_cost_module;
		this.player = player;
		this.pathfinder = pathfinder;
		this.trainingExample = trainingExample;
		trainingExample.setModuleLoadTime(game.getPlayerIndex(player), "rp_makeattackplan" ,0);
	}
	
	// startCountry: An occupied territory.
	// targetCountry: An enemy territory.
	public void run(I_RP_MakeAttackPlan input, boolean isOpponentFramework) {
		if (type.equals("random")) {
			random_run(input.getStartCountry(), input.getTargetCountry());
		} else if (type.equals("script")) {
			timer.startTimer();
			script_run(input.getStartCountry(), input.getTargetCountry(), input.getBoard(), input.getGoalDistribution(), input.getAttackerArmiesLeftMap());
			timer.endTimer();
			if (!isOpponentFramework) {
				trainingExample.addModuleRunTime(game.getPlayerIndex(player), "rp_makeattackplan", timer.getTimer());
			}
		}
	}
	
	public O_RP_MakeAttackPlan getOutput() {
		return output;
	}
	
	// Random
	
	private void random_run(Country startCountry, Country targetCountry) {
		output = new O_RP_MakeAttackPlan();
		Vector paths = pathfinder.findPaths(startCountry, targetCountry, player);
		for (int i = 0; i < paths.size(); i++) {
			T_RP_AttackPlan p = new T_RP_AttackPlan();
			p.addCountries((Vector)paths.get(i));
			Vector leaveArmies = new Vector();
			for(int j = 0; j < p.getCountries().size(); j++) {
				leaveArmies.add(1 + rand.nextInt(2));
			}
			p.addArmiesToLeave(leaveArmies);
			output.addAttackPlan(p);
		}
	}
	
	// Script
	
	private void script_run(Country startCountry, Country targetCountry, T_Board board, O_MP_GoalDistribution gd, T_InfluenceMap attackerArmiesLeftMap) {
		output = new O_RP_MakeAttackPlan();
		Vector paths = pathfinder.findPaths(startCountry, targetCountry, player); //findShortestPath(startCountry, targetCountry, currentPlayer, doHamPath);
		for (int i = 0; i < paths.size(); i++) {
			// Does the path cover any continents?
			Vector<Continent> coveredContinents = calcCoveredContinents((Vector) paths.get(i));
			if (coveredContinents.size() > 0) {
				// Make another attack plan where the defense cost of the continent is included in armiesToLeave
				T_RP_AttackPlan p = new T_RP_AttackPlan();
				p.addCountries((Vector)paths.get(i));
				p.addArmiesToLeave(calcArmiesToLeaveDefense(p, board, gd, attackerArmiesLeftMap, coveredContinents));
				p.setCoveredContinents(coveredContinents);
				//System.out.println(p);
				output.addAttackPlan(p);
			}
			// Make attack plan that only conquers the territories and does not defend them
			T_RP_AttackPlan p = new T_RP_AttackPlan();
			p.addCountries((Vector)paths.get(i));
			p.addArmiesToLeave(calcArmiesToLeaveMinimum(gd, p));
			p.setCoveredContinents(null);
			output.addAttackPlan(p);
		}
	}
	
	/** Checks if following the path will conquer a continent. Will also include already owned continent if the path starts in a such. */
	private Vector<Continent> calcCoveredContinents(Vector path) {
		Vector<Continent> checked = new Vector();
		Vector<Continent> result = new Vector();
		for(int territoryIndex = 1; territoryIndex < path.size(); territoryIndex++) {
			Country territory = (Country) path.get(territoryIndex);
			Continent continent = territory.getContinent();
			// Make sure we haven't checked the continent before
			if (!checked.contains(continent)) {
				Vector territories = continent.getTerritoriesContained();
				// Is every enemy territory in the continent in the path?
				boolean covered = true;
				for(int i = 0; (covered) && (i < territories.size()); i++) {
					Country otherTerritory = (Country) territories.get(i);
					if (otherTerritory.getOwner() != player) {
						covered = path.contains(otherTerritory);
					}
				}
				// Yep, the path covers a continent
				if (covered) {
					result.add(continent);
				}
				checked.add(continent);
			}
		}
		return result;
	}
	
	/** Calculates the minimum armies to leave */
	private Vector calcArmiesToLeaveMinimum(O_MP_GoalDistribution goalDist, T_RP_AttackPlan plan) {
		Vector leaveArmies = new Vector();
		
		Integer armies = 1;
		// If the goal "18 with 2 on each" is important, at least 2 armies should be left behind
		if (goalDist.getDistribution_18() > game.getAISettings().importanceThreshold_18w2) {
			armies = 2;
		}
		
		for(int i = 0; i < plan.size(); i++) {
			leaveArmies.add(armies);
		}
		return leaveArmies;
	}
	
	/** Calculates the number of armies that should be left in border territories of covered continents in order to defend them */
	private Vector calcArmiesToLeaveDefense(T_RP_AttackPlan plan, T_Board board, O_MP_GoalDistribution goalDist, T_InfluenceMap attackerArmiesLeftMap, Vector<Continent> coveredContinents) {
		// Add minimum armies to leave
		Vector leaveArmies = calcArmiesToLeaveMinimum(goalDist, plan);
		
		// Calculate the number of armies needed to defend each border territory in the plan
		// It is assumed that the whole continent is conquered so the enemy territories inside it
		// should not be taken into account when calculating this number
		
		// Temporarily make the territories in the plan occupied and
		// set the number of armies on each to 1
		Player[] originalOwners = new Player[plan.size()];
		int[] originalArmyCount = new int[plan.size()];
		for(int i = 0; i < plan.size(); i++) {
			Country territory = plan.getCountry(i);
			originalOwners[i] = territory.getOwner();
			originalArmyCount[i] = territory.getArmies();
			territory.setOwner(player);
			// TODO YURA: this is crazy, need to find another way of doing this without messing with the board
			BadUtils.setArmies(territory, 1);
		}
		// Record this new board in the training data
		this.trainingExample.recordBoardState(board);
		
		// Calculate number of armies to leave in border territories
		for(int i = 0; i < plan.size(); i++) {
			Country territory = plan.getCountry(i);
			Continent continent = territory.getContinent();
			if (board.isBorderTerritory(territory) && (coveredContinents.contains(continent))) {
				// Find the enemy neighbor territory that will receive most reinforcements
				int maxEnemyReinforcements = 0;
				for(int neighborIndex = 0; neighborIndex < territory.getNeighbours().size(); neighborIndex++) {
					Country neighbor = (Country) territory.getNeighbours().get(neighborIndex);
					Player owner = neighbor.getOwner();
					if (owner != player) {
						int enemyReinforcements = game.calcFutureReinforcements(owner, 0).get(0);
						if (enemyReinforcements > maxEnemyReinforcements) {
							maxEnemyReinforcements = enemyReinforcements;
						}
					}
				}
				// Run the cost module
				//de_cost_module.run(new I_RP_DefenseCost(territory, board, Math.max(attackerArmiesLeftMap.getValue(territory), 3))); // "3" just to leave SOME even if there is only 1 enemy neighbor army
				de_cost_module.run(new I_RP_DefenseCost(territory, board, Math.max(attackerArmiesLeftMap.getValue(territory), maxEnemyReinforcements)), true);
				int defenseCost = de_cost_module.getOutput().getCost();
				leaveArmies.set(i, (Integer)Math.max((Integer)leaveArmies.get(i), defenseCost));
				BadUtils.setArmies(territory, (Integer)leaveArmies.get(i));
			}
		}
		
		// Restore the territories' original owners and original number of armies
		for(int i = 0; i < plan.size(); i++) {
			Country territory = plan.getCountry(i);
			territory.setOwner(originalOwners[i]);
			BadUtils.setArmies(territory, originalArmyCount[i]);
		}
		// Record the old board in the training data again
		this.trainingExample.recordBoardState(board);
		
		/*
		// Calculate leave armies
		for(int territoryIndex = 0; territoryIndex < plan.size(); territoryIndex++) {
			Country territory = plan.getCountry(territoryIndex);
			Continent continent = territory.getContinent();
			if (coveredContinents.contains(continent)) {
				// If the start territory's continent is occupied, leave armies to defend the border
				if (territoryIndex == 0) {
					de_cost_module.run(new I_RP_DefenseCost(territory, board, attackerArmiesLeftMap.getValue(territory)));
					int defenseCost = de_cost_module.getOutput().getCost();
					// Find the enemy neighbor which will receive most reinforcements
					Country neighbor;
					int enemyReinforcement, maxEnemyReinforcement = 0;
					for(int neighborIndex = 0; neighborIndex < territory.getNeighbours().size(); neighborIndex++) {
						neighbor = ((Country) territory.getNeighbours().get(neighborIndex));
						if (neighbor.getOwner() != player) {
							enemyReinforcement = game.calcFutureReinforcements(neighbor.getOwner(), 1).get(0);
							if (enemyReinforcement > maxEnemyReinforcement) {
								maxEnemyReinforcement = enemyReinforcement;
							}
						}
					}
					defenseCost += maxEnemyReinforcement;
					leaveArmies.set(0, (Integer)Math.max((Integer)leaveArmies.get(0), defenseCost));
				}
			}
		}
		 
		 
		// If the goal "Conquer & Defend" for that continent is more important than "Conquer" for that continent
		// then the armies needed to defend border territories in the plan should be included in the cost
		float priority_CD = goalDist.getDistribution_CD()[board.getContinents().indexOf(continent)];
		float priority_C = goalDist.getDistribution_C()[board.getContinents().indexOf(continent)];
		if (priority_CD >= priority_C) {
		}
		 
		// If the continent is owned, leave armies to defend it at the border
		Country territory = plan.getCountry(0);
		if (territory.getContinent().isOwned(player)) {
		}
		 */
		return leaveArmies;
	}

	public void setNewCurrentPlayer(int playerIndex) {
		this.player = (Player)game.getPlayers().get(playerIndex);
	}


}