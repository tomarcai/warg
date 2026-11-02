package org.warg.core;

/**
 * Resolves a single combat dice comparison, independently of UI or game state.
 *
 * Domination RiskGame.battle compares dice positionally, with ties won by the
 * defender. The caller supplies dice in descending order, as the original
 * dice-generation pipeline does. This class validates rather than sorts inputs
 * so that invalid upstream roll handling cannot be concealed.
 */
public final class BattleRound {
    private BattleRound() {}

    public static final class Losses {
        public final int attacker;
        public final int defender;

        private Losses(int attacker, int defender) {
            this.attacker = attacker;
            this.defender = defender;
        }
    }

    /** Dice values are zero-based [0..5], mirroring Domination's DICE protocol. */
    public static Losses resolve(int[] attackerDice, int[] defenderDice) {
        validate(attackerDice, 3, "attacker");
        validate(defenderDice, 3, "defender");
        int attackerLosses = 0;
        int defenderLosses = 0;
        for (int i = 0; i < Math.min(attackerDice.length, defenderDice.length); i++) {
            if (attackerDice[i] > defenderDice[i]) defenderLosses++;
            else attackerLosses++;
        }
        return new Losses(attackerLosses, defenderLosses);
    }

    private static void validate(int[] dice, int max, String name) {
        if (dice == null || dice.length < 1 || dice.length > max) {
            throw new IllegalArgumentException("invalid " + name + " dice count");
        }
        for (int i = 0; i < dice.length; i++) {
            if (dice[i] < 0 || dice[i] > 5) {
                throw new IllegalArgumentException("invalid " + name + " die value");
            }
            if (i > 0 && dice[i] > dice[i - 1]) {
                throw new IllegalArgumentException(name + " dice must be descending");
            }
        }
    }
}
