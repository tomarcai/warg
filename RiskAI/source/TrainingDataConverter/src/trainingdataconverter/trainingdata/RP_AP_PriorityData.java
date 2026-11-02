package trainingdataconverter.trainingdata;

import java.io.File;
import trainingdataconverter.trainingdata.gamedata.PriorityAttribute;
import trainingdataconverter.trainingdata.io.ARFFOutput;
import trainingdataconverter.trainingdata.io.NNTDOutput;

public class RP_AP_PriorityData extends TrainingData {
    public RP_AP_PriorityData() {
    }

    public static String makeARFFRelationName() {
        return "RP_AP_Priority";
    }

    private void writeARFFHeader(ARFFOutput output) {
        output.writeRelationName(makeARFFRelationName());
        output.writeLine("");
        output.writeHeader(this.gameDataArray[0].getAttackPlan());
        output.writeHeader(this.gameDataArray[0].getBoard());
        output.writeHeader(this.gameDataArray[0].getGoalDistribution());
        output.writeHeader(this.gameDataArray[0].getPriority());
        output.writeLine("");
        output.writeLine("@data");
    }

    private void writeARFFDataLine(ARFFOutput output, GameData data, String targetValue) {
        output.writeData(data.getAttackPlan(), false);
        output.writeData(data.getBoard(), false);
        output.writeData(data.getGoalDistribution(), false);
        output.writeLine(targetValue);
    }

    private void writeARFFData(ARFFOutput output) {
        String[] targetValues = PriorityAttribute.getARFFAttributeStates().getStringArray();

        for(int dataIndex = 0; dataIndex < this.gameDataArray.length && this.gameDataArray[dataIndex] != null; ++dataIndex) {
            GameData data = this.gameDataArray[dataIndex];
            int winnerIndex = data.getWinningPlayer();
            int playerReference = data.getPlayerReference();
            if (playerReference != winnerIndex) {
                String loserValue = data.getPriority().getARFFValue();

                for(int valueIndex = 0; valueIndex < targetValues.length; ++valueIndex) {
                    String value = targetValues[valueIndex];
                    if (!value.equals(loserValue)) {
                        this.writeARFFDataLine(output, data, value);
                    }
                }
            } else {
                for(int i = 0; i < targetValues.length - 1; ++i) {
                    this.writeARFFDataLine(output, data, data.getPriority().getARFFValue());
                }
            }
        }

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
        int inputNodes = 122;
        int outputNodes = 1;
        if (initFiles) {
            output.writeHeader(inputNodes, 12, outputNodes);
            output.writeLine("");
            output.writeLine("@data");
        }

        for(int dataIndex = 0; dataIndex < this.gameDataArray.length; ++dataIndex) {
            String inputData = "";

            int goalIndex;
            for(goalIndex = 0; goalIndex < 42; ++goalIndex) {
                if (this.gameDataArray[dataIndex].getAttackPlan()[goalIndex] == null) {
                    inputData = inputData + "0.0,";
                } else {
                    inputData = inputData + this.gameDataArray[dataIndex].getAttackPlan()[goalIndex].getNNValue() + ",";
                }
            }

            for(goalIndex = 0; goalIndex < 42; ++goalIndex) {
                inputData = inputData + this.gameDataArray[dataIndex].getBoard()[goalIndex].getNNValue() + ",";
            }

            for(goalIndex = 0; goalIndex < 38; ++goalIndex) {
                inputData = inputData + this.gameDataArray[dataIndex].getGoalDistribution()[goalIndex].getNNValue();
                if (goalIndex < 38 - 1) {
                    inputData = inputData + ",";
                }
            }

            String targetData = this.gameDataArray[dataIndex].getPriority().getNNValue();
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
