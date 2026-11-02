package trainingdataconverter.trainingdata;

import java.io.File;
import trainingdataconverter.trainingdata.gamedata.CostAttribute;
import trainingdataconverter.trainingdata.io.ARFFOutput;
import trainingdataconverter.trainingdata.io.NNTDOutput;

public class RP_De_CostData extends TrainingData {
    public RP_De_CostData() {
    }

    public static String makeARFFRelationName() {
        return "RP_De_Cost";
    }

    private void writeARFFHeader(ARFFOutput output) {
        output.writeRelationName(makeARFFRelationName());
        output.writeLine("");
        output.writeHeader(this.gameDataArray[0].getBoard());
        output.writeHeader(this.gameDataArray[0].getAttackerArmiesLeft());
        output.writeHeader(this.gameDataArray[0].getMergedPlanTerritory());
        output.writeHeader(this.gameDataArray[0].getEstimatedCost());
        output.writeLine("");
        output.writeLine("@data");
    }

    private void writeARFFDataLine(ARFFOutput output, GameData data, String targetValue) {
        output.writeData(data.getBoard(), false);
        output.writeData(data.getAttackerArmiesLeft(), false);
        output.writeData(data.getMergedPlanTerritory(), false);
        output.writeLine(targetValue);
    }

    private void writeARFFData(ARFFOutput output) {
        String[] targetValues = CostAttribute.getARFFAttributeStates().getStringArray();

        for(int dataIndex = 0; dataIndex < this.gameDataArray.length && this.gameDataArray[dataIndex] != null; ++dataIndex) {
            GameData data = this.gameDataArray[dataIndex];
            int winnerIndex = data.getWinningPlayer();
            int playerReference = data.getPlayerReference();
            if (playerReference != winnerIndex) {
                String loserValue = data.getEstimatedCost().getARFFValue();

                for(int valueIndex = 0; valueIndex < targetValues.length; ++valueIndex) {
                    String value = targetValues[valueIndex];
                    if (!value.equals(loserValue)) {
                        this.writeARFFDataLine(output, data, value);
                    }
                }
            } else {
                for(int i = 0; i < targetValues.length - 1; ++i) {
                    this.writeARFFDataLine(output, data, data.getEstimatedCost().getARFFValue());
                }
            }
        }

    }

    public String makeARFFString() {
        ARFFOutput output = new ARFFOutput();
        this.writeARFFHeader(output);
        GameData data = this.gameDataArray[0];
        this.writeARFFDataLine(output, data, data.getEstimatedCost().getARFFValue());
        return output.getString();
    }

    public void saveARFFToDirectory(String directoryName, boolean initFiles) {
        if (this.gameDataArray == null) {
            throw new RuntimeException("Can not write ARFF when gameDataArray is null");
        } else {
            this.createDirectory(directoryName);
            String filename = directoryName + makeARFFRelationName() + ".arff";
            ARFFOutput output = new ARFFOutput();
            if (initFiles) {
                this.writeARFFHeader(output);
            }

            this.writeARFFData(output);
            output.saveToFile(filename, initFiles);
            debug("Converted and saved to directory", (new File(directoryName)).toString());
        }
    }

    public void saveNNTDToDirectory(String directoryName, boolean initFiles) {
        this.createDirectory(directoryName);
        String filename = directoryName + "data.nntd";
        NNTDOutput output = new NNTDOutput();
        int inputNodes = 44;
        int outputNodes = 1;
        if (initFiles) {
            output.writeHeader(inputNodes, 8, outputNodes);
            output.writeLine("");
            output.writeLine("@data");
        }

        for(int dataIndex = 0; dataIndex < this.gameDataArray.length; ++dataIndex) {
            String inputData = this.gameDataArray[dataIndex].getMergedPlanTerritory().getNNValue() + ",";

            for(int territoryIndex = 0; territoryIndex < 42; ++territoryIndex) {
                inputData = inputData + this.gameDataArray[dataIndex].getBoard()[territoryIndex].getNNValue() + ",";
            }

            inputData = inputData + this.gameDataArray[dataIndex].getAttackerArmiesLeft().getNNValue();
            String targetData = this.gameDataArray[dataIndex].getEstimatedCost().getNNValue();
            if (this.gameDataArray[dataIndex].getWinningPlayer() == this.gameDataArray[dataIndex].getPlayerReference()) {
                output.writeData(true, inputData, targetData, inputNodes, outputNodes);
            } else {
                output.writeData(false, inputData, targetData, inputNodes, outputNodes);
            }
        }

        output.saveToFile(filename, initFiles);
        debug("Converted and saved to directory", (new File(directoryName)).toString());
    }
}
