package trainingdataconverter.trainingdata.gamedata;

public class ArmiesInStartAttribute extends GameDataAttribute {
    private static String[] states = new String[]{"1.0;1.0", "2.0;2.0", "3.0;3.0", "4.0;4.0", "5.0;5.0", "6.0;6.0", "7.0;7.0", "8.0;8.0", "9.0;9.0", "10.0;10.0", "11.0;11.0", "12.0;12.0", "12.0;12.0", "13.0;13.0", "14.0;14.0", "15.0;17.0", "17.0;20.0", "20.0;30.0", "30.0;43.0", "43.0;68.0"};
    private static AttributeStates arffAttributeStates;
    private int armies;

    public ArmiesInStartAttribute(int armies) {
        this.name = "ArmiesInStart";
        if (armies < 0) {
            throw new UnsupportedOperationException("Armies must be >= 0 (armies = " + armies + ")");
        } else {
            this.armies = armies;
            this.setValue(String.valueOf(armies));
        }
    }

    public String getARFFValue() {
        return arffAttributeStates.getState((float)this.armies);
    }

    public String getNNValue() {
        return String.valueOf((float)arffAttributeStates.getStateIndex((float)this.armies) / (float)(states.length - 1));
    }

    public String getARFFHeader() {
        return arffAttributeStates.getARFFHeader(this.name);
    }

    public static float getArffValueFromState(int stateIndex) {
        return arffAttributeStates.getValueFromIndex(stateIndex);
    }

    public static float getNNValueFromNNOutput(float output) {
        return arffAttributeStates.getValueFromIndex(Math.round(output * (float)(states.length - 1)));
    }

    static {
        arffAttributeStates = new AttributeStates(states);
    }
}
