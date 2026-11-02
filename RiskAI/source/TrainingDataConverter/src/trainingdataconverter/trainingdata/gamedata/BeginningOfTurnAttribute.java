package trainingdataconverter.trainingdata.gamedata;

public class BeginningOfTurnAttribute extends GameDataAttribute {
    private static String[] arffAttributeStates = new String[]{"false", "true"};
    private boolean bool_value;

    public BeginningOfTurnAttribute(boolean value) {
        this.name = "BeginningOfTurn";
        this.bool_value = value;
        this.setValue(String.valueOf(value));
    }

    public String getARFFValue() {
        return this.bool_value ? arffAttributeStates[1] : arffAttributeStates[0];
    }

    public String getNNValue() {
        return this.bool_value ? "1.0" : "0.0";
    }

    public String getARFFHeader() {
        return this.makeARFFHeader(arffAttributeStates);
    }

    public static float getEstimateFromARFFState(int arffStateIndex) {
        return Float.parseFloat(arffAttributeStates[arffStateIndex]);
    }
}
