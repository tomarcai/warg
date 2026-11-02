package trainingdataconverter.trainingdata.gamedata;

public class AttackPlanTerritoryAttribute extends GameDataAttribute {
    private static String[] arff_states = new String[]{"0.0;0.0", "1.0;1.0", "2.0;2.0"};
    private static AttributeStates arffAttributeStates;
    private int armiesToLeave;

    public AttackPlanTerritoryAttribute(String territoryName, int armiesToLeave) {
        this.name = "AttackPlan_" + territoryName;
        if (armiesToLeave < 0) {
            throw new UnsupportedOperationException("Number of armies must at least be 0! (armiesToLeave = " + armiesToLeave + ")");
        } else {
            this.armiesToLeave = armiesToLeave;
            this.setValue(String.valueOf(armiesToLeave));
        }
    }

    public String getARFFValue() {
        return arffAttributeStates.getState((float)this.armiesToLeave);
    }

    public String getNNValue() {
        if (this.armiesToLeave > 50) {
            this.armiesToLeave = 50;
        }

        return String.valueOf((float)this.armiesToLeave / 50.0F);
    }

    public String getARFFHeader() {
        return arffAttributeStates.getARFFHeader(this.name);
    }

    protected String[] getARFFAttributeStates() {
        return arffAttributeStates.getStringArray();
    }

    static {
        arffAttributeStates = new AttributeStates(arff_states);
    }
}
