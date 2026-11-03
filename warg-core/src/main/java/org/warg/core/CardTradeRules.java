package org.warg.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Card set scoring, independently reimplemented from Domination r2844
 * RiskGame.getTradeAbsValue and getNewCardState.
 *
 * Territory-specific card bonuses and hand mutations are separate concerns.
 */
public final class CardTradeRules {
    public enum Mode { INCREASING, FIXED, ITALIAN }
    public enum Suit { INFANTRY, CAVALRY, CANNON, WILDCARD }

    private CardTradeRules() {}

    public static int nextIncreasingValue(int previous) {
        if (previous < 0) throw new IllegalArgumentException("negative card state");
        if (previous < 4) return previous + 4;
        if (previous < 12) return previous + 2;
        if (previous < 15) return previous + 3;
        return previous + 5;
    }

    public static int value(Mode mode, int previousValue, Suit first, Suit second, Suit third) {
        if (mode == null || first == null || second == null || third == null)
            throw new IllegalArgumentException("missing card or mode");
        Suit[] suits = {first, second, third};
        int wild = 0;
        for (Suit suit : suits) if (suit == Suit.WILDCARD) wild++;
        Set<Suit> distinct = new HashSet<>(Arrays.asList(suits));
        if (mode == Mode.INCREASING) {
            return wild > 0 || distinct.size() == 1 || distinct.size() == 3
                    ? nextIncreasingValue(previousValue) : 0;
        }
        if (mode == Mode.FIXED) {
            if (wild == 3) return 12;
            if (wild == 2 || (wild == 0 && distinct.size() == 3)) return 10;
            if (wild == 1) {
                if (first == Suit.WILDCARD) return fixedPair(second, third);
                if (second == Suit.WILDCARD) return fixedPair(first, third);
                return fixedPair(first, second);
            }
            if (distinct.size() == 1) return fixedPair(first, second);
            return 0;
        }
        // Italian rules: 3 identical non-wildcards, 3 different non-wildcards,
        // or one wildcard and two identical non-wildcards.
        if (wild == 0) {
            if (distinct.size() == 3) return 10;
            if (distinct.size() != 1) return 0;
            if (first == Suit.CAVALRY) return 8;
            if (first == Suit.INFANTRY) return 6;
            return 4;
        }
        if (wild == 1) {
            Suit a = null, b = null;
            for (Suit suit : suits) if (suit != Suit.WILDCARD) {
                if (a == null) a = suit;
                else b = suit;
            }
            return a == b ? 12 : 0;
        }
        return 0;
    }

    private static int fixedPair(Suit a, Suit b) {
        if (a != b) return 0;
        if (a == Suit.INFANTRY) return 4;
        if (a == Suit.CAVALRY) return 6;
        if (a == Suit.CANNON) return 8;
        return 12;
    }
}
