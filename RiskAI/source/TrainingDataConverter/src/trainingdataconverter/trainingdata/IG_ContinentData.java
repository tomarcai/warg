package trainingdataconverter.trainingdata;

import java.io.File;
import progressstats.ProgressStats;
import trainingdataconverter.trainingdata.gamedata.ContinentEstimateAttribute;
import trainingdataconverter.trainingdata.gamedata.OpponentCardAttribute;
import trainingdataconverter.trainingdata.io.ARFFOutput;
import trainingdataconverter.trainingdata.io.BNOutput;
import trainingdataconverter.trainingdata.io.NNTDOutput;

public class IG_ContinentData extends TrainingData {
    public IG_ContinentData() {
    }

    public static String makeARFFRelationName(int continentIndex, int roundIndex) {
        return CONTINENT_NAMES[continentIndex] + "_round" + roundIndex;
    }

    private void writeARFFHeader(ARFFOutput output, int continentIndex, int roundIndex) {
        output.writeRelationName(makeARFFRelationName(continentIndex, roundIndex));
        output.writeLine("");
        output.writeHeader(this.gameDataArray[0].getBoard());
        output.writeHeader(this.gameDataArray[0].getOpponentCards());
        output.writeHeader(this.gameDataArray[0].getContinentEstimate(0, 0));
        output.writeLine("");
        output.writeLine("@data");
    }

    private void writeARFFDataLine(ARFFOutput output, GameData data, String targetValue) {
        output.writeData(data.getBoard(), false);
        output.writeData(data.getOpponentCards(), false);
        output.writeLine(targetValue);
    }

    private void writeARFFData(ARFFOutput output, int continentIndex, int roundIndex) {
        String[] targetValues = ContinentEstimateAttribute.getARFFAttributeStates().getStringArray();
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
                    String loserValue = data.getContinentEstimate(continentIndex, roundIndex).getARFFValue();

                    for(int valueIndex = 0; valueIndex < targetValues.length; ++valueIndex) {
                        String value = targetValues[valueIndex];
                        if (!value.equals(loserValue)) {
                            this.writeARFFDataLine(output, data, value);
                            ++loserData;
                        }
                    }

                    ++loserCount;
                } else {
                    for(int i = 0; i < targetValues.length - 1; ++i) {
                        this.writeARFFDataLine(output, data, data.getContinentEstimate(continentIndex, roundIndex).getARFFValue());
                        ++winnerData;
                    }

                    ++winnerCount;
                }
            }
        }

    }

    public String makeARFFString(int continentIndex, int roundIndex) {
        ARFFOutput output = new ARFFOutput();
        this.writeARFFHeader(output, continentIndex, roundIndex);
        GameData data = this.gameDataArray[0];
        this.writeARFFDataLine(output, data, data.getContinentEstimate(continentIndex, roundIndex).getARFFValue());
        return output.getString();
    }

    public void saveARFFToDirectory(String directoryName, boolean initFiles) {
        if (this.gameDataArray == null) {
            throw new RuntimeException("Can not write ARFF when gameDataArray is null");
        } else {
            this.createDirectory(directoryName);

            for(int continentIndex = 0; continentIndex < 6; ++continentIndex) {
                for(int roundIndex = 0; roundIndex < 5; ++roundIndex) {
                    String filename = directoryName + makeARFFRelationName(continentIndex, roundIndex) + ".arff";
                    ARFFOutput output = new ARFFOutput();
                    if (initFiles) {
                        this.writeARFFHeader(output, continentIndex, roundIndex);
                    }

                    this.writeARFFData(output, continentIndex, roundIndex);
                    output.saveToFile(filename, initFiles);
                    debug(ProgressStats.getText(roundIndex + continentIndex * 5, 6 * 5) + " Training data saved to", filename);
                }
            }

            debug("Converted and saved to directory", (new File(directoryName)).toString());
        }
    }

    public void saveNNTDToDirectory(String directoryName, boolean initFiles) {
        this.createDirectory(directoryName);
        String filename = directoryName + "data.nntd";
        NNTDOutput output = new NNTDOutput();
        int inputNodes = 50;
        int outputNodes = 6;
        if (initFiles) {
            output.writeHeader(inputNodes, 10, outputNodes);
            output.writeLine("");
            output.writeLine("@data");
        }

        for(int dataIndex = 0; dataIndex < this.gameDataArray.length; ++dataIndex) {
            for(int playerIndex = 0; playerIndex < this.gameDataArray[dataIndex].getNumberOfPlayers(); ++playerIndex) {
                this.gameDataArray[dataIndex].setCurrentPlayer(playerIndex);

                for(int roundIndex = 0; roundIndex < 5; ++roundIndex) {
                    String inputData = (float)roundIndex / 4.0F + ",";

                    for(int territoryIndex = 0; territoryIndex < 42; ++territoryIndex) {
                        inputData = inputData + this.gameDataArray[dataIndex].getBoard()[territoryIndex].getNNValue() + ",";
                    }

                    OpponentCardAttribute[] opc = this.gameDataArray[dataIndex].getOpponentCards();

                    for(int opponentCardIndex = 0; opponentCardIndex < opc.length; ++opponentCardIndex) {
                        inputData = inputData + opc[opponentCardIndex].getNNValue() + ",";
                    }

                    inputData = inputData + this.gameDataArray[dataIndex].getBeginningOfTurn().getNNValue();
                    String targetData = "";

                    for(int continentIndex = 0; continentIndex < 6; ++continentIndex) {
                        targetData = targetData + this.gameDataArray[dataIndex].getContinentEstimate(continentIndex, roundIndex).getNNValue();
                        if (continentIndex != 6 - 1) {
                            targetData = targetData + ",";
                        }
                    }

                    if (this.gameDataArray[dataIndex].getWinningPlayer() == this.gameDataArray[dataIndex].getPlayerReference()) {
                        output.writeData(true, inputData, targetData, inputNodes, outputNodes);
                    } else {
                        output.writeData(false, inputData, targetData, inputNodes, outputNodes);
                    }
                }
            }
        }

        output.saveToFile(filename, initFiles);
        debug("Converted and saved to directory", (new File(directoryName)).toString());
    }

    public void saveBNToDirectory(String directoryName, boolean initFiles) {
        String[] targetValues = ContinentEstimateAttribute.getARFFAttributeStates().getStringArray();
        this.createDirectory(directoryName);
        String filename = directoryName + "bn.dat";
        BNOutput output = new BNOutput();
        int header_size = 7;
        if (initFiles) {
            output.writeHeader("Armies_X_Cont,Armies_Opp_Cont,Armies_Opp_Close,Armies_X_Close,Armies_Left_Cont,Armies_Left_Close,Close_To_Own", header_size);
        }

        for(int dataIndex = 0; dataIndex < this.gameDataArray.length; ++dataIndex) {
            this.gameDataArray[dataIndex].initBnContinentValueCalc();
            int winnerIndex = this.gameDataArray[dataIndex].getWinningPlayer();
            int playerReference = this.gameDataArray[dataIndex].getPlayerReference();

            for(int playerIndex = 0; playerIndex < this.gameDataArray[dataIndex].getNumberOfPlayers(); ++playerIndex) {
                this.gameDataArray[dataIndex].setCurrentPlayer(playerIndex);

                for(int continentIndex = 0; continentIndex < 6; ++continentIndex) {
                    for(int roundIndex = 0; roundIndex < 5; ++roundIndex) {
                        String standard = "";
                        standard = standard + this.convertToBnState(this.gameDataArray[dataIndex].getBnContinentValueCalc().getState(this.gameDataArray[dataIndex].getBnContinentValueCalc().calcCurrentPlayerArmiesInContinentStateIndex(continentIndex, roundIndex))) + ",";
                        standard = standard + this.convertToBnState(this.gameDataArray[dataIndex].getBnContinentValueCalc().getState(this.gameDataArray[dataIndex].getBnContinentValueCalc().calcOpponentPlayersArmiesInContinentStateIndex(continentIndex, roundIndex))) + ",";
                        standard = standard + this.convertToBnState(this.gameDataArray[dataIndex].getBnContinentValueCalc().getState(this.gameDataArray[dataIndex].getBnContinentValueCalc().calcOpponentPlayersArmiesOutsideContinentStateIndex(continentIndex, roundIndex))) + ",";
                        standard = standard + this.convertToBnState(this.gameDataArray[dataIndex].getBnContinentValueCalc().getState(this.gameDataArray[dataIndex].getBnContinentValueCalc().calcCurrentPlayerArmiesOutsideContinentStateIndex(continentIndex, roundIndex))) + ",";
                        standard = standard + this.convertToBnState(this.gameDataArray[dataIndex].getBnContinentValueCalc().calcArmiesLeftInContinentState()) + ",";
                        standard = standard + this.convertToBnState(this.gameDataArray[dataIndex].getBnContinentValueCalc().calcArmiesLeftClostToContinentState()) + ",";
                        String value = this.gameDataArray[dataIndex].getContinentEstimate(continentIndex, roundIndex).getBNValue();
                        String data = standard + this.convertToBnState(value);
                        output.writeData(data, header_size);
                    }
                }
            }
        }

        output.saveToFile(filename, initFiles);
        debug("Converted and saved to directory", (new File(directoryName)).toString());
    }
}
