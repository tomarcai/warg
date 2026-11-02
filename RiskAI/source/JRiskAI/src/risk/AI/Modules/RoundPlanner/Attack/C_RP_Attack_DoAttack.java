package risk.AI.Modules.RoundPlanner.Attack;

import net.yura.domination.engine.core.Continent;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Mission;
import net.yura.domination.engine.core.Player;
import risk.AI.Data_Structures.*;
import risk.AI.Data_Structures.Module_Output.O_IG_WinningEstimate;

public class C_RP_Attack_DoAttack {  

	private String[] types;
	
	private boolean reviseAttackPlan = false;
	//private int attackPlanCost = 0;
    private Country attackingCountry;
    private int attackingCountry_armiesToLeave = 0;
	private T_BattleOutcomeProbTable battleTable;
	private Mission ownMission;
	
	private String output;
	private Player currentPlayer;
	private T_Game game;
	
	public C_RP_Attack_DoAttack(T_Game game, Player currentPlayer, Mission ownMission) {
		this.game = game;
		this.battleTable = game.getBattleOutcomeProbTable();
		this.currentPlayer = currentPlayer;
		this.ownMission = ownMission;
	}
	
	public boolean doesAttackPlanNeedRevising() {
		return reviseAttackPlan;
	}
	
	public String getOutput() {
		return output;
	}
	
	/**
	 * @param attack_plan
	 * @param revisedAttackPlan Flag stating whether or not we are following the same attack plan. If the attack plan has been revised, then this flag is set to true.
	 */
	public void run(T_RP_AttackPlanListElement attack_plan, O_IG_WinningEstimate winningEstimate, boolean revisedAttackPlan) {
		// Only the first country in the attack plan must be "self-occupied". If the second country is also "self-occupied", then the first element is removed. This is repeated.
		while ((attack_plan.getAttackPlan().size() > 1) && 
			(attack_plan.getAttackPlan().getCountry(1).getOwner().equals(currentPlayer))) {
			attack_plan.getAttackPlan().remove(0);
		}
		// There should always be two countries in the attack_plan (one to attack from and one to attack).
		if (attack_plan.getAttackPlan().size() > 1) {
			/*if (!revisedAttackPlan) {
				attackPlanCost += game.getLastBattle_ArmiesLost(); 
				//attack_plan.addCostToCostSpent()
				System.out.println("AP-cost so far: "+attackPlanCost);
				System.out.println("Estimated cost: "+attack_plan.getEstimatedCost());
			} else {
				attackPlanCost = 0;
			}*/
			// Has the attack plan been too expensive so far? - if so, revise it!
			//if (!isAttackPlanTooExpensive(attack_plan,game.getAISettings())) {
				reviseAttackPlan = false;
				int currentPlayerIndex = game.getPlayerIndex(currentPlayer);
				// continue with the plan... 
				// OutcomeOfBattle notes:
				// if probOfWinning > 35% then goahead 
				// if closeToWinning and prob > 5% then goahead (Kamikaze)
				// if lastTerritoryInContinent and prob > 25% then goahead
				Country attacker = attack_plan.getAttackPlan().getCountry(0);
				Country defender = attack_plan.getAttackPlan().getCountry(1);

				int leaveBehind = attack_plan.getAttackPlan().getArmiesToLeave(0);

				int armiesAvailable = attacker.getArmies()-leaveBehind;
				if (armiesAvailable > 0) {
					float probOfWinning = battleTable.getBattleProbability(armiesAvailable,defender.getArmies());
					float probOfWinningKamikaze;
					if (attacker.getArmies()-1 > 0) {
						probOfWinningKamikaze = battleTable.getBattleProbability(attacker.getArmies()-1,defender.getArmies());
					} else {
						probOfWinningKamikaze = 0.0f;
					}
					if (((winningEstimate.getEstimate(currentPlayerIndex,0) > game.getAISettings().attackProbabilityOfWinning_threshold) && (probOfWinningKamikaze > game.getAISettings().attackProbabilityOfWinning_closeToWinning)) ||
					(((lastTerritoryInContinent(defender,currentPlayer)) && (probOfWinning > game.getAISettings().attackProbabilityOfWinning_lastTerritory)) || (probOfWinning > game.getAISettings().attackProbabilityOfWinning))) {
						output = "attack "+attacker.getColor()+" "+defender.getColor();
						attackingCountry = attacker;
						attackingCountry_armiesToLeave = attack_plan.getAttackPlan().getArmiesToLeave(0);
					} else {
						reviseAttackPlan = true;
					}
				} else {
					reviseAttackPlan = true;
				}
			//} else {
			//	reviseAttackPlan = true;
			//}
		} else {
			// When the attack plan has been followed, make new plans!
			reviseAttackPlan = true;
		}
	}
	
	/*private boolean isAttackPlanTooExpensive(T_RP_AttackPlanListElement attack_plan, T_AISettings aiSettings) {
		return (attack_plan.getEstimatedCost() < (attackPlanCost/(1+aiSettings.attackEstimatedCostExceed)));
	}
	*/
	private boolean lastTerritoryInContinent(Country c, Player p) {
		boolean lastCountry = true;
		Continent cont = c.getContinent();
		int i = 0;
		while ((i < cont.getTerritoriesContained().size()) && (lastCountry)) {
			Country n = (Country)cont.getTerritoriesContained().get(i);
			if ((!n.getOwner().equals(p)) && (!n.equals(c))) {
				lastCountry = false;
			}
			i++;
		}
		return lastCountry;
	}
        
	public Country getAttackingCountry() {
		return this.attackingCountry;
	}

	public int getAttackingCountry_ArmiesToLeave() {
		return this.attackingCountry_armiesToLeave;
	}
}