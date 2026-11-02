package trainingdataconverter.trainingdata.gamedata;

public class OwnershipAttribute extends GameDataAttribute {
    private static String[] arffAttributeStates = new String[]{"false", "true"};
    private boolean isOwned;

    public OwnershipAttribute(String continentName, boolean isOwned) {
        this.name = "Ownership" + continentName;
        this.isOwned = isOwned;
        this.setValue(String.valueOf(isOwned));
    }

    public String getARFFValue() {
        return this.isOwned ? arffAttributeStates[1] : arffAttributeStates[0];
    }

    public String getNNValue() {
        return this.isOwned ? "1.0" : "0.0";
    }

    public String getARFFHeader() {
        return this.makeARFFHeader(arffAttributeStates);
    }

    public static float getEstimateFromARFFState(int arffStateIndex) {
        return Float.parseFloat(arffAttributeStates[arffStateIndex]);
    }
}
