package trainingdataconverter.trainingdata.gamedata;

import trainingdataconverter.trainingdata.TrainingData;

public class PastAttribute extends GameDataAttribute {
    private static String[] arffAttributeStates = new String[]{"0.0", "0.05", "0.1", "0.15", "0.2", "0.25", "0.3", "0.35", "0.4", "0.45", "0.5", "0.55", "0.6", "0.65", "0.7", "0.75", "0.8", "0.85", "0.9", "0.95", "1.0"};
    private PastCollection past;
    private int playerIndex;
    private String[] playerColors;
    private InterestAttribute[] continentsInterest = null;
    private InterestAttribute[] killPlayerInterest = null;
    private InterestAttribute[] territories18Interest = null;
    private InterestAttribute[] territories24Interest = null;

    public PastAttribute(PastCollection past, int playerIndex, String[] playerColors) {
        this.name = "Past" + playerIndex;
        this.past = past;
        this.playerColors = playerColors;
        this.playerIndex = playerIndex;
    }

    public String getARFFValue() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public String getARFFHeader() {
        return this.makeARFFHeader(arffAttributeStates);
    }

    public String getNNValue() {
        float[] result = new float[48];
        int interestInTerritory = 0;
        int interestInPlayer = 0;

        int i;
        for(i = 0; i < this.past.size(); ++i) {
            String[] action = this.past.getAction(i).split(" ");

            int territoryIndex;
            if (action[0].equals(this.playerColors[this.playerIndex]) && action[1].equals("ATTACK")) {
                ++interestInTerritory;
                ++interestInPlayer;
                territoryIndex = this.findTerritory(action[3]);
                result[territoryIndex]++;
                ++result[42 + this.findOwnerIndex(territoryIndex, i)];
            } else if (action[0].equals(this.playerColors[this.playerIndex]) && action[1].equals("PLACE_ARMY")) {
                ++interestInTerritory;
                territoryIndex = this.findTerritory(action[2]);
                result[territoryIndex]++;
            }
        }

        for(i = 0; i < 42; ++i) {
            if (interestInTerritory > 0) {
                result[i] /= (float)interestInTerritory;
            } else {
                result[i] = 0.0F;
            }
        }

        for(i = 42; i < 48; ++i) {
            if (interestInPlayer > 0) {
                result[i] /= (float)interestInPlayer;
            } else {
                result[i] = 0.0F;
            }
        }

        String out = "";

        for(i = 0; i < result.length; ++i) {
            out = out + String.valueOf(result[i]);
            if (i < result.length - 1) {
                out = out + ",";
            }
        }

        return out;
    }

    private int findTerritory(String territory) {
        int i;
        for(i = TrainingData.TERRITORY_NAMES.length - 1; i > 0 && !TrainingData.TERRITORY_NAMES[i].equals(territory); --i) {
        }

        return i;
    }

    private int findOwnerIndex(int territoryIndex, int boardIndex) {
        String owner = this.past.getBoardState(boardIndex)[territoryIndex].getOwner();

        int i;
        for(i = this.playerColors.length - 1; i > 0 && !this.playerColors[i].equals(owner); --i) {
        }

        return i;
    }

    private void calcInterests() {
        float[] result = new float[48];
        int interestInTerritory = 0;
        int interestInPlayer = 0;
        int armyCount24w1 = 0;
        int armyCount18w2 = 0;
        boolean mission24w1FulfilledInPast = false;
        boolean mission18w2FulfilledInPast = false;

        int i;
        for(i = 0; i < this.past.size(); ++i) {
            String[] action = this.past.getAction(i).split(" ");
            int territoryIndex;

            if (action[0].equals(this.playerColors[this.playerIndex]) && action[1].equals("ATTACK")) {
                ++interestInTerritory;
                ++interestInPlayer;
                territoryIndex = this.findTerritory(action[3]);
                result[territoryIndex]++;
                ++result[42 + this.findOwnerIndex(territoryIndex, i)];
            } else if (action[0].equals(this.playerColors[this.playerIndex]) && action[1].equals("PLACE_ARMY")) {
                ++interestInTerritory;
                territoryIndex = this.findTerritory(action[2]);
                result[territoryIndex]++;
            }

            armyCount24w1 = 0;
            armyCount18w2 = 0;
            TerritoryAttribute[] board = this.past.getBoardState(i);

            for(territoryIndex = 0; territoryIndex < board.length; ++territoryIndex) {
                if (board[territoryIndex].getOwner().equals(this.playerColors[this.playerIndex])) {
                    ++armyCount24w1;
                    if (board[territoryIndex].getArmies() > 1) {
                        ++armyCount18w2;
                    }
                }
            }

            mission24w1FulfilledInPast |= armyCount24w1 >= 24;
            mission18w2FulfilledInPast |= armyCount18w2 >= 18;
        }

        this.continentsInterest = new InterestAttribute[42];

        for(i = 0; i < 42; ++i) {
            if (interestInTerritory > 0) {
                result[i] /= (float)interestInTerritory;
            } else {
                result[i] = 0.0F;
            }

            this.continentsInterest[i] = new InterestAttribute(TrainingData.TERRITORY_NAMES[i], result[i]);
        }

        this.killPlayerInterest = new InterestAttribute[6];

        for(i = 0; i < 6; ++i) {
            if (interestInPlayer > 0) {
                result[i] /= (float)interestInPlayer;
            } else {
                result[i] = 0.0F;
            }

            this.killPlayerInterest[i] = new InterestAttribute("Player_TargetPlayer", result[i]);
        }

        this.territories18Interest = new InterestAttribute[1];
        this.territories24Interest = new InterestAttribute[1];
        float interest24w1 = 0.0F;
        float interest18w2 = 0.0F;
        if (!mission24w1FulfilledInPast) {
            interest24w1 = (float)armyCount24w1 / 24.0F;
        }

        if (!mission18w2FulfilledInPast) {
            interest18w2 = (float)armyCount18w2 / 18.0F;
        }

        this.territories18Interest[0] = new InterestAttribute(TrainingData.MISSION_NAMES[6], interest18w2);
        this.territories24Interest[0] = new InterestAttribute(TrainingData.MISSION_NAMES[7], interest24w1);
    }

    public InterestAttribute[] getInterest(int missionIndex) {
        InterestAttribute[] result = null;
        if (this.continentsInterest == null || this.killPlayerInterest == null || this.territories18Interest == null || this.territories24Interest == null) {
            this.calcInterests();
        }

        if (missionIndex >= 0 && missionIndex <= 5) {
            result = this.continentsInterest;
        } else if (missionIndex == 6) {
            result = this.territories18Interest;
        } else if (missionIndex == 7) {
            result = this.territories24Interest;
        } else {
            if (missionIndex < 8 || missionIndex > 13) {
                throw new RuntimeException("Unknown mission index (missionIndex = " + missionIndex + ")");
            }

            result = new InterestAttribute[]{this.killPlayerInterest[missionIndex - 8]};
        }

        return result;
    }
}
