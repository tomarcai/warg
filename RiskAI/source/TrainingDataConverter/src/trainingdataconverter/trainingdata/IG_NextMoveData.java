package trainingdataconverter.trainingdata;

import java.io.File;
import trainingdataconverter.trainingdata.io.NNTDOutput;

public class IG_NextMoveData extends TrainingData {
    public IG_NextMoveData() {
    }

    public void saveARFFToDirectory(String directoryName, boolean initFiles) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void saveNNTDToDirectory(String directoryName, boolean initFiles) {
        this.createDirectory(directoryName);
        String filename = directoryName + "data.nntd";
        NNTDOutput output = new NNTDOutput();
        int inputNodes = 330;
        int outputNodes = 42;
        if (initFiles) {
            output.writeHeader(inputNodes, 20, outputNodes);
            output.writeLine("");
            output.writeLine("@data");
        }

        for(int dataIndex = 0; dataIndex < this.gameDataArray.length; ++dataIndex) {
            String inputData = "";

            int playerIndex;
            int territoryIndex;
            for(playerIndex = 0; playerIndex < 6; ++playerIndex) {
                this.gameDataArray[dataIndex].setCurrentPlayer(playerIndex);

                for(territoryIndex = 0; territoryIndex < 5; ++territoryIndex) {
                    inputData = inputData + this.gameDataArray[dataIndex].getWinningEstimate(territoryIndex).getNNValue() + ",";
                }
            }

            for(playerIndex = 0; playerIndex < 6; ++playerIndex) {
                this.gameDataArray[dataIndex].setCurrentPlayer(playerIndex);

                for(territoryIndex = 0; territoryIndex < 14; ++territoryIndex) {
                    inputData = inputData + this.gameDataArray[dataIndex].getMissionEstimate(territoryIndex).getNNValue() + ",";
                }
            }

            for(playerIndex = 0; playerIndex < 6; ++playerIndex) {
                this.gameDataArray[dataIndex].setCurrentPlayer(playerIndex);

                for(territoryIndex = 0; territoryIndex < 5; ++territoryIndex) {
                    for(int continentIndex = 0; continentIndex < 6; ++continentIndex) {
                        inputData = inputData + this.gameDataArray[dataIndex].getContinentEstimate(continentIndex, territoryIndex).getNNValue() + ",";
                    }
                }
            }

            for(playerIndex = 0; playerIndex < 6; ++playerIndex) {
                this.gameDataArray[dataIndex].setCurrentPlayer(playerIndex);

                for(territoryIndex = 0; territoryIndex < 6; ++territoryIndex) {
                    inputData = inputData + this.gameDataArray[dataIndex].getOwnership(territoryIndex).getNNValue();
                    if (territoryIndex != 6 - 1 || playerIndex != 6 - 1) {
                        inputData = inputData + ",";
                    }
                }
            }

            String targetData = "";

            for(territoryIndex = 0; territoryIndex < 42; ++territoryIndex) {
                targetData = targetData + this.gameDataArray[dataIndex].getNextMoveAttribute()[territoryIndex].getNNValue();
                if (territoryIndex < 42 - 1) {
                    targetData = targetData + ",";
                }
            }

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
