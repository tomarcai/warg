package trainingdataconverter.trainingdata.gamedata;

import trainingdataconverter.trainingdata.TrainingData;

public class MissionAttribute extends GameDataAttribute {
    private static String[] arff_states;
    private static AttributeStates arffAttributeStates;

    public MissionAttribute(String missionName) {
        this.name = "Mission";
        this.setValue(missionName);
    }

    public String getARFFValue() {
        return this.value;
    }

    public String getNNValue() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    public String getARFFHeader() {
        return arffAttributeStates.getARFFHeader(this.name);
    }

    public static AttributeStates getARFFAttributeStates() {
        return arffAttributeStates;
    }

    static {
        arff_states = TrainingData.MISSION_NAMES;
        arffAttributeStates = new AttributeStates(arff_states);
    }
}
