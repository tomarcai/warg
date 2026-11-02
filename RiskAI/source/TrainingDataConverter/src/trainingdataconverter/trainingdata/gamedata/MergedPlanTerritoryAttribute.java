package trainingdataconverter.trainingdata.gamedata;

import trainingdataconverter.trainingdata.TrainingData;

public class MergedPlanTerritoryAttribute extends GameDataAttribute {
    private static String[] arff_states;
    private static AttributeStates arffAttributeStates;

    public MergedPlanTerritoryAttribute(String territoryName) {
        this.name = "MergedPlanTerritory";
        this.setValue(territoryName);
    }

    public String getARFFValue() {
        return this.value;
    }

    public String getNNValue() {
        int i;
        for(i = TrainingData.TERRITORY_NAMES.length - 1; i > -1 && !TrainingData.TERRITORY_NAMES[i].equals(this.value); --i) {
        }

        return String.valueOf((float)i / (float)(TrainingData.TERRITORY_NAMES.length - 1));
    }

    public String getARFFHeader() {
        return arffAttributeStates.getARFFHeader(this.name);
    }

    public static AttributeStates getARFFAttributeStates() {
        return arffAttributeStates;
    }

    static {
        arff_states = TrainingData.TERRITORY_NAMES;
        arffAttributeStates = new AttributeStates(arff_states);
    }
}
