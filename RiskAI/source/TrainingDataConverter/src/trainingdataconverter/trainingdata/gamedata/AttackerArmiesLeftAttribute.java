package trainingdataconverter.trainingdata.gamedata;

public class AttackerArmiesLeftAttribute extends GameDataAttribute {
    private static String[] states = new String[]{"-1.0;-1.0", "0.0;0.0", "1.0;1.0", "2.0;2.0", "3.0;3.0", "4.0;4.0", "5.0;5.0", "6.0;6.0", "7.0;7.0", "8.0;8.0", "9.0;9.0", "10.0;10.0", "11.0;11.0", "12.0;13.0", "14.0;16.0", "17.0;19.0", "20.0;23.0", "24.0;27.0", "28.0;33.0", "34.0;39.0"};
    private static AttributeStates arffAttributeStates;
    private int armies;

    public AttackerArmiesLeftAttribute(int armies) {
        this.name = "AttackerArmiesLeft";
        if (armies < -1) {
            throw new UnsupportedOperationException("Armies must be >= -1 (armies = " + armies + ")");
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
