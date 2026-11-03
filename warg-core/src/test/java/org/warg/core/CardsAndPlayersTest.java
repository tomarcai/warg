package org.warg.core;

import static org.warg.core.CardTradeRules.Suit.*;
import static org.warg.core.CardTradeRules.Mode.*;

public final class CardsAndPlayersTest {
    private static void eq(int expected, int actual, String message) {
        if (expected != actual) throw new AssertionError(message + " expected=" + expected + " got=" + actual);
    }
    private static void yes(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        eq(4, CardTradeRules.nextIncreasingValue(0), "first set");
        eq(6, CardTradeRules.nextIncreasingValue(4), "second set");
        eq(15, CardTradeRules.nextIncreasingValue(12), "15");
        eq(20, CardTradeRules.nextIncreasingValue(15), "20");
        eq(4, CardTradeRules.value(INCREASING, 0, INFANTRY, INFANTRY, INFANTRY), "three of kind");
        eq(0, CardTradeRules.value(INCREASING, 0, CANNON, CANNON, INFANTRY), "invalid mixed pair");
        eq(4, CardTradeRules.value(INCREASING, 0, WILDCARD, CANNON, CANNON), "wild increasing");
        eq(4, CardTradeRules.value(FIXED, 0, INFANTRY, INFANTRY, INFANTRY), "fixed infantry");
        eq(6, CardTradeRules.value(FIXED, 0, CAVALRY, CAVALRY, WILDCARD), "fixed cavalry wild");
        eq(8, CardTradeRules.value(FIXED, 0, CANNON, CANNON, CANNON), "fixed cannon");
        eq(10, CardTradeRules.value(FIXED, 0, INFANTRY, CAVALRY, CANNON), "fixed distinct");
        eq(10, CardTradeRules.value(FIXED, 0, WILDCARD, WILDCARD, INFANTRY), "fixed two wild");
        eq(12, CardTradeRules.value(FIXED, 0, WILDCARD, WILDCARD, WILDCARD), "fixed all wild");
        eq(0, CardTradeRules.value(FIXED, 0, WILDCARD, CANNON, INFANTRY), "fixed wild mixed");
        eq(8, CardTradeRules.value(ITALIAN, 0, CAVALRY, CAVALRY, CAVALRY), "Italian cavalry");
        eq(6, CardTradeRules.value(ITALIAN, 0, INFANTRY, INFANTRY, INFANTRY), "Italian infantry");
        eq(4, CardTradeRules.value(ITALIAN, 0, CANNON, CANNON, CANNON), "Italian cannon");
        eq(10, CardTradeRules.value(ITALIAN, 0, INFANTRY, CAVALRY, CANNON), "Italian distinct");
        eq(12, CardTradeRules.value(ITALIAN, 0, WILDCARD, CANNON, CANNON), "Italian wildcard pair");
        eq(0, CardTradeRules.value(ITALIAN, 0, WILDCARD, CANNON, INFANTRY), "Italian wild mixed");
        eq(0, CardTradeRules.value(ITALIAN, 0, WILDCARD, WILDCARD, INFANTRY), "Italian two wild");

        PlayerRoster roster = new PlayerRoster(PlayerRoster.DEFAULT_MAX_CARDS);
        roster.add("red");
        roster.add("blue");
        roster.giveCard("red", INFANTRY);
        roster.giveCard("blue", CAVALRY);
        roster.giveCard("blue", CANNON);
        roster.giveCard("blue", INFANTRY);
        roster.giveCard("blue", WILDCARD);
        roster.giveCard("blue", CAVALRY);
        roster.eliminate("red", "blue");
        eq(6, roster.get("red").cards().size(), "all cards transferred");
        eq(0, roster.get("blue").cards().size(), "victim hand cleared");
        yes(roster.get("red").eliminatedPlayers().contains("blue"), "elimination credit");
        yes(roster.requiresForcedTrade("red"), "forced trade at six cards");
        boolean repeated = false;
        try { roster.eliminate("red", "blue"); } catch (IllegalStateException expected) { repeated = true; }
        yes(repeated, "cannot eliminate twice");
        System.out.println("WARG cards and players tests passed");
    }
}
