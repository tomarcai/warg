package trainingdataconverter.trainingdata.gamedata;

public class MissionEstimateAttribute extends GameDataAttribute {
    private static String[] arffStates = new String[]{"-0.11265432;-7.558579E-4", "-7.558579E-4;0.0", "0.0;0.0", "0.0;0.006313131", "0.006313131;0.026774691", "0.026774691;0.059553873", "0.059553873;0.107077725", "0.107077725;0.16700661", "0.16700661;0.6099537", "0.6099537;0.61634696"};
    private static AttributeStates arffAttributeStates;
    private float estimate;

    public MissionEstimateAttribute(String missionName, float estimate) {
        this.name = "MissionEstimate" + missionName;
        if (!(estimate < -0.15F) && !(estimate > 1.0F)) {
            this.estimate = estimate;
            this.setValue(String.valueOf(estimate));
        } else {
            throw new UnsupportedOperationException("Estimate must be >= -0.15 and <= 1.0! (estimate = " + estimate + ")");
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

    public float getEstimate() {
        return this.estimate;
    }

    static {
        arffAttributeStates = new AttributeStates(arffStates);
    }
}
