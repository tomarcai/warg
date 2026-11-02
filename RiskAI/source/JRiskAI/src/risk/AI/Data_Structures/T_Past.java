package risk.AI.Data_Structures;

import net.yura.domination.engine.core.Card;
import net.yura.domination.engine.core.Continent;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Player;

import java.util.*;
import java.awt.*;

public class T_Past {
	
	private Vector boardPast = new Vector();
	private Vector actionPast = new Vector();

	private Vector players;
	private Vector continents;
	
	private int[][] interestInContinent; 
	private int[][] attacksOnPlayer;
	private int[] totalInterestInContinent; 
	private int[] totalAttacksOnPlayer;
	
	public T_Past(Vector players, Vector continents) {
		this.players = players;
		this.continents = continents;
		this.interestInContinent = new int[players.size()][continents.size()];
		this.attacksOnPlayer = new int[players.size()][players.size()];
		this.totalInterestInContinent = new int[players.size()];
		this.totalAttacksOnPlayer = new int[players.size()];
	}
	
	public void addAction_PlaceArmy(Player p, Country c, int armies) {
		actionPast.add(T_Game.decentColorToString(p.getColor()) + " PLACE_ARMY " + c.getIdString() + " "+armies);
	}
	
	public void addAction_Attack(Player p, Country source_country, Country target_country, int armies) {
		actionPast.add(T_Game.decentColorToString(p.getColor()) + " ATTACK " + source_country.getIdString() + " " + target_country.getIdString()+" "+armies);
		int indexOfPlayer = players.indexOf(p);
		interestInContinent[indexOfPlayer][continents.indexOf(target_country.getContinent())]++;
		totalInterestInContinent[indexOfPlayer]++;
		attacksOnPlayer[indexOfPlayer][players.indexOf(target_country.getOwner())]++;
		totalAttacksOnPlayer[indexOfPlayer]++;
	}

	public void addAction_Move(Player p, int armies) {
		actionPast.add(T_Game.decentColorToString(p.getColor()) + " MOVE " + armies);
	}

	public void addAction_Fortify(Player p, Country source_country, Country target_country, int armies) {
		actionPast.add(T_Game.decentColorToString(p.getColor()) + " FORTIFY "+source_country.getIdString()+" "+target_country.getIdString()+" "+armies);
	}

	public void addAction_CashCards(Player p, Card card1, Card card2, Card card3) {
		actionPast.add(T_Game.decentColorToString(p.getColor()) + " CASH_CARDS "+card1.getName()+" "+card2.getName()+" "+card3.getName());
	}
	
	public void addBoardState(Vector countries) {
		String str = "";
		for (int i = 0; i < countries.size(); i++) {
			str = str + ((Country)countries.get(i)).getArmies() + "," + players.indexOf(((Country)countries.get(i)).getOwner()) + ";";
		}
		boardPast.add(str);
		for (int i = 0; i < this.players.size(); i++) {
			for (int j = 0; j < this.continents.size(); j++) {
				if (((Continent)continents.get(j)).isOwned((Player)players.get(i))) {
					interestInContinent[i][j]++;
					totalInterestInContinent[i]++;
				}
			}
		}
	}
	
	/**
	 * Is called "Interest in" since it is more than just a player's attacks in a continent. If a player conquers a continent, that is also counted.
	 */
	public int getPlayersInterestInContinent(Player player, Continent continent) {
		return this.interestInContinent[players.indexOf(player)][continents.indexOf(continent)];
	}
	
	public int getPlayersInterestInContinent(int playerIndex, int continentIndex) {
		return this.interestInContinent[playerIndex][continentIndex];
	}
	
	public int getPlayersInterestInContinent(int playerIndex, Continent continent) {
		return this.interestInContinent[playerIndex][continents.indexOf(continent)];
	}

	public int getTotalInterestPoints(Player player) {
		return this.totalInterestInContinent[players.indexOf(player)];
	}
	
	public int getTotalInterestPoints(int playerIndex) {
		return this.totalInterestInContinent[playerIndex];
	}
	
	public int getNumOfAttacksOnPlayer(Player player, Player opponent) {
		return this.attacksOnPlayer[players.indexOf(player)][players.indexOf(opponent)];
	}

	public int getNumOfAttacksOnPlayer(int playerIndex, int opponentIndex) {
		return this.attacksOnPlayer[playerIndex][opponentIndex];
	}

	public int getNumOfAttacksOnPlayer(int playerIndex, Player opponent) {
		return this.attacksOnPlayer[playerIndex][players.indexOf(opponent)];
	}

	public int getTotalAttacksByPlayer(Player player) {
		return this.totalAttacksOnPlayer[players.indexOf(player)];
	}

	public int getTotalAttacksByPlayer(int playerIndex) {
		return this.totalAttacksOnPlayer[playerIndex];
	}
	
	public Vector getBoardPast() {
		return this.boardPast;
	}

	public Vector getActionPast() {
		return this.actionPast;
	}
}