package trainingdataconverter.trainingdata.gamedata;

public class InfluenceAttribute extends GameDataAttribute {
    private static String[] arffStatesHalf = new String[]{"0.0;0.0", "0.0;1.0", "1.0;2.0", "2.0;3.0", "3.0;5.0", "5.0;6.0", "6.0;7.0", "7.0;10.0", "10.0;20.0", "20.0;20.0"};
    private static AttributeStates arffAttributeStates;
    private int influence;

    public InfluenceAttribute(String name, int influence) {
        this.name = "Influence_" + name;
        if (influence < 0) {
            throw new UnsupportedOperationException("Influence must at least be 0! (influence = " + influence + ")");
        } else {
            this.influence = influence;
            this.setValue(String.valueOf(influence));
        }
    }

    public String getARFFValue() {
        return arffAttributeStates.getState((float)this.influence);
    }

    public String getNNValue() {
        if (this.influence > 20) {
            this.influence = 20;
        }

        return String.valueOf((float)this.influence / 20.0F);
    }

    public String getARFFHeader() {
        return arffAttributeStates.getARFFHeader(this.name);
    }

    protected AttributeStates getARFFAttributeStates() {
        return arffAttributeStates;
    }

    public String getName() {
        return this.name;
    }

    static {
        arffAttributeStates = new AttributeStates(arffStatesHalf);
    }
}
