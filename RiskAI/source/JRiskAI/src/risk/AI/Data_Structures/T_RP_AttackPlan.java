package risk.AI.Data_Structures;

import net.yura.domination.engine.core.Continent;
import net.yura.domination.engine.core.Country;
import java.util.*;

/**
 * Contains a simple attack plan (a list of countries).
 */
public class T_RP_AttackPlan {
	
	Vector attack_plan = new Vector();
	Vector leaveArmies = new Vector();
	Vector<Continent> coveredContinents = new Vector();
	
	public void addCountry(Country c) {
            attack_plan.add(c);
        }
        
	public Country getCountry(int index) {
	    return (Country)attack_plan.get(index);
	}
	
	public Vector getCountries() {
	    return attack_plan;
	}
	
	public int size() {
	    return attack_plan.size();
	}
	
	public Country getLastCountry() {
		return (Country)attack_plan.lastElement();
	}
	
	public Country getFirstCountry() {
		return (Country)attack_plan.firstElement();
	}
	
	public boolean containsCountry(Object elem) {
		return attack_plan.contains(elem);
	}
	
	public boolean containsAllCountries(Collection<?> c) {
		return attack_plan.containsAll(c);
	}
	
	public void addCountries(Vector path) {
		attack_plan.addAll(path);
	}
	
	public void remove(int index) {
		attack_plan.remove(index);
		leaveArmies.remove(index);
	}
	
	public void addArmiesToLeave(Vector leaveArmies) {
		this.leaveArmies.addAll(leaveArmies);
	}
	
	public int getArmiesToLeave(int index) {
		return (Integer)leaveArmies.get(index);
	}

	public void setArmiesToLeave(int index, int armiesToLeave) {
		this.leaveArmies.set(index, armiesToLeave);
	}

	public void addCountry(int i) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public Vector<Continent> getCoveredContinents() {
		return coveredContinents;
	}

	public void setCoveredContinents(Vector coveredContinents) {
		this.coveredContinents = coveredContinents;
	}


	public String toString() {
		String str = "";
		for (int j = 0; j < attack_plan.size(); j++) {
			str = str + ((Country) attack_plan.get(j)).getIdString() + "(" + leaveArmies.get(j) + ")";
			if (j < attack_plan.size() - 1) {
				str = str + ",";
			}
		}
		if (coveredContinents != null) {
			str = str + ". Continents: ";
			for (int i = 0; i < coveredContinents.size(); i++) {
				str = str + coveredContinents.get(i).getName();
				if (i < coveredContinents.size() - 1) {
					 str = str + ",";
				}
			}
		}
		return str;
	}
}