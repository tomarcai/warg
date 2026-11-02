package trainingdataconverter.trainingdata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Vector;
import net.yura.domination.engine.core.RiskGame;
import progressstats.ProgressStats;
import risk.AI.Data_Structures.T_Game;
import trainingdataconverter.trainingdata.gamedata.BeginningOfTurnAttribute;
import trainingdataconverter.trainingdata.gamedata.ContinentEstimateAttribute;
import trainingdataconverter.trainingdata.gamedata.ExtendedAttackPlan;
import trainingdataconverter.trainingdata.gamedata.MergedPlanElement;
import trainingdataconverter.trainingdata.gamedata.MissionEstimateAttribute;
import trainingdataconverter.trainingdata.gamedata.OpponentCardAttribute;
import trainingdataconverter.trainingdata.gamedata.OwnershipAttribute;
import trainingdataconverter.trainingdata.gamedata.TerritoryAttribute;
import trainingdataconverter.trainingdata.gamedata.WinningEstimateAttribute;
import trainingdataconverter.trainingdata.io.XMLInput;
import trainingdataconverter.trainingdata.winnerloser.WinnerLoserFileList;

public class TrainingData {
    public static final String[] CONTINENT_NAMES = T_Game.CONTINENT_NAMES;
    public static final int[] CONTINENTS_VALUE = new int[]{5, 2, 5, 3, 7, 2};
    public static final String[][] TERRITORIES_IN_CONTINENTS = new String[][]{{"Alaska", "North-West-Territory", "Alberta", "Western-United-States", "Central-America", "Greenland", "Ontario", "Quebec", "Eastern-United-States"}, {"Venezuela", "Peru", "Brazil", "Argentina"}, {"Iceland", "Scandinavia", "Ukraine", "Great-Britain", "Northern-Europe", "Western-Europe", "Southern-Europe"}, {"North-Africa", "Egypt", "Congo", "East-Africa", "South-Africa", "Madagascar"}, {"Siberia", "Ural", "China", "Afghanistan", "Middle-East", "India", "Siam", "Yakutsk", "Irkutsk", "Mongolia", "Japan", "Kamchatka"}, {"Indonesia", "New-Guinea", "Western-Australia", "Eastern-Australia"}};

    public static final String[] TERRITORY_NAMES = new String[]{"Alaska", "North-West-Territory", "Alberta", "Western-United-States", "Central-America", "Greenland", "Ontario", "Quebec", "Eastern-United-States", "Venezuela", "Peru", "Brazil", "Argentina", "Iceland", "Scandinavia", "Ukraine", "Great-Britain", "Northern-Europe", "Western-Europe", "Southern-Europe", "North-Africa", "Egypt", "Congo", "East-Africa", "South-Africa", "Madagascar", "Siberia", "Ural", "China", "Afghanistan", "Middle-East", "India", "Siam", "Yakutsk", "Irkutsk", "Mongolia", "Japan", "Kamchatka", "Indonesia", "New-Guinea", "Western-Australia", "Eastern-Australia"};
    public static final int NUMBER_OF_TERRITORIES = TERRITORY_NAMES.length;

    public static final int NUMBER_OF_PLAYERS_MAX = RiskGame.MAX_PLAYERS;
    public static final String[] MISSION_NAMES = T_Game.MISSION_NAMES;

    public static final double[] REINFORCEMENTS_FROM_CARDS = T_Game.reinforcementsFromCards;

/*
    public static final int NUMBER_OF_CONTINENTS = 6;
    public static final int NUMBER_OF_MISSIONS = 14;
    public static final int NUMBER_OF_GOALS = 38;
    public static final int NUMBER_OF_ROUNDS_PREDICTED = 5;
    protected static final int SAVED_GAMEDATA_SET_SIZE = 50;
*/

    protected GameData[] gameDataArray;

    public TrainingData() {
    }

    public void saveARFFToDirectory(String directoryName, boolean initFiles) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void saveNNTDToDirectory(String directoryName, boolean initFiles) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void saveBNToDirectory(String directoryName, boolean initFiles) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void convert(String convertType, String modulename, String aiDataLoadDirectory, String aiDataSaveDirectory) throws IOException {
        File directory = new File(aiDataLoadDirectory);
        if (directory.exists() && directory.isDirectory()) {
            File winnerLoserFile = new File(aiDataLoadDirectory + "/winner-loser.dat");
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".xml");
                }
            };
            File[] files;
            if (!modulename.equals("ig_mission")) {
                if (winnerLoserFile.exists()) {
                    debug("Using winner-loser.dat");
                    Vector fileVector = new Vector();

                    try {
                        BufferedReader f = new BufferedReader(new FileReader(winnerLoserFile.getPath()));

                        for(String line = f.readLine(); line != null; line = f.readLine()) {
                            fileVector.add(new File(line));
                        }

                        f.close();
                    } catch (IOException var17) {
                        System.err.println("Unable to open file: " + winnerLoserFile.getPath());
                    }

                    files = (File[])((File[])fileVector.toArray(new File[fileVector.size()]));
                } else {
                    files = directory.listFiles(filter);
                    files = this.makeWinnerLoserList(files);

                    try {
                        BufferedWriter f = new BufferedWriter(new FileWriter(winnerLoserFile.getPath()));

                        for(int i = 0; i < files.length; ++i) {
                            f.write(files[i].getAbsolutePath());
                            f.newLine();
                        }

                        f.close();
                    } catch (IOException var16) {
                        System.err.println("Unable to write file: " + winnerLoserFile.getPath());
                    }

                    debug("Saved list to " + winnerLoserFile.getPath());
                }
            } else {
                files = directory.listFiles(filter);
            }

            this.gameDataArray = new GameData[50];
            int gameDataIndex = 0;
            boolean initFiles = true;

            for(int fileIndex = 0; fileIndex < files.length; ++fileIndex) {
                GameData gameData = new GameData();
                String filename = files[fileIndex].getPath();
                this.loadModuleData(gameData, filename);
                this.gameDataArray[gameDataIndex] = gameData;
                ++gameDataIndex;
                debug(ProgressStats.getText(fileIndex, files.length) + " Training data loaded from", filename);
                if (gameDataIndex == 50 || fileIndex == files.length - 1) {
                    if (fileIndex == files.length - 1) {
                        GameData[] tmp = this.gameDataArray;
                        this.gameDataArray = new GameData[gameDataIndex];

                        for(int i = 0; i < gameDataIndex; ++i) {
                            this.gameDataArray[i] = tmp[i];
                        }
                    }

                    if (convertType.equals("nn") || convertType.equals("all")) {
                        this.saveNNTDToDirectory(aiDataSaveDirectory + modulename + "/nn/", initFiles);
                    }

                    if (convertType.equals("dt") || convertType.equals("all")) {
                        this.saveARFFToDirectory(aiDataSaveDirectory + modulename + "/dt/", initFiles);
                    }

                    if (convertType.equals("bn") || convertType.equals("all")) {
                        this.saveBNToDirectory(aiDataSaveDirectory + modulename + "/bn/", initFiles);
                    }

                    gameDataIndex = 0;
                    initFiles = false;
                }
            }

        } else {
            throw new IOException("Could not find directory " + directory);
        }
    }

    private File[] makeWinnerLoserList(File[] files) {
        debug("Started to construct file list with an equal amount of winners and losers");
        WinnerLoserFileList list = new WinnerLoserFileList();
        int printEveryMillisecond = 5000;
        long timeStamp = System.currentTimeMillis();

        for(int i = 0; i < files.length; ++i) {
            list.add(files[i]);
            if (System.currentTimeMillis() > timeStamp + (long)printEveryMillisecond || i == files.length - 1) {
                debug(ProgressStats.getText(i, files.length) + " Examined winner and loser files");
                timeStamp = System.currentTimeMillis();
            }
        }

        File[] result = list.getList();
        debug("Finished constructing file list");
        return result;
    }

    public void setData(GameData gameData) {
        if (gameData != null) {
            this.gameDataArray = new GameData[1];
            this.gameDataArray[0] = gameData;
        } else {
            this.gameDataArray = null;
        }

    }

    public static void debug(String message) {
        System.out.println(message);
    }

    public static void debug(String message, String filename) {
        try {
            filename = (new File(filename)).getCanonicalPath();
        } catch (Exception var3) {
        }

        debug(message + " " + filename);
    }

    protected void createDirectory(String directoryName) {
        File directory = new File(directoryName);
        if (!directory.exists()) {
            directory.mkdirs();
        }

    }

    private void loadModuleData(GameData gameData, String filename) {
        XMLInput input = new XMLInput(filename);

        try {
            input.readLine();
            String playerReference = "";
            String currentPlayer = "";
            String moduleStart = "";
            String moduleEnd = "";
            String dataStart = "";

            for(String dataEnd = ""; !input.endReached(); input.readLine()) {
                if (input.contains("<module_run>")) {
                    moduleStart = input.getText();
                    moduleEnd = "</" + moduleStart.substring(1);
                    dataStart = "<input>";
                    dataEnd = "</output>";
                } else if (input.contains("<attack_plan>")) {
                    moduleStart = input.getText();
                    moduleEnd = "</" + moduleStart.substring(1);
                    dataStart = "<data_used>";
                    dataEnd = "</attack_plan_extended>";
                } else if (input.contains("<defense_plan>")) {
                    moduleStart = input.getText();
                    moduleEnd = "</" + moduleStart.substring(1);
                    dataStart = "<data_used>";
                    dataEnd = "</merged_plan_element>";
                }

                if (!moduleStart.equals("")) {
                    do {
                        input.readLine();
                        if (input.contains("<player_reference>")) {
                            input.readLine();
                            playerReference = input.getText();

                            do {
                                input.readLine();
                            } while(!input.contains("</player_reference>"));
                        }

                        String boardFile;
                        if (input.contains("<game_file>")) {
                            input.readLine();
                            boardFile = (new File(filename)).getParent() + "/../../" + input.getText();
                            this.loadGameData(gameData, boardFile);
                            gameData.setPlayerReference(playerReference);

                            do {
                                input.readLine();
                            } while(!input.contains("</game_file>"));
                        }

                        if (input.contains(dataStart)) {
                            do {
                                input.readLine();
                                if (input.contains("<board_file>")) {
                                    input.readLine();
                                    boardFile = (new File(filename)).getParent() + "/../../boardstates/" + input.getText();
                                    this.loadBoardData(gameData, boardFile);
                                    gameData.setCurrentPlayer(gameData.getPlayerReference());

                                    do {
                                        input.readLine();
                                    } while(!input.contains("</board_file>"));
                                } else if (input.contains("<opp_risk_cards>")) {
                                    this.loadOpponentRiskCards(input, gameData);
                                } else if (input.contains("<beginning_of_turn>")) {
                                    this.loadBeginningOfTurn(input, gameData);
                                } else if (input.contains("<continent_estimates>")) {
                                    this.loadContinentEstimate(input, gameData);
                                } else if (input.contains("<missions_estimates>")) {
                                    this.loadMissionsEstimate(input, gameData);
                                } else if (input.contains("<winning_estimates>")) {
                                    this.loadWinningEstimate(input, gameData);
                                } else if (input.contains("<goal_distribution>")) {
                                    this.loadGoalDistribution(input, gameData);
                                } else if (input.contains("<past>")) {
                                    this.loadPast(input, gameData, filename);
                                } else if (input.contains("<nextmove_estimate>")) {
                                    this.loadNextMoveEstimate(input, gameData);
                                } else if (input.contains("<ownership>")) {
                                    this.loadOwnership(input, gameData);
                                } else if (input.contains("<influence_map>")) {
                                    this.loadInfluenceMap(input, gameData);
                                } else if (input.contains("<territory>")) {
                                    this.loadTerritoryOutput(input, gameData);
                                } else if (input.contains("<attack_plan_extended>")) {
                                    this.loadExtendedAttackPlan(input, gameData);
                                } else if (input.contains("<merged_plan_element>")) {
                                    this.loadMergedPlanElement(input, gameData);
                                } else if (input.contains("<merged_plan_list>")) {
                                    this.loadMergedPlanList(input, gameData);
                                } else if (input.contains("<armies_left>")) {
                                    this.loadArmiesLeft(input, gameData);
                                }
                            } while(!input.contains(dataEnd));
                        }
                    } while(!input.contains(moduleEnd));
                }
            }
        } catch (EOFException var11) {
        }

    }

    private void loadBoardData(GameData gameData, String filename) {
        XMLInput input = new XMLInput(filename);
        int territoryIndex = 0;

        try {
            input.readLine();

            for(; !input.endReached(); input.readLine()) {
                if (input.contains("<boardstate>")) {
                    do {
                        input.readLine();
                        if (input.contains("<territory")) {
                            do {
                                String territoryName = input.getText("name");
                                input.readLine();
                                String owner = null;
                                if (input.contains("<owner>")) {
                                    input.readLine();
                                    owner = input.getText();

                                    do {
                                        input.readLine();
                                    } while(!input.contains("</owner>"));
                                }

                                input.readLine();
                                int armies = 0;
                                if (input.contains("<armies>")) {
                                    input.readLine();
                                    armies = Integer.parseInt(input.getText());

                                    do {
                                        input.readLine();
                                    } while(!input.contains("</armies>"));
                                }

                                gameData.setBoard(territoryIndex, new TerritoryAttribute(territoryName, owner, armies));
                                ++territoryIndex;
                                input.readLine();
                            } while(!input.contains("</territory>"));
                        }
                    } while(!input.contains("</boardstate>"));
                }
            }
        } catch (EOFException var8) {
        }

    }

    private void loadGameData(GameData gameData, String filename) {
        int numberOfPlayers = 0;
        XMLInput input = new XMLInput(filename);
        Vector playerMissions = new Vector();

        try {
            input.readLine();

            for(; !input.endReached(); input.readLine()) {
                if (input.contains("<game>")) {
                    do {
                        input.readLine();
                        if (input.contains("<player")) {
                            gameData.setPlayerColor(numberOfPlayers, input.getText("color"));
                            ++numberOfPlayers;

                            do {
                                input.readLine();
                                if (input.contains("<mission>")) {
                                    input.readLine();
                                    playerMissions.add(input.getText());

                                    do {
                                        input.readLine();
                                    } while(!input.contains("</mission>"));
                                }
                            } while(!input.contains("</player>"));
                        }

                        if (input.contains("<winner>")) {
                            input.readLine();
                            gameData.setWinningPlayer(input.getText());

                            do {
                                input.readLine();
                            } while(!input.contains("</winner>"));
                        }
                    } while(!input.contains("</game>"));
                }
            }
        } catch (EOFException var7) {
        }

        gameData.setNumberOfPlayers(numberOfPlayers);

        for(int playerIndex = 0; playerIndex < playerMissions.size(); ++playerIndex) {
            gameData.setPlayerMissionIndex(playerIndex, this.calcMissionIndexFromString((String)playerMissions.get(playerIndex), gameData.getPlayerColors()));
        }

    }

    private void loadContinentEstimate(XMLInput input, GameData data) {
        try {
            int playerIndex = 0;

            do {
                input.readLine();
                int continentIndex = 0;
                if (input.contains("<player")) {
                    do {
                        input.readLine();
                        if (input.contains("<continent")) {
                            int roundIndex = 0;

                            do {
                                input.readLine();
                                if (input.contains("<round")) {
                                    input.readLine();
                                    data.setContinentEstimates(playerIndex, continentIndex, roundIndex, new ContinentEstimateAttribute(CONTINENT_NAMES[continentIndex], Float.parseFloat(input.getText())));

                                    do {
                                        input.readLine();
                                    } while(!input.contains("</round>"));

                                    ++roundIndex;
                                }
                            } while(!input.contains("</continent>"));

                            ++continentIndex;
                        }
                    } while(!input.contains("</player>"));

                    ++playerIndex;
                }
            } while(!input.contains("</continent_estimates>"));
        } catch (EOFException var6) {
        }

    }

    private void loadOpponentRiskCards(XMLInput input, GameData data) {
        try {
            int opponentIndex = 0;

            do {
                input.readLine();
                if (input.contains("<player")) {
                    String opponent = input.getText("color");
                    input.readLine();
                    data.setOpponentCards(opponentIndex, new OpponentCardAttribute(opponentIndex, Integer.parseInt(input.getText())));
                    ++opponentIndex;

                    do {
                        input.readLine();
                    } while(!input.contains("</player>"));
                }
            } while(!input.contains("</opp_risk_cards>"));
        } catch (EOFException var5) {
        }

    }

    private void loadBeginningOfTurn(XMLInput input, GameData data) {
        try {
            input.readLine();
            data.setBeginningOfTurn(new BeginningOfTurnAttribute(Boolean.parseBoolean(input.getText())));

            do {
                input.readLine();
            } while(!input.contains("</beginning_of_turn>"));
        } catch (EOFException var4) {
        }

    }

    private void loadMissionsEstimate(XMLInput input, GameData data) {
        try {
            int playerIndex = 0;

            do {
                input.readLine();
                if (input.contains("<player")) {
                    int missionIndex = 0;

                    do {
                        input.readLine();
                        if (input.contains("<mission")) {
                            input.readLine();
                            data.setMissionEstimates(playerIndex, missionIndex, new MissionEstimateAttribute(MISSION_NAMES[missionIndex], Float.parseFloat(input.getText())));

                            do {
                                input.readLine();
                            } while(!input.contains("</mission>"));

                            ++missionIndex;
                        }
                    } while(!input.contains("</player>"));

                    ++playerIndex;
                }
            } while(!input.contains("</missions_estimates>"));
        } catch (EOFException var5) {
        }

    }

    private void loadWinningEstimate(XMLInput input, GameData data) {
        try {
            int playerIndex = 0;

            do {
                input.readLine();
                if (input.contains("<player")) {
                    do {
                        input.readLine();
                        if (input.contains("<round")) {
                            int roundIndex = 0;
                            input.readLine();
                            data.setWinningEstimates(playerIndex, roundIndex, new WinningEstimateAttribute(Float.parseFloat(input.getText())));

                            do {
                                input.readLine();
                            } while(!input.contains("</round>"));

                            ++roundIndex;
                        }
                    } while(!input.contains("</player>"));

                    ++playerIndex;
                }
            } while(!input.contains("</winning_estimate>"));
        } catch (EOFException var5) {
        }

    }

    private void loadPast(XMLInput input, GameData data, String filename) {
        try {
            Vector<String> boards = new Vector();
            Vector<String> actions = new Vector();

            do {
                input.readLine();
                if (input.contains("<board_past>")) {
                    input.readLine();

                    while(!input.contains("</board_past>")) {
                        boards.add(input.getText());
                        input.readLine();
                    }
                } else if (input.contains("<action_past>")) {
                    input.readLine();

                    while(!input.contains("</action_past>")) {
                        actions.add(input.getText());
                        input.readLine();
                    }
                }
            } while(!input.contains("</past>"));

            if (boards.size() == actions.size()) {
                for(int i = 0; i < actions.size(); ++i) {
                    TerritoryAttribute[] board = new TerritoryAttribute[42];
                    String[] boardStr = ((String)boards.get(i)).split(";");
                    String[] actionStr = ((String)actions.get(i)).split(" ");

                    for(int j = 0; j < boardStr.length; ++j) {
                        if (boardStr[j] != "") {
                            String[] s = boardStr[j].split(",");
                            board[j] = new TerritoryAttribute(TERRITORY_NAMES[j], data.getPlayerColor(Integer.parseInt(s[1])), Integer.parseInt(s[0]));
                        }
                    }

                    data.addPastElement_BoardState(board);
                    data.addPastElement_Action((String)actions.get(i));
                }
            } else {
                System.out.println("Error loading past (" + filename + "): number of boardstates does not match number of actions (words containing the letters F, U, C and K is allowed!)");
            }
        } catch (EOFException var12) {
        }

    }

    private void loadNextMoveEstimate(XMLInput input, GameData data) {
        try {
            input.readLine();

            while(!input.contains("</nextmove_estimate>")) {
                data.setNextMove(input.getText());
                input.readLine();
            }
        } catch (EOFException var4) {
        }

    }

    private void loadOwnership(XMLInput input, GameData data) {
        try {
            int playerIndex = 0;

            do {
                input.readLine();
                int continentIndex = 0;
                if (input.contains("<player")) {
                    do {
                        input.readLine();
                        if (input.contains("<continent")) {
                            input.readLine();
                            data.setOwnership(playerIndex, continentIndex, new OwnershipAttribute(CONTINENT_NAMES[continentIndex], Boolean.parseBoolean(input.getText())));

                            do {
                                input.readLine();
                            } while(!input.contains("</continent>"));

                            ++continentIndex;
                        }
                    } while(!input.contains("</player>"));

                    ++playerIndex;
                }
            } while(!input.contains("</ownership>"));
        } catch (EOFException var5) {
        }

    }

    private void loadInfluenceMap(XMLInput input, GameData data) {
        try {
            int territoryIndex = 0;
            input.readLine();

            while(!input.contains("</influence_map>")) {
                data.setInfluenceMap(territoryIndex, Integer.parseInt(input.getText()));
                ++territoryIndex;
                input.readLine();
            }
        } catch (EOFException var4) {
        }

    }

    private void loadTerritoryOutput(XMLInput input, GameData data) {
        try {
            input.readLine();

            while(!input.contains("</territory>")) {
                data.setTerritoryOutput(input.getText());
                input.readLine();
            }
        } catch (EOFException var4) {
        }

    }

    private void loadGoalDistribution(XMLInput input, GameData data) {
        try {
            int goalIndex = 0;

            do {
                input.readLine();
                if (input.contains("<continent")) {
                    input.readLine();
                    data.setGoalDistribution(goalIndex, Float.parseFloat(input.getText()));

                    do {
                        input.readLine();
                    } while(!input.contains("</continent>"));

                    ++goalIndex;
                } else if (input.contains("<player")) {
                    input.readLine();
                    data.setGoalDistribution(goalIndex, Float.parseFloat(input.getText()));

                    do {
                        input.readLine();
                    } while(!input.contains("</player>"));

                    ++goalIndex;
                } else if (input.contains("<territories_")) {
                    while(goalIndex < 36) {
                        data.setGoalDistribution(goalIndex, 0.0F);
                        ++goalIndex;
                    }

                    input.readLine();
                    data.setGoalDistribution(goalIndex, Float.parseFloat(input.getText()));

                    do {
                        input.readLine();
                    } while(!input.contains("</territories_"));

                    ++goalIndex;
                }
            } while(!input.contains("</goal_distribution>"));
        } catch (EOFException var4) {
        }

    }

    private void loadExtendedAttackPlan(XMLInput input, GameData data) {
        try {
            Vector territories = new Vector();
            Vector armies_to_leave = new Vector();
            int estimatedCost = 0;
            int minimumCost = 0;
            float priority = 0.0F;
            float score = 0.0F;

            do {
                input.readLine();
                if (input.contains("<attack_plan>")) {
                    input.readLine();
                    if (input.contains("<territories>")) {
                        input.readLine();

                        while(!input.contains("</territories>")) {
                            territories.add(input.getText());
                            input.readLine();
                        }
                    }

                    input.readLine();
                    if (input.contains("<armies_to_leave>")) {
                        input.readLine();

                        while(!input.contains("</armies_to_leave>")) {
                            armies_to_leave.add(input.getText());
                            input.readLine();
                        }
                    }

                    if (territories.size() != armies_to_leave.size()) {
                        throw new RuntimeException("Error loading extended attack plan! Number of territories should equal to number of armies_to_leave.");
                    }

                    do {
                        input.readLine();
                    } while(!input.contains("</attack_plan>"));
                } else if (input.contains("<cost_estimated>")) {
                    input.readLine();
                    estimatedCost = Integer.parseInt(input.getText());

                    do {
                        input.readLine();
                    } while(!input.contains("</cost_estimated>"));
                } else if (input.contains("<cost_minimum>")) {
                    input.readLine();
                    minimumCost = Integer.parseInt(input.getText());

                    do {
                        input.readLine();
                    } while(!input.contains("</cost_minimum>"));
                } else if (input.contains("<priority>")) {
                    input.readLine();
                    priority = Float.parseFloat(input.getText());

                    do {
                        input.readLine();
                    } while(!input.contains("</priority>"));
                } else if (input.contains("<score>")) {
                    input.readLine();
                    score = Float.parseFloat(input.getText());

                    do {
                        input.readLine();
                    } while(!input.contains("</score>"));
                }
            } while(!input.contains("</attack_plan_extended>"));

            TerritoryAttribute[] attackPlan = new TerritoryAttribute[42];

            for(int i = 0; i < territories.size(); ++i) {
                int indexOf;
                for(indexOf = TERRITORY_NAMES.length - 1; indexOf > -1 && !TERRITORY_NAMES[indexOf].equals((String)territories.get(i)); --indexOf) {
                }

                attackPlan[indexOf] = new TerritoryAttribute((String)territories.get(i), data.getCurrentPlayer(), Integer.parseInt((String)armies_to_leave.get(i)));
                attackPlan[indexOf].setCurrentPlayer(data.getCurrentPlayer());
            }

            data.setExtendedAttackPlan(new ExtendedAttackPlan(minimumCost, estimatedCost, priority, score, attackPlan));
            data.setPriority(priority);
            data.setEstimatedCost(estimatedCost);
            data.setMinimumCost(minimumCost);
            data.setScore(score);
            TerritoryAttribute firstTerritory = new TerritoryAttribute((String)territories.get(0), data.getCurrentPlayer(), Integer.parseInt((String)armies_to_leave.get(0)));
            firstTerritory.setCurrentPlayer(data.getCurrentPlayer());
            data.setAttackPlan(attackPlan, firstTerritory);
        } catch (EOFException var12) {
        }

    }

    private void loadMergedPlanElement(XMLInput input, GameData data) {
        try {
            String territory = "";
            int attackerArmiesLeft = 0;
            int estimatedCost = 0;
            int minimumCost = 0;
            float priority = 0.0F;
            float score = 0.0F;
            boolean isDefensePlan = true;

            do {
                input.readLine();
                if (input.contains("<territory>")) {
                    input.readLine();
                    territory = input.getText();

                    do {
                        input.readLine();
                    } while(!input.contains("</territory>"));
                } else if (input.contains("<cost_estimated>")) {
                    input.readLine();
                    estimatedCost = Integer.parseInt(input.getText());

                    do {
                        input.readLine();
                    } while(!input.contains("</cost_estimated>"));
                } else if (input.contains("<cost_minimum>")) {
                    input.readLine();
                    minimumCost = Integer.parseInt(input.getText());

                    do {
                        input.readLine();
                    } while(!input.contains("</cost_minimum>"));
                } else if (input.contains("<priority>")) {
                    input.readLine();
                    priority = Float.parseFloat(input.getText());

                    do {
                        input.readLine();
                    } while(!input.contains("</priority>"));
                } else if (input.contains("<attacker_armies_left>")) {
                    input.readLine();
                    attackerArmiesLeft = Integer.parseInt(input.getText());

                    do {
                        input.readLine();
                    } while(!input.contains("</attacker_armies_left>"));
                } else if (input.contains("<is_defense_plan>")) {
                    input.readLine();
                    isDefensePlan = Boolean.parseBoolean(input.getText());

                    do {
                        input.readLine();
                    } while(!input.contains("</is_defense_plan>"));
                } else if (input.contains("<score>")) {
                    input.readLine();
                    score = Float.parseFloat(input.getText());

                    do {
                        input.readLine();
                    } while(!input.contains("</score>"));
                }
            } while(!input.contains("</merged_plan_element>"));

            data.setDefensePlanOutput(new MergedPlanElement(territory, minimumCost, estimatedCost, priority, score, attackerArmiesLeft, isDefensePlan));
            data.setMergedPlanTerritory(territory);
            data.setPriority(priority);
            data.setEstimatedCost(estimatedCost);
            data.setMinimumCost(minimumCost);
            data.setScore(score);
            data.setAttackerArmiesLeft(attackerArmiesLeft);
            data.setIsDefensePlan(isDefensePlan);
        } catch (EOFException var10) {
        }

    }

    private void loadMergedPlanList(XMLInput input, GameData data) {
        while(true) {
            try {
                input.readLine();
                if (input.contains("<merged_plan_element>")) {
                    String territory = "";
                    int attackerArmiesLeft = 0;
                    int estimatedCost = 0;
                    int minimumCost = 0;
                    float priority = 0.0F;
                    float score = 0.0F;
                    boolean isDefensePlan = true;

                    while(true) {
                        input.readLine();
                        if (input.contains("<territory>")) {
                            input.readLine();
                            territory = input.getText();

                            do {
                                input.readLine();
                            } while(!input.contains("</territory>"));
                        } else if (input.contains("<cost_estimated>")) {
                            input.readLine();
                            estimatedCost = Integer.parseInt(input.getText());

                            do {
                                input.readLine();
                            } while(!input.contains("</cost_estimated>"));
                        } else if (input.contains("<cost_minimum>")) {
                            input.readLine();
                            minimumCost = Integer.parseInt(input.getText());

                            do {
                                input.readLine();
                            } while(!input.contains("</cost_minimum>"));
                        } else if (input.contains("<priority>")) {
                            input.readLine();
                            priority = Float.parseFloat(input.getText());

                            do {
                                input.readLine();
                            } while(!input.contains("</priority>"));
                        } else if (input.contains("<attacker_armies_left>")) {
                            input.readLine();
                            attackerArmiesLeft = Integer.parseInt(input.getText());

                            do {
                                input.readLine();
                            } while(!input.contains("</attacker_armies_left>"));
                        } else if (input.contains("<is_defense_plan>")) {
                            input.readLine();
                            isDefensePlan = Boolean.parseBoolean(input.getText());

                            do {
                                input.readLine();
                            } while(!input.contains("</is_defense_plan>"));
                        } else if (input.contains("<score>")) {
                            input.readLine();
                            score = Float.parseFloat(input.getText());

                            do {
                                input.readLine();
                            } while(!input.contains("</score>"));
                        }

                        if (input.contains("</merged_plan_element>")) {
                            data.getMergedPlanList().add(new MergedPlanElement(territory, minimumCost, estimatedCost, priority, score, attackerArmiesLeft, isDefensePlan));
                            break;
                        }
                    }
                }

                if (!input.contains("</merged_plan_list>")) {
                    continue;
                }
            } catch (EOFException var10) {
            }

            return;
        }
    }

    private void loadArmiesLeft(XMLInput input, GameData data) {
        try {
            input.readLine();
            data.setArmiesLeft(Integer.parseInt(input.getText()));

            do {
                input.readLine();
            } while(!input.contains("</armies_left>"));
        } catch (EOFException var4) {
        }

    }

    protected String convertToBnState(String input) {
        String[] split = input.split(";");
        return split[0] + "_" + split[1];
    }

    private int calcMissionIndexFromString(String mission, String[] playerColors) {
        if (mission.equals("18")) {
            return 6;
        } else if (mission.equals("24")) {
            return 7;
        } else if (mission.startsWith("kill")) {
            return 8 + this.getPlayerIndexFromColor(mission.substring(5), playerColors);
        } else {
            String[] split = mission.split(",");
            if (split[0].equals(CONTINENT_NAMES[4]) && split[1].equals(CONTINENT_NAMES[1])) {
                return 0;
            } else if (split[0].equals(CONTINENT_NAMES[2]) && split[1].equals(CONTINENT_NAMES[5])) {
                return 1;
            } else if (split[0].equals(CONTINENT_NAMES[4]) && split[1].equals(CONTINENT_NAMES[3])) {
                return 2;
            } else if (split[0].equals(CONTINENT_NAMES[2]) && split[1].equals(CONTINENT_NAMES[1])) {
                return 3;
            } else if (split[0].equals(CONTINENT_NAMES[0]) && split[1].equals(CONTINENT_NAMES[5])) {
                return 4;
            } else if (split[0].equals(CONTINENT_NAMES[0]) && split[1].equals(CONTINENT_NAMES[3])) {
                return 5;
            } else {
                throw new IllegalArgumentException("Did not find a matching mission!");
            }
        }
    }

    private int getPlayerIndexFromColor(String color, String[] playerColors) {
        int i;
        for(i = playerColors.length - 1; i > -1 && !playerColors[i].equals(color); --i) {
        }

        if (i == -1) {
            throw new IllegalArgumentException("Could not find the color of a player in the player-array!");
        } else {
            return i;
        }
    }
}
