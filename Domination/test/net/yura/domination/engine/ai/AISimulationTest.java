package net.yura.domination.engine.ai;

import junit.framework.TestCase;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskAdapter;
import net.yura.domination.engine.ai.logic.AIDomination;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.test.TestUtil;

public class AISimulationTest extends TestCase {

        private static final int NO_GAMES = 300;
	private static final boolean debug = false;

	int hard;
	int easy;
	int avg;
	int other;

        @Override
        protected void setUp() throws Exception {
            super.setUp();
            
            hard = 0;
            easy = 0;
            avg = 0;
            other = 0;
        }

	public void testDominationFixedGames() throws Exception {
            playGames("domination fixed recycle");
        }
        public void testDominationIncreasingGames() throws Exception {
            playGames("domination increasing recycle");
        }
        public void testDominationItalianGames() throws Exception {
            playGames("domination italianlike recycle");
        }


        public void testCapitalFixedGames() throws Exception {
            playGames("capital fixed recycle");
        }
        public void testCapitalIncreasingGames() throws Exception {
            playGames("capital increasing recycle");
        }
        public void testCapitalItalianGames() throws Exception {
            playGames("capital italianlike recycle");
        }


        public void testMissionFixedGames() throws Exception {
            playGames("mission fixed recycle");
        }
        public void testMissionIncreasingGames() throws Exception {
            playGames("mission increasing recycle");
        }
        public void testMissionItalianGames() throws Exception {
            playGames("mission italianlike recycle");
        }

        public void playGames(String mode) throws Exception {
        
                final Risk risk = TestUtil.newRisk();
		risk.addRiskListener(new RiskAdapter() {

		    public void sendMessage(String output, boolean redrawNeeded, boolean repaintNeeded) {
		    	if (debug) {
		    		System.out.print(output+"\n");
		    	}
		    }

		    public void needInput(int s) {
		    	synchronized (risk) {
		    		risk.notifyAll();
			}
		    }

		    public void noInput() {
		    }
		} );

                long start = System.currentTimeMillis();
		for (int i = 0; i < NO_GAMES; i++) {
			playGame(risk, mode);
                        
                        // save the first game to a script
                        //if (i==0) {
                        //    net.yura.domination.engine.RiskUtil.saveGameLog(TestUtil.getScriptFile(mode), risk.getGame());
                        //}
		}
		System.out.println("-- " + mode + " Wins --\neasy: " + easy + "\naverage: " + avg + "\nhard: " + hard + "\nother: " + other);
		if (debug) {
			System.out.println(System.currentTimeMillis()-start);
		}
		assertTrue(easy <= avg);
		assertTrue(avg <= hard);
		risk.kill();
		risk.join();
	}

	private void playGame(Risk risk, String mode) throws InterruptedException {
		risk.parserAndWait("closegame");
		risk.parser("newgame");

		risk.parser("newplayer ai easy 6 6");
		risk.parser("newplayer ai average 2 2");
		risk.parser("newplayer ai hard 1 1");
		risk.parser("newplayer ai easy 3 3");
		risk.parser("newplayer ai average 4 4");
		risk.parser("newplayer ai hard 5 5");

		risk.parser("startgame " + mode);

                // wait for game to finish
		synchronized (risk) {
			while (risk.getGame() == null || risk.getGame().getState() != RiskGame.STATE_GAME_OVER) {
				risk.wait();
			}
                }

                Player p = risk.getGame().getCurrentPlayer();
                switch (p.getType()) {
                    case AIDomination.PLAYER_AI_AVERAGE:
                        avg++;
                        break;
                    case AIDomination.PLAYER_AI_EASY:
                        easy++;
                        break;
                    case AIDomination.PLAYER_AI_HARD:
                        hard++;
                        break;
                    default:
                        other++;
                        break;
                }

                if (debug) {
                        System.out.println(p);
                }
	}
}
