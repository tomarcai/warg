/*
 * T_Game.java
 *
 * Created on 20. februar 2006, 19:31
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package risk.AI.Data_Structures;

import net.yura.domination.engine.ColorUtil;
import net.yura.domination.engine.core.Card;
import net.yura.domination.engine.core.Continent;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Mission;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import java.util.List;
import java.util.Vector;

/**
 * @author Pelle
 */
public class T_Game {

	public static final String[] MODULE_NAMES = { // size: 17
			"ig_mission", 			// 0
			"ig_winning",			// 1
			"ig_nextmove",			// 2
			"ig_continent",			// 3
			"master_prioritizer",		// 4
			"rp_makeattackplan",		// 5
			"rp_ap_cost",			// 6
			"rp_ap_priority",		// 7
			"rp_discardattackplan",		// 8
			"rp_de_cost",			// 9
			"rp_de_priority",		// 10
			"rp_cashcards",			// 11
			"rp_placearmies",		// 12
			"rp_scoreattackplan",		// 13
			"rp_fortify",			// 14
			"initialplacement",		// 15
			"rp_scoremergedplan"		// 16
	};

	public static final String[][] AI_TECHNIQUES = { // size: 17
			{"script", "nn"},			// 0
			{"script", "nn", "nb", "nn_wo", "bn"},	// 1
			{"script", "nn_wo"},			// 2
			{"script", "nn", "nb", "nn_wo", "bn"},	// 3
			{"script", "nn", "nn_wo"},		// 4
			{"script"},				// 5
			{"script"},				// 6
			{"script", "nn", "nn_wo"},		// 7
			{"script"},				// 8
			{"script", "nn", "dt", "nb", "nn_wo"},	// 9
			{"script", "nn", "dt", "nb", "nn_wo"},	// 10
			{"script"},				// 11
			{"script"},				// 12
			{"script", "nn", "dt", "nb", "nn_wo"},	// 13
			{"script"},				// 14
			{"script", "nn", "nn_wo"},		// 15
			{"script", "nn", "dt", "nb", "nn_wo"}};	// 16

	// TODO YURA why is this here? can it be removed and loaded from the map?
	public static final String[] CONTINENT_NAMES = {"North-America", "South-America", "Europe", "Africa", "Asia", "Australia"};
	public static final String[] MISSION_NAMES = {
			"Asia_SouthAmerica",
			"Europe_Australia_3rd",
			"Asia_Africa",
			"Europe_SouthAmerica_3rd",
			"NorthAmerica_Australia",
			"NorthAmerica_Africa",
			"18_Territories",
			"24_Territories",
			"Kill_Player1",
			"Kill_Player2",
			"Kill_Player3",
			"Kill_Player4",
			"Kill_Player5",
			"Kill_Player6"};

	public static final int NUMBER_OF_PLAYERS_MAX = RiskGame.MAX_PLAYERS;
	public static final int NUMBER_OF_TERRITORIES = 42, 
		NUMBER_OF_CONTINENTS = 6, 
		NUMBER_OF_ROUNDS_PREDICTED = 5, 
		NUMBER_OF_MISSIONS = 14,
		NUMBER_OF_GOALS = 38;
	
	private RiskGame game;

 	private T_AISettings aiSettings;
	private T_BattleOutcomeProbTable battleOutcomeProbTable = new T_BattleOutcomeProbTable();
	private T_Board board;
	public static final double[] reinforcementsFromCards = {0.0, 0.0, 0.0, 3.73132, 7.1289, 8.91622, 9.4314, 9.70446, 9.84778, 9.91876, 9.95572};

	public T_Game(RiskGame game, T_Board board, T_AISettings aiSettings) {
		this.game = game;
		this.board = board;
		this.aiSettings = aiSettings;
	}

	public int getMissionCount() {
		return game.getNoMissions();
	}

	public List<Mission> getMissions() {
		return game.getMissions();
	}

	public int getPlayerCount() {
		return game.getNoPlayers();
	}

	public List<Player> getPlayers() {
		return game.getPlayers();
	}

	public boolean canTrade() {
		return game.canTrade();
	}

	public int getCardCombinationScore(Card card1, Card card2, Card card3) {
		// YURA old code used to call: game.getNewCardState(card1,card2,card3);
		return game.getTradeAbsValue(card1.getName(), card2.getName(), card3.getName(), game.getCardMode());
	}

	public Card[] getBestCardCombination(List cards) {
		if (this.canTrade()) {
			Card[] bestCombination = new Card[3];
			int bestScore = 1;

			for (int i = 0; i < cards.size(); i++) {
				for (int j = i+1; j < cards.size(); j++) {
					for (int k = j+1; k < cards.size(); k++) {
						int score = getCardCombinationScore((Card)cards.get(i),(Card)cards.get(j),(Card)cards.get(k));
						// If there is two combinations of equal score, then the combination without wildcards is the better.
						if ((score > bestScore) || ((score == bestScore) && (doCombinationContainWildcard(bestCombination)))) {
							bestScore = score;
							bestCombination[0] = (Card)cards.get(i);
							bestCombination[1] = (Card)cards.get(j);
							bestCombination[2] = (Card)cards.get(k);
						}
					}
				}
			}		
			if (bestScore == 1) {
				return null;
			}
			return bestCombination;
		} else {
			return null;
		}
	}

	private boolean doCombinationContainWildcard(Card[] combination) {
		if (combination[0].equals("wildcard") || 
			combination[1].equals("wildcard") ||
			combination[2].equals("wildcard")) {
			return true;
		} else {
			return false;
		}
	}

	Country attacker;
	int attackerArmies = -1;

	public int getLastBattle_ArmiesLost() {

		if (attacker != game.getAttacker()) {
			attacker = game.getAttacker();
			attackerArmies = -1;
		}

		int oldAttackerArmies = this.attackerArmies;
		this.attackerArmies = game.getAttacker().getArmies();
		int armiesLost = oldAttackerArmies == -1 ? 0 : oldAttackerArmies - attackerArmies;

		//int checkValue = game.getLastBattle_ArmiesLost();
		//if (armiesLost != checkValue) throw new IllegalStateException("value missmatch " + armiesLost + " != " + checkValue);

		return armiesLost;
	}

	public void resetLastBattle_ArmiesLost() {
		attacker = null;
		attackerArmies = -1;
		//game.resetLastBattle_ArmiesLost();
	}

	public int getPlayerIndex(Player player) {
		return this.getPlayers().indexOf(player);
	}

	public T_AISettings getAISettings() {
		return aiSettings;
	}

	public int getNumberOfReinforcements(Player p){
		int armies_received = Math.max((int)(p.getNoTerritoriesOwned()/3),3) + calcContinentReinforcements(p);
		return armies_received;
	}

	public float calcReinforcementsFromCards(int numberOfCards) {
		if (numberOfCards <= 10) {
			return (float)reinforcementsFromCards[numberOfCards];
		} else {
			return 10;
		}
	}

	public Vector<Integer> calcFutureReinforcements(Player player, int numberOfRounds) {
		Vector<Integer> result = new Vector();
		int numberOfRiskCards = player.getCards().size();
		int reinforcementsFromCards = 0;
		for(int rn = 1; rn <= numberOfRounds + 1; rn++){ // "numberOfRounds + 1" because the number of reinforcements this round should also be accounted for when called with numberOfRounds = 0
			result.add(rn * this.getNumberOfReinforcements(player) + Math.round(calcReinforcementsFromCards(numberOfRiskCards)));
			numberOfRiskCards +=1;
		}
		return result;
	}

	private int calcContinentReinforcements(Player p) {
		// How many reinforcements does the player get from Continents?
		int armies = 0;
		for (int i = 0; i < board.getContinents().size(); i++) {
			Continent c = (Continent)board.getContinents().get(i);
			if (c.isOwned(p)) {
				armies += c.getArmyValue();
			}
		}
		return armies;
	}

	public double calcEstimatedArmyCost(int defendingArmies){
		return 0.8534144 * defendingArmies - 0.2213413 * (1 - Math.pow(-0.525359, (double)defendingArmies));
		
	}

	public T_BattleOutcomeProbTable getBattleOutcomeProbTable() {
		return battleOutcomeProbTable;
	}

	public boolean isPlayerDead(Player player){
		if(player.getNoTerritoriesOwned() != 0){
			return false;
		} else {
			return true;
		}
	}

	public static String decentColorToString(int c) {
		switch (c) {
			case ColorUtil.BLACK: return "black";
			case ColorUtil.BLUE: return "blue";
			case ColorUtil.CYAN: return "cyan";
			case ColorUtil.DARK_GRAY: return "darkgray";
			case ColorUtil.GRAY: return "gray";
			case ColorUtil.GREEN: return "green";
			case ColorUtil.LIGHT_GRAY: return "lightgray";
			case ColorUtil.MAGENTA: return "magenta";
			case ColorUtil.ORANGE: return "orange";
			case ColorUtil.PINK: return "pink";
			case ColorUtil.RED: return "red";
			case ColorUtil.WHITE: return "white";
			case ColorUtil.YELLOW: return "yellow";
			default: return null;
		}
	}

	/**
	 * Returns a decent String-version of a mission. If player is null, it is simply a converter.
	 * If player is not null, then the players mission is tranformed to the 24-mission when necessary.
	 */
	public static String decentMissionToString(Mission m, Player p) {
		if (m.getContinent1() != null) {
			if (m.getContinent3() == null) {
				return m.getContinent1().getName() + "," + m.getContinent2().getName();
			} else {
				return m.getContinent1().getName() + "," + m.getContinent2().getName() + ",random";
			}
		} else if (m.getPlayer() != null) {
			if ((p != null) && (m.getPlayer().equals(p))) {
				return "24";
			} else {
				return "kill "+T_Game.decentColorToString(m.getPlayer().getColor());
			}
		} else if (m.getNoofcountries() == 18) {
			return "18";
		} else {
			return "24";
		}
	}	

	public boolean riskCardIsReceivedThisRound(Player p) {
		return !(game.getDesrvedCard().equals(""));
	}
}
