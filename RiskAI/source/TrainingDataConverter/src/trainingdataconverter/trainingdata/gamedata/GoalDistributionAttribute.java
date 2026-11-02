package trainingdataconverter.trainingdata.gamedata;

public class GoalDistributionAttribute extends GameDataAttribute {
    private static String[] arff_states = new String[]{"0.0;0.0", "0.0;0.19999999", "0.19999999;0.2", "0.2;0.24999999", "0.24999999;0.29999998", "0.29999998;0.29999998", "0.29999998;0.3", "0.3;0.3", "0.3;0.35", "0.35;0.35", "0.35;0.4", "0.4;0.4", "0.4;0.45", "0.45;0.5", "0.5;0.5", "0.5;0.6", "0.6;0.79999995", "0.79999995;0.95", "0.95;1.0", "1.0;1.0"};
    private static AttributeStates arffAttributeStates;
    private float estimate;

    public GoalDistributionAttribute(int goalIndex, float estimate) {
        this.name = "GoalDistribution" + goalIndex;
        if (!(estimate < 0.0F) && !(estimate > 1.0F)) {
            this.estimate = estimate;
            this.setValue(String.valueOf(estimate));
        } else {
            throw new UnsupportedOperationException("Estimate must be >= 0.0 and <= 1.0! (estimate = " + estimate + ")");
        }
    }

    public String getARFFValue() {
        return arffAttributeStates.getState(this.estimate);
    }

    public String getNNValue() {
        return String.valueOf(this.estimate);
    }

    public String getARFFHeader() {
        return arffAttributeStates.getARFFHeader(this.name);
    }

    public static float getEstimateFromARFFState(int arffStateIndex) {
        return arffAttributeStates.getValueFromIndex(arffStateIndex);
    }

    static {
        arffAttributeStates = new AttributeStates(arff_states);
    }
}
