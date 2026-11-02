package risk.AI.Data_Structures;

import java.util.*;

public class T_RP_DefenseList {
	
	Vector defense_list = new Vector();
	
	public void add(T_RP_DefenseListElement defense_list_element) {
	    defense_list.add(defense_list_element);
	}
	
	public T_RP_DefenseListElement get(int index) {
	    return (T_RP_DefenseListElement)defense_list.get(index);
	}	
	
	public int size() {
	    return defense_list.size();
	}
}