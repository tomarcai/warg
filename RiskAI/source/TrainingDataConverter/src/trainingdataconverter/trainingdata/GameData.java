package trainingdataconverter.trainingdata;

import java.util.List;
import java.util.Vector;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Player;
import risk.AI.Data_Structures.T_Board;
import risk.AI.Data_Structures.T_Game;
import risk.AI.Data_Structures.T_InfluenceMap;
import risk.AI.Data_Structures.T_Opp_RiskCards;
import risk.AI.Data_Structures.T_Past;
import risk.AI.Data_Structures.T_RP_AttackPlan;
import risk.AI.Data_Structures.T_RP_AttackPlanListElement;
import risk.AI.Data_Structures.T_RP_MergedPlanElement;
import risk.AI.Data_Structures.Module_Output.O_IG_ContinentEstimate;
import risk.AI.Data_Structures.Module_Output.O_IG_MissionEstimate;
import risk.AI.Data_Structures.Module_Output.O_IG_NextMoveEstimate;
import risk.AI.Data_Structures.Module_Output.O_IG_Ownership;
import risk.AI.Data_Structures.Module_Output.O_IG_WinningEstimate;
import risk.AI.Data_Structures.Module_Output.O_MP_GoalDistribution;
import trainingdataconverter.trainingdata.gamedata.ArmiesLeftAttribute;
import trainingdataconverter.trainingdata.gamedata.AttackerArmiesLeftAttribute;
import trainingdataconverter.trainingdata.gamedata.BeginningOfTurnAttribute;
import trainingdataconverter.trainingdata.gamedata.BnContinentValueCalculator;
import trainingdataconverter.trainingdata.gamedata.BnWinningValueCalculator;
import trainingdataconverter.trainingdata.gamedata.ContinentEstimateAttribute;
import trainingdataconverter.trainingdata.gamedata.CostAttribute;
import trainingdataconverter.trainingdata.gamedata.ExtendedAttackPlan;
import trainingdataconverter.trainingdata.gamedata.GoalDistributionAttribute;
import trainingdataconverter.trainingdata.gamedata.InfluenceAttribute;
import trainingdataconverter.trainingdata.gamedata.IsDefensePlanAttribute;
import trainingdataconverter.trainingdata.gamedata.MergedPlanElement;
import trainingdataconverter.trainingdata.gamedata.MergedPlanList;
import trainingdataconverter.trainingdata.gamedata.MergedPlanTerritoryAttribute;
import trainingdataconverter.trainingdata.gamedata.MissionAttribute;
import trainingdataconverter.trainingdata.gamedata.MissionEstimateAttribute;
import trainingdataconverter.trainingdata.gamedata.MissionGivenToAnOpponentAttribute;
import trainingdataconverter.trainingdata.gamedata.NextMoveEstimateAttribute;
import trainingdataconverter.trainingdata.gamedata.OpponentCardAttribute;
import trainingdataconverter.trainingdata.gamedata.OwnershipAttribute;
import trainingdataconverter.trainingdata.gamedata.PastAttribute;
import trainingdataconverter.trainingdata.gamedata.PastCollection;
import trainingdataconverter.trainingdata.gamedata.PriorityAttribute;
import trainingdataconverter.trainingdata.gamedata.ScoreAttribute;
import trainingdataconverter.trainingdata.gamedata.TerritoryAttribute;
import trainingdataconverter.trainingdata.gamedata.TerritoryOutputAttribute;
import trainingdataconverter.trainingdata.gamedata.WinningEstimateAttribute;

public class GameData {
    private int currentPlayerIndex;
    private int winningPlayerIndex;
    private OpponentCardAttribute[] opponentCards;
    private ContinentEstimateAttribute[][][] continentEstimates;
    private MissionEstimateAttribute[][] missionEstimates;
    private WinningEstimateAttribute[][] winningEstimates;
    private OwnershipAttribute[][] ownership;
    private TerritoryAttribute[] board;
    private InfluenceAttribute[] influenceMap;
    private TerritoryOutputAttribute territoryOutput;
    private PastCollection past;
    private PastAttribute[] pastAttributes;
    private GoalDistributionAttribute[] goalDistribution;
    private NextMoveEstimateAttribute[] nextmove;
    private ExtendedAttackPlan extendedAttackPlan;
    private TerritoryAttribute[] attackPlan;
    private TerritoryAttribute attackPlan_firstTerritory;
    private PriorityAttribute priority;
    private CostAttribute estimatedCost;
    private CostAttribute minimumCost;
    private ScoreAttribute score;
    private String[] playerColors;
    private int[] playerMissionIndex;
    private BeginningOfTurnAttribute beginningOfTurn;
    private MergedPlanElement defensePlanOutput;
    private MergedPlanTerritoryAttribute mergedPlanTerritory;
    private AttackerArmiesLeftAttribute attackerArmiesLeft;
    private BnContinentValueCalculator bnContinentValueCalc;
    private BnWinningValueCalculator bnWinningValueCalc;
    private IsDefensePlanAttribute isDefensePlan;
    private MergedPlanList mergedPlanList;
    private ArmiesLeftAttribute armiesLeft;
    private int numberOfPlayers;
    private int playerReference;

    GameData() {
        this.past = new PastCollection();
        this.playerColors = null;
        this.mergedPlanList = new MergedPlanList();
        int maxPlayers = 6;
        this.playerColors = new String[maxPlayers];
        this.playerMissionIndex = new int[maxPlayers];
        this.opponentCards = new OpponentCardAttribute[maxPlayers];
        this.continentEstimates = new ContinentEstimateAttribute[maxPlayers][6][5];
        this.missionEstimates = new MissionEstimateAttribute[maxPlayers][14];
        this.winningEstimates = new WinningEstimateAttribute[maxPlayers][5];
        this.ownership = new OwnershipAttribute[maxPlayers][6];
        this.pastAttributes = new PastAttribute[maxPlayers];
        this.nextmove = new NextMoveEstimateAttribute[42];

        int playerIndex;
        for(playerIndex = 0; playerIndex < maxPlayers; ++playerIndex) {
            this.playerColors[playerIndex] = "";
            this.playerMissionIndex[playerIndex] = -1;
            this.opponentCards[playerIndex] = new OpponentCardAttribute(playerIndex, 0);
            this.pastAttributes[playerIndex] = new PastAttribute(this.past, playerIndex, this.playerColors);

            int continentIndex;
            for(continentIndex = 0; continentIndex < 5; ++continentIndex) {
                int missionIndex;
                for(missionIndex = 0; missionIndex < 6; ++missionIndex) {
                    this.continentEstimates[playerIndex][missionIndex][continentIndex] = new ContinentEstimateAttribute(TrainingData.CONTINENT_NAMES[missionIndex], 0.0F);
                }

                this.winningEstimates[playerIndex][continentIndex] = new WinningEstimateAttribute(0.0F);

                for(missionIndex = 0; missionIndex < 14; ++missionIndex) {
                    this.missionEstimates[playerIndex][missionIndex] = new MissionEstimateAttribute(TrainingData.MISSION_NAMES[missionIndex], 0.0F);
                }
            }

            for(continentIndex = 0; continentIndex < 6; ++continentIndex) {
                this.ownership[playerIndex][continentIndex] = new OwnershipAttribute(TrainingData.CONTINENT_NAMES[continentIndex], false);
            }
        }

        for(playerIndex = 0; playerIndex < 42; ++playerIndex) {
            this.nextmove[playerIndex] = new NextMoveEstimateAttribute(TrainingData.TERRITORY_NAMES[playerIndex], false);
        }

        this.board = new TerritoryAttribute[42];
        this.attackPlan = new TerritoryAttribute[42];
        this.influenceMap = new InfluenceAttribute[42];
        this.goalDistribution = new GoalDistributionAttribute[38];
        this.territoryOutput = new TerritoryOutputAttribute(TrainingData.TERRITORY_NAMES[0], new TerritoryAttribute[0]);
        this.score = new ScoreAttribute(0.0F);
    }

    public GameData(List<Player> players, int currentPlayerIndex) {
        this();
        this.numberOfPlayers = players.size();
        this.playerColors = new String[6];

        int i;
        for(i = 0; i < this.numberOfPlayers; ++i) {
            this.playerColors[i] = T_Game.decentColorToString(((Player)players.get(i)).getColor());
        }

        for(i = this.numberOfPlayers; i < 6; ++i) {
            this.playerColors[i] = "Player" + i;
        }

        this.currentPlayerIndex = currentPlayerIndex;
    }

    public BeginningOfTurnAttribute getBeginningOfTurn() {
        return this.beginningOfTurn;
    }

    public String getCurrentPlayer() {
        return this.playerColors[this.currentPlayerIndex];
    }

    public int getWinningPlayer() {
        return this.winningPlayerIndex;
    }

    public int getPlayerReference() {
        return this.playerReference;
    }

    public OpponentCardAttribute[] getOpponentCards() {
        OpponentCardAttribute[] result = new OpponentCardAttribute[this.opponentCards.length];
        result[0] = this.opponentCards[this.currentPlayerIndex];
        int resultIndex = 1;

        for(int opponentIndex = 0; opponentIndex < this.opponentCards.length; ++opponentIndex) {
            if (opponentIndex != this.currentPlayerIndex) {
                result[resultIndex] = this.opponentCards[opponentIndex];
                ++resultIndex;
            }
        }

        return result;
    }

    public GoalDistributionAttribute[] getGoalDistribution() {
        return this.goalDistribution;
    }

    public TerritoryAttribute[] getBoard() {
        return this.board;
    }

    public String getPlayerColor(int playerIndex) {
        return this.playerColors[playerIndex];
    }

    public int getNumberOfPlayers() {
        return this.numberOfPlayers;
    }

    public ContinentEstimateAttribute getContinentEstimate(int continentIndex, int roundIndex) {
        return this.continentEstimates[this.currentPlayerIndex][continentIndex][roundIndex];
    }

    public MissionGivenToAnOpponentAttribute[] getMissionsGivenToOpponents() {
        MissionGivenToAnOpponentAttribute[] missionsGiven = new MissionGivenToAnOpponentAttribute[14];

        int playerIndex;
        for(playerIndex = 0; playerIndex < this.numberOfPlayers; ++playerIndex) {
            if (playerIndex != this.currentPlayerIndex) {
                float bestEstimate = 0.0F;
                int bestMissionIndex = 0;

                for(int missionIndex = 0; missionIndex < 14; ++missionIndex) {
                    float estimate = this.missionEstimates[playerIndex][missionIndex].getEstimate();
                    if (estimate > bestEstimate) {
                        bestEstimate = estimate;
                        bestMissionIndex = missionIndex;
                    }
                }

                missionsGiven[bestMissionIndex] = new MissionGivenToAnOpponentAttribute(true, bestMissionIndex);
            }
        }

        for(playerIndex = 0; playerIndex < missionsGiven.length; ++playerIndex) {
            if (missionsGiven[playerIndex] == null) {
                missionsGiven[playerIndex] = new MissionGivenToAnOpponentAttribute(false, playerIndex);
            }
        }

        return missionsGiven;
    }

    public MissionEstimateAttribute getMissionEstimate(int missionIndex) {
        return this.missionEstimates[this.currentPlayerIndex][missionIndex];
    }

    public MissionAttribute getMostLikelyMission() {
        float bestEstimate = -1.0F;
        int bestMission = -1;

        for(int missionIndex = 0; missionIndex < this.missionEstimates[this.currentPlayerIndex].length; ++missionIndex) {
            float estimate = this.missionEstimates[this.currentPlayerIndex][missionIndex].getEstimate();
            if (estimate > bestEstimate) {
                bestEstimate = estimate;
                bestMission = missionIndex;
            }
        }

        return new MissionAttribute(TrainingData.MISSION_NAMES[bestMission]);
    }

    public WinningEstimateAttribute getWinningEstimate(int roundIndex) {
        return this.winningEstimates[this.currentPlayerIndex][roundIndex];
    }

    public PastAttribute getPastAttribute() {
        return this.pastAttributes[this.currentPlayerIndex];
    }

    public NextMoveEstimateAttribute[] getNextMoveAttribute() {
        return this.nextmove;
    }

    public OwnershipAttribute getOwnership(int continentIndex) {
        return this.ownership[this.currentPlayerIndex][continentIndex];
    }

    public InfluenceAttribute[] getInfluenceMap() {
        return this.influenceMap;
    }

    public InfluenceAttribute getInfluenceMap(int territoryIndex) {
        return this.influenceMap[territoryIndex];
    }

    public ExtendedAttackPlan getExtendedAttackPlan() {
        return this.extendedAttackPlan;
    }

    public TerritoryAttribute[] getAttackPlan() {
        return this.attackPlan;
    }

    public TerritoryOutputAttribute getTerritoryOutput() {
        return this.territoryOutput;
    }

    public PriorityAttribute getPriority() {
        return this.priority;
    }

    public CostAttribute getEstimatedCost() {
        return this.estimatedCost;
    }

    public CostAttribute getMinimumCost() {
        return this.minimumCost;
    }

    public ScoreAttribute getScore() {
        return this.score;
    }

    public void setBeginningOfTurn(BeginningOfTurnAttribute beginningOfTurn) {
        this.beginningOfTurn = beginningOfTurn;
    }

    public void setBeginningOfTurn(boolean beginningOfTurn) {
        this.beginningOfTurn = new BeginningOfTurnAttribute(beginningOfTurn);
    }

    public void setCurrentPlayer(int playerIndex) {
        this.currentPlayerIndex = playerIndex;
        if (this.board[0] != null) {
            for(int i = 0; i < this.board.length; ++i) {
                this.board[i].setCurrentPlayer(this.playerColors[playerIndex]);
            }
        }

    }

    public void setWinningPlayer(String winningPlayer) {
        this.winningPlayerIndex = this.getPlayerIndex(winningPlayer);
    }

    public void setPlayerReference(String playerName) {
        this.playerReference = this.getPlayerIndex(playerName);
        this.setCurrentPlayer(this.playerReference);
    }

    public void setOpponentCards(int opponentIndex, OpponentCardAttribute value) {
        this.opponentCards[opponentIndex] = value;
    }

    public void setOpponentCards(T_Opp_RiskCards cards) {
        for(int playerIndex = 0; playerIndex < this.numberOfPlayers; ++playerIndex) {
            this.setOpponentCards(playerIndex, new OpponentCardAttribute(playerIndex, cards.getNumOfRiskCards(playerIndex)));
        }

    }

    public void setBoard(int territoryIndex, TerritoryAttribute value) {
        this.board[territoryIndex] = value;
    }

    public void setBoard(T_Board board) {
        if (this.playerColors[0].equals("")) {
            throw new RuntimeException("Player colors are not initialized. setPlayerColor() should have been called!");
        } else {
            String currentPlayerColor = this.playerColors[this.currentPlayerIndex];

            for(int territoryIndex = 0; territoryIndex < board.getCountryCount(); ++territoryIndex) {
                Country territory = (Country)board.getCountries().get(territoryIndex);
                String ownerColor = T_Game.decentColorToString(territory.getOwner().getColor());
                TerritoryAttribute territoryAttribute = new TerritoryAttribute(territory.getIdString(), ownerColor, territory.getArmies());
                territoryAttribute.setCurrentPlayer(currentPlayerColor);
                this.setBoard(territoryIndex, territoryAttribute);
            }

        }
    }

    public void setNextMove(int territoryIndex, NextMoveEstimateAttribute value) {
        this.nextmove[territoryIndex] = value;
    }

    public void setNextMove(String territoryName) {
        int c;
        for(c = 41; c > -1 && !TrainingData.TERRITORY_NAMES[c].equals(territoryName); --c) {
        }

        if (c == -1) throw new IllegalArgumentException("bad country name: " + territoryName);

        this.nextmove[c] = new NextMoveEstimateAttribute(territoryName, true);
    }

    public void setNextMove(O_IG_NextMoveEstimate nextMoveEstimate) {
        if (nextMoveEstimate.getTerritoryInDangerList() != null) {
            for(int i = 0; i < nextMoveEstimate.getTerritoryInDangerList().size(); ++i) {
                String name = ((Country)nextMoveEstimate.getTerritoryInDangerList().get(i)).getIdString();
                this.setNextMove(name);
            }
        }

    }

    void setPlayerColor(int playerIndex, String color) {
        this.playerColors[playerIndex] = color;
    }

    public void setNumberOfPlayers(int numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }

    public void setOwnership(int playerIndex, int continentIndex, OwnershipAttribute ownership) {
        this.ownership[playerIndex][continentIndex] = ownership;
    }

    public void setOwnership(O_IG_Ownership ownership) {
        for(int playerIndex = 0; playerIndex < 6; ++playerIndex) {
            for(int continentIndex = 0; continentIndex < 6; ++continentIndex) {
                this.ownership[playerIndex][continentIndex] = new OwnershipAttribute(TrainingData.CONTINENT_NAMES[continentIndex], ownership.getIsOwned(playerIndex, continentIndex));
            }
        }

    }

    public void setContinentEstimates(int playerIndex, int continentIndex, int roundIndex, ContinentEstimateAttribute estimate) {
        this.continentEstimates[playerIndex][continentIndex][roundIndex] = estimate;
    }

    public void setContinentEstimates(O_IG_ContinentEstimate continentEstimate) {
        for(int playerIndex = 0; playerIndex < 6; ++playerIndex) {
            for(int roundIndex = 0; roundIndex < 5; ++roundIndex) {
                for(int continentIndex = 0; continentIndex < 6; ++continentIndex) {
                    this.continentEstimates[playerIndex][continentIndex][roundIndex] = new ContinentEstimateAttribute(TrainingData.CONTINENT_NAMES[continentIndex], continentEstimate.getEstimate(playerIndex, continentIndex, roundIndex));
                }
            }
        }

    }

    public void setMissionEstimates(int playerIndex, int missionIndex, MissionEstimateAttribute estimate) {
        this.missionEstimates[playerIndex][missionIndex] = estimate;
    }

    public void setMissionEstimates(O_IG_MissionEstimate missionEstimate) {
        for(int playerIndex = 0; playerIndex < 6; ++playerIndex) {
            for(int missionIndex = 0; missionIndex < 14; ++missionIndex) {
                this.missionEstimates[playerIndex][missionIndex] = new MissionEstimateAttribute(TrainingData.MISSION_NAMES[missionIndex], missionEstimate.getEstimate(playerIndex, missionIndex));
            }
        }

    }

    public void setWinningEstimates(int playerIndex, int roundIndex, WinningEstimateAttribute estimate) {
        this.winningEstimates[playerIndex][roundIndex] = estimate;
    }

    public void setWinningEstimates(O_IG_WinningEstimate winningEstimate) {
        for(int playerIndex = 0; playerIndex < 6; ++playerIndex) {
            for(int roundIndex = 0; roundIndex < 5; ++roundIndex) {
                this.winningEstimates[playerIndex][roundIndex] = new WinningEstimateAttribute(winningEstimate.getEstimate(playerIndex, roundIndex));
            }
        }

    }

    public void addPastElement_Action(String action) {
        this.past.addAction(action);
    }

    public void addPastElement_BoardState(TerritoryAttribute[] boardState) {
        this.past.addBoardState(boardState);
    }

    public void setPast(T_Past t_past) {
        this.past.clear();

        int i;
        for(i = 0; i < t_past.getActionPast().size(); ++i) {
            this.addPastElement_Action((String)t_past.getActionPast().get(i));
        }

        for(i = 0; i < t_past.getBoardPast().size(); ++i) {
            TerritoryAttribute[] board = new TerritoryAttribute[42];
            String[] boardStr = ((String)t_past.getBoardPast().get(i)).split(";");

            for(int j = 0; j < boardStr.length; ++j) {
                if (boardStr[j] != "") {
                    String[] s = boardStr[j].split(",");
                    board[j] = new TerritoryAttribute(TrainingData.TERRITORY_NAMES[j], this.getPlayerColor(Integer.parseInt(s[1])), Integer.parseInt(s[0]));
                }
            }

            this.addPastElement_BoardState(board);
        }

    }

    public void setGoalDistribution(int goalIndex, float goaldist) {
        this.goalDistribution[goalIndex] = new GoalDistributionAttribute(goalIndex, goaldist);
    }

    public void setGoalDistribution(O_MP_GoalDistribution goaldist) {
        float[] gd = goaldist.getFullDistribution();

        for(int i = 0; i < gd.length; ++i) {
            this.goalDistribution[i] = new GoalDistributionAttribute(i, gd[i]);
        }

    }

    public void setInfluenceMap(T_InfluenceMap influenceMap) {
        for(int i = 0; i < 42; ++i) {
            this.influenceMap[i] = new InfluenceAttribute(TrainingData.TERRITORY_NAMES[i], influenceMap.getValue(i));
        }

    }

    public void setInfluenceMap(int territoryIndex, int armies) {
        this.influenceMap[territoryIndex] = new InfluenceAttribute(TrainingData.TERRITORY_NAMES[territoryIndex], armies);
    }

    public void setExtendedAttackPlan(ExtendedAttackPlan extendedAttackPlan) {
        this.extendedAttackPlan = extendedAttackPlan;
    }

    public void setExtendedAttackPlan(T_RP_AttackPlanListElement extendedAttackPlan) {
        TerritoryAttribute[] attackPlan = new TerritoryAttribute[42];

        for(int i = 0; i < extendedAttackPlan.getAttackPlan().getCountries().size(); ++i) {
            int indexOf;
            for(indexOf = TrainingData.TERRITORY_NAMES.length - 1; indexOf > -1 && !TrainingData.TERRITORY_NAMES[indexOf].equals(extendedAttackPlan.getAttackPlan().getCountry(i).getIdString()); --indexOf) {
            }

            attackPlan[indexOf] = new TerritoryAttribute(extendedAttackPlan.getAttackPlan().getCountry(i).getIdString(), this.getCurrentPlayer(), extendedAttackPlan.getAttackPlan().getArmiesToLeave(i));
        }

        this.extendedAttackPlan = new ExtendedAttackPlan(extendedAttackPlan.getMinimumCost(), extendedAttackPlan.getEstimatedCost(), extendedAttackPlan.getPriority(), extendedAttackPlan.getScore(), attackPlan);
    }

    public void setAttackPlan(T_RP_AttackPlan attackPlan) {
        for(int i = 0; i < attackPlan.getCountries().size(); ++i) {
            int indexOf;
            for(indexOf = TrainingData.TERRITORY_NAMES.length - 1; indexOf > -1 && !TrainingData.TERRITORY_NAMES[indexOf].equals(attackPlan.getCountry(i).getIdString()); --indexOf) {
            }

            this.attackPlan[indexOf] = new TerritoryAttribute(attackPlan.getCountry(i).getIdString(), this.getCurrentPlayer(), attackPlan.getArmiesToLeave(i));
            this.attackPlan[indexOf].setCurrentPlayer(this.getCurrentPlayer());
        }

        this.attackPlan_firstTerritory = new TerritoryAttribute(attackPlan.getCountry(0).getIdString(), this.getCurrentPlayer(), attackPlan.getArmiesToLeave(0));
        this.attackPlan_firstTerritory.setCurrentPlayer(this.getCurrentPlayer());
    }

    public void setAttackPlan(TerritoryAttribute[] attackPlan, TerritoryAttribute firstTerritory) {
        this.attackPlan = attackPlan;
        this.attackPlan_firstTerritory = firstTerritory;
    }

    public void setTerritoryOutput(String territoryName) {
        if (this.board[0] != null) {
            Vector occupiedTerritories = new Vector();

            for(int i = 0; i < this.board.length; ++i) {
                if (this.getPlayerIndex(this.board[i].getOwner()) == this.currentPlayerIndex) {
                    occupiedTerritories.add(this.board[i]);
                }
            }

            if (occupiedTerritories.size() > 0) {
                this.territoryOutput = new TerritoryOutputAttribute(territoryName, (TerritoryAttribute[])((TerritoryAttribute[])occupiedTerritories.toArray(new TerritoryAttribute[occupiedTerritories.size()])));
            } else {
                throw new RuntimeException("CurrentPlayer does not own any territories (perhaps currentPlayer has not been set yet?)");
            }
        } else {
            throw new RuntimeException("The board should be set before calling setTerritoryOutput!");
        }
    }

    public void setPriority(PriorityAttribute priority) {
        this.priority = priority;
    }

    public void setPriority(float priority) {
        this.priority = new PriorityAttribute(priority);
    }

    public void setEstimatedCost(CostAttribute estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public void setEstimatedCost(int estimatedCost) {
        this.estimatedCost = new CostAttribute("Estimated", estimatedCost);
    }

    public void setMinimumCost(CostAttribute minimumCost) {
        this.minimumCost = minimumCost;
    }

    public void setMinimumCost(int minimumCost) {
        this.minimumCost = new CostAttribute("Minimum", minimumCost);
    }

    public void setScore(ScoreAttribute score) {
        this.score = score;
    }

    public void setScore(float score) {
        this.score = new ScoreAttribute(score);
    }

    public MergedPlanElement getDefensePlanOutput() {
        return this.defensePlanOutput;
    }

    public void setDefensePlanOutput(MergedPlanElement defensePlanOutput) {
        this.defensePlanOutput = defensePlanOutput;
    }

    public void setDefensePlanOutput(T_RP_MergedPlanElement defensePlan) {
        this.defensePlanOutput = new MergedPlanElement(defensePlan.getCountry().getIdString(), defensePlan.getMinimumCost(), defensePlan.getEstimatedCost(), defensePlan.getPriority(), defensePlan.getScore(), defensePlan.getAttackerArmiesLeft(), defensePlan.getIsDefensePlan());
    }

    private int getPlayerIndex(String playerColor) {
        int playerIndex;
        for(playerIndex = this.playerColors.length - 1; playerIndex > -1 && !this.playerColors[playerIndex].equals(playerColor); --playerIndex) {
        }

        if (playerIndex == -1) {
            throw new UnsupportedOperationException("Could not find a player colored '" + playerColor + "' in the list of players in the game");
        } else {
            return playerIndex;
        }
    }

    public void setMergedPlanTerritory(MergedPlanTerritoryAttribute mergedPlanTerritory) {
        this.mergedPlanTerritory = mergedPlanTerritory;
    }

    public void setMergedPlanTerritory(String territoryName) {
        this.mergedPlanTerritory = new MergedPlanTerritoryAttribute(territoryName);
    }

    public MergedPlanTerritoryAttribute getMergedPlanTerritory() {
        return this.mergedPlanTerritory;
    }

    public void setAttackerArmiesLeft(AttackerArmiesLeftAttribute attackerArmiesLeft) {
        this.attackerArmiesLeft = attackerArmiesLeft;
    }

    public void setAttackerArmiesLeft(int armies) {
        this.attackerArmiesLeft = new AttackerArmiesLeftAttribute(armies);
    }

    public void initBnContinentValueCalc() {
        if (this.bnContinentValueCalc == null) {
            if (this.board[0] == null) {
                throw new RuntimeException("Board should have been set!");
            }

            if (this.opponentCards[0] == null) {
                throw new RuntimeException("OpponentCards should have been set!");
            }

            if (this.beginningOfTurn == null) {
                throw new RuntimeException("BeginningOfTurn should have been set!");
            }

            if (this.playerColors[0] == null) {
                throw new RuntimeException("PlayerColors should have been set!");
            }

            this.bnContinentValueCalc = new BnContinentValueCalculator(this.board, this.opponentCards, this.beginningOfTurn, this.playerColors);
        }

    }

    public BnContinentValueCalculator getBnContinentValueCalc() {
        return this.bnContinentValueCalc;
    }

    public AttackerArmiesLeftAttribute getAttackerArmiesLeft() {
        return this.attackerArmiesLeft;
    }

    public MergedPlanList getMergedPlanList() {
        return this.mergedPlanList;
    }

    public void setArmiesLeft(int armies) {
        this.armiesLeft = new ArmiesLeftAttribute(armies);
    }

    public void initBnWinningValueCalc() {
        if (this.bnWinningValueCalc == null) {
            if (this.board[0] == null) {
                throw new RuntimeException("Board should have been set!");
            }

            if (this.continentEstimates[0][0][0] == null) {
                throw new RuntimeException("ContinentEstimate should have been set!");
            }

            if (this.opponentCards[0] == null) {
                throw new RuntimeException("OpponentCards should have been set!");
            }

            if (this.beginningOfTurn == null) {
                throw new RuntimeException("BeginningOfTurn should have been set!");
            }

            if (this.playerColors[0] == null) {
                throw new RuntimeException("PlayerColors should have been set!");
            }

            if (this.missionEstimates[0][0] == null) {
                throw new RuntimeException("MissionEstimates should have been set!");
            }

            this.bnWinningValueCalc = new BnWinningValueCalculator(this.board, this.continentEstimates, this.opponentCards, this.beginningOfTurn, this.playerColors, this.missionEstimates);
        }

    }

    public BnWinningValueCalculator getBnWinningValueCalc() {
        return this.bnWinningValueCalc;
    }

    public ArmiesLeftAttribute getArmiesLeft() {
        return this.armiesLeft;
    }

    public void setPlayerMissionIndex(int playerIndex, int missionIndex) {
        this.playerMissionIndex[playerIndex] = missionIndex;
    }

    public int getPlayerMissionIndex() {
        return this.playerMissionIndex[this.currentPlayerIndex];
    }

    public String[] getPlayerColors() {
        return this.playerColors;
    }

    public IsDefensePlanAttribute getIsDefensePlan() {
        return this.isDefensePlan;
    }

    public void setIsDefensePlan(boolean isDefensePlan) {
        this.isDefensePlan = new IsDefensePlanAttribute(isDefensePlan);
    }
}
