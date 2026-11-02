package org.warg.core;

/** Contract cases verified against Domination r2844 source. */
public final class BoardTest {
    static void check(boolean value, String description) {
        if (!value) throw new AssertionError(description);
    }
    static void equal(int expected, int actual, String description) {
        check(expected == actual, description + " expected " + expected + " got " + actual);
    }
    static void rejects(Runnable r, String description) {
        try { r.run(); } catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("expected rejection: " + description);
    }

    public static void main(String[] args) {
        Board board = new Board();
        board.addContinent("north", 5);
        board.addContinent("south", 2);
        board.addContinent("empty", 0);
        board.addTerritory("a", "north");
        board.addTerritory("b", "north");
        board.addTerritory("c", "south");
        board.connect("a", "b"); // directed, not automatically symmetric
        board.connect("b", "c");
        board.occupy("a", "red", 4);
        board.occupy("b", "blue", 2);
        board.occupy("c", "red", 1);

        check(board.mayAttack("red", "a", "b"), "directed attack allowed");
        check(!board.mayAttack("blue", "b", "a"), "reverse attack disallowed");
        check(!board.mayAttack("red", "c", "b"), "not adjacent");
        check(!board.ownsContinent("red", "north"), "partial continent");
        check(board.ownsContinent("red", "south"), "full continent");
        check(board.ownsContinent("red", "empty"), "original empty continent semantics");
        equal(2, board.ownedTerritories("red"), "owned count");
        equal(5, new Rules(2, true).territoryReinforcements(17), "integer division");
        equal(5, board.reinforcements("red", new Rules(2, true)), "base + south bonus");
        rejects(() -> board.addTerritory("a", "north"), "duplicate territory");
        rejects(() -> board.connect("a", "missing"), "nonexistent target");
        rejects(() -> board.occupy("a", null, 2), "unowned occupied territory");
        rejects(() -> board.fortify("red", "a", "b", 1), "different owner");
        board.occupy("b", "red", 2);
        check(board.mayFortify("red", "a", "b", 3), "fortification allowed");
        check(!board.mayFortify("red", "a", "b", 4), "leave one army");
        check(!board.mayFortify("red", "b", "a", 1), "directed fortification");
        board.fortify("red", "a", "b", 3);
        equal(1, board.territory("a").armies(), "source armies");
        equal(5, board.territory("b").armies(), "destination armies");
        equal(10, board.reinforcements("red", new Rules(2, true)), "both continents plus base");
        System.out.println("WARG board contract tests passed");
    }
}
