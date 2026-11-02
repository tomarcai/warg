package trainingdataconverter.trainingdata.gamedata;

import trainingdataconverter.trainingdata.TrainingData;

public class TerritoryOutputAttribute extends GameDataAttribute {
    private static String[] arff_states;
    private static AttributeStates arffAttributeStates;
    private TerritoryAttribute[] occupiedTerritories;

    public TerritoryOutputAttribute(String territoryName, TerritoryAttribute[] occupiedTerritories) {
        this.name = "TerritoryOutput";
        this.occupiedTerritories = occupiedTerritories;
        this.setValue(territoryName);
    }

    public String getARFFValue() {
        return this.value;
    }

    public String getNNValue() {
        int i;
        for(i = this.occupiedTerritories.length - 1; i > -1 && !this.occupiedTerritories[i].getName().equals(this.value); --i) {
        }

        if (i == -1) {
            throw new RuntimeException("territoryName not found in list of occupied territories!");
        } else {
            return this.occupiedTerritories.length == 1 ? "0.0" : String.valueOf((float)i / (float)(this.occupiedTerritories.length - 1));
        }
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
