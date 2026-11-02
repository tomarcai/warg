package trainingdataconverter.trainingdata.gamedata;

public class ScoreAttribute extends GameDataAttribute {
    private static String[] arffStatesFull = new String[]{"0.0;0.0", "0.0;8.4675486E-16", "8.4675486E-16;1.137778E-8", "1.137778E-8;4.783743E-7", "4.783743E-7;3.2868045E-6", "3.2868045E-6;8.785053E-6", "8.785053E-6;4.3925265E-5", "4.3925265E-5;1.76698E-4", "1.76698E-4;7.321831E-4", "7.321831E-4;0.0016419431", "0.0016419431;0.0045621316", "0.0045621316;0.0093585225", "0.0093585225;0.023413863", "0.023413863;0.07165468", "0.07165468;0.2630756", "0.2630756;1.0604496", "1.0604496;6.0259304", "6.0259304;33.123405", "33.123405;1809251.1", "1809251.1;5.089442E7"};
    private static String[] arffStatesHalf = new String[]{"0.0;1.1809805E-6", "1.1809805E-6;5.84704E-4", "5.84704E-4;0.035219815", "0.035219815;0.35325336", "0.35325336;1.3785844", "1.3785844;4.398047", "4.398047;19.77763", "19.77763;248.2609", "248.2609;8.6851885E8", "8.6851885E8;8.6851885E8"};
    private static AttributeStates nnAttributeStates;
    private static AttributeStates arffAttributeStates;
    private float score;

    public ScoreAttribute(int index, float score) {
        this.name = "Score" + index;
        if (score < 0.0F) {
            throw new UnsupportedOperationException("Score must be >= 0.0 (score = " + score + ")");
        } else {
            this.score = score;
            this.setValue(String.valueOf(score));
        }
    }

    public ScoreAttribute(float score) {
        this.name = "Score";
        if (score < 0.0F) {
            throw new UnsupportedOperationException("Score must be >= 0.0 (score = " + score + ")");
        } else {
            this.score = score;
            this.setValue(String.valueOf(score));
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
        return arffAttributeStates.getState(this.score);
    }

    public String getNNValue() {
        return String.valueOf((float)nnAttributeStates.getStateIndex(this.score) / (float)(arffStatesFull.length - 1));
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
