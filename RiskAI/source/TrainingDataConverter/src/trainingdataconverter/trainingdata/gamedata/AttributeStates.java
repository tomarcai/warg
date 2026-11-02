package trainingdataconverter.trainingdata.gamedata;

public class AttributeStates {
    private String[] states;

    public AttributeStates(String[] states) {
        this.states = states;
    }

    public String getState(float value) {
        for(int i = 0; i < this.states.length - 1; ++i) {
            String[] split = this.states[i].split(";");
            if (i == 0 && value < Float.parseFloat(split[0])) {
                return this.states[i];
            }

            if (value >= Float.parseFloat(split[0]) && value < Float.parseFloat(split[1])) {
                return this.states[i];
            }

            if (split[0].equals(split[1]) && value == Float.parseFloat(split[0])) {
                return this.states[i];
            }
        }

        return this.states[this.states.length - 1];
    }

    public int getStateIndex(float value) {
        for(int i = 0; i < this.states.length - 1; ++i) {
            String[] split = this.states[i].split(";");
            if (i == 0 && value < Float.parseFloat(split[0])) {
                return i;
            }

            if (value >= Float.parseFloat(split[0]) && value < Float.parseFloat(split[1])) {
                return i;
            }

            if (split[0].equals(split[1]) && value == Float.parseFloat(split[0])) {
                return i;
            }
        }

        return this.states.length - 1;
    }

    public String getARFFHeader(String name) {
        String text = "";

        for(int i = 0; i < this.states.length; ++i) {
            text = text + this.states[i];
            if (i < this.states.length - 1) {
                text = text + ", ";
            }
        }

        return "@attribute " + name + " " + "{" + text + "}";
    }

    public float getValueFromIndex(int index) {
        String[] split = this.states[index].split(";");
        return (Float.parseFloat(split[0]) + Float.parseFloat(split[1])) / 2.0F;
    }

    public String[] getStringArray() {
        return this.states;
    }

    public String getState(boolean bool_value) {
        return bool_value ? this.states[1] : this.states[0];
    }
}
