package trainingdataconverter.trainingdata.gamedata;

public class IsDefensePlanAttribute extends GameDataAttribute {
    private static String[] arff_states = new String[]{"false", "true"};
    private static AttributeStates arffAttributeStates;
    private boolean bool_value;

    public IsDefensePlanAttribute(boolean value) {
        this.name = "IsDefensePlan";
        this.bool_value = value;
        this.setValue(String.valueOf(value));
    }

    public String getARFFValue() {
        return arffAttributeStates.getState(this.bool_value);
    }

    public String getNNValue() {
        return this.bool_value ? "1.0" : "0.0";
    }

    public String getARFFHeader() {
        return arffAttributeStates.getARFFHeader(this.name);
    }

    static {
        arffAttributeStates = new AttributeStates(arff_states);
    }
}
