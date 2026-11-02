package risk.AI.Data_Structures;

import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Player;

/**
 * Represents an influence map where each entry corresponds to a territory on the board.
 */
public class T_InfluenceMap {
	
	private T_Board board;
	private T_Game game;
	private float[] map;
	
	/**
	 * Creates a new instance of T_InfluenceMap
	 * @param board The board
	 * @param game The game
	 */
	public T_InfluenceMap(T_Board board, T_Game game) {
		this.board = board;
		this.game = game;
		map = new float[board.getCountryCount()];
	}
	
	/**
	 * Returns the value of an entry on the influence map.
	 * @param index The entry number
	 * @return The influence map value
	 */
	public int getValue(int index) {
		return Math.round(map[index]);
	}
	
	/**
	 * Returns the value of the entry corresponding to the territory.
	 * @param territory The territory
	 * @return The influence map value
	 */
	public int getValue(Country territory) {
		return Math.round(map[board.getCountries().indexOf(territory)]);
	}
	
	public int size() {
		return this.map.length;
	}
	
	/**
	 * Resets the influence map by setting all values to 0
	 */
	private void reset() {
		for(int i = 0; i < map.length; i++) {
			map[i] = 0;
		}
	}
	
	/**
	 * Creates an influence map where each entry in the map corresponds to a territory on the board.
	 * Each entry gives the estimated number of enemy armies that would be left
	 * if the territory was attacked by the strongest enemy army that can reach the territory.
	 */
	public void calcAttackerArmiesLeft() {
		reset();
		// Simulate enemy attacks on player's territories
		float[] greatestArmiesLeft = new float[map.length];
		for(int i = 0; i < greatestArmiesLeft.length; i++) {
			greatestArmiesLeft[i] = 0;
		}
		float armiesLeft;
		Country territory, neighbor;
		Player attacker;
		int mapIndex;
		
		for(int territoryIndex = 0; territoryIndex < board.getCountryCount(); territoryIndex++) {
			territory = (Country) board.getCountries().get(territoryIndex);
			attackNeighbors(territory, territory.getArmies(), territory.getOwner());
		}
	}
	
	private void attackNeighbors(Country territory, float attackingArmies, Player attacker) {
		//if (attacker != player) {
			float armiesLeft;
			Country neighbor;
			int mapIndex;
			// Attack each neighbor
			for(int neighborIndex = 0; neighborIndex < territory.getNeighbours().size(); neighborIndex++) {
				neighbor = (Country) territory.getNeighbours().get(neighborIndex);
				if (neighbor.getOwner() != attacker) {
					// Attack neighbor
					armiesLeft = territory.getArmies() - (float)game.calcEstimatedArmyCost(neighbor.getArmies()) - 1; // "- 1" because one army must be left behind
					mapIndex = board.getCountries().indexOf(neighbor);
					if (armiesLeft > map[mapIndex]) {
						map[mapIndex] = armiesLeft;
						// Attack this neighbor's neighbors
						attackNeighbors(neighbor, armiesLeft, attacker);
					} else {
						// Stop
					}
				}
			}
		//}
	}
}
