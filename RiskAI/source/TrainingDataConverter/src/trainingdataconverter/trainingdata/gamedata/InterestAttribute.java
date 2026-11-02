//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package trainingdataconverter.trainingdata.gamedata;

public class InterestAttribute extends GameDataAttribute {
    private static String[] nnStates = new String[]{"0.0;0.0", "0.0;1.5167602E-4", "1.5167602E-4;0.0017969452", "0.0017969452;0.007936508", "0.007936508;0.011627907", "0.011627907;0.015151516", "0.015151516;0.018867925", "0.018867925;0.022727273", "0.022727273;0.027027028", "0.027027028;0.032258064", "0.032258064;0.038961038", "0.038961038;0.04761905", "0.04761905;0.055555556", "0.055555556;0.06363636", "0.06363636;0.083333336", "0.083333336;0.11111111", "0.11111111;0.16", "0.16;0.25", "0.25;0.35714287", "0.35714287;0.5833333", "0.5833333;1.0", "1.0;1.0"};
    private static String[] arffStatesHalf = new String[]{"0.0;0.0", "0.0;7.770008E-4", "7.770008E-4;0.0125", "0.0125;0.022222223", "0.022222223;0.03529412", "0.03529412;0.055555556", "0.055555556;0.10810811", "0.10810811;0.29166666", "0.29166666;1.0", "1.0;1.0"};
    private static String[] arffStatesQuarter = new String[]{"0.0;0.0", "0.0;0.014084507", "0.014084507;0.055555556", "0.055555556;1.0", "1.0;1.0"};
    private static AttributeStates arffAttributeStates;
    private float interest;

    public InterestAttribute(String name, float interest) {
        this.name = "Interest_" + name;
        if (!(interest < 0.0F) && !(interest > 1.0F)) {
            this.interest = interest;
            this.setValue(String.valueOf(interest));
        } else {
            throw new UnsupportedOperationException("Interest must be greater than or equal to 0 and less than or equal to 1 (interest = " + interest + ")");
        }
    }

    public String getARFFValue() {
        return arffAttributeStates.getState(this.interest);
    }

    public String getNNValue() {
        throw new RuntimeException("Not yet implemented.");
    }

    public String getARFFHeader() {
        return arffAttributeStates.getARFFHeader(this.name);
    }

    public static float getARFFValueFromState(int stateIndex) {
        return arffAttributeStates.getValueFromIndex(stateIndex);
    }

    public static float getNNValueFromNNOutput(float output) {
        return arffAttributeStates.getValueFromIndex(Math.round(output * (float)(nnStates.length - 1)));
    }

    static {
        arffAttributeStates = new AttributeStates(arffStatesHalf);
    }
}
