package net.yura.domination.engine.core;

import junit.framework.TestCase;
import net.yura.domination.test.TestUtil;

public class RiskGameEndGoTest extends TestCase {

    private RiskGame createAndStartGame(int noPlayers, int noCountries, int gameMode, boolean minimumThreeReinforcements) throws Exception {
        RiskGame instance = TestUtil.createBasicMap(noCountries);
        RiskGameTest.addPlayers(instance, noPlayers);
        instance.startGame(gameMode, RiskGame.CARD_FIXED_SET, true, true, 2, minimumThreeReinforcements);
        instance.setCurrentPlayer(0);
        return instance;
    }

    private void setPlayerExtraArmies(Player player, int armies) {
        int current = player.getExtraArmies();
        if (current < armies) {
            player.addArmies(armies - current);
        } else if (current > armies) {
            player.loseExtraArmy(current - armies);
        }
    }

    private void progressGameToEndTurnState(RiskGame instance) throws Exception {
        int noCountries = instance.getCountries().length;
        int noPlayers = instance.getPlayers().size();

        // 1. Fill empty countries
        for (int i = 0; i < noCountries; i++) {
            instance.placeArmy(instance.getCountryInt(i + 1), 1);
            instance.endGo();
        }

        // 2. Place remaining armies
        int armiesLeft = 0;
        for (int p = 0; p < noPlayers; p++) {
            armiesLeft += ((Player) instance.getPlayers().get(p)).getExtraArmies();
        }
        for (int c = 0; c < armiesLeft; c++) {
            Player player = instance.getCurrentPlayer();
            instance.placeArmy((Country) player.getTerritoriesOwned().get(0), 1);
            instance.endGo();
        }

        // Now setup is done, and it's some player's turn in STATE_PLACE_ARMIES.
        // 3. Place current player's extra armies so they go into STATE_ATTACKING
        Player currentPlayer = instance.getCurrentPlayer();
        instance.placeArmy((Country) currentPlayer.getTerritoriesOwned().get(0), currentPlayer.getExtraArmies());

        // 4. Transition to STATE_FORTIFYING and then STATE_END_TURN
        instance.endAttack();
        instance.noMove();
    }

    public void testEndGo_WrongState() throws Exception {
        RiskGame instance = createAndStartGame(2, 6, RiskGame.MODE_DOMINATION, true);
        // Initially in STATE_PLACE_ARMIES
        assertEquals(RiskGame.STATE_PLACE_ARMIES, instance.getState());
        
        Player origPlayer = instance.getCurrentPlayer();
        Player result = instance.endGo();
        
        assertNull(result);
        assertEquals(origPlayer, instance.getCurrentPlayer());
        assertEquals(RiskGame.STATE_PLACE_ARMIES, instance.getState());
    }

    public void testEndGo_SetupNotDone() throws Exception {
        RiskGame instance = createAndStartGame(2, 6, RiskGame.MODE_DOMINATION, true);
        
        Player p1 = (Player) instance.getPlayers().get(0);
        Player p2 = (Player) instance.getPlayers().get(1);
        
        // Initially in STATE_PLACE_ARMIES. Place an army to transition state to STATE_END_TURN
        assertEquals(RiskGame.STATE_PLACE_ARMIES, instance.getState());
        assertEquals(1, instance.placeArmy(instance.getCountryInt(1), 1));
        assertEquals(RiskGame.STATE_END_TURN, instance.getState());
        assertFalse(instance.getSetupDone());
        
        int p2ArmiesBefore = p2.getExtraArmies();
        
        Player nextPlayer = instance.endGo();
        
        assertEquals(p2, nextPlayer);
        assertEquals(p2, instance.getCurrentPlayer());
        
        // Setup not done, so nextTurn() should NOT be called on p2, nor should getExtraArmiesForPlayer be added.
        assertEquals(p2ArmiesBefore, p2.getExtraArmies());
        
        // Assert the correct gameState is transitioned to
        assertEquals(RiskGame.STATE_PLACE_ARMIES, instance.getState());
        
        // Assert reset flags
        assertFalse(instance.isCapturedCountry());
        assertFalse(instance.getTradeCap());
    }

    public void testEndGo_SetupDone_NextPlayerHasTerritories() throws Exception {
        RiskGame instance = createAndStartGame(2, 6, RiskGame.MODE_DOMINATION, true);
        progressGameToEndTurnState(instance);
        
        Player p1 = instance.getCurrentPlayer(); // This is the player whose turn just ended
        // Work out next player index
        int nextIndex = (instance.getPlayers().indexOf(p1) + 1) % 2;
        Player p2 = (Player) instance.getPlayers().get(nextIndex);
        
        // Ensure p2 has at least one territory
        Country c1 = instance.getCountries()[0];
        c1.setOwner(p2);
        if (!p2.getTerritoriesOwned().contains(c1)) {
            p2.newCountry(c1);
        }
        
        int p2ExtraArmiesBefore = p2.getExtraArmies();
        
        Player nextPlayer = instance.endGo();
        
        assertEquals(p2, nextPlayer);
        assertEquals(p2, instance.getCurrentPlayer());
        
        // Since setup is done, p2 gets extra armies (at least minimumNewArmies)
        assertTrue(p2.getExtraArmies() > p2ExtraArmiesBefore);
        assertEquals(RiskGame.STATE_PLACE_ARMIES, instance.getState());
    }

    public void testEndGo_SetupDone_NextPlayerHasNoTerritories() throws Exception {
        // 3 players
        RiskGame instance = createAndStartGame(3, 6, RiskGame.MODE_DOMINATION, true);
        progressGameToEndTurnState(instance);
        
        Player p1 = instance.getCurrentPlayer(); // This is the player whose turn just ended
        int p1Index = instance.getPlayers().indexOf(p1);
        Player p2 = (Player) instance.getPlayers().get((p1Index + 1) % 3); // next player
        Player p3 = (Player) instance.getPlayers().get((p1Index + 2) % 3); // next-next player
        
        // Remove all territories from p2, assign them to p3
        for (Country country : instance.getCountries()) {
            if (country.getOwner() == p2) {
                country.setOwner(p3);
                if (!p3.getTerritoriesOwned().contains(country)) {
                    p3.newCountry(country);
                }
            }
        }
        p2.getTerritoriesOwned().clear();
        
        // p3 must have at least one territory
        Country c = instance.getCountries()[0];
        c.setOwner(p3);
        if (!p3.getTerritoriesOwned().contains(c)) {
            p3.newCountry(c);
        }
        
        Player nextPlayer = instance.endGo();
        
        // Since p2 owns 0 territories, endGo() should skip p2 and select p3
        assertEquals(p3, nextPlayer);
        assertEquals(p3, instance.getCurrentPlayer());
        
        // Assert the correct gameState is transition to
        assertEquals(RiskGame.STATE_PLACE_ARMIES, instance.getState());
    }

    public void testEndGo_CapitalMode_SetupNotFinished() throws Exception {
        RiskGame instance = createAndStartGame(2, 6, RiskGame.MODE_CAPITAL, true);
        
        int noCountries = instance.getCountries().length;
        int noPlayers = instance.getPlayers().size();

        // 1. Fill empty countries
        for (int i = 0; i < noCountries; i++) {
            instance.placeArmy(instance.getCountryInt(i + 1), 1);
            instance.endGo();
        }

        // 2. Place remaining armies except the very last one
        int armiesLeft = 0;
        for (int p = 0; p < noPlayers; p++) {
            armiesLeft += ((Player) instance.getPlayers().get(p)).getExtraArmies();
        }
        
        // Place all but the last one
        for (int c = 0; c < armiesLeft - 1; c++) {
            Player player = instance.getCurrentPlayer();
            instance.placeArmy((Country) player.getTerritoriesOwned().get(0), 1);
            instance.endGo();
        }
        
        // Place the very last army
        Player player = instance.getCurrentPlayer();
        assertEquals(1, instance.placeArmy((Country) player.getTerritoriesOwned().get(0), 1));
        
        // Assert setup is now completed, but capital is null
        assertTrue(instance.getSetupDone());
        assertEquals(RiskGame.STATE_END_TURN, instance.getState());
        
        Player nextPlayerExpected = (Player) instance.getPlayers().get((instance.getPlayers().indexOf(player) + 1) % noPlayers);
        assertNull(nextPlayerExpected.getCapital());
        
        int nextPlayerArmiesBefore = nextPlayerExpected.getExtraArmies();
        
        Player nextPlayer = instance.endGo();
        
        assertEquals(nextPlayerExpected, nextPlayer);
        assertEquals(nextPlayerExpected, instance.getCurrentPlayer());
        
        // Capital is null, so nextTurn() and extra armies are NOT called/added, and gameState becomes STATE_SELECT_CAPITAL
        assertEquals(nextPlayerArmiesBefore, nextPlayer.getExtraArmies());
        assertEquals(RiskGame.STATE_SELECT_CAPITAL, instance.getState());
    }

    public void testEndGo_CanTrade() throws Exception {
        RiskGame instance = createAndStartGame(2, 6, RiskGame.MODE_DOMINATION, true);
        progressGameToEndTurnState(instance);
        
        Player p1 = instance.getCurrentPlayer();
        int nextIndex = (instance.getPlayers().indexOf(p1) + 1) % 2;
        Player p2 = (Player) instance.getPlayers().get(nextIndex);
        
        // Ensure p2 has territories so they are not skipped
        Country c1 = instance.getCountries()[0];
        c1.setOwner(p2);
        if (!p2.getTerritoriesOwned().contains(c1)) {
            p2.newCountry(c1);
        }
        
        // Give p2 three identical cards (e.g. Cavalry) to enable trade
        Card card1 = new Card(Card.CAVALRY, c1);
        Card card2 = new Card(Card.CAVALRY, c1);
        Card card3 = new Card(Card.CAVALRY, c1);
        p2.giveCard(card1);
        p2.giveCard(card2);
        p2.giveCard(card3);
        
        Player nextPlayer = instance.endGo();
        
        assertEquals(p2, nextPlayer);
        // Because they can trade, gameState must become STATE_TRADE_CARDS
        assertEquals(RiskGame.STATE_TRADE_CARDS, instance.getState());
    }

    public void testEndGo_CannotTrade_ExtraArmies() throws Exception {
        RiskGame instance = createAndStartGame(2, 6, RiskGame.MODE_DOMINATION, true);
        progressGameToEndTurnState(instance);
        
        Player p1 = instance.getCurrentPlayer();
        int nextIndex = (instance.getPlayers().indexOf(p1) + 1) % 2;
        Player p2 = (Player) instance.getPlayers().get(nextIndex);
        
        Country c1 = instance.getCountries()[0];
        c1.setOwner(p2);
        if (!p2.getTerritoriesOwned().contains(c1)) {
            p2.newCountry(c1);
        }
        
        // p2 has some cards but not a tradeable set
        Card card1 = new Card(Card.CAVALRY, c1);
        p2.giveCard(card1);
        
        Player nextPlayer = instance.endGo();
        
        assertEquals(p2, nextPlayer);
        // Cannot trade, has extra armies (due to minimum armies added at end of turn setup)
        assertTrue(p2.getExtraArmies() > 0);
        assertEquals(RiskGame.STATE_PLACE_ARMIES, instance.getState());
    }

    public void testEndGo_ItalianMode_CanAttack() throws Exception {
        // Set minimumThreeReinforcements to false so next player gets 0 extra armies
        RiskGame instance = createAndStartGame(2, 6, RiskGame.MODE_DOMINATION, false);
        progressGameToEndTurnState(instance);
        
        Player p1 = instance.getCurrentPlayer();
        int nextIndex = (instance.getPlayers().indexOf(p1) + 1) % 2;
        Player p2 = (Player) instance.getPlayers().get(nextIndex);
        
        p2.getTerritoriesOwned().clear();
        p1.getTerritoriesOwned().clear();
        
        // Set up next player p2 with 0 extra armies using helper method
        setPlayerExtraArmies(p2, 0);
        
        // p2 owns c1, has 2 armies (can attack)
        Country c1 = instance.getCountries()[0];
        c1.setOwner(p2);
        p2.newCountry(c1);
        while (c1.getArmies() > 0) { c1.looseArmy(); }
        c1.addArmies(2); // total 2 armies (1 left to attack)
        
        // Adjacent c2 owned by p1
        Country c2 = instance.getCountries()[1];
        c2.setOwner(p1);
        p1.newCountry(c2);
        while (c2.getArmies() > 0) { c2.looseArmy(); }
        c2.addArmies(1);
        
        // Ensure neighbors are connected: c1 and c2 are neighbors
        c1.getNeighbours().clear();
        c2.getNeighbours().clear();
        c1.addNeighbour(c2);
        c2.addNeighbour(c1);
        
        Player nextPlayer = instance.endGo();
        
        assertEquals(p2, nextPlayer);
        assertEquals(0, p2.getExtraArmies());
        assertEquals(RiskGame.STATE_ATTACKING, instance.getState());
    }

    public void testEndGo_ItalianMode_CanMove() throws Exception {
        // Set minimumThreeReinforcements to false so next player gets 0 extra armies
        RiskGame instance = createAndStartGame(2, 6, RiskGame.MODE_DOMINATION, false);
        progressGameToEndTurnState(instance);
        
        Player p1 = instance.getCurrentPlayer();
        int nextIndex = (instance.getPlayers().indexOf(p1) + 1) % 2;
        Player p2 = (Player) instance.getPlayers().get(nextIndex);
        
        p2.getTerritoriesOwned().clear();
        p1.getTerritoriesOwned().clear();
        setPlayerExtraArmies(p2, 0);
        
        // p2 owns both connected countries c1 and c2
        Country c1 = instance.getCountries()[0];
        c1.setOwner(p2);
        p2.newCountry(c1);
        while (c1.getArmies() > 0) { c1.looseArmy(); }
        c1.addArmies(2); // >1 armies
        
        Country c2 = instance.getCountries()[1];
        c2.setOwner(p2);
        p2.newCountry(c2);
        while (c2.getArmies() > 0) { c2.looseArmy(); }
        c2.addArmies(1);
        
        // connect neighbors
        c1.getNeighbours().clear();
        c2.getNeighbours().clear();
        c1.addNeighbour(c2);
        c2.addNeighbour(c1);
        
        Player nextPlayer = instance.endGo();
        
        assertEquals(p2, nextPlayer);
        assertEquals(0, p2.getExtraArmies());
        // Can't attack (no enemy neighbor), but can move (c1 has 2 armies and friendly neighbor c2)
        assertEquals(RiskGame.STATE_FORTIFYING, instance.getState());
    }

    public void testEndGo_ItalianMode_NoMove() throws Exception {
        // Set minimumThreeReinforcements to false so next player gets 0 extra armies
        RiskGame instance = createAndStartGame(2, 6, RiskGame.MODE_DOMINATION, false);
        progressGameToEndTurnState(instance);
        
        Player p1 = instance.getCurrentPlayer();
        int nextIndex = (instance.getPlayers().indexOf(p1) + 1) % 2;
        Player p2 = (Player) instance.getPlayers().get(nextIndex);
        
        p2.getTerritoriesOwned().clear();
        p1.getTerritoriesOwned().clear();
        setPlayerExtraArmies(p2, 0);
        
        // p2 owns c1, but it only has 1 army (cannot attack/move)
        Country c1 = instance.getCountries()[0];
        c1.setOwner(p2);
        p2.newCountry(c1);
        
        while (c1.getArmies() > 1) {
            c1.looseArmy();
        }
        while (c1.getArmies() < 1) {
            c1.addArmy();
        }

        Player nextPlayer = instance.endGo();

        assertEquals(p1, nextPlayer);
        assertEquals(0, p2.getExtraArmies());
        // Cannot attack, cannot move.
        assertEquals(RiskGame.STATE_GAME_OVER, instance.getState());
    }
}
