package trainingdataconverter.trainingdata.gamedata;

public class CostAttribute extends GameDataAttribute {
    private static String[] arffStatesFull = new String[]{"1.0;1.0", "2.0;2.0", "3.0;3.0", "4.0;4.0", "5.0;5.0", "6.0;6.0", "7.0;7.0", "8.0;8.0", "9.0;9.0", "10.0;10.0", "11.0;11.0", "12.0;12.0", "13.0;13.0", "14.0;14.0", "15.0;15.0", "16.0;16.0", "16.0;18.0", "18.0;26.0", "26.0;36.0", "36.0;39.0"};
    private static String[] arffStatesHalf = new String[]{"1.0;1.0", "1.0;3.0", "3.0;4.0", "4.0;6.0", "6.0;7.0", "7.0;9.0", "9.0;10.0", "10.0;13.0", "13.0;15.0", "15.0;39.0", "39.0;39.0"};
    private static AttributeStates arffAttributeStates;
    private int cost;

    public CostAttribute(String type, int index, int cost) {
        this.name = "Cost" + type + index;
        if (cost < 0) {
            throw new UnsupportedOperationException("Cost must be >= 0 (cost = " + cost + ")");
        } else {
            this.cost = cost;
            this.setValue(String.valueOf(cost));
        }
    }

    public CostAttribute(String type, int cost) {
        this.name = "Cost" + type;
        if (cost < 0) {
            throw new UnsupportedOperationException("Cost must be >= 0 (cost = " + cost + ")");
        } else {
            this.cost = cost;
            this.setValue(String.valueOf(cost));
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
        return arffAttributeStates.getState((float)this.cost);
    }

    public String getNNValue() {
        return String.valueOf((float)this.cost / 40.0F);
    }

    public String getARFFHeader() {
        return arffAttributeStates.getARFFHeader(this.name);
    }

    public static float getARFFValueFromState(int stateIndex) {
        return arffAttributeStates.getValueFromIndex(stateIndex);
    }

    public static float getNNValueFromNNOutput(float output) {
        return (float)Math.round(output * 40.0F);
    }

    public static AttributeStates getARFFAttributeStates() {
        return arffAttributeStates;
    }

    static {
        arffAttributeStates = new AttributeStates(arffStatesFull);
    }
}
