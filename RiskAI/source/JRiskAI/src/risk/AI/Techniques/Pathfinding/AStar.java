package risk.AI.Techniques.Pathfinding;

import java.util.*;

import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Player;
import risk.AI.Data_Structures.T_Board;

public class AStar {

	private T_Board board;

	private Vector<AStarElement> infomap;
	private int numberOfCountries;
	private HeuristicTable hTable;
	private boolean hTableInit = false;
	
	private final boolean debugging = false;

	public AStar(T_Board board) {
		this.board = board;
	}
	
	public void initHTable() {
		this.numberOfCountries = board.getCountries().size();
		infomap = new Vector();
		for (int i = 0; i < numberOfCountries; i++) {
			infomap.add(new AStarElement());
		}
		hTable = new HeuristicTable(board);
		this.hTableInit = true;
	}

	// The resulting Vector will hold endCountry at the last element, but not indlude startCountry!
	
	public Vector getShortestPath(Country startCountry, Country endCountry, Player p) {
		// First line necessary because JRISK suck (the board is not setup at player-creation-time).
		if (!hTableInit) {
			initHTable();
		}
		debug("a*-start");
		if ((startCountry.getOwner() != p) || (endCountry.getOwner() == p))	{
			return null;
		}
		if (startCountry == endCountry) {
			Vector c = new Vector();
			c.add(endCountry);
			return c;
		}
		// Init infomap.
		for (int i = 0; i < numberOfCountries; i++) {
			((AStarElement)infomap.get(i)).resetValues();
		}
		// Init open- and closedList.
		Vector<Country> openList = new Vector();
		Vector<Country> closedList = new Vector();
		// Init the resulting path.
		Vector<Country> result = new Vector();
		
		// Setup openlist and infomap.
		openList.add(startCountry);
		AStarElement s = getInfomapElement(startCountry);
		s.setHeuristic(hTable.getHeuristic(startCountry,endCountry));
		s.setStepCost(0);
		s.updateScore();
		
		Country q_country = startCountry;
		
		boolean foundTarget = false;
		debug("a*-begin-while");
		// And we're off...
		while ((!openList.isEmpty()) && (!foundTarget)) {
			// We start with the tile with the lowest score.
			int leastScore = 0;
			for (int i = 1; i < openList.size(); i++) {
				if (getInfomapElement(openList.get(i)).getScore() < getInfomapElement(((Country)openList.get(leastScore))).getScore()) {
					leastScore = i;
				}
			}
			q_country = openList.get(leastScore);
			// No need to go any further if the tile with the lowest score is our target.
			if (q_country == endCountry) { 
				foundTarget = true;
			}
			
		    // Erase the selected tile from openlist.
			openList.remove(leastScore);

			// q_country is always the country with the lowest (best) score, so put that in the closedlist (unless it is the starting position).
	    	if (!(q_country == startCountry)) { 
	    		debug("a*-added-to-closedList: "+q_country.getIdString());
	    		closedList.add(q_country); 
	    	}


		    // Now we update the score for all the tiles surrounding the selected tile.
		    for (int i = 0; i < q_country.getNeighbours().size() && !foundTarget; i++) {
		    	// The neighboring country:
		    	Country n_country = (Country)q_country.getNeighbours().get(i);
		    	
		    	// Checking if the surrounding tile is in the closedList.
		    	boolean inClosedList = false;
		    	if (closedList.contains(n_country)) {
		    		inClosedList = true;
		    	}

		    	// Checking if the surrounding tile is in the openList.
		    	boolean inOpenList = false;
		    	if (openList.contains(n_country)) {
		    		inOpenList = true;
		    	}
		    	
		    	// Check whether or not the surrounding country is not my own.
		    	if (!(n_country.getOwner() == p)) {
		    		// it is not...
	    			AStarElement a = getInfomapElement(n_country);
		    		if ((!inOpenList) && (!inClosedList)) {
		    			// if the tile is not in either closed- nor openlist, put it in the openlist and update info on that tile (it is the first time we've seen it).
		    			openList.add(n_country);
		    			a.setHeuristic(hTable.getHeuristic(startCountry,n_country));
		    			a.setParent(q_country);
		    			a.setStepCost(getInfomapElement(q_country).getStepCost()+n_country.getArmies());
		    			a.updateScore();
		    		} else {
		    			// The country has been recorded before, but a possibly better path has been found to it - so update infomap!	
		    			int stepCost = getInfomapElement(q_country).getStepCost()+n_country.getArmies();
		    			if (stepCost < a.getStepCost()) {
		    				a.setHeuristic(hTable.getHeuristic(startCountry,n_country));
		    				a.setParent(q_country);
		    				a.setStepCost(stepCost);
		    				a.updateScore();
		    			}
		    		}
		    	}
		    }
  		}
		debug("a*-while-done");
		// End of while and main algorithm.

		// If the openlist becomes empty and we are not at our target location, then there is no path to the target.
		if ((openList.isEmpty()) && !foundTarget) { 
			return null;
		} else {
			// Follow the parents from target back to the start - and copy countries to the result-path.
			AStarElement a = getInfomapElement(endCountry);
			result.add(endCountry);
			while (!(a.getParent() == startCountry)) {
				//debug("a*-adding-to-result:"+a.getParent().getName());
				result.add(a.getParent());
				a = getInfomapElement(a.getParent());
			}
			result = invertVector(result);
		}
		debug("a*-done");
		
		return result;
	}
	
	private AStarElement getInfomapElement(Country c) {
		return (AStarElement)(infomap.get(board.getCountries().indexOf(c)));
	}
	
	private Vector invertVector(Vector v) {
		Vector r = new Vector();
		for (int i = v.size()-1; i > -1; i--) {
			r.add(v.get(i));
		}
		return r;
	}
	
	private void debug(String s) {
		if (debugging) {
			System.out.println(s);
		}
	}

	// The resulting Vector will hold endCountry at the last element, but not indlude startCountry!
	
	public Vector<Country> getShortestOccupiedPath(Country startCountry, Country endCountry, Player p) {
		// First line necessary because JRISK suck (the board is not setup at player-creation-time).
		if (!hTableInit) {
			initHTable();
		}
		debug("a*-start");
		if ((startCountry.getOwner() != p) || (endCountry.getOwner() != p))	{
			return null;
		}
		if (startCountry == endCountry) {
			Vector c = new Vector();
			c.add(endCountry);
			return c;
		}
		// Init infomap.
		for (int i = 0; i < numberOfCountries; i++) {
			((AStarElement)infomap.get(i)).resetValues();
		}
		// Init open- and closedList.
		Vector<Country> openList = new Vector();
		Vector<Country> closedList = new Vector();
		// Init the resulting path.
		Vector<Country> result = new Vector();
		
		// Setup openlist and infomap.
		openList.add(startCountry);
		AStarElement s = getInfomapElement(startCountry);
		s.setHeuristic(hTable.getHeuristic(startCountry,endCountry));
		s.setStepCost(0);
		s.updateScore();
		
		Country q_country = startCountry;
		
		boolean foundTarget = false;
		debug("a*-begin-while");
		// And we're off...
		while ((!openList.isEmpty()) && (!foundTarget)) {
			// We start with the tile with the lowest score.
			int leastScore = 0;
			for (int i = 1; i < openList.size(); i++) {
				if (getInfomapElement(openList.get(i)).getScore() < getInfomapElement(((Country)openList.get(leastScore))).getScore()) {
					leastScore = i;
				}
			}
			q_country = openList.get(leastScore);
			// No need to go any further if the tile with the lowest score is our target.
			if (q_country == endCountry) { 
				foundTarget = true;
			}
			
		    // Erase the selected tile from openlist.
			openList.remove(leastScore);

			// q_country is always the country with the lowest (best) score, so put that in the closedlist (unless it is the starting position).
	    	if (!(q_country == startCountry)) { 
	    		debug("a*-added-to-closedList: "+q_country.getIdString());
	    		closedList.add(q_country); 
	    	}


		    // Now we update the score for all the tiles surrounding the selected tile.
		    for (int i = 0; i < q_country.getNeighbours().size() && !foundTarget; i++) {
		    	// The neighboring country:
		    	Country n_country = (Country)q_country.getNeighbours().get(i);
		    	
		    	// Checking if the surrounding tile is in the closedList.
		    	boolean inClosedList = false;
		    	if (closedList.contains(n_country)) {
		    		inClosedList = true;
		    	}

		    	// Checking if the surrounding tile is in the openList.
		    	boolean inOpenList = false;
		    	if (openList.contains(n_country)) {
		    		inOpenList = true;
		    	}
		    	
		    	// Check whether or not the surrounding country is !not my own.
		    	if ((n_country.getOwner() == p)) {
		    		// it is !not...
	    			AStarElement a = getInfomapElement(n_country);
		    		if ((!inOpenList) && (!inClosedList)) {
		    			// if the tile is not in either closed- nor openlist, put it in the openlist and update info on that tile (it is the first time we've seen it).
		    			openList.add(n_country);
		    			a.setHeuristic(hTable.getHeuristic(startCountry,n_country));
		    			a.setParent(q_country);
		    			a.setStepCost(getInfomapElement(q_country).getStepCost()+n_country.getArmies());
		    			a.updateScore();
		    		} else {
		    			// The country has been recorded before, but a possibly better path has been found to it - so update infomap!	
		    			int stepCost = getInfomapElement(q_country).getStepCost()+n_country.getArmies();
		    			if (stepCost < a.getStepCost()) {
		    				a.setHeuristic(hTable.getHeuristic(startCountry,n_country));
		    				a.setParent(q_country);
		    				a.setStepCost(stepCost);
		    				a.updateScore();
		    			}
		    		}
		    	}
		    }
  		}
		debug("a*-while-done");
		// End of while and main algorithm.

		// If the openlist becomes empty and we are not at our target location, then there is no path to the target.
		if ((openList.isEmpty()) && !foundTarget) { 
			return null;
		} else {
			// Follow the parents from target back to the start - and copy countries to the result-path.
			AStarElement a = getInfomapElement(endCountry);
			result.add(endCountry);
			while (!(a.getParent() == startCountry)) {
				//debug("a*-adding-to-result:"+a.getParent().getName());
				result.add(a.getParent());
				a = getInfomapElement(a.getParent());
			}
			result = invertVector(result);
		}
		debug("a*-done");
		
		return result;
	}
}