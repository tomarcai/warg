package risk.AI.Data_Structures;

import net.yura.domination.engine.core.Continent;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;

import java.util.*;

public class T_Board {
	
	private RiskGame game;
	private Vector borderTerritories = new Vector();
//	private T_AISettings aiSettings; // Remove - depricated!
	private Vector bordersInContinents = new Vector();
	private T_BattleOutcomeProbTable battleOutcomeProbTable = new T_BattleOutcomeProbTable();
	
	public T_Board(RiskGame game) {
		this.game = game;
//		aiSettings  = new T_AISettings("AISettings.txt");// Remove - depricated!
		for(int i =0; i < game.getContinents().length; i++){
			bordersInContinents.add(new Vector());
		}
		findBorderTerritories();
	}
	
	public List getContinents() {
		return Arrays.asList(game.getContinents());
	}
	
	public List getCountries() {
		return Arrays.asList(game.getCountries());
	}

	// YURA maybe we dont need this??
	//public Country getCountry(String name) {
	//	return game.getCountryByName(name);
	//}

	public Country getCountryByInt(int index) {
		return game.getCountries()[index];
	}
	
	public int getNumOfContinentsOwned(Player p) {
		return game.getNoContinentsOwned(p);
	}
	
	public int getCountryCount() {
		return game.getNoCountries();
	}

	private void findBorderTerritories() {
		Country[] territories = game.getCountries();
		Country territory;
		Vector neighbors;
		Country neighbor;
		Continent continent;
		for(int i = 0; i < territories.length; i ++) {
			territory = territories[i];
			neighbors = territory.getNeighbours();
			// Check if the territory has neighbors on other continents
			for(int j = 0; j < neighbors.size(); j++) {
				neighbor = (Country)neighbors.get(j);
				if (neighbor.getContinent() != territory.getContinent()) {
					borderTerritories.add(territory);
					int continentNumber = getContinents().indexOf(territory.getContinent());
					((Vector)bordersInContinents.get(continentNumber)).add(territory);
					break;
				}
			}
		}
	}
	
	public boolean isBorderTerritory(Country territory) {
		return borderTerritories.contains(territory);
	}
	
	public Vector getBorderTerritories(Continent continent){
		int continentNumber = getContinents().indexOf(continent);
		return ((Vector)bordersInContinents.get(continentNumber));
	}
	
	public Vector getBorderTerritories(int continentIndex){
		return ((Vector)bordersInContinents.get(continentIndex));
	}
	
	// Remove all below! - depricated!
/*	
	public int getMissionCount() {
		return game.getAllMissionsVector().size();
	}
	
	public Vector getMissions() {
		return game.getAllMissionsVector();
	}
	
	public int getPlayerCount() {
		return game.getNoPlayers();
	}

	public Vector getPlayers() {
		return game.getPlayers();
	}
	
	public boolean canTrade() {
		return game.canTrade();
	}
	
	public int getCardCombinationScore(Card card1, Card card2, Card card3) {
		return game.getNewCardState(card1,card2,card3);
	}

	public int getLastBattle_ArmiesLost() {
		return game.getLastBattle_ArmiesLost();
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
	
	public Vector<Integer> getNumberOfReinforcements(Player player, int numberOfRounds) {
		Vector<Integer> result = new Vector();
		int numberOfRiskCards = player.getCards().size();
		int reinforcementsFromCards = 0;
		for(int rn = 1; rn <= numberOfRounds + 1; rn++){ // "numberOfRounds + 1" because the number of reinforcements this round should also be accounted for when called with numberOfRounds = 0
			if (numberOfRiskCards >= 3){
				reinforcementsFromCards = 10;
				numberOfRiskCards = 1;
			}
			result.add(rn * this.getNumberOfReinforcements(player) + reinforcementsFromCards);
			numberOfRiskCards +=1;
		}
		return result;
	}
	
	private int calcContinentReinforcements(Player p) {
		// How many reinforcements does the player get from Continents?
		int armies = 0;
		for (int i = 0; i < this.getContinents().size(); i++) {
			Continent c = (Continent)this.getContinents().get(i);
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
	}*/
}