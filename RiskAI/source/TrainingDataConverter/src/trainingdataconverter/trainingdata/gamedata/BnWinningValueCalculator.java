package trainingdataconverter.trainingdata.gamedata;

import java.util.Vector;
import trainingdataconverter.trainingdata.TrainingData;

public class BnWinningValueCalculator extends GameDataAttribute {
    private String[] missionStateList = new String[]{"AS_SA", "AF_SA", "NA_AF", "EU_AU_ANY", "NA_AU", "EU_SA_ANY", "Player1", "Player2", "Player3", "Player4", "Player5", "Player6", "24", "18"};
    private static String[] subGoalStates = new String[]{"0.00;0.00", "0.00;0.05", "0.05;0.10", "0.10;0.15", "0.15;0.20", "0.20;0.25", "0.25;0.30", "0.30;0.35", "0.35;0.40", "0.45;0.50", "0.50;0.55", "0.55;0.60", "0.60;0.65", "0.65;0.70", "0.75;0.80", "0.80;0.85", "0.85;0.90", "0.90;0.95", "0.95;1.00", "1.00;1.00"};
    private static AttributeStates subGoals;
    private TerritoryAttribute[] board;
    private ContinentEstimateAttribute[][][] continentEstimate;
    private MissionEstimateAttribute[][] missionEstimates;
    private OpponentCardAttribute[] opponentCards;
    private BeginningOfTurnAttribute beginningOfTurn;
    private String[] playerColors;
    private int playerCount;

    public BnWinningValueCalculator(TerritoryAttribute[] board, ContinentEstimateAttribute[][][] continentEstimate, OpponentCardAttribute[] opponentCards, BeginningOfTurnAttribute beginningOfTurn, String[] playerColors, MissionEstimateAttribute[][] missionEstimates) {
        this.name = "BnWinningValueCalculator";
        if (board[0] == null) {
            throw new UnsupportedOperationException("Board should have been set.");
        } else {
            this.board = board;
            this.continentEstimate = continentEstimate;
            this.opponentCards = opponentCards;
            this.beginningOfTurn = beginningOfTurn;
            this.playerColors = playerColors;
            this.playerCount = 0;
            this.missionEstimates = missionEstimates;

            for(int i = 0; i < playerColors.length; ++i) {
                if (!playerColors.equals("")) {
                    ++this.playerCount;
                }
            }

        }
    }

    public String getARFFValue() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public String getNNValue() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public String getARFFHeader() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public int calcCurrentPlayerCloseToConq24StateIndex(int roundIndex) {
        Vector territoriesOwned = new Vector();

        for(int i = 0; i < this.board.length; ++i) {
            if (this.board[i].getOwner().equals(this.board[0].getCurrentPlayer())) {
                territoriesOwned.add(this.board[i].getName());
            }
        }

        if (territoriesOwned.size() > 0) {
            return subGoals.getStateIndex(24.0F / (float)territoriesOwned.size());
        } else {
            return 0;
        }
    }

    public String calcCurrentPlayerCloseToConq24State(int roundIndex) {
        return subGoalStates[this.calcCurrentPlayerCloseToConq24StateIndex(roundIndex)];
    }

    public int calcCurrentPlayerCloseToConq18StateIndex(int roundIndex) {
        int territoriesOwnedWithMoreThanTwo = 0;
        int numberofTerritoriesWithLessThanTwo = 0;

        int i;
        for(i = 0; i < this.board.length; ++i) {
            if (this.board[i].getOwner().equals(this.board[0].getCurrentPlayer())) {
                if (this.board[i].getArmies() >= 2) {
                    ++territoriesOwnedWithMoreThanTwo;
                } else {
                    ++numberofTerritoriesWithLessThanTwo;
                }
            }
        }

        i = this.calcReinforcementsForPlayer(roundIndex, this.board[0].getCurrentPlayer());

        for(int k = 0; k < i; ++k) {
            ++territoriesOwnedWithMoreThanTwo;
        }

        return territoriesOwnedWithMoreThanTwo > 0 ? subGoals.getStateIndex(18.0F / (float)territoriesOwnedWithMoreThanTwo) : 0;
    }

    public String calcCurrentPlayerCloseToConq18State(int roundIndex) {
        return subGoalStates[this.calcCurrentPlayerCloseToConq18StateIndex(roundIndex)];
    }

    public int calcCurrentPlayerCloseToKillPlayerStateIndex(int opn, int roundIndex) {
        int returnValue = 0;
        String playerName = this.board[0].getCurrentPlayer();
        if (!this.playerColors[opn].equals(this.board[0].getCurrentPlayer())) {
            for(int pn = 0; pn < this.playerColors.length; ++pn) {
                int opponentPlayerArmyCount = 0;
                int currentPlayerArmyCount = 0;
                String opponentPlayer = this.playerColors[opn];

                for(int i = 0; i < this.board.length; ++i) {
                    if (this.board[i].getOwner().equals(opponentPlayer)) {
                        opponentPlayerArmyCount += this.board[i].getArmies();
                    } else if (this.board[i].getOwner().equals(this.board[0].getCurrentPlayer())) {
                        currentPlayerArmyCount += this.board[i].getArmies();
                    }
                }

                opponentPlayerArmyCount += this.calcReinforcementsForPlayer(roundIndex, opponentPlayer);
                currentPlayerArmyCount += this.calcReinforcementsForPlayer(roundIndex, this.board[0].getCurrentPlayer());
                if (opponentPlayerArmyCount <= 0) {
                    return subGoalStates.length - 1;
                }

                returnValue = subGoals.getStateIndex((float)currentPlayerArmyCount / (float)opponentPlayerArmyCount);
            }
        } else {
            returnValue = 0;
        }

        return returnValue;
    }

    public String calcCurrentPlayerCloseToKillPlayerState(int playerIndex, int roundIndex) {
        return subGoalStates[this.calcCurrentPlayerCloseToKillPlayerStateIndex(playerIndex, roundIndex)];
    }

    public int calcCurrentPlayerCloseToContinentStateIndex(int roundIndex, int continentIndex, int playerIndex) {
        return this.continentEstimate[playerIndex][continentIndex][roundIndex].getStateIndex();
    }

    public float calcCurrentPlayerCloseToContinentEstimate(int roundIndex, int continentIndex, int playerIndex) {
        return this.continentEstimate[playerIndex][continentIndex][roundIndex].getEstimate();
    }

    public String calcCurrentPlayerCloseToContinentState(int roundIndex, int continentIndex, int playerIndex) {
        return this.continentEstimate[playerIndex][continentIndex][roundIndex].getBNValue();
    }

    public int calcCurrentPlayerMissionStateIndex(int playerIndex) {
        float bestEstimate = this.missionEstimates[playerIndex][0].getEstimate();
        String mostProbableMissionName = this.missionEstimates[playerIndex][0].getName();

        for(int missionIndex = 1; missionIndex < 14; ++missionIndex) {
            if (this.missionEstimates[playerIndex][missionIndex].getEstimate() > bestEstimate) {
                bestEstimate = this.missionEstimates[playerIndex][missionIndex].getEstimate();
                mostProbableMissionName = TrainingData.MISSION_NAMES[missionIndex];
            }
        }

        return this.getStateIndexFromMissionName(mostProbableMissionName);
    }

    public String calcCurrentPlayerMissionState(int playerIndex) {
        return this.missionStateList[this.calcCurrentPlayerMissionStateIndex(playerIndex)];
    }

    public int getStateIndexFromMissionName(String missionName) {
        int returnValue = 0;
        if (missionName.equals("Asia_SouthAmerica")) {
            returnValue = 0;
        }

        if (missionName.equals("Europe_Australia_3rd")) {
            returnValue = 3;
        }

        if (missionName.equals("Asia_Africa")) {
            returnValue = 1;
        }

        if (missionName.equals("Europe_SouthAmerica_3rd")) {
            returnValue = 5;
        }

        if (missionName.equals("NorthAmerica_Australia")) {
            returnValue = 4;
        }

        if (missionName.equals("NorthAmerica_Africa")) {
            returnValue = 2;
        }

        if (missionName.equals("18_Territories")) {
            returnValue = 13;
        }

        if (missionName.equals("24_Territories")) {
            returnValue = 12;
        }

        if (missionName.equals("Kill_Player1")) {
            returnValue = 6;
        }

        if (missionName.equals("Kill_Player2")) {
            returnValue = 7;
        }

        if (missionName.equals("Kill_Player3")) {
            returnValue = 8;
        }

        if (missionName.equals("Kill_Player4")) {
            returnValue = 9;
        }

        if (missionName.equals("Kill_Player5")) {
            returnValue = 10;
        }

        if (missionName.equals("Kill_Player6")) {
            returnValue = 11;
        }

        return returnValue;
    }

    public String getStateFromMissionName(String missionName) {
        return this.missionStateList[this.getStateIndexFromMissionName(missionName)];
    }

    public int calcCurrentPlayerMostLikelyAnyContinentStateIndex(int playerIndex, int roundIndex, int givenMissionIndex) {
        String continentInMissionName1 = "";
        String continentInMissionName2 = "";
        if (givenMissionIndex == 1) {
            continentInMissionName1 = "Europe";
            continentInMissionName2 = "Australia";
        } else {
            if (givenMissionIndex != 3) {
                throw new UnsupportedOperationException("MissionIndex should be 1 or 3!");
            }

            continentInMissionName1 = "Europe";
            continentInMissionName2 = "South-America";
        }

        int bestContinentIndex = 0;

        for(int i = 1; i < 6; ++i) {
            if (!TrainingData.CONTINENT_NAMES[i].equals(continentInMissionName1) && !TrainingData.CONTINENT_NAMES[i].equals(continentInMissionName2) && this.continentEstimate[playerIndex][bestContinentIndex][roundIndex].getEstimate() < this.continentEstimate[playerIndex][i][roundIndex].getEstimate()) {
                bestContinentIndex = i;
            }
        }

        return this.continentEstimate[playerIndex][bestContinentIndex][roundIndex].getStateIndex();
    }

    private int calcReinforcementsForPlayer(int round, String player) {
        Vector territoriesOwned = new Vector();

        int continentReinforcements;
        for(continentReinforcements = 0; continentReinforcements < this.board.length; ++continentReinforcements) {
            if (this.board[continentReinforcements].getOwner().equals(player)) {
                territoriesOwned.add(this.board[continentReinforcements].getName());
            }
        }

        continentReinforcements = 0;

        for(int i = 0; i < 6; ++i) {
            boolean ownsContinent = true;

            for(int j = 0; j < TrainingData.TERRITORIES_IN_CONTINENTS[i].length; ++j) {
                if (!territoriesOwned.contains(TrainingData.TERRITORIES_IN_CONTINENTS[i][j])) {
                    ownsContinent = false;
                    break;
                }
            }

            if (ownsContinent) {
                continentReinforcements += TrainingData.CONTINENTS_VALUE[i];
            }
        }

        int beginningOfTurnInt = 0;
        if (Boolean.valueOf(this.beginningOfTurn.getValue())) {
            beginningOfTurnInt = 1;
        }

        int reinforcements = (Math.max(territoriesOwned.size() / 3, 3) + continentReinforcements) * (round + beginningOfTurnInt);
        this.getPlayerIndex(player);
        int numOfCards = Integer.parseInt(this.opponentCards[this.getPlayerIndex(player)].getValue()) + round;

        for(int r = 0; r < round + beginningOfTurnInt; ++r) {
            if (numOfCards > 0) {
                if (numOfCards > 10) {
                    reinforcements += 10;
                    numOfCards -= 3;
                } else {
                    reinforcements = (int)((double)reinforcements + TrainingData.REINFORCEMENTS_FROM_CARDS[numOfCards]);
                    numOfCards -= 3;
                }
            }
        }

        return reinforcements;
    }

    public String getState(int stateIndex) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private int calcReinforcementsForOpponents(int round) {
        float armies = 0.0F;

        for(int i = 0; i < this.playerCount; ++i) {
            if (!this.playerColors[i].equals(this.board[0].getCurrentPlayer())) {
                armies += (float)this.calcReinforcementsForPlayer(round, this.playerColors[i]) * 0.15F;
            }
        }

        return Math.round(armies);
    }

    private int getPlayerIndex(String playerColor) {
        int i;
        for(i = this.playerColors.length - 1; i > -1 && !this.playerColors[i].equals(playerColor); --i) {
        }

        return i;
    }

    static {
        subGoals = new AttributeStates(subGoalStates);
    }
}
