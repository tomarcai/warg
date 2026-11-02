package trainingdataconverter.trainingdata;

import java.io.File;
import trainingdataconverter.trainingdata.io.NNTDOutput;

public class MasterPrioritizerData extends TrainingData {
    public MasterPrioritizerData() {
    }

    public void saveARFFToDirectory(String directoryName, boolean initFiles) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void saveNNTDToDirectory(String directoryName, boolean initFiles) {
        this.createDirectory(directoryName);
        String filename = directoryName + "data.nntd";
        NNTDOutput output = new NNTDOutput();
        int inputNodes = 372;
        int outputNodes = 38;
        if (initFiles) {
            output.writeHeader(inputNodes, 22, outputNodes);
            output.writeLine("");
            output.writeLine("@data");
        }

        for(int dataIndex = 0; dataIndex < this.gameDataArray.length; ++dataIndex) {
            String inputData = "";

            int playerIndex;
            for(playerIndex = 0; playerIndex < 42; ++playerIndex) {
                inputData = inputData + this.gameDataArray[dataIndex].getNextMoveAttribute()[playerIndex].getNNValue() + ",";
            }

            int goalIndex;
            for(playerIndex = 0; playerIndex < 6; ++playerIndex) {
                this.gameDataArray[dataIndex].setCurrentPlayer(playerIndex);

                for(goalIndex = 0; goalIndex < 5; ++goalIndex) {
                    inputData = inputData + this.gameDataArray[dataIndex].getWinningEstimate(goalIndex).getNNValue() + ",";
                }
            }

            for(playerIndex = 0; playerIndex < 6; ++playerIndex) {
                this.gameDataArray[dataIndex].setCurrentPlayer(playerIndex);

                for(goalIndex = 0; goalIndex < 14; ++goalIndex) {
                    inputData = inputData + this.gameDataArray[dataIndex].getMissionEstimate(goalIndex).getNNValue() + ",";
                }
            }

            for(playerIndex = 0; playerIndex < 6; ++playerIndex) {
                this.gameDataArray[dataIndex].setCurrentPlayer(playerIndex);

                for(goalIndex = 0; goalIndex < 5; ++goalIndex) {
                    for(int continentIndex = 0; continentIndex < 6; ++continentIndex) {
                        inputData = inputData + this.gameDataArray[dataIndex].getContinentEstimate(continentIndex, goalIndex).getNNValue() + ",";
                    }
                }
            }

            for(playerIndex = 0; playerIndex < 6; ++playerIndex) {
                this.gameDataArray[dataIndex].setCurrentPlayer(playerIndex);

                for(goalIndex = 0; goalIndex < 6; ++goalIndex) {
                    inputData = inputData + this.gameDataArray[dataIndex].getOwnership(goalIndex).getNNValue();
                    if (goalIndex != 6 - 1 || playerIndex != 6 - 1) {
                        inputData = inputData + ",";
                    }
                }
            }

            String targetData = "";

            for(goalIndex = 0; goalIndex < 38; ++goalIndex) {
                targetData = targetData + this.gameDataArray[dataIndex].getGoalDistribution()[goalIndex].getNNValue();
                if (goalIndex < 38 - 1) {
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
