package net.yura.domination.engine.ai.framework;

import net.yura.domination.engine.ai.AI;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import risk.AI.AIPlayerFramework;
import risk.AI.Data_Structures.T_AISettings;
import risk.AI.Data_Structures.T_Board;
import risk.AI.Data_Structures.T_Game;
import risk.AI.Data_Structures.T_Past;
import risk.AI.Data_Structures.T_TrainingExampleWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;

/**
 * Only one class per AI type is created
 */
public abstract class AIPlayerFrameworkAbstract implements AI {

    // TODO YURA no reason for this to be static and here, remove this and just pass settings directly to AI
    public static Vector<String[]> aiComposition = new Vector();

    static WeakHashMap<RiskGame, AIPlayerFrameworkGlobal> gameGlobal = new WeakHashMap<>();

    AIPlayerFramework currentFrameworkAI;

    public static class AIPlayerFrameworkGlobal {

        private RiskGame riskGame;
        Map<Player, AIPlayerFramework> aiPlayers = new HashMap<>();

        private T_Past past;
        private T_TrainingExampleWriter trainingExample;
        private T_Board board;
        private T_Game game;
        private T_AISettings aiSettings;
        private String winnerStatsDir;

        public AIPlayerFrameworkGlobal(RiskGame riskGame) {
            this.riskGame = riskGame;

            // check this AI is able to play in this game
            if (riskGame.getGameMode() != RiskGame.MODE_SECRET_MISSION) throw new IllegalStateException();
            if (riskGame.getNoContinents() != T_Game.NUMBER_OF_CONTINENTS) throw new IllegalStateException();
            if (riskGame.getNoCountries() != T_Game.NUMBER_OF_TERRITORIES) throw new IllegalStateException();
            // TODO YURA it seems the AI has been trained on a board with East-Africa - Middle-East connection present

            this.aiSettings  = new T_AISettings("AIPlayerFrameworkSettings.txt");

            this.past = new T_Past(riskGame.getPlayers(), new java.util.Vector(Arrays.asList(riskGame.getContinents())));
            this.board = new T_Board(riskGame);

            this.game = new T_Game(riskGame, board, aiSettings);
            this.trainingExample = new T_TrainingExampleWriter(riskGame, aiSettings, aiSettings.saveTrainingExample, "ai-data/");
            this.trainingExample.init(riskGame); // this used to be done AFTER player init, but i hope it will work if done before
        }

        public AIPlayerFramework getFrameworkAI(Player currentplayer) {
            return aiPlayers.computeIfAbsent(currentplayer, k -> {
                int type = currentplayer.getType();
                AIPlayerFramework newPlayer;
                switch (type) {
                    case AIPlayerFrameworkScripted.TYPE:  // Framework
                        newPlayer = new AIPlayerFramework(currentplayer, AIPlayerFrameworkGlobal.this, "(AI Framework_scripted)>", "core.help.move.ai.framework", past, trainingExample, true, type);
                        break;
                    case AIPlayerFrameworkCustom.TYPE:  // Customized framework
                        newPlayer = new AIPlayerFramework(currentplayer, AIPlayerFrameworkGlobal.this, "(AI Framework_custom)>", "core.help.move.ai.framework_custom", past, trainingExample, false, type);
                        break;
                    case AIPlayerFrameworkBest.TYPE:  // Framework
                        String[] best = {"nn", "script", "nn_wo", "nn_wo", "script", "script", "script", "script", "script", "script", "nn_wo", "script", "script", "script", "script", "script", "script"};
                        aiComposition.add(best);
                        newPlayer = new AIPlayerFramework(currentplayer, AIPlayerFrameworkGlobal.this, "(AI Framework_best)>", "core.help.move.ai.framework_best", past, trainingExample, false, type);
                        break;
                    default:
                        throw new IllegalArgumentException("unexpected AI type: " + type);
                }
                // Initialize AIPlayerFramework (setup board etc.)
                newPlayer.init(this.past, this.game, this.board);
                return newPlayer;
            });
        }

        public void setWinnerStatsDir(String winnerStatsDir) {
            this.winnerStatsDir = winnerStatsDir;
        }

        public RiskGame getGame() {
            return riskGame;
        }

        public T_AISettings getAISettings() {
            return this.aiSettings;
        }

        public T_TrainingExampleWriter getTrainingExampleWriter() {
            return this.trainingExample;
        }

        public void gameOver(Player winner) {
            this.trainingExample.endAllFrameworkTimer();
            this.trainingExample.closeGameAndSave(winner);
            if (this.aiSettings.saveWinnerFile) {
                this.trainingExample.saveWinnerFile(winner, this.winnerStatsDir);
            }
            if (this.aiSettings.saveFullGameStats) {
                this.trainingExample.saveGameStats(winner, aiPlayers);
            }
        }
    }

    @Override
    public void setGame(RiskGame riskGame) {
        if (riskGame == null) {
            
            // TODO YURA we can prob just get it from the currentFrameworkAI
            //AIPlayerFrameworkGlobal ais = currentFrameworkAI.frameworkGame;
            //ais.gameOver(ais.riskGame.getCurrentPlayer());
            //gameGlobal.remove(ais.riskGame);
            
            for (AIPlayerFrameworkGlobal global : gameGlobal.values()) {
                if (global.aiPlayers.containsValue(currentFrameworkAI)) {
                    if (global.riskGame.getState() == RiskGame.STATE_GAME_OVER) {
                        global.gameOver(global.riskGame.getCurrentPlayer());
                        gameGlobal.remove(global.riskGame);
                        return;
                    }
                }
            }
        }
        else {
            Player currentplayer = riskGame.getCurrentPlayer();
            AIPlayerFrameworkGlobal ais = gameGlobal.computeIfAbsent(riskGame, k -> new AIPlayerFrameworkGlobal(riskGame));
            currentFrameworkAI = ais.getFrameworkAI(currentplayer);
        }
    }

    @Override
    public String getBattleWon() {
        return currentFrameworkAI.play();
    }
    @Override
    public String getTacMove() {
        return currentFrameworkAI.play();
    }
    @Override
    public String getTrade() {
        return currentFrameworkAI.play();
    }
    @Override
    public String getPlaceArmies() {
        return currentFrameworkAI.play();
    }
    @Override
    public String getAttack() {
        return currentFrameworkAI.play();
    }
    @Override
    public String getRoll() {
        return currentFrameworkAI.play();
    }
    @Override
    public String getCapital() {
        return currentFrameworkAI.play();
    }
    @Override
    public String getAutoDefendString() {
        return currentFrameworkAI.play();
    }
}
