package trainingdataconverter.trainingdata.gamedata;

public class ArmiesLeftAttribute extends GameDataAttribute {
    private static String[] states = new String[]{"0;0", "1;1", "2;2", "3;3", "4;4", "5;5", "6;6", "7;7", "8;8", "9;9", "10;12", "12;15", "15;20", "20;25", "25;30", "30;35", "35;40", "40;50", "50;75", "75;100", "100;100"};
    private static AttributeStates arffAttributeStates;
    private static String[] bnStates;
    private static AttributeStates bnAttributeStates;
    private int armies;

    public ArmiesLeftAttribute(int armies) {
        this.name = "ArmiesLeft";
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

    public String getBnValue() {
        return bnAttributeStates.getState((float)this.armies);
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
        bnStates = new String[]{"0;0", "1;1", "2;2", "3;3", "4;4", "5;5", "6;6", "7;7", "8;8", "9;9", "10;12", "12;15", "15;20", "20;25", "25;30", "30;35", "35;40", "40;50", "50;75", "75;100", "100;100"};
        bnAttributeStates = new AttributeStates(bnStates);
    }
}
