package net.yura.domination.lobby.server;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import junit.framework.TestCase;
import net.yura.domination.engine.OnlineUtil;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.ai.AIManager;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import net.yura.lobby.server.LobbySession;
import net.yura.lobby.server.ServerGame;
import net.yura.lobby.server.ServerGameListener;
import net.yura.lobby.model.Game;

import static junit.framework.TestCase.assertEquals;

/**
 * WARNING! this test is NOT compatible to being run with other tests in the same VM!
 */
public class ServerGameTest extends TestCase {

    ServerGame serverGame;

    public static void setupMapsForTest() throws Exception {

        File test = new File(".");
        System.out.println("running tests in " + test.getCanonicalFile());

        // we must change the current folder for map loading to work
        String testRoot = System.getProperty("user.dir");
        if ("/".equals(testRoot)) {
            throw new RuntimeException("current dir set incorrectly! /");
        }
        System.setProperty("user.dir", testRoot + File.separator + "game");

        // force static init to run that setups map access
        Class.forName(ServerGameRisk.class.getName());

        // after we have done the ServerGameRisk init, we can revert to the standard value
        // this will allow this method to run multiple tiles without messing anything up
        System.setProperty("user.dir", testRoot);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setupMapsForTest();

        AIManager.setWait(0);
        Risk.setShowDice(false);

        // WARNING!! this will call the ServerGameRisk static initializer!! (that sets RiskUtil.streamOpener)
        // WARNING!! if this test is not run forked, it will break tests that come after!!
        serverGame = new ServerGameRisk() {
            AIManager fakeHuman = new AIManager();
            @Override
            public void getInputFromSomeone() {
                super.getInputFromSomeone();
                
                RiskGame game = myrisk.getGame();
                Player player = game.getCurrentPlayer();

                // Player should never be null, but better not to crash here
                if (player != null && player.getType() == Player.PLAYER_HUMAN && game.getState() != RiskGame.STATE_GAME_OVER &&
                        (!player.getAutoDefend() || game.getState() != RiskGame.STATE_DEFEND_YOURSELF)) {

                    //System.out.println("getInputFromSomeone " + player);
                    String move = fakeHuman.getOutput(game, Player.PLAYER_AI_HARD);
                    serverGame.messageFromUser(player.getName(), move);
                }
                else {
                    //System.out.println("getInputFromSomeone " + player+" IGNORED");
                }
            }
        };

        serverGame.addServerGameListener(new ServerGameListener() {
            @Override
            public void messageFromGame(Object message, Collection<LobbySession> towho) {
                System.out.println("messageFromGame " + message);
            }

            @Override
            public void sendChatroomMessage(String message) {
                System.out.println("sendChatroomMessage " + message);
            }

            @Override
            public void needInputFrom(String whoiwantinputfrom) {
                System.out.println("needInputFrom " + whoiwantinputfrom);
            }

            @Override
            public void resignPlayer(String username) {
                System.out.println("resignPlayer " + username);
            }

            @Override
            public void gameStarted() {
                System.out.println("gameStarted");
            }

            @Override
            public boolean gameFinished(Map<String,Integer> winner) {
                System.out.println("gameFinished " + winner);
                return false;
            }

            @Override
            public void logGameMove(String user, String move) {
                System.out.println("logGameMove " + user + " " + move);
            }
        });
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        serverGame.destroyServerGame();
    }

    public void testPlayGame() throws Exception {
        String startGameOptions = OnlineUtil.createGameString(1, 1, 1, RiskGame.MODE_DOMINATION, RiskGame.CARD_INCREASING_SET, false, true, "luca.map");

        String[] players = new String[] {"bob1", "fred2"};

        Game game = new Game();
        game.setOptions(startGameOptions);
        serverGame.setGameDetails(game);
        serverGame.startGame(players);

        Risk myrisk = ((ServerGameRisk)serverGame).myrisk;

        assertEquals(5, myrisk.getGame().getPlayers().size());

        while(!serverGame.isFinished()) {
            Thread.sleep(1000);
            Thread.yield();
        }
    }
}
