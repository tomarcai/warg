package risk.AI.Data_Structures.Module_Output;

import net.yura.domination.engine.core.Country;

import java.util.*;

public class O_IG_NextMoveEstimate {
	
	private Vector<Country> territoryList = new Vector();
	
	/**
	 *
	 * @param territoriesToBeAttacked List of territories that will be attacked by opponents (those who are in their attack plans)
	 */
	public O_IG_NextMoveEstimate(Vector<Country> territoriesToBeAttacked) {
		this.territoryList = territoriesToBeAttacked;
	}
	
	public Vector getTerritoryInDangerList(){
		return territoryList;
	}
}