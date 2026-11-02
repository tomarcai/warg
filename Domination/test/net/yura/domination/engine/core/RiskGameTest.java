package net.yura.domination.engine.core;

import java.util.Arrays;
import junit.framework.TestCase;
import net.yura.domination.engine.ColorUtil;
import net.yura.domination.test.TestUtil;

/**
 * @author Yura Mamyrin
 */
public class RiskGameTest extends TestCase {

    public void testDominationGame() throws Exception {

        int noPlayers = 2;
        int noCountries = 6;

        // for 2 players, minimum need 6 countries
        RiskGame instance = TestUtil.createBasicMap(noCountries);

        addPlayers(instance, noPlayers);

        instance.startGame(RiskGame.MODE_DOMINATION, RiskGame.CARD_FIXED_SET, true, true, 2, true);
        assertEquals(2, instance.getMaxDefendDice());
        instance.setCurrentPlayer(0);

        assertEquals(RiskGame.STATE_PLACE_ARMIES, instance.getState());

        // fill up all empty countries
        for (int i = 0; i < noCountries; i++) {
            assertFalse(instance.NoEmptyCountries());
            assertEquals(1, instance.placeArmy(instance.getCountryInt(i + 1), 1));
            assertNotNull(instance.endGo());
        }

        assertTrue(instance.NoEmptyCountries());

        //[A] [B] [A] [B] [A] [B]
        // 1   1   1   1   1   1

        // each player now has 5 armies
        int armiesLeft = 0;
        for (int p = 0; p < noPlayers; p++) {
            armiesLeft = armiesLeft + ((Player)instance.getPlayers().get(p)).getExtraArmies();
        }
        // place all other armies
        for (int c = 0; c < armiesLeft; c++) {
            Player player = instance.getCurrentPlayer();
            assertEquals(1, instance.placeArmy((Country)player.getTerritoriesOwned().get(0), 1));
            assertNotNull(instance.endGo());
        }

        //[A] [B] [A] [B] [A] [B]
        // 6   6   1   1   1   1

        assertEquals(RiskGame.STATE_PLACE_ARMIES, instance.getState());
        Player player = instance.getCurrentPlayer();
        // we get 3 extra armie at the start of our turn
        assertEquals(3, player.getExtraArmies());

        Country[] countries = instance.getCountries();

        assertEquals(countries[0], (Country)player.getTerritoriesOwned().get(0));
        // place 3 extra army in first country
        assertEquals(1, instance.placeArmy((Country)player.getTerritoriesOwned().get(0), player.getExtraArmies()));

        assertEquals(RiskGame.STATE_ATTACKING, instance.getState());

        //[A] [B] [A] [B] [A] [B]
        // 9   6   1   1   1   1

        wipeOut(instance, countries[0], countries[1], true);

        assertEquals(1, instance.moveArmies(countries[0].getArmies() - 1));
        assertEquals(RiskGame.STATE_ATTACKING, instance.getState());

        //[A] [A] [A] [B] [A] [B]
        // 1   8   1   1   1   1

        assertTrue(instance.endAttack());
        assertEquals(RiskGame.STATE_FORTIFYING, instance.getState());
        assertTrue(instance.moveArmy(countries[1], countries[2], countries[1].getArmies() - 1));

        //[A] [A] [A] [B] [A] [B]
        // 1   1   8   1   1   1

        assertEquals(RiskGame.STATE_END_TURN, instance.getState());
        player = instance.endGo();
        assertEquals(instance.getPlayers().get(1), player);

        assertEquals(RiskGame.STATE_PLACE_ARMIES, instance.getState());
        assertEquals(3, player.getExtraArmies());
        assertEquals(1, instance.placeArmy(countries[5], player.getExtraArmies()));

        //[A] [A] [A] [B] [A] [B]
        // 1   1   8   1   1   4

        assertEquals(RiskGame.STATE_ATTACKING, instance.getState());
        attack(instance, countries[5], countries[4]);
        assertEquals(1, instance.moveArmies(countries[5].getArmies() - 1));

        //[A] [A] [A] [B] [B] [B]
        // 1   1   8   1   3   1

        assertTrue(instance.endAttack());
        assertEquals(RiskGame.STATE_FORTIFYING, instance.getState());
        assertTrue(instance.moveArmy(countries[4], countries[3], countries[4].getArmies() - 1));

        //[A] [A] [A] [B] [B] [B]
        // 1   1   8   3   1   1

        assertEquals(RiskGame.STATE_END_TURN, instance.getState());
        player = instance.endGo();
        assertEquals(instance.getPlayers().get(0), player);
        assertEquals(RiskGame.STATE_PLACE_ARMIES, instance.getState());
        assertEquals(3, player.getExtraArmies());
        assertEquals(1, instance.placeArmy(countries[2], player.getExtraArmies()));

        //[A] [A] [A] [B] [B] [B]
        // 1   1   11  3   1   1

        assertEquals(RiskGame.STATE_ATTACKING, instance.getState());
        wipeOut(instance, countries[2], countries[3], true);

        assertEquals(1, instance.moveArmies(countries[2].getArmies() - 1));

        //[A] [A] [A] [A] [B] [B]
        // 1   1   1   10  1   1

        assertEquals(RiskGame.STATE_ATTACKING, instance.getState());
        attack(instance, countries[3], countries[4]);
        assertEquals(1, instance.moveArmies(countries[3].getArmies() - 1));

        //[A] [A] [A] [A] [A] [B]
        // 1   1   1   1   9   1

        assertEquals(RiskGame.STATE_ATTACKING, instance.getState());
        attack(instance, countries[4], countries[5]);
        assertEquals(2, instance.moveArmies(countries[4].getArmies() - 1));

        //[A] [A] [A] [A] [A] [A]
        // 1   1   1   1   1   8

        assertEquals(RiskGame.STATE_GAME_OVER, instance.getState());

        System.out.println("map " + toString(instance.getCountries()));
    }
    
    public void testItalianMinimumArmies() throws Exception {

        int noPlayers = 2;
        int noCountries = 6;

        // for 2 players, minimum need 6 countries
        RiskGame instance = TestUtil.createBasicMap(noCountries);

        addPlayers(instance, noPlayers);

        instance.startGame(RiskGame.MODE_DOMINATION, RiskGame.CARD_ITALIANLIKE_SET, true, false, 3, false);
        assertEquals(3, instance.getMaxDefendDice());
        instance.setCurrentPlayer(0);

        assertEquals(RiskGame.STATE_PLACE_ARMIES, instance.getState());

        // fill up all empty countries
        for (int i = 0; i < noCountries; i++) {
            assertFalse(instance.NoEmptyCountries());
            assertEquals(1, instance.placeArmy(instance.getCountryInt(i + 1), 1));
            assertNotNull(instance.endGo());
        }

        assertTrue(instance.NoEmptyCountries());

        //[A] [B] [A] [B] [A] [B]
        // 1   1   1   1   1   1

        // each player now has 5 armies
        int armiesLeft = 0;
        for (int p = 0; p < noPlayers; p++) {
            armiesLeft = armiesLeft + ((Player)instance.getPlayers().get(p)).getExtraArmies();
        }
        // place all other armies
        for (int c = 0; c < armiesLeft; c++) {
            Player player = instance.getCurrentPlayer();
            assertEquals(1, instance.placeArmy((Country)player.getTerritoriesOwned().get(0), 1));
            assertNotNull(instance.endGo());
        }

        //[A] [B] [A] [B] [A] [B]
        // 6   6   1   1   1   1

        assertEquals(RiskGame.STATE_PLACE_ARMIES, instance.getState());
        Player player = instance.getCurrentPlayer();
        // we get 1 extra armie at the start of our turn 3 / 3
        assertEquals((noCountries / noPlayers) / 3, player.getExtraArmies());

        Country[] countries = instance.getCountries();

        assertEquals(countries[0], (Country)player.getTerritoriesOwned().get(0));
        // place 1 extra army in first country
        assertEquals(1, instance.placeArmy((Country)player.getTerritoriesOwned().get(0), player.getExtraArmies()));

        assertEquals(RiskGame.STATE_ATTACKING, instance.getState());

        //[A] [B] [A] [B] [A] [B]
        // 7   6   1   1   1   1

        // in italian, attack twice to kill all armies
        wipeOut(instance, countries[0], countries[1], true);

        assertEquals(1, instance.moveArmies(countries[0].getArmies() - 1));
        assertEquals(RiskGame.STATE_ATTACKING, instance.getState());

        //[A] [A] [A] [B] [A] [B]
        // 1   6   1   1   1   1

        assertTrue(instance.endAttack());
        assertEquals(RiskGame.STATE_FORTIFYING, instance.getState());
        assertTrue(instance.moveArmy(countries[1], countries[2], countries[1].getArmies() - 1));

        //[A] [A] [A] [B] [A] [B]
        // 1   1   6   1   1   1

        assertEquals(RiskGame.STATE_END_TURN, instance.getState());
        player = instance.endGo();
        //assertEquals(instance.getPlayers().get(1), player);

        // as we dont have a min of 3 armies, and we only own 2 countries, we get no new armies!!!
        //assertEquals(RiskGame.STATE_ATTACKING, instance.getState());
        //assertTrue(instance.endAttack());
        //assertEquals(RiskGame.STATE_FORTIFYING, instance.getState());
        //assertTrue(instance.noMove());

        //assertEquals(RiskGame.STATE_END_TURN, instance.getState());
        //player = instance.endGo();
        assertEquals(instance.getPlayers().get(0), player);
        assertEquals(RiskGame.STATE_PLACE_ARMIES, instance.getState());
        assertEquals(player.getNoTerritoriesOwned() / 3, player.getExtraArmies()); // 4 / 3 = 1
        assertEquals(1, instance.placeArmy(countries[4], player.getExtraArmies()));

        //[A] [A] [A] [B] [A] [B]
        // 1   1   6   1   2   1

        assertEquals(RiskGame.STATE_ATTACKING, instance.getState());
        attack(instance, countries[2], countries[3]);
        assertEquals(1, instance.moveArmies(countries[2].getArmies() - 1));

        //[A] [A] [A] [A] [A] [B]
        // 1   1   1   5   2   1

        assertEquals(RiskGame.STATE_ATTACKING, instance.getState());
        attack(instance, countries[4], countries[5]);
        assertEquals(2, instance.moveArmies(countries[4].getArmies() - 1));

        //[A] [A] [A] [A] [A] [A]
        // 1   1   1   5   1   1

        assertEquals(RiskGame.STATE_GAME_OVER, instance.getState());
        
        System.out.println("map " + toString(instance.getCountries()));
    }

    public void testBadItalianGame() throws Exception {

        int noPlayers = 3;
        int noCountries = 6;

        // for 2 players, minimum need 6 countries
        RiskGame instance = TestUtil.createBasicMap(noCountries);

        addPlayers(instance, noPlayers);

        instance.startGame(RiskGame.MODE_DOMINATION, RiskGame.CARD_ITALIANLIKE_SET, true, false, 3, false);
        assertEquals(3, instance.getMaxDefendDice());
        instance.setCurrentPlayer(0);

        assertEquals(RiskGame.STATE_PLACE_ARMIES, instance.getState());

        // fill up all empty countries
        for (int i = 0; i < noCountries; i++) {
            assertFalse(instance.NoEmptyCountries());
            assertEquals(1, instance.placeArmy(instance.getCountryInt(i + 1), 1));
            assertNotNull(instance.endGo());
        }

        assertTrue(instance.NoEmptyCountries());

        //[A] [B] [C] [A] [B] [C]
        // 1   1   1   1   1   1

        // each player now has 5 armies
        int armiesLeft = 0;
        for (int p = 0; p < noPlayers; p++) {
            armiesLeft = armiesLeft + ((Player)instance.getPlayers().get(p)).getExtraArmies();
        }
        // place all other armies
        for (int c = 0; c < armiesLeft; c++) {
            Player player = instance.getCurrentPlayer();
            assertEquals(1, instance.placeArmy((Country)player.getTerritoriesOwned().get(0), 1));
            assertNotNull(instance.endGo());
        }

        //[A] [B] [C] [A] [B] [C]
        // 6   6   6   1   1   1
        
        // we get NO extra armies at the start of our turn
        
        for (int c = 0; c < noPlayers; c++) {

            Player player = instance.getCurrentPlayer();
            assertEquals(instance.getPlayers().get(c), player);

            assertEquals(RiskGame.STATE_ATTACKING, instance.getState());

            //[A] [B] [C] [A] [B] [C]
            // 6   6   6   1   1   1

            Country[] countries = instance.getCountries();

            Country attacker = (Country)player.getTerritoriesOwned().get(0);
            Country defender = countries[Arrays.asList(countries).indexOf(attacker) + 1];
            
            // in italian, attack twice to kill all armies
            wipeOut(instance, attacker, defender, false);

            //assertEquals(1, instance.moveArmies(countries[0].getArmies() - 1));
            assertEquals(RiskGame.STATE_ATTACKING, instance.getState());

            //[A] [B] [C] [A] [B] [C]
            // 1   6   6   1   1   1

            assertTrue(instance.endAttack());
            assertEquals(RiskGame.STATE_FORTIFYING, instance.getState());
            assertTrue(instance.noMove());

            //[A] [B] [C] [A] [B] [C]
            // 1   6   6   1   1   1

            assertEquals(RiskGame.STATE_END_TURN, instance.getState());
            Player nextPlayer = instance.endGo();
            assertNotNull(nextPlayer);

            if (player == nextPlayer) {
                // we are on the last player
                assertEquals(noPlayers - 1, c);
            }
        }

        assertEquals(RiskGame.STATE_GAME_OVER, instance.getState());

        System.out.println("map " + toString(instance.getCountries()));
    }

    public static void addPlayers(RiskGame instance, int noPlayers) {
        for (int p = 0; p < noPlayers; p++) {
            int color = ColorUtil.BLACK;
            switch (p) {
                case 0: color = ColorUtil.CYAN; break;
                case 1: color = ColorUtil.GREEN; break;
                case 2: color = ColorUtil.MAGENTA; break;
                case 3: color = ColorUtil.RED; break;
                case 4: color = ColorUtil.BLUE; break;
                case 5: color = ColorUtil.YELLOW; break;
            }

            instance.addPlayer(Player.PLAYER_HUMAN, "p" + (p + 1), color, "address12345");
        }
    }

    private void attack(RiskGame instance, Country attacker, Country defender) {
        assertTrue(instance.attack(attacker, defender));
        rollDice(instance, attacker, defender, true);
    }

    private void wipeOut(RiskGame instance, Country attacker, Country defender, boolean win) {
        assertTrue(instance.attack(attacker, defender));
        while (win ? defender.getArmies() > 0 : attacker.getArmies() > 1) {
            rollDice(instance, attacker, defender, win);
        }
    }

    private void rollDice(RiskGame instance, Country attacker, Country defender, boolean win) {

        assertEquals(attacker, instance.getAttacker());
        assertEquals(defender, instance.getDefender());
        
        int maxDefenderDice = instance.getMaxDefendDice();

        int attackerArmies = attacker.getArmies();
        int defenderArmies = defender.getArmies();
        int attackerDice = attackerArmies > 3 ? 3 : attackerArmies - 1;
        int defenderDice = defenderArmies >= maxDefenderDice ? maxDefenderDice : defenderArmies;

        assertEquals(RiskGame.STATE_ROLLING, instance.getState());
        assertTrue(instance.rollA(attackerDice));
        assertEquals(RiskGame.STATE_DEFEND_YOURSELF, instance.getState());
        assertTrue(instance.rollD(defenderDice)); // sets the current player back to the attacker

        // can cheat, can always roll perfect 6s
        int[] result = instance.battle(getDice(attackerDice, win ? 5 : 0), getDice(defenderDice, win ? 0 : 5));
        assertEquals(1, result[0]); // rolling dice worked or not
        assertEquals(win ? 0 : Math.min(attackerDice, defenderDice), result[1]); // no of armies attacker lost
        assertEquals(win ? Math.min(attackerDice, defenderDice) : 0, result[2]); // no of armies defender lost

        //assertEquals(0, result[3]); // did you win
        if (result[3] == 0) {
            assertEquals(0, result[4]); // min move
            assertEquals(0, result[5]); // max move
            if (attacker.getArmies() > 1) {
                assertEquals(RiskGame.STATE_ROLLING, instance.getState());
            }
            else {
                assertEquals(RiskGame.STATE_ATTACKING, instance.getState());
            }
        }
        else if (result[3] == 1 || result[3] == 2) { // we won battle OR we wiped out player
            assertEquals(attackerDice, result[4]); // min move
            assertEquals(attacker.getArmies() - 1, result[5]); // max move
            assertEquals(RiskGame.STATE_BATTLE_WON, instance.getState());
        }
        else {
            fail("unexpected result " + result[3]); 
        }
    }

    private static int[] getDice(int count, int result) {
        int[] results = new int[count];
        Arrays.fill(results, result);
        return results;
    }

    private String toString(Country[] countries) {
        StringBuilder builder = new StringBuilder();
        for (int c = 0; c < countries.length; c++) {
            builder.append(countries[c].getIdString());
            builder.append(":");
            builder.append(countries[c].getOwner());
            builder.append("(");
            builder.append(countries[c].getArmies());
            builder.append(")  ");
        }
        return builder.toString();
    }
}
