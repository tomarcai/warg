package risk.AI.Data_Structures;

import java.util.*;

public class T_RP_MergedPlanList {
	
	// Contains: country, priority, estimated_cost and minimum_cost.
	Vector mergedPlanList = new Vector();
	
	public void add(T_RP_MergedPlanElement mergedPlan) {
		mergedPlanList.add(mergedPlan);
	}
	
	public T_RP_MergedPlanElement get(int index) {
		return (T_RP_MergedPlanElement)mergedPlanList.get(index);
	}	
	
	public int size() {
		return mergedPlanList.size();
	}
	
	public Object remove(int index) {
		return mergedPlanList.remove(index);
	}
	
	public boolean remove(Object o) {
		return mergedPlanList.remove(o);
	}
	
	public T_RP_MergedPlanList getListSortedByScore() {
		Vector sorted_plan = new Vector();
		if (mergedPlanList.size() > 0) {
			sorted_plan.add(mergedPlanList.get(0));
			for (int i = 1; i < mergedPlanList.size(); i++) {
				int j = 0;
				while ((j < sorted_plan.size()) && 
					(((T_RP_MergedPlanElement)sorted_plan.get(j)).getScore() > ((T_RP_MergedPlanElement)mergedPlanList.get(i)).getScore())) {
					j++;
				}
				sorted_plan.add(j,(T_RP_MergedPlanElement)mergedPlanList.get(i));
			}
		}
		T_RP_MergedPlanList out = new T_RP_MergedPlanList();
		out.mergedPlanList = sorted_plan;
		return out;
	}
}