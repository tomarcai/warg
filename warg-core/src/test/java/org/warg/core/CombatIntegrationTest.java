package org.warg.core;

public final class CombatIntegrationTest {
    private static void ok(boolean truth, String name) {
        if (!truth) throw new AssertionError(name);
    }
    private static void eq(int expected, int actual, String name) {
        if (expected != actual) throw new AssertionError(name + ": " + actual + " != " + expected);
    }
    private static Board board(int attackerArmies, int defenderArmies, boolean extraBlue) {
        Board b = new Board();
        b.addContinent("test", 0);
        b.addTerritory("a", "test");
        b.addTerritory("d", "test");
        b.connect("a", "d");
        b.occupy("a", "red", attackerArmies);
        b.occupy("d", "blue", defenderArmies);
        if (extraBlue) {
            b.addTerritory("e", "test");
            b.occupy("e", "blue", 1);
        }
        return b;
    }
    private static TurnController start(Board b, int attackDice, int defenseDice) {
        TurnController turn = new TurnController(b, new Rules(2, true), "red", 0);
        ok(turn.attack("a", "d"), "attack");
        ok(turn.rollAttacker(attackDice), "attack roll selection");
        ok(turn.rollDefender(defenseDice, "blue"), "defense roll selection");
        return turn;
    }
    public static void main(String[] args) {
        Board b = board(6, 2, true);
        TurnController t = start(b, 3, 2);
        CombatResult won = t.battle(new int[]{5, 4, 3}, new int[]{3, 2});
        eq(0, won.attackerLosses, "attacker casualties");
        eq(2, won.defenderLosses, "defender casualties");
        ok(won.conquered, "conquest");
        ok(!won.defenderEliminated, "blue retains another territory");
        eq(3, won.minimumMove, "minimum follows rolled attacker dice");
        eq(5, won.maximumMove, "maximum leaves one");
        eq(0, b.territory("d").armies(), "new territory empty until move");
        ok("red".equals(b.territory("d").owner()), "ownership immediate");
        ok(t.phase() == TurnController.Phase.BATTLE_WON, "compulsory movement");
        ok(!t.moveAfterConquest(2), "min move enforced");
        ok(!t.moveAfterConquest(6), "max move enforced");
        eq(5, t.moveAllAfterConquest(), "move all available");
        ok(t.moveAfterConquest(3), "minimum post-conquest move");
        eq(3, b.territory("a").armies(), "origin after conquest");
        eq(3, b.territory("d").armies(), "destination after conquest");
        ok(t.phase() == TurnController.Phase.ATTACKING, "return to attack");
        ok(!t.moveAfterConquest(1), "cannot move twice");

        Board b2 = board(4, 1, false);
        TurnController t2 = start(b2, 3, 1);
        CombatResult eliminated = t2.battle(new int[]{5, 4, 3}, new int[]{0});
        ok(eliminated.conquered && eliminated.defenderEliminated, "last territory eliminated");
        eq(3, eliminated.minimumMove, "roll count is required");
        eq(3, eliminated.maximumMove, "only three can move");
        ok(t2.moveAfterConquest(3), "conquest mandatory armies");
        eq(1, b2.territory("a").armies(), "leave one behind");

        Board b3 = board(2, 2, false);
        TurnController t3 = start(b3, 1, 1);
        CombatResult lost = t3.battle(new int[]{2}, new int[]{2});
        eq(1, lost.attackerLosses, "ties favor defense");
        ok(!lost.conquered, "no conquest");
        ok(t3.phase() == TurnController.Phase.ATTACKING, "forced retreat");
        eq(1, b3.territory("a").armies(), "forced retreat with one army");

        Board b4 = board(5, 3, false);
        TurnController t4 = start(b4, 2, 2);
        boolean rejected = false;
        try { t4.battle(new int[]{5}, new int[]{4, 3}); }
        catch (IllegalArgumentException expected) { rejected = true; }
        ok(rejected, "reject wrong length");
        eq(5, b4.territory("a").armies(), "invalid dice do not mutate");
        eq(3, b4.territory("d").armies(), "invalid dice preserve defender");
        CombatResult ongoing = t4.battle(new int[]{4, 1}, new int[]{5, 0});
        eq(1, ongoing.attackerLosses, "ongoing attacker loss");
        eq(1, ongoing.defenderLosses, "ongoing defender loss");
        ok(t4.phase() == TurnController.Phase.ROLLING, "continue combat");
        eq(1, t4.battleRounds(), "count battle rounds");
        Board b5 = board(4, 1, false);
        PlayerRoster players = new PlayerRoster(5);
        players.add("red");
        players.add("blue");
        players.giveCard("red", CardTradeRules.Suit.CANNON);
        for (int i = 0; i < 5; i++) players.giveCard("blue", CardTradeRules.Suit.INFANTRY);
        TurnController t5 = new TurnController(b5, new Rules(2, true), "red", 0, players);
        ok(t5.attack("a", "d"), "roster attack");
        ok(t5.rollAttacker(3), "roster attacker roll");
        ok(t5.rollDefender(1, "blue"), "roster defender roll");
        CombatResult result = t5.battle(new int[]{5, 4, 3}, new int[]{0});
        ok(result.defenderEliminated, "roster defender eliminated");
        eq(6, players.get("red").cards().size(), "eliminated cards transferred");
        eq(0, players.get("blue").cards().size(), "loser cards cleared");
        ok(players.get("red").eliminatedPlayers().contains("blue"), "elimination recorded");
        ok(t5.moveAfterConquest(3), "finish move with transferred cards");
        ok(t5.phase() == TurnController.Phase.TRADE_CARDS, "excess cards force trade phase");
        ok(!t5.endAttack(), "cannot skip forced trade");
        System.out.println("WARG combat integration tests passed");
    }
}
