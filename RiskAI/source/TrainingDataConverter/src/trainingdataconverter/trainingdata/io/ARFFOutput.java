package trainingdataconverter.trainingdata.io;

import trainingdataconverter.trainingdata.gamedata.GameDataAttribute;

public class ARFFOutput extends DataOutput {
    public ARFFOutput() {
    }

    public void writeRelationName(String relationName) {
        this.writeLine("@relation " + relationName);
    }

    public void writeHeader(GameDataAttribute attribute) {
        this.writeLine(attribute.getARFFHeader());
    }

    public void writeHeader(GameDataAttribute[] attributes) {
        for(int i = 0; i < attributes.length; ++i) {
            this.writeHeader(attributes[i]);
        }

    }

    public void writeData(GameDataAttribute attribute, boolean endLine) {
        String text = attribute.getARFFValue();
        if (!endLine) {
            text = text + ",";
        }

        this.write(text, endLine);
    }

    public void writeData(GameDataAttribute[] attributes, boolean endLine) {
        String text = "";

        for(int i = 0; i < attributes.length; ++i) {
            text = text + attributes[i].getARFFValue();
            if (!endLine || i < attributes.length - 1) {
                text = text + ",";
            }
        }

        this.write(text, endLine);
    }
}
