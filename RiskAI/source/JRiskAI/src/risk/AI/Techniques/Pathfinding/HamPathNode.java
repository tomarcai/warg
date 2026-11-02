package risk.AI.Techniques.Pathfinding;

import java.util.*;
import net.yura.domination.engine.core.Country;

public class HamPathNode implements Cloneable {
	
	private Vector neighbors = new Vector();
	private boolean visited = false;
	private Country countryPointer;
	
	public HamPathNode(Country countryPointer) {
		this.countryPointer = countryPointer;
	}
	
	public HamPathNode getNeighbor(int index) {
		return (HamPathNode)neighbors.get(index);
	}

	public void addNeighbor(HamPathNode node) {
		neighbors.add(node);
	}
	
	public int getNeighborCount() {
		return neighbors.size();
	}

	public boolean getVisited() {
		return visited;
	}
	
	public void setVisited(boolean value) {
		visited = value;
	}
	
	public Country getCountry() {
		return countryPointer;
	}
	
}