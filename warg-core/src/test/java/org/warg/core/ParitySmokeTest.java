package org.warg.core;

/** Dependency-free Java 8 parity smoke tests. Run with scripts/test-warg-core.sh. */
public final class ParitySmokeTest {
    private static void equal(int expected, int actual, String label) {
        if (expected != actual) throw new AssertionError(label + ": expected " + expected + ", got " + actual);
    }

    private static void rejects(Runnable code, String label) {
        try { code.run(); }
        catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("expected rejection: " + label);
    }

    public static void main(String[] args) {
        Rules classic = new Rules(2, true);
        Rules italian = new Rules(3, false);
        equal(0, classic.maximumAttackerDice(1), "minimum attack army");
        equal(1, classic.maximumAttackerDice(2), "attack 2 armies");
        equal(3, classic.maximumAttackerDice(5), "attack maximum");
        equal(2, classic.maximumDefenderDice(4), "classic defense");
        equal(3, italian.maximumDefenderDice(4), "three-die defense");
        equal(3, classic.territoryReinforcements(0), "minimum reinforcements");
        equal(3, classic.territoryReinforcements(9), "territory division");
        equal(4, classic.territoryReinforcements(12), "territory division 12");
        equal(0, italian.territoryReinforcements(2), "optional minimum");
        equal(3, italian.territoryReinforcements(9), "optional minimum 9");
        rejects(() -> new Rules(4, true), "maximum defender dice");
        rejects(() -> classic.territoryReinforcements(-1), "negative territories");

        BattleRound.Losses win = BattleRound.resolve(new int[]{5, 3, 2}, new int[]{4, 1});
        equal(0, win.attacker, "attacker wins");
        equal(2, win.defender, "defender losses");
        BattleRound.Losses tie = BattleRound.resolve(new int[]{4, 2}, new int[]{4, 1});
        equal(1, tie.attacker, "tie favors defender");
        equal(1, tie.defender, "second comparison");
        rejects(() -> BattleRound.resolve(new int[]{1, 4}, new int[]{2}), "unsorted dice");
        rejects(() -> BattleRound.resolve(new int[]{6}, new int[]{2}), "invalid die face");
        System.out.println("WARG core parity smoke tests passed");
    }
}
