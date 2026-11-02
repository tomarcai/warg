package risk.AI.Techniques.Pathfinding;

import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Player;

import java.util.*;

public class HamPath {
	
	public Vector getPath(Vector theTerritories, Country startTerritory, Country targetTerritory, Player currentPlayer) {
		Vector territories = (Vector)theTerritories.clone();
		// Remove occupied territories from the list since they cannot be in the path
		int i = 0;
		Country territory;
		while (i < territories.size()) {
			territory = (Country)territories.get(i);
			// Occupied territories should not be a part of the list
			if (!(territory.equals(startTerritory)) && (territory.getOwner() == currentPlayer)) {
				territories.remove(i);
			} else {
				i++;
			}
		}
		Country[] countryList = new Country[territories.size()];
		for(i = 0; i < countryList.length; i++) {
			countryList[i] = (Country)territories.get(i);
		}		
		int startIndex = territories.indexOf(startTerritory);
		int endIndex = territories.indexOf(targetTerritory);
		countryList = getPath(countryList, startIndex, endIndex);
		Vector result = new Vector();
		if (countryList == null) {
			result = null;
		} else {
			for(i = 0; i < countryList.length; i++) {
				result.add(countryList[i]);
			}
		}
		return result;
	}
	
	public Country[] getPath(Country[] countryList, int startIndex, int endIndex) {
		// Building the subgraph from countryList.
		HamPathNode[] graph = new HamPathNode[countryList.length];
		for (int i = 0; i < graph.length; i++) {
			graph[i] = new HamPathNode(countryList[i]);
		}
		for (int i = 0; i < graph.length; i++) {
			for (int j = 0; j < countryList[i].getNeighbours().size(); j++) {
				int indexOfNeighbor = getIndexOfNeighbor((Country)countryList[i].getNeighbours().get(j),countryList);
				if (indexOfNeighbor != -1) {
					graph[i].addNeighbor(graph[indexOfNeighbor]);
				}
			}
		}
		
		// Find a hamiltonian path in the subgraph.
		if (startIndex < 0) {
			int i = 0;
		}
		if (endIndex < 0) {
			int i = 0;	
		}
		Vector path = findPath(graph[startIndex], graph[endIndex], graph);
		
		// Checking a path from start to end was found
		if ((path == null) || (path.size() == 0) || (!path.get(path.size()-1).equals(graph[startIndex])) || (!path.get(0).equals(graph[endIndex]))) {
			return null;
		} else {
			// Reversing the path.
			Country[] result = new Country[path.size()];
			for (int i = 0; i < path.size(); i++) {
				result[i] = ((HamPathNode)path.get(path.size()-i-1)).getCountry();
			}
			return result;
		}
	}
	
	private Vector findPath(HamPathNode s, HamPathNode t, HamPathNode[] g) {
		Vector r = null;
		if (!s.getVisited()) {
			s.setVisited(true);
			if (s == t) {
				if (allVisited(g)) {
					r = new Vector();
					r.add(t);
				} else {
					t.setVisited(false);
				}
			} else {
				int i = 0;
				while ((i < s.getNeighborCount()) && (r == null)) {
					r = findPath(s.getNeighbor(i),t,g);
					i++;
				} 
				if (r == null) {
					s.setVisited(false);
				} else {
					r.add(s);
				}
			}
		}
		return r;
	}
	
	private boolean allVisited(HamPathNode[] g) {
		int i = 0;
		while (i < g.length)  {
			if (!g[i].getVisited()) { 
				return false; 
			} else {
				i++;
			}
		}
		return true;
	}
	
	private int getIndexOfNeighbor(Country c, Country[] countryList) {
		boolean found = false;
		int i = 0;
		while ((!found) && (i < countryList.length)) {
			if (c.equals(countryList[i])) { 
				found = true; 
			} else {
				i++;
			}
		}
		if (!found) {
			return -1;
		} else {
			return i;
		}
		
	}
}