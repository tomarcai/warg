package net.yura.domination.engine.core;

import junit.framework.TestCase;

/**
 * Tests of getNoArmies method, of class Player.
 */
public class PlayerTest extends TestCase {

    private static final Continent CONTINENT = new Continent("meow", "Meow", 5, 0);

    private static Country newCountry(String id, int armies) {
        Country country = new Country(1, id, id, CONTINENT, 0, 0);
        country.addArmies(armies);
        return country;
    }

    private static Player newPlayer() {
        return new Player(Player.PLAYER_HUMAN, "Yura", 1, "address");
    }

    public void testNoArmiesIsZeroForNewPlayer() {
        Player player = newPlayer();

        assertEquals(0, player.getNoArmies());
    }

    public void testNoArmiesCountsSingleTerritory() {
        Player player = newPlayer();

        player.newCountry(newCountry("A", 5));

        assertEquals(5, player.getNoArmies());
    }

    public void testNoArmiesCountsArmiesEvenWhenZero() {
        Player player = newPlayer();

        player.newCountry(newCountry("A", 0));

        assertEquals(0, player.getNoArmies());
    }

    public void testNoArmiesSumsMultipleTerritories() {
        Player player = newPlayer();

        player.newCountry(newCountry("A", 3));
        player.newCountry(newCountry("B", 7));
        player.newCountry(newCountry("C", 1));

        assertEquals(11, player.getNoArmies());
    }

    public void testNoArmiesReflectsArmiesAddedAfterOwnership() {
        Player player = newPlayer();

        Country country = newCountry("A", 2);
        player.newCountry(country);
        assertEquals(2, player.getNoArmies());

        country.addArmies(4);
        assertEquals(6, player.getNoArmies());
    }

    public void testNoArmiesReflectsArmiesRemoved() {
        Player player = newPlayer();

        Country country = newCountry("A", 5);
        player.newCountry(country);

        country.removeArmies(2);
        assertEquals(3, player.getNoArmies());
    }

    public void testNoArmiesDropsLostTerritory() {
        Player player = newPlayer();

        Country a = newCountry("A", 3);
        Country b = newCountry("B", 4);
        player.newCountry(a);
        player.newCountry(b);
        assertEquals(7, player.getNoArmies());

        player.lostCountry(a);
        assertEquals(4, player.getNoArmies());
    }

    public void testNoArmiesIgnoresExtraArmies() {
        Player player = newPlayer();

        player.newCountry(newCountry("A", 2));
        player.addArmies(10);

        // extraArmies are reinforcements not yet placed on a territory,
        // so they must not be counted as armies on the board
        assertEquals(2, player.getNoArmies());
    }

    public void testNoArmiesIsIndependentPerPlayer() {
        Player playerA = newPlayer();
        Player playerB = new Player(Player.PLAYER_HUMAN, "Other", 2, "address2");

        playerA.newCountry(newCountry("A", 3));
        playerB.newCountry(newCountry("B", 9));

        assertEquals(3, playerA.getNoArmies());
        assertEquals(9, playerB.getNoArmies());
    }
}
