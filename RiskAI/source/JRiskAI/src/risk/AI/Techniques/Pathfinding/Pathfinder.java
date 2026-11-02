/*
 * Pathfinder.java
 *
 * Created on 16. februar 2006, 12:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package risk.AI.Techniques.Pathfinding;

import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Player;
import risk.AI.Data_Structures.T_Board;
import java.util.*;

public class Pathfinder {

	private HamPath hamPathFinder;
	private AStar aStarPathFinder;
	private T_Board board;
	
	/** Creates a new instance of Pathfinder */
	public Pathfinder(T_Board board) {
		hamPathFinder = new HamPath();
		aStarPathFinder = new AStar(board);
		this.board = board;
	}
	
	public HamPath getHamPathFinder() {
		return this.hamPathFinder;
	}
	
	public AStar getAStarPathFinder() {
		return this.aStarPathFinder;
	}
	
	/** Finds an attack plan between two territories. 
	 * If doHamPath is true, hamPath is used within the target-territory's continent and A* to one of that continent's border territories. 
	 * If doHamPath is false, ordinary A* is used.
	 */
	
	/*public Vector findShortestPath(Country startCountry, Country targetCountry, Player player, boolean doHamPath) {
		Vector path = null;
		// Run AStar
		if ((!doHamPath) || (doHamPath && (!(startCountry.getContinent().getName().equals(targetCountry.getContinent().getName()))))) {
			path = aStarPathFinder.getShortestPath(startCountry, targetCountry, player);
			if (path != null) {
				path.add(0, startCountry);
			} else {
				doHamPath = false; // No need to do hamPath if no path at all were found.
			}
		}
		Country s=startCountry;
		Country t=targetCountry;
		// Run HamPath
		if (doHamPath) {
			// If the start and target are in different continents do ShortestPath to a border-territory in the target-continent and then hamPath within the continent.
			if (!(startCountry.getContinent().equals(targetCountry.getContinent()))) {
				// Find the border territory in the targetCountry's continent
				Country territory = null;
				for(int i = 0; i < path.size(); i++) {
					territory = (Country)path.get(i);
					if ((board.isBorderTerritory(territory)) && (territory.getContinent().equals(targetCountry.getContinent()))) {
						startCountry = territory;
						// Remove territories from the path that are in the target territory's continent
						path.subList(i, path.size()).clear();
						break;
					}
				}
			} else {
				// If the start and target are in the SAME continent do hamPath within the continent.
				path = new Vector();
			}
			Vector hamPath = hamPathFinder.getPath(targetCountry.getContinent().getTerritoriesContained(), startCountry, targetCountry, player);
			if (hamPath != null) {
				if (hamPath.size() < 2) {
					int k = 0;
				}
				path.addAll(hamPath);
			} else {
				path = null;
			}
		}
		return path;
	}
	*/
	
	/** returns a vector of paths, instead of just a single path.*/
	public Vector findPaths(Country startCountry, Country targetCountry, Player player) {
		// Run AStar
		Vector pathSegments_StartCountries = new Vector();
		Vector pathSegments_TargetCountries = new Vector();
		Vector followPath = aStarPathFinder.getShortestPath(startCountry, targetCountry, player);
		if (followPath != null) {
			followPath.add(0, startCountry);
		} else {
			return new Vector();
		}
		Vector tmpPath = (Vector)followPath.clone();
		Country currentStart = startCountry;
		Country lastCountry = startCountry;
		if (startCountry.getContinent().equals(targetCountry.getContinent())) {
			currentStart = startCountry;
			lastCountry = targetCountry;
		} else {
			while (!followPath.isEmpty()) {
				Country c = (Country)followPath.firstElement();
				if ((board.isBorderTerritory(c)) && (!c.getContinent().equals(currentStart.getContinent()))) {
					pathSegments_StartCountries.add(currentStart);
					pathSegments_TargetCountries.add(lastCountry);
					currentStart = c;
				}
				lastCountry = c;
				followPath.remove(0);
			}
		}
		pathSegments_StartCountries.add(currentStart);
		pathSegments_TargetCountries.add(lastCountry);
		
		// Clean the segments (eg.: remove segments with only a single territory).
		/*for (int i = pathSegments_StartCountries.size()-2; i >= 0; i--) {
			if (pathSegments_StartCountries.get(i).equals(pathSegments_TargetCountries.get(i))) {
				pathSegments_StartCountries.set(i+1,pathSegments_StartCountries.get(i));
				pathSegments_StartCountries.remove(i);
				pathSegments_TargetCountries.remove(i);
			}
		}
		if ((pathSegments_StartCountries.size() > 1) && (pathSegments_StartCountries.lastElement().equals(pathSegments_TargetCountries.lastElement()))) {
			pathSegments_TargetCountries.set(pathSegments_TargetCountries.size()-2,pathSegments_TargetCountries.lastElement());
			pathSegments_StartCountries.remove(pathSegments_TargetCountries.size()-1);
			pathSegments_TargetCountries.remove(pathSegments_TargetCountries.size()-1);
		}*/
		
		
		// Build A* path-segments from the first path we found (tmpPath).
		Vector pathSegments_Result_AStar = new Vector();
		for (int i = 0; i < pathSegments_StartCountries.size(); i++) {
			//pathSegments_Result_AStar.add(new Vector());
			int s = tmpPath.indexOf((Country)pathSegments_StartCountries.get(i));
			int t = tmpPath.indexOf((Country)pathSegments_TargetCountries.get(i));
			pathSegments_Result_AStar.add(new Vector());
			((Vector)pathSegments_Result_AStar.lastElement()).addAll(tmpPath.subList(s,t+1));
		}
		boolean[] continentsDone = new boolean[board.getContinents().size()];
		for (int i = 0; i < continentsDone.length; i++) {
			continentsDone[i] = false;
		}
		// Build hamPath-segments
		Vector pathSegments_Result_HamPath = new Vector();
		for (int i = 0; i < pathSegments_StartCountries.size(); i++) {
			pathSegments_Result_HamPath.add(new Vector());
			
			Vector hamPathTerritories = (Vector)((Country)pathSegments_StartCountries.get(i)).getContinent().getTerritoriesContained().clone();
			// Remove all territories on the path, that is inside the given continent - otherwise the resulting path might contain the same territory twice (if the path visited the same continent twice).
			Vector removeTerritories = (Vector)tmpPath.clone();
			removeTerritories.removeAll((Vector)pathSegments_Result_AStar.get(i));
			hamPathTerritories.removeAll(removeTerritories);
			
			Vector hamPath = hamPathFinder.getPath(hamPathTerritories ,(Country)pathSegments_StartCountries.get(i), (Country)pathSegments_TargetCountries.get(i), player);
			
			int continentIndex = board.getContinents().indexOf(((Country)pathSegments_StartCountries.get(i)).getContinent());
			
			if ((hamPath != null) && (!continentsDone[continentIndex])) {
				((Vector)pathSegments_Result_HamPath.lastElement()).addAll(hamPath);
				continentsDone[continentIndex] = true;
			} else {
				hamPath = null;
				pathSegments_Result_HamPath.set(pathSegments_Result_HamPath.size()-1,hamPath);
			}
		}		
		// Build paths from the A* segments and hamPath segments.
		int resultSize = (int)Math.pow(2.0d,(double)pathSegments_Result_HamPath.size());
		Vector result = new Vector();
		for (int i = 0; i < resultSize; i++) {
			result.add(new Vector());
			Vector r = (Vector)result.lastElement();
			boolean containsNullPath = false;
			int n = i;
			for (int k = 0; k < pathSegments_Result_HamPath.size(); k++) {
				int half = resultSize;
				for (int l = 0; l < k+1; l++) {
					if ((l > 0) && (n >= half)) {
						n -= half;
					}
					half /= 2;
				}
				if (n < half) {
					r.addAll(((Vector)pathSegments_Result_AStar.get(k)));
				} else {
					if (pathSegments_Result_HamPath.get(k) == null) {
						containsNullPath = true;
						break;
					} else {
						r.addAll(((Vector)pathSegments_Result_HamPath.get(k)));
					}
				}
			}
			if (containsNullPath) {
				result.remove(result.size()-1);
			}
		}
		
		// Check path integrity
		for (int i = 0; i < result.size(); i++) {
			Vector p = (Vector)result.get(i);
			for (int j = 1; j < p.size(); j++) {
				if ((!((Country)p.get(j-1)).getNeighbours().contains((Country)p.get(j))) || (((Country)p.get(j)).getOwner().equals(player))) {
					int a = 0;
				}
			}
			if (!((Country)p.get(0)).getOwner().equals(player)) {
				int a = 0;
			}
			for (int j = 0; j < p.size(); j++) {
				for (int k = 0; k < p.size(); k++) {
					if ((j != k) && (((Country)p.get(k)).equals((Country)p.get(j)))) {
						int a = 0;
					}
				}
			}
		}
		return result;
	}	
}
