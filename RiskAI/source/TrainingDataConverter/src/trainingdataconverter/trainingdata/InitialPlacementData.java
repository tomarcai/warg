package trainingdataconverter.trainingdata;

import java.io.File;
import trainingdataconverter.trainingdata.gamedata.TerritoryOutputAttribute;
import trainingdataconverter.trainingdata.io.ARFFOutput;
import trainingdataconverter.trainingdata.io.NNTDOutput;

public class InitialPlacementData extends TrainingData {
    public InitialPlacementData() {
    }

    public static String makeARFFRelationName() {
        return "InitialPlacement";
    }

    private void writeARFFHeader(ARFFOutput output) {
        output.writeRelationName(makeARFFRelationName());
        output.writeLine("");
        output.writeHeader(this.gameDataArray[0].getInfluenceMap());
        output.writeHeader(this.gameDataArray[0].getTerritoryOutput());
        output.writeLine("");
        output.writeLine("@data");
    }

    private void writeARFFDataLine(ARFFOutput output, GameData data, String targetValue) {
        output.writeData(data.getInfluenceMap(), false);
        output.writeLine(targetValue);
    }

    private void writeARFFData(ARFFOutput output) {
        String[] targetValues = TerritoryOutputAttribute.getARFFAttributeStates().getStringArray();

        for(int dataIndex = 0; dataIndex < this.gameDataArray.length && this.gameDataArray[dataIndex] != null; ++dataIndex) {
            GameData data = this.gameDataArray[dataIndex];
            int winnerIndex = data.getWinningPlayer();
            int playerReference = data.getPlayerReference();

            for(int playerIndex = 0; playerIndex < data.getNumberOfPlayers(); ++playerIndex) {
                data.setCurrentPlayer(playerIndex);
                if (playerReference != winnerIndex) {
                    String loserValue = data.getTerritoryOutput().getARFFValue();

                    for(int valueIndex = 0; valueIndex < targetValues.length; ++valueIndex) {
                        String value = targetValues[valueIndex];
                        if (!value.equals(loserValue)) {
                            this.writeARFFDataLine(output, data, value);
                        }
                    }
                } else {
                    for(int i = 0; i < targetValues.length - 1; ++i) {
                        this.writeARFFDataLine(output, data, data.getTerritoryOutput().getARFFValue());
                    }
                }
            }
        }

    }

    public String makeARFFString() {
        ARFFOutput output = new ARFFOutput();
        this.writeARFFHeader(output);
        GameData data = this.gameDataArray[0];
        this.writeARFFDataLine(output, data, data.getTerritoryOutput().getARFFValue());
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
        int inputNodes = 168;
        int outputNodes = 1;
        if (initFiles) {
            output.writeHeader(inputNodes, 12, outputNodes);
            output.writeLine("");
            output.writeLine("@data");
        }

        for(int dataIndex = 0; dataIndex < this.gameDataArray.length; ++dataIndex) {
            String inputData = "";

            int territoryIndex;
            for(territoryIndex = 0; territoryIndex < 42; ++territoryIndex) {
                inputData = inputData + this.gameDataArray[dataIndex].getBoard()[territoryIndex].getNNValue() + ",";
            }

            for(territoryIndex = 0; territoryIndex < 6; ++territoryIndex) {
                this.gameDataArray[dataIndex].setCurrentPlayer(territoryIndex);

                for(int missionIndex = 0; missionIndex < 14; ++missionIndex) {
                    inputData = inputData + this.gameDataArray[dataIndex].getMissionEstimate(missionIndex).getNNValue() + ",";
                }
            }

            for(territoryIndex = 0; territoryIndex < 42; ++territoryIndex) {
                inputData = inputData + this.gameDataArray[dataIndex].getInfluenceMap(territoryIndex).getNNValue();
                if (territoryIndex < 42 - 1) {
                    inputData = inputData + ",";
                }
            }

            String targetData = this.gameDataArray[dataIndex].getTerritoryOutput().getNNValue();
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
