package org.warg.core;

/**
 * Immutable rules options supported by the Domination r2844 reference engine.
 * Independent WARG implementation; no dependency on legacy classes.
 */
public final class Rules {
    private final int maximumDefenderDice;
    private final int minimumReinforcements;

    public Rules(int maximumDefenderDice, boolean minimumThreeReinforcements) {
        if (maximumDefenderDice < 1 || maximumDefenderDice > 3) {
            throw new IllegalArgumentException("maximumDefenderDice must be 1..3");
        }
        this.maximumDefenderDice = maximumDefenderDice;
        this.minimumReinforcements = minimumThreeReinforcements ? 3 : 0;
    }

    public int maximumAttackerDice(int armies) {
        if (armies < 1) throw new IllegalArgumentException("armies must be positive");
        return Math.min(3, armies - 1);
    }

    public int maximumDefenderDice(int armies) {
        if (armies < 1) throw new IllegalArgumentException("armies must be positive");
        return Math.min(maximumDefenderDice, armies);
    }

    /** Base reinforcements only; caller adds bonuses for fully controlled continents. */
    public int territoryReinforcements(int territoriesOwned) {
        if (territoriesOwned < 0) throw new IllegalArgumentException("negative territory count");
        return Math.max(minimumReinforcements, territoriesOwned / 3);
    }
}
