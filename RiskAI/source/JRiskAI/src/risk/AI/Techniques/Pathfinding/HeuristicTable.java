package risk.AI.Techniques.Pathfinding;

import net.yura.domination.engine.core.Country;
import risk.AI.Data_Structures.T_Board;

public class HeuristicTable {
	
	//private RiskGame game;
	private T_Board board;
	private int[][] table;
	
	public HeuristicTable(T_Board board) {
		this.board = board;
		// Setting up the heuristic table
		table = new int[board.getCountries().size()][board.getCountries().size()];
		for (int i = 0; i < board.getCountries().size(); i++) {
			for (int j = 0; j < board.getCountries().size(); j++) {
				if (i==j) {
					table[i][j] = 0;
				} else {
					table[i][j] = 10000;
				}
			}
		}
		calculateTable();
		
		/* Debug
		 Country c = (Country)game.getCountries().get(0);
		for (int i = 0; i < game.getCountries().size(); i++) {
			Country n = (Country)game.getCountries().get(i);
			System.out.println(c.getName()+" -> "+n.getName()+" = "+table[0][i]);
		}*/
	}
	
	public int getHeuristic(Country fromCountry, Country toCountry) {
		return table[board.getCountries().indexOf(fromCountry)][board.getCountries().indexOf(toCountry)];
	}
	
	private void setHeuristic(Country fromCountry, Country toCountry, int value) {
		table[board.getCountries().indexOf(fromCountry)][board.getCountries().indexOf(toCountry)] = value	;
	}
	
	private void calculateTable() {
		for (int i = 0; i < board.getCountries().size(); i++) {
			Country c = (Country)board.getCountries().get(i);
			calculateTableForCountry(c,c);
		}
	}
	
	private void calculateTableForCountry(Country start, Country c) {
		for (int i = 0; i < c.getNeighbours().size(); i++) {
			Country n = (Country)c.getNeighbours().get(i);
			if (getHeuristic(start,c)+1 < getHeuristic(start,n)) {
				setHeuristic(start,n,getHeuristic(start,c)+1);
				calculateTableForCountry(start,n);
			}
		}
	}
}