/*
 * O_RP_MakeAttackPlan.java
 *
 * Created on 2. marts 2006, 09:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package risk.AI.Data_Structures.Module_Output;

import java.util.Vector;
import risk.AI.Data_Structures.*;

/**
 *
 * @author Administrator
 */
public class O_RP_MakeAttackPlan {
	
	Vector paths = new Vector();
	
	/** Creates a new instance of O_RP_MakeAttackPlan */
	public O_RP_MakeAttackPlan() {
	}
	
	public void addAttackPlan(T_RP_AttackPlan plan) {
		paths.add(plan);
	}
	
	/*public void setPaths(Vector paths) {
		this.paths = paths;
	}*/
	
	public T_RP_AttackPlan getAttackPlan(int index) {
		return (T_RP_AttackPlan)paths.get(index);
	}
	
	public int size() {
		return paths.size();
	}
	
}
