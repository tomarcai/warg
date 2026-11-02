package trainingdataconverter.trainingdata.gamedata;

public class OpponentCardAttribute extends GameDataAttribute {
    private static String[] arffStatesFull = new String[]{"0;0", "1;1", "2;2", "3;3", "4;4", "5;5", "6;6", "7;7", "8;8", "9;9", "10;10"};
    private static String[] arffStatesHalf = new String[]{"0;0", "1;1", "2;2", "3;3", "4;4", "5;5", "6;10"};
    private static AttributeStates arffAttributeStates;
    private static AttributeStates nnAttributeStates;
    private int numberOfCards;

    public OpponentCardAttribute(int opponentIndex, int numberOfCards) {
        this.name = "Player" + opponentIndex + "Cards";
        this.numberOfCards = numberOfCards;
        this.setValue(String.valueOf(numberOfCards));
    }

    public void useHalfIntervals(boolean useHalfIntervals) {
        if (useHalfIntervals) {
            arffAttributeStates = new AttributeStates(arffStatesHalf);
        } else {
            arffAttributeStates = new AttributeStates(arffStatesFull);
        }

    }

    public String getARFFValue() {
        return arffAttributeStates.getState((float)this.numberOfCards);
    }

    public String getNNValue() {
        return String.valueOf((float)nnAttributeStates.getStateIndex((float)this.numberOfCards) / (float)(arffStatesFull.length - 1));
    }

    public String getARFFHeader() {
        return arffAttributeStates.getARFFHeader(this.name);
    }

    public static float getArffScoreFromState(int stateIndex) {
        return arffAttributeStates.getValueFromIndex(stateIndex);
    }

    public static float getNNValueFromNNOutput(float output) {
        return nnAttributeStates.getValueFromIndex(Math.round(output * (float)(arffStatesFull.length - 1)));
    }

    static {
        arffAttributeStates = new AttributeStates(arffStatesFull);
        nnAttributeStates = new AttributeStates(arffStatesFull);
    }
}
