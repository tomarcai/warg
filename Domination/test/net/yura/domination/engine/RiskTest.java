package net.yura.domination.engine;

import junit.framework.TestCase;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.engine.core.RiskGameTest;
import net.yura.domination.test.TestUtil;
import java.util.Arrays;

public class RiskTest extends TestCase {

    Risk risk = TestUtil.newRisk();

    public RiskTest(String testName) throws Exception {
        super(testName);
    }

    @Override
    protected void tearDown() throws Exception {
        risk.kill();
    }

    public void testRisk() throws Exception {

        risk.setReplay(true);

        int noPlayers = 3;
        int noCountries = 6;

        RiskGame game = TestUtil.createBasicMap(noCountries);
        risk.newMemoryGame(game);

        assertEquals(RiskGame.STATE_NEW_GAME, game.getState());

        RiskGameTest.addPlayers(game, noPlayers);

        risk.inGameParser("local startgame domination italianlike recycle");
        risk.inGameParser("PLAYER 0");

        for (int i = 0; i < noCountries; i++) {

            assertEquals(RiskGame.STATE_PLACE_ARMIES, game.getState());

            Player p1 = game.getCurrentPlayer();
            
            risk.inGameParser("local placearmies " + (i + 1) + " 1");

            assertEquals(RiskGame.STATE_END_TURN, game.getState());
            //risk.inGameParser("local endgo"); // this does nothing when autoendgo is enabled
            risk.inGameParser("CARD");
            
            Player p2 = game.getCurrentPlayer();
            
            assertNotSame(p1,p2);
        }

        assertEquals(RiskGame.STATE_PLACE_ARMIES, game.getState());


        // each player now has 5 armies
        int armiesLeft = 0;
        for (int p = 0; p < noPlayers; p++) {
            armiesLeft = armiesLeft + ((Player)game.getPlayers().get(p)).getExtraArmies();
        }
        // place all other armies
        for (int c = 0; c < armiesLeft; c++) {
            Player player = game.getCurrentPlayer();

            risk.inGameParser("local placearmies " + ((Country)player.getTerritoriesOwned().get(0)).getColor() + " 1");

            risk.inGameParser("CARD");
        }

        for (int c = 0; c < noPlayers; c++) {

            Player player = game.getCurrentPlayer();
            assertEquals(game.getPlayers().get(c), player);

            assertEquals(RiskGame.STATE_ATTACKING, game.getState());

            //[A] [B] [C] [A] [B] [C]
            // 6   6   6   1   1   1

            Country[] countries = game.getCountries();

            Country attacker = (Country)player.getTerritoriesOwned().get(0);
            Country defender = countries[Arrays.asList(countries).indexOf(attacker) + 1];

            // in italian, attack twice to kill all armies
            //wipeOut(game, attacker, defender, false);
            risk.inGameParser("local attack " + attacker.getColor() + " " + defender.getColor());
            while (attacker.getArmies() > 1) {
                risk.inGameParser("local roll 1");
                risk.inGameParser("local roll 1");
                risk.inGameParser("DICE 1 1 0 5");
            }

            //assertEquals(1, instance.moveArmies(countries[0].getArmies() - 1));
            assertEquals(RiskGame.STATE_ATTACKING, game.getState());

            //[A] [B] [C] [A] [B] [C]
            // 1   6   6   1   1   1

            risk.inGameParser("local endattack");
            assertEquals(RiskGame.STATE_FORTIFYING, game.getState());
            risk.inGameParser("local nomove");

            //[A] [B] [C] [A] [B] [C]
            // 1   6   6   1   1   1

            assertEquals(RiskGame.STATE_END_TURN, game.getState());
            risk.inGameParser("CARD");
            Player nextPlayer = game.getCurrentPlayer();
            assertNotNull(nextPlayer);
            if (player == nextPlayer) {
                assertEquals(noPlayers - 1, c);
            }
        }
        
        assertEquals(RiskGame.STATE_GAME_OVER, game.getState());
    }
}
