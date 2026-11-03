package net.yura.domination.engine.core;

import java.io.File;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskAdapter;
import net.yura.domination.test.FailOnWarningsRule;
import net.yura.domination.test.TestUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PlayGameTest {

    private static final long REPLAY_TIMEOUT_MS = 60000;

    @Rule
    public TestRule failOnWarnings = new FailOnWarningsRule();

    @Test
    public void testDominationFixedGames() throws Exception {
        playGame(Risk.STARTGAME_OPTION_MODE_DOMINATION + " fixed recycle");
    }
    @Test
    public void testDominationIncreasingGames() throws Exception {
        playGame(Risk.STARTGAME_OPTION_MODE_DOMINATION + " increasing recycle");
    }
    @Test
    public void testDominationItalianGames() throws Exception {
        playGame(Risk.STARTGAME_OPTION_MODE_DOMINATION + " italianlike recycle");
    }
    @Test
    public void testDominationItalianAutoplaceallGames() throws Exception {
        playGame(Risk.STARTGAME_OPTION_MODE_DOMINATION + " italianlike autoplaceall recycle");
    }

    @Test
    public void testCapitalFixedGames() throws Exception {
        playGame(Risk.STARTGAME_OPTION_MODE_CAPITAL + " fixed recycle");
    }
    @Test
    public void testCapitalIncreasingGames() throws Exception {
        playGame(Risk.STARTGAME_OPTION_MODE_CAPITAL + " increasing recycle");
    }
    @Test
    public void testCapitalItalianGames() throws Exception {
        playGame(Risk.STARTGAME_OPTION_MODE_CAPITAL + " italianlike recycle");
    }

    @Test
    public void testMissionFixedGames() throws Exception {
        playGame(Risk.STARTGAME_OPTION_MODE_SECRET_MISSION + " fixed recycle");
    }
    @Test
    public void testMissionIncreasingGames() throws Exception {
        playGame(Risk.STARTGAME_OPTION_MODE_SECRET_MISSION + " increasing recycle");
    }
    @Test
    public void testMissionItalianGames() throws Exception {
        playGame(Risk.STARTGAME_OPTION_MODE_SECRET_MISSION + " italianlike recycle");
    }

    public void playGame(String mode) throws Exception {
        File file = TestUtil.getScriptFile(mode);

        assertTrue(file.getAbsolutePath() + " NOT FOUND!", file.exists());

        playGame(file);
    }
    
    public void playGame(File file) throws Exception {
        //InputStream in = ReplayGameTest.class.getResourceAsStream("test.risk");
        final Risk risk = TestUtil.newRisk();

        risk.addRiskListener(new RiskAdapter() {
            public void sendMessage(String output, boolean redrawNeeded, boolean repaintNeeded) {
                //System.out.println(output);
            }

            /**
             * if the game finishes or the replay thread fails, it will call here
             */
            public void needInput(int s) {
                //System.out.println("need input " + s);
                synchronized (risk) {
                    risk.notifyAll();
                }
            }
        });

        risk.parserAndWait("play " + file.getPath()); // maybe getAbsolutePath() for full path

        // Wait until the replay thread has finished the whole script.
        // Waiting only until the game leaves STATE_NEW_GAME is racy: a needInput()
        // notification sent during setup can wake this thread after the replay
        // has already moved on, so the game is still in progress when we assert.
        // The replay thread clears the replay flag and then calls needInput(),
        // whose notifyAll() under this monitor makes the final state visible here.
        long deadline = System.currentTimeMillis() + REPLAY_TIMEOUT_MS;
        synchronized (risk) {
            while (risk.getGame() == null || risk.isReplay()) {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining <= 0) {
                    fail("replay of " + file + " did not finish within " + REPLAY_TIMEOUT_MS + "ms");
                }
                risk.wait(remaining);
            }
        }

        assertEquals(RiskGame.STATE_GAME_OVER, risk.getGame().getState());

        if (risk.getGame().getGameMode() == RiskGame.MODE_DOMINATION) {
            assertEquals(risk.getGame().getNoCountries(), risk.getGame().getCurrentPlayer().getNoTerritoriesOwned());
        }

        System.out.println("winner " + risk.getGame().getCurrentPlayer());
        risk.parserAndWait("closegame");
        risk.kill();
    }
}
