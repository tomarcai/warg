package trainingdataconverter.trainingdata.gamedata;

public class WinningEstimateAttribute extends GameDataAttribute {
    private static String[] arffStatesFull = new String[]{"0.0;0.0", "0.0;0.0027465275", "0.0027465275;0.008466655", "0.008466655;0.01987", "0.01987;0.038419135", "0.038419135;0.06563", "0.06563;0.103913955", "0.103913955;0.15193805", "0.15193805;0.21235168", "0.21235168;0.2832437", "0.2832437;0.3680335", "0.3680335;0.46292588", "0.46292588;0.5709525", "0.5709525;0.6481481", "0.6481481;0.72485", "0.72485;0.78703696", "0.78703696;0.87795", "0.87795;0.97448194", "0.97448194;1.0", "1.0;1.0"};
    private static String[] arffStatesHalf = new String[]{"0.0;0.0", "0.0;0.0036779928", "0.0036779928;0.032423794", "0.032423794;0.1158335", "0.1158335;0.271736", "0.271736;0.50797", "0.50797;0.7159058", "0.7159058;0.9259259", "0.9259259;1.0", "1.0;1.0"};
    private static AttributeStates arffAttributeStates;
    private static AttributeStates bnAttributeStates;
    private float estimate;

    public WinningEstimateAttribute(float estimate) {
        this.name = "WinningEstimate";
        if (!(estimate < 0.0F) && !(estimate > 1.0F)) {
            this.estimate = estimate;
            this.setValue(String.valueOf(estimate));
        } else {
            throw new UnsupportedOperationException("Estimate must be >= 0.0 and <= 1.0! (estimate = " + estimate + ")");
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
        return arffAttributeStates.getState(this.estimate);
    }

    public String getNNValue() {
        return String.valueOf(this.estimate);
    }

    public String getARFFHeader() {
        return arffAttributeStates.getARFFHeader(this.name);
    }

    public static AttributeStates getARFFAttributeStates() {
        return arffAttributeStates;
    }

    public static float getEstimateFromARFFState(int arffStateIndex) {
        return arffAttributeStates.getValueFromIndex(arffStateIndex);
    }

    public static float getBNValueFromState(int stateIndex) {
        return bnAttributeStates.getValueFromIndex(stateIndex);
    }

    public String getBNValue() {
        return bnAttributeStates.getState(this.estimate);
    }

    static {
        arffAttributeStates = new AttributeStates(arffStatesFull);
        bnAttributeStates = new AttributeStates(arffStatesFull);
    }
}
