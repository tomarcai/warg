package trainingdataconverter.trainingdata.gamedata;

public class ContinentEstimateAttribute extends GameDataAttribute {
    private static String[] arffStatesFull = new String[]{"0.0;0.0", "0.0;0.0018", "0.0018;0.01127", "0.01127;0.03875", "0.03875;0.08421", "0.08421;0.1521", "0.1521;0.2544", "0.2544;0.32806", "0.32806;0.46622", "0.46622;0.57682", "0.57682;0.72485", "0.72485;0.80835", "0.80835;0.887", "0.887;0.94793", "0.94793;0.98082", "0.98082;0.99543", "0.99543;0.99946", "0.99946;0.99998", "0.99998;1.0", "1.0;1.0"};
    private static String[] arffStatesHalf = new String[]{"0.0;0.0", "0.0;0.01037", "0.01037;0.09196", "0.09196;0.31485", "0.31485;0.57682", "0.57682;0.83554", "0.83554;0.97607", "0.97607;0.99967", "0.99967;1.0", "1.0;1.0"};
    private static String[] bnStates;
    private static AttributeStates arffAttributeStates;
    private static AttributeStates bnAttributeStates;
    private float estimate;

    public ContinentEstimateAttribute(String continentName, float estimate) {
        this.name = "ContinentEstimate" + continentName;
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

    public String getBNValue() {
        return bnAttributeStates.getState(this.estimate);
    }

    public int getStateIndex() {
        return bnAttributeStates.getStateIndex(this.estimate);
    }

    public String getNNValue() {
        return String.valueOf(this.estimate);
    }

    public String getARFFHeader() {
        return arffAttributeStates.getARFFHeader(this.name);
    }

    public static float getEstimateFromARFFState(int arffStateIndex) {
        return arffAttributeStates.getValueFromIndex(arffStateIndex);
    }

    public static AttributeStates getARFFAttributeStates() {
        return arffAttributeStates;
    }

    public static float getBNValueFromState(int stateIndex) {
        return bnAttributeStates.getValueFromIndex(stateIndex);
    }

    public static String getStateFromIndex(int stateIndex) {
        return bnStates[stateIndex];
    }

    public float getEstimate() {
        return this.estimate;
    }

    static {
        bnStates = arffStatesFull;
        arffAttributeStates = new AttributeStates(arffStatesFull);
        bnAttributeStates = new AttributeStates(bnStates);
    }
}
