package trainingdataconverter.trainingdata.gamedata;

import java.util.Vector;

public class MergedPlanList {
    private Vector<MergedPlanElement> mergedPlans = new Vector();

    public MergedPlanList() {
    }

    public void add(MergedPlanElement e) {
        this.mergedPlans.add(e);
    }

    public MergedPlanElement get(int index) {
        return (MergedPlanElement)this.mergedPlans.get(index);
    }

    public int size() {
        return this.mergedPlans.size();
    }
}
