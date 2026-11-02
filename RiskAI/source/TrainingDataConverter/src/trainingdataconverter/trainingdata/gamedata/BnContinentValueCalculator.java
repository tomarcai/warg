package trainingdataconverter.trainingdata.gamedata;

import java.util.Vector;
import trainingdataconverter.trainingdata.TrainingData;

public class BnContinentValueCalculator extends GameDataAttribute {
    private static String[] states = new String[]{"0;0", "1;1", "2;2", "3;3", "4;4", "5;5", "6;6", "7;7", "8;8", "9;9", "10;10", "10;12", "12;15", "15;18", "20;23", "23;25", "25;28", "28;30", "30;33", "33;35"};
    private static AttributeStates arffAttributeStates;
    private TerritoryAttribute[] board;
    private OpponentCardAttribute[] opponentCards;
    private BeginningOfTurnAttribute beginningOfTurn;
    private String[] playerColors;
    private int playerCount;
    int currentPlayerArmiesInContinent;
    int currentPlayerArmiesClose;
    int oppArmiesInContinent;
    int oppArmiesClose;

    public BnContinentValueCalculator(TerritoryAttribute[] board, OpponentCardAttribute[] opponentCards, BeginningOfTurnAttribute beginningOfTurn, String[] playerColors) {
        this.name = "ArmiesInContinentPlayer";
        if (board[0] == null) {
            throw new UnsupportedOperationException("Board should have been set.");
        } else {
            this.board = board;
            this.opponentCards = opponentCards;
            this.beginningOfTurn = beginningOfTurn;
            this.playerColors = playerColors;
            this.playerCount = 0;

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
        return arffAttributeStates.getARFFHeader(this.name);
    }

    public int calcCurrentPlayerArmiesInContinentStateIndex(int continentIndex, int roundIndex) {
        float armies = (float)this.calcReinforcementsForPlayer(roundIndex, this.board[0].getCurrentPlayer()) / 6.0F;

        for(int i = 0; i < TrainingData.TERRITORIES_IN_CONTINENTS[continentIndex].length; ++i) {
            int j;
            for(j = this.board.length - 1; j > -1 && !this.board[j].getName().equals(TrainingData.TERRITORIES_IN_CONTINENTS[continentIndex][i]); --j) {
            }

            if (this.board[j].getOwner().equals(this.board[j].getCurrentPlayer())) {
                armies += (float)Integer.parseInt(this.board[j].getValue());
            }
        }

        this.currentPlayerArmiesInContinent = Math.round(armies);
        return arffAttributeStates.getStateIndex((float)Math.round(armies));
    }

    public int calcOpponentPlayersArmiesInContinentStateIndex(int continentIndex, int roundIndex) {
        float armies = (float)this.calcReinforcementsForOpponents(roundIndex) / 6.0F;

        for(int i = 0; i < TrainingData.TERRITORIES_IN_CONTINENTS[continentIndex].length; ++i) {
            int j;
            for(j = this.board.length - 1; j > -1 && !this.board[j].getName().equals(TrainingData.TERRITORIES_IN_CONTINENTS[continentIndex][i]); --j) {
            }

            if (!this.board[j].getOwner().equals(this.board[j].getCurrentPlayer())) {
                armies += (float)Integer.parseInt(this.board[j].getValue());
            }
        }

        this.oppArmiesInContinent = Math.round(armies);
        return arffAttributeStates.getStateIndex((float)Math.round(armies));
    }

    public int calcCurrentPlayerArmiesOutsideContinentStateIndex(int continentIndex, int roundIndex) {
        float armies = (float)this.calcReinforcementsForPlayer(roundIndex, this.board[0].getCurrentPlayer()) / 6.0F;

        for(int k = 0; k < TrainingData.TERRITORIES_IN_CONTINENTS.length; ++k) {
            if (k != continentIndex) {
                for(int i = 0; i < TrainingData.TERRITORIES_IN_CONTINENTS[k].length; ++i) {
                    int j;
                    for(j = this.board.length - 1; j > -1 && !this.board[j].getName().equals(TrainingData.TERRITORIES_IN_CONTINENTS[k][i]); --j) {
                    }

                    if (this.board[j].getOwner().equals(this.board[j].getCurrentPlayer())) {
                        armies += (float)Integer.parseInt(this.board[j].getValue()) * 0.2F;
                    }
                }
            }
        }

        this.currentPlayerArmiesClose = Math.round(armies);
        return arffAttributeStates.getStateIndex((float)Math.round(armies));
    }

    public int calcOpponentPlayersArmiesOutsideContinentStateIndex(int continentIndex, int roundIndex) {
        float armies = (float)this.calcReinforcementsForOpponents(roundIndex) / 6.0F;

        for(int k = 0; k < TrainingData.TERRITORIES_IN_CONTINENTS.length; ++k) {
            if (k != continentIndex) {
                for(int i = 0; i < TrainingData.TERRITORIES_IN_CONTINENTS[k].length; ++i) {
                    int j;
                    for(j = this.board.length - 1; j > -1 && !this.board[j].getName().equals(TrainingData.TERRITORIES_IN_CONTINENTS[k][i]); --j) {
                    }

                    if (!this.board[j].getOwner().equals(this.board[j].getCurrentPlayer())) {
                        armies += (float)Integer.parseInt(this.board[j].getValue()) * 0.2F;
                    }
                }
            }
        }

        this.oppArmiesClose = Math.round(armies);
        return arffAttributeStates.getStateIndex((float)Math.round(armies));
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

        int numOfCards;
        for(int i = 0; i < 6; ++i) {
            boolean ownsContinent = true;

            for(numOfCards = 0; numOfCards < TrainingData.TERRITORIES_IN_CONTINENTS[i].length; ++numOfCards) {
                if (!territoriesOwned.contains(TrainingData.TERRITORIES_IN_CONTINENTS[i][numOfCards])) {
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
        numOfCards = Integer.parseInt(this.opponentCards[this.getPlayerIndex(player)].getValue()) + round;

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
        return states[stateIndex];
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

    public static AttributeStates getARFFAttributeStates() {
        return arffAttributeStates;
    }

    public String calcArmiesLeftInContinentState() {
        double requiredArmies = this.calcEstimatedArmyCost(this.oppArmiesInContinent);
        long armiesLeft = (long)this.currentPlayerArmiesInContinent - Math.round(requiredArmies);
        if (armiesLeft < 0L) {
            armiesLeft = 0L;
        }

        ArmiesLeftAttribute armiesLeftState = new ArmiesLeftAttribute((int)armiesLeft);
        return armiesLeftState.getBnValue();
    }

    public String calcArmiesLeftClostToContinentState() {
        double requiredArmies = this.calcEstimatedArmyCost(this.oppArmiesClose);
        long armiesLeft = (long)this.currentPlayerArmiesClose - Math.round(requiredArmies);
        if (armiesLeft < 0L) {
            armiesLeft = 0L;
        }

        ArmiesLeftAttribute armiesLeftState = new ArmiesLeftAttribute((int)armiesLeft);
        return armiesLeftState.getBnValue();
    }

    private double calcEstimatedArmyCost(int defendingArmies) {
        return 0.8534144 * (double)defendingArmies - 0.2213413 * (1.0 - Math.pow(-0.525359, (double)defendingArmies));
    }

    public int getCurrentPlayerArmiesClose() {
        return this.currentPlayerArmiesClose;
    }

    public int getCurrentPlayerArmiesInContinent() {
        return this.currentPlayerArmiesInContinent;
    }

    public int getOppArmiesClose() {
        return this.oppArmiesClose;
    }

    public int getOppArmiesInContinent() {
        return this.oppArmiesInContinent;
    }

    static {
        arffAttributeStates = new AttributeStates(states);
    }
}
