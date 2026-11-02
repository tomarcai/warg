package trainingdataconverter.trainingdata.gamedata;

public class PriorityAttribute extends GameDataAttribute {
    private static String[] arffStatesFull = new String[]{"0.0;0.075", "0.075;0.28088894", "0.28088894;0.34555137", "0.34555137;0.45499998", "0.45499998;0.585", "0.585;0.7335714", "0.7335714;0.94250005", "0.94250005;1.020139", "1.020139;1.1688889", "1.1688889;1.3", "1.3;1.3333333", "1.3333333;1.5199999", "1.5199999;1.6", "1.6;1.6133333", "1.6133333;1.8399999", "1.8399999;1.9949999", "1.9949999;2.3530667", "2.3530667;2.97", "2.97;10.9375", "10.9375;11.005"};
    private static String[] arffStatesHalf = new String[]{"0.0;0.29999998", "0.29999998;0.4875", "0.4875;0.8", "0.8;1.0833684", "1.0833684;1.3", "1.3;1.6", "1.6;1.805", "1.805;2.2800002", "2.2800002;11.005", "11.005;11.005"};
    private static AttributeStates nnAttributeStates;
    private static AttributeStates arffAttributeStates;
    private float priority;

    public PriorityAttribute(int index, float priority) {
        this.name = "Priority" + index;
        if (!(priority < 0.0F) && !(priority > 15.0F)) {
            this.priority = priority;
            this.setValue(String.valueOf(priority));
        } else {
            throw new UnsupportedOperationException("Priority must be >= 0.0 and <= 15.0 (priority = " + priority + ")");
        }
    }

    public PriorityAttribute(float priority) {
        this.name = "Priority";
        if (!(priority < 0.0F) && !(priority > 15.0F)) {
            this.priority = priority;
            this.setValue(String.valueOf(priority));
        } else {
            throw new UnsupportedOperationException("Priority must be >= 0.0 and <= 15.0 (priority = " + priority + ")");
        }
    }

    public void useHalfIntervals(boolean useHalfIntervals) {
        if (useHalfIntervals) {
            arffAttributeStates = new AttributeStates(arffStatesHalf);
        } else {
            arffAttributeStates = new AttributeStates(arffStatesFull);
        }

    }

    public String getARFFValue() {
        return arffAttributeStates.getState(this.priority);
    }

    public String getNNValue() {
        return String.valueOf((float)nnAttributeStates.getStateIndex(this.priority) / (float)(arffStatesFull.length - 1));
    }

    public String getARFFHeader() {
        return arffAttributeStates.getARFFHeader(this.name);
    }

    public static AttributeStates getARFFAttributeStates() {
        return arffAttributeStates;
    }

    public static float getARFFValueFromState(int stateIndex) {
        return arffAttributeStates.getValueFromIndex(stateIndex);
    }

    public static float getNNValueFromNNOutput(float output) {
        return nnAttributeStates.getValueFromIndex(Math.round(output * (float)(arffStatesFull.length - 1)));
    }

    static {
        nnAttributeStates = new AttributeStates(arffStatesFull);
        arffAttributeStates = new AttributeStates(arffStatesFull);
    }
}
