package trainingdataconverter.trainingdata;

import java.io.File;
import progressstats.ProgressStats;
import trainingdataconverter.trainingdata.gamedata.InterestAttribute;
import trainingdataconverter.trainingdata.gamedata.MissionEstimateAttribute;
import trainingdataconverter.trainingdata.io.ARFFOutput;
import trainingdataconverter.trainingdata.io.NNTDOutput;

public class IG_MissionData extends TrainingData {
    public IG_MissionData() {
    }

    public static String makeARFFRelationName(int missionIndex) {
        return missionIndex >= 8 ? "Kill_Player" : TrainingData.MISSION_NAMES[missionIndex];
    }

    private void writeARFFHeader(ARFFOutput output, int missionIndex) {
        output.writeRelationName(makeARFFRelationName(missionIndex));
        output.writeLine("");
        output.writeHeader(this.gameDataArray[0].getPastAttribute().getInterest(missionIndex));
        output.writeHeader(this.gameDataArray[0].getMissionEstimate(missionIndex));
        output.writeLine("");
        output.writeLine("@data");
    }

    private void writeARFFDataLine(ARFFOutput output, GameData data, int missionIndex, String targetValue) {
        output.writeData(data.getPastAttribute().getInterest(missionIndex), false);
        output.writeLine(targetValue);
    }

    private void writeARFFData(ARFFOutput output, int missionIndex) {
        String[] targetValues = MissionEstimateAttribute.getARFFAttributeStates().getStringArray();
        int loserCount = 0;
        int winnerCount = 0;
        int loserData = 0;
        int winnerData = 0;

        for(int dataIndex = 0; dataIndex < this.gameDataArray.length && this.gameDataArray[dataIndex] != null; ++dataIndex) {
            GameData data = this.gameDataArray[dataIndex];
            int winnerIndex = data.getWinningPlayer();
            int playerReference = data.getPlayerReference();

            for(int playerIndex = 0; playerIndex < data.getNumberOfPlayers(); ++playerIndex) {
                data.setCurrentPlayer(playerIndex);
                if (playerReference != winnerIndex) {
                    String loserValue = data.getMissionEstimate(missionIndex).getARFFValue();

                    for(int valueIndex = 0; valueIndex < targetValues.length; ++valueIndex) {
                        String value = targetValues[valueIndex];
                        if (!value.equals(loserValue)) {
                            this.writeARFFDataLine(output, data, missionIndex, value);
                            ++loserData;
                        }
                    }

                    ++loserCount;
                } else {
                    for(int i = 0; i < targetValues.length - 1; ++i) {
                        this.writeARFFDataLine(output, data, missionIndex, data.getMissionEstimate(missionIndex).getARFFValue());
                        ++winnerData;
                    }

                    ++winnerCount;
                }
            }
        }

    }

    public String makeARFFString(int missionIndex) {
        ARFFOutput output = new ARFFOutput();
        GameData data = this.gameDataArray[0];
        InterestAttribute[] interestArray = null;
        this.writeARFFHeader(output, missionIndex);
        this.writeARFFDataLine(output, data, missionIndex, data.getMissionEstimate(missionIndex).getARFFValue());
        return output.getString();
    }

    public void saveARFFToDirectory(String directoryName, boolean initFiles) {
        if (this.gameDataArray == null) {
            throw new RuntimeException("Can not write ARFF when gameDataArray is null");
        } else {
            this.createDirectory(directoryName);

            for(int missionIndex = 0; missionIndex < 14; ++missionIndex) {
                String filename = directoryName + makeARFFRelationName(missionIndex) + ".arff";
                ARFFOutput output = new ARFFOutput();
                if (initFiles) {
                    this.writeARFFHeader(output, missionIndex);
                }

                this.writeARFFData(output, missionIndex);
                output.saveToFile(filename, initFiles);
                debug(ProgressStats.getText(missionIndex, 14) + " Training data saved to", filename);
            }

            debug("Converted and saved to directory", (new File(directoryName)).toString());
        }
    }

    public void saveNNTDToDirectory(String directoryName, boolean initFiles) {
        this.createDirectory(directoryName);
        String filename = directoryName + "data.nntd";
        NNTDOutput output = new NNTDOutput();
        int inputNodes = 48;
        int outputNodes = 14;
        if (initFiles) {
            output.writeHeader(inputNodes, 10, outputNodes);
            output.writeLine("");
            output.writeLine("@data");
        }

        for(int dataIndex = 0; dataIndex < this.gameDataArray.length; ++dataIndex) {
            for(int playerIndex = 0; playerIndex < this.gameDataArray[dataIndex].getNumberOfPlayers(); ++playerIndex) {
                this.gameDataArray[dataIndex].setCurrentPlayer(playerIndex);
                String inputData = this.gameDataArray[dataIndex].getPastAttribute().getNNValue();
                String targetData = "";

                for(int missionIndex = 0; missionIndex < 14; ++missionIndex) {
                    if (missionIndex == this.gameDataArray[dataIndex].getPlayerMissionIndex()) {
                        targetData = targetData + "1.0";
                    } else {
                        targetData = targetData + "0.0";
                    }

                    if (missionIndex < 14 - 1) {
                        targetData = targetData + ",";
                    }
                }

                output.writeData(true, inputData, targetData, inputNodes, outputNodes);
            }
        }

        output.saveToFile(filename, initFiles);
        debug("Converted and saved to directory", (new File(directoryName)).toString());
    }
}
