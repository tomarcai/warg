package trainingdataconverter.trainingdata;

import java.io.File;
import progressstats.ProgressStats;
import trainingdataconverter.trainingdata.gamedata.ContinentEstimateAttribute;
import trainingdataconverter.trainingdata.gamedata.OpponentCardAttribute;
import trainingdataconverter.trainingdata.gamedata.TerritoryAttribute;
import trainingdataconverter.trainingdata.gamedata.WinningEstimateAttribute;
import trainingdataconverter.trainingdata.io.ARFFOutput;
import trainingdataconverter.trainingdata.io.BNOutput;
import trainingdataconverter.trainingdata.io.NNTDOutput;

public class IG_WinningData extends TrainingData {
    public IG_WinningData() {
    }

    public static String makeARFFRelationName(int roundNumber) {
        return "Round" + roundNumber;
    }

    private void writeARFFHeader(ARFFOutput output, int roundNumber) {
        TerritoryAttribute[] board = this.gameDataArray[0].getBoard();

        for(int territoryIndex = 0; territoryIndex < board.length; ++territoryIndex) {
            board[territoryIndex].useHalfIntervals(true);
        }

        OpponentCardAttribute[] opponentCards = this.gameDataArray[0].getOpponentCards();

        int continentIndex;
        for(continentIndex = 0; continentIndex < opponentCards.length; ++continentIndex) {
            opponentCards[continentIndex].useHalfIntervals(true);
        }

        for(continentIndex = 0; continentIndex < 6; ++continentIndex) {
            this.gameDataArray[0].getContinentEstimate(continentIndex, roundNumber).useHalfIntervals(true);
        }

        this.gameDataArray[0].getWinningEstimate(roundNumber).useHalfIntervals(true);
        output.writeRelationName(makeARFFRelationName(roundNumber));
        output.writeLine("");
        output.writeHeader(this.gameDataArray[0].getBoard());
        output.writeHeader(this.gameDataArray[0].getOpponentCards());
        output.writeHeader(this.gameDataArray[0].getMostLikelyMission());

        for(continentIndex = 0; continentIndex < 6; ++continentIndex) {
            output.writeHeader(this.gameDataArray[0].getContinentEstimate(continentIndex, roundNumber));
        }

        output.writeHeader(this.gameDataArray[0].getWinningEstimate(roundNumber));
        output.writeLine("");
        output.writeLine("@data");
    }

    private void writeARFFDataLine(ARFFOutput output, GameData data, int roundNumber, String targetValue) {
        TerritoryAttribute[] board = data.getBoard();

        for(int territoryIndex = 0; territoryIndex < board.length; ++territoryIndex) {
            board[territoryIndex].useHalfIntervals(true);
        }

        OpponentCardAttribute[] opponentCards = data.getOpponentCards();

        int continentIndex;
        for(continentIndex = 0; continentIndex < opponentCards.length; ++continentIndex) {
            opponentCards[continentIndex].useHalfIntervals(true);
        }

        for(continentIndex = 0; continentIndex < 6; ++continentIndex) {
            data.getContinentEstimate(continentIndex, roundNumber).useHalfIntervals(true);
        }

        data.getWinningEstimate(roundNumber).useHalfIntervals(true);
        output.writeData(data.getBoard(), false);
        output.writeData(data.getOpponentCards(), false);
        output.writeData(data.getMostLikelyMission(), false);

        for(continentIndex = 0; continentIndex < 6; ++continentIndex) {
            output.writeData(data.getContinentEstimate(continentIndex, roundNumber), false);
        }

        output.writeLine(targetValue);
    }

    private void writeARFFData(ARFFOutput output, int roundIndex) {
        String[] targetValues = WinningEstimateAttribute.getARFFAttributeStates().getStringArray();

        for(int dataIndex = 0; dataIndex < this.gameDataArray.length && this.gameDataArray[dataIndex] != null; ++dataIndex) {
            GameData data = this.gameDataArray[dataIndex];
            int winnerIndex = data.getWinningPlayer();
            int playerReference = data.getPlayerReference();

            for(int playerIndex = 0; playerIndex < data.getNumberOfPlayers(); ++playerIndex) {
                data.setCurrentPlayer(playerIndex);
                if (playerReference != winnerIndex) {
                    String loserValue = data.getWinningEstimate(roundIndex).getARFFValue();

                    for(int valueIndex = 0; valueIndex < targetValues.length; ++valueIndex) {
                        String value = targetValues[valueIndex];
                        if (!value.equals(loserValue)) {
                            this.writeARFFDataLine(output, data, roundIndex, value);
                        }
                    }
                } else {
                    for(int i = 0; i < targetValues.length - 1; ++i) {
                        this.writeARFFDataLine(output, data, roundIndex, data.getWinningEstimate(roundIndex).getARFFValue());
                    }
                }
            }
        }

    }

    public String makeARFFString(int roundIndex) {
        ARFFOutput output = new ARFFOutput();
        this.writeARFFHeader(output, roundIndex);
        GameData data = this.gameDataArray[0];
        this.writeARFFDataLine(output, data, roundIndex, data.getWinningEstimate(roundIndex).getARFFValue());
        return output.getString();
    }

    public void saveARFFToDirectory(String directoryName, boolean initFiles) {
        if (this.gameDataArray == null) {
            throw new RuntimeException("Can not write ARFF when gameDataArray is null");
        } else {
            this.createDirectory(directoryName);

            for(int roundIndex = 0; roundIndex < 5; ++roundIndex) {
                String filename = directoryName + makeARFFRelationName(roundIndex) + ".arff";
                ARFFOutput output = new ARFFOutput();
                if (initFiles) {
                    this.writeARFFHeader(output, roundIndex);
                }

                this.writeARFFData(output, roundIndex);
                output.saveToFile(filename, initFiles);
                debug(ProgressStats.getText(roundIndex, 5) + " Training data saved to", filename);
            }

            debug("Converted and saved to directory", (new File(directoryName)).toString());
        }
    }

    public void saveNNTDToDirectory(String directoryName, boolean initFiles) {
        this.createDirectory(directoryName);
        String filename = directoryName + "data.nntd";
        NNTDOutput output = new NNTDOutput();
        int inputNodes = 70;
        int outputNodes = 1;
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

                    int continentIndex;
                    for(continentIndex = 0; continentIndex < opc.length; ++continentIndex) {
                        inputData = inputData + opc[continentIndex].getNNValue() + ",";
                    }

                    for(continentIndex = 0; continentIndex < 14; ++continentIndex) {
                        inputData = inputData + this.gameDataArray[dataIndex].getMissionEstimate(continentIndex).getNNValue() + ",";
                    }

                    for(continentIndex = 0; continentIndex < 6; ++continentIndex) {
                        inputData = inputData + this.gameDataArray[dataIndex].getContinentEstimate(continentIndex, roundIndex).getNNValue() + ",";
                    }

                    inputData = inputData + this.gameDataArray[dataIndex].getBeginningOfTurn().getNNValue();
                    String targetData = this.gameDataArray[dataIndex].getWinningEstimate(roundIndex).getNNValue();
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
        String[] targetValues = WinningEstimateAttribute.getARFFAttributeStates().getStringArray();
        this.createDirectory(directoryName);
        String filename = directoryName + "bn.dat";
        BNOutput output = new BNOutput();
        int header_size = 32;
        if (initFiles) {
            output.writeHeader("AnyContinent2,Conq24_Conq18,Kill5_Kill6,Kill3_Kill4,Kill1_Kill2,Observed_Mission,SA_EU_ANY_OR_EU_AU_ANY,NA_AU_OR_NA_AF,AS_SA_OR_AS_AF,Mission,Conq18,Conq24,Kill_Player1,Kill_Player2,Kill_Player3,kill_Player4,Kill_player5,Kill_Player6,EU_AU_ANY,EU_SA_ANY,NA_AU,NA_AF,AS_AF,AS_SA,AnyContinent1,NorthAmerica,SouthAmerica,Europe,Africa,Asia,Australia,CloseToWin", header_size);
        }

        for(int dataIndex = 0; dataIndex < this.gameDataArray.length; ++dataIndex) {
            this.gameDataArray[dataIndex].initBnWinningValueCalc();
            int winnerIndex = this.gameDataArray[dataIndex].getWinningPlayer();
            int playerReference = this.gameDataArray[dataIndex].getPlayerReference();

            for(int playerIndex = 0; playerIndex < this.gameDataArray[dataIndex].getNumberOfPlayers(); ++playerIndex) {
                this.gameDataArray[dataIndex].setCurrentPlayer(playerIndex);

                for(int roundIndex = 0; roundIndex < 5; ++roundIndex) {
                    String standard = this.convertToBnState(ContinentEstimateAttribute.getStateFromIndex(this.gameDataArray[dataIndex].getBnWinningValueCalc().calcCurrentPlayerMostLikelyAnyContinentStateIndex(playerIndex, roundIndex, 1))) + ",";
                    standard = standard + "N/A,N/A,N/A,N/A,";
                    standard = standard + this.gameDataArray[dataIndex].getBnWinningValueCalc().calcCurrentPlayerMissionState(playerIndex) + ",";
                    standard = standard + "N/A,N/A,N/A,";
                    standard = standard + this.gameDataArray[dataIndex].getBnWinningValueCalc().getStateFromMissionName(TrainingData.MISSION_NAMES[this.gameDataArray[dataIndex].getPlayerMissionIndex()]) + ",";
                    standard = standard + this.convertToBnState(this.gameDataArray[dataIndex].getBnWinningValueCalc().calcCurrentPlayerCloseToConq18State(roundIndex)) + ",";
                    standard = standard + this.convertToBnState(this.gameDataArray[dataIndex].getBnWinningValueCalc().calcCurrentPlayerCloseToConq24State(roundIndex)) + ",";

                    int continentIndex;
                    for(continentIndex = 0; continentIndex < 6; ++continentIndex) {
                        if (continentIndex < this.gameDataArray[dataIndex].getNumberOfPlayers()) {
                            standard = standard + this.convertToBnState(this.gameDataArray[dataIndex].getBnWinningValueCalc().calcCurrentPlayerCloseToKillPlayerState(continentIndex, roundIndex)) + ",";
                        } else {
                            standard = standard + "N/A,";
                        }
                    }

                    standard = standard + "N/A,N/A,N/A,N/A,N/A,N/A,";
                    standard = standard + this.convertToBnState(ContinentEstimateAttribute.getStateFromIndex(this.gameDataArray[dataIndex].getBnWinningValueCalc().calcCurrentPlayerMostLikelyAnyContinentStateIndex(playerIndex, roundIndex, 3))) + ",";

                    for(continentIndex = 0; continentIndex < 6; ++continentIndex) {
                        standard = standard + this.convertToBnState(this.gameDataArray[dataIndex].getBnWinningValueCalc().calcCurrentPlayerCloseToContinentState(roundIndex, continentIndex, playerIndex)) + ",";
                    }

                    String value = this.gameDataArray[dataIndex].getWinningEstimate(roundIndex).getBNValue();
                    String data = standard + this.convertToBnState(value);
                    output.writeData(data, header_size);
                }
            }
        }

        output.saveToFile(filename, initFiles);
        debug("Converted and saved to directory", (new File(directoryName)).toString());
    }
}
