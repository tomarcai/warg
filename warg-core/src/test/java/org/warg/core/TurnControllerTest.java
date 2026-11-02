package org.warg.core;

public final class TurnControllerTest {
    private static void check(boolean ok, String what) {
        if (!ok) throw new AssertionError(what);
    }
    private static void equal(Object expected, Object actual, String what) {
        if (!expected.equals(actual)) throw new AssertionError(what + " expected " + expected + " got " + actual);
    }

    private static Board board() {
        Board board = new Board();
        board.addContinent("c", 5);
        board.addTerritory("a", "c");
        board.addTerritory("b", "c");
        board.addTerritory("c", "c");
        board.connect("a", "b");
        board.connect("a", "c");
        board.connect("b", "a");
        board.occupy("a", "red", 4);
        board.occupy("b", "blue", 2);
        board.occupy("c", "red", 2);
        return board;
    }

    public static void main(String[] args) {
        Board b = board();
        TurnController turn = new TurnController(b, new Rules(2, true), "red", 3);
        equal(TurnController.Phase.PLACE_ARMIES, turn.phase(), "initial phase");
        check(!turn.attack("a", "b"), "cannot attack before placing");
        check(!turn.placeArmies("b", 1), "cannot place on enemy");
        check(!turn.placeArmies("a", 4), "cannot exceed reserve");
        check(!turn.placeArmies("a", 0), "no zero placements in WARG");
        check(turn.placeArmies("a", 2), "valid placement");
        equal(1, turn.unplacedArmies(), "reserve decremented");
        check(turn.placeArmies("c", 1), "last placement");
        equal(TurnController.Phase.ATTACKING, turn.phase(), "attack phase entered");
        check(!turn.fortify("a", "c", 1), "cannot fortify during attack");
        check(turn.attack("a", "b"), "attack entered");
        equal(TurnController.Phase.ROLLING, turn.phase(), "rolling phase");
        check(!turn.rollAttacker(4), "too many dice");
        check(turn.rollAttacker(3), "three attack dice");
        equal(TurnController.Phase.DEFEND_YOURSELF, turn.phase(), "defend phase");
        check(!turn.rollDefender(1, "red"), "only actual defender may select");
        check(!turn.rollDefender(3, "blue"), "defender max two");
        check(turn.rollDefender(2, "blue"), "valid defender dice");
        equal(TurnController.Phase.DEFEND_YOURSELF, turn.phase(), "legacy waits for battle result");
        check(!turn.retreat(), "no retreat after attacker has rolled");
        // Combat resolution is intentionally not available in this incremental controller.

        Board b2 = board();
        TurnController second = new TurnController(b2, new Rules(3, false), "red", 0);
        equal(TurnController.Phase.ATTACKING, second.phase(), "zero placement reserve");
        check(second.attack("a", "b"), "start second attack");
        check(second.retreat(), "retreat before rolling");
        equal(TurnController.Phase.ATTACKING, second.phase(), "back to attack");
        check(second.attackingFrom() == null, "retreat clears state");
        check(second.endAttack(), "end attack");
        equal(TurnController.Phase.FORTIFYING, second.phase(), "fortification phase");
        check(!second.fortify("a", "b", 1), "different owner");
        check(second.fortify("a", "c", 2), "legal directed fortification");
        equal(2, b2.territory("a").armies(), "fortify source");
        equal(4, b2.territory("c").armies(), "fortify destination");
        equal(TurnController.Phase.END_TURN, second.phase(), "end turn state");
        check(!second.skipFortification(), "only one tactical move");

        TurnController third = new TurnController(board(), new Rules(2, true), "red", 0);
        check(third.endAttack(), "skip attacking");
        check(third.skipFortification(), "skip fortification");
        equal(TurnController.Phase.END_TURN, third.phase(), "turn end");
        System.out.println("WARG turn controller tests passed");
    }
}
