package trainingdataconverter.trainingdata.gamedata;

public abstract class GameDataAttribute {
    protected String value;
    protected String name;

    public GameDataAttribute() {
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    protected void setValue(String value) {
        this.value = value;
    }

    public abstract String getARFFValue();

    public abstract String getNNValue();

    public abstract String getARFFHeader();

    /** @deprecated */
    protected String makeARFFHeader(String[] arffAttributeStates) {
        String text = "";

        for(int i = 0; i < arffAttributeStates.length; ++i) {
            text = text + arffAttributeStates[i];
            if (i < arffAttributeStates.length - 1) {
                text = text + ", ";
            }
        }

        return "@attribute " + this.name + " " + "{" + text + "}";
    }
}
