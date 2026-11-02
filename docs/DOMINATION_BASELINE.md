# Domination r2844 — initial behavioral audit

Reference: `Domination/src/net/yura/domination/engine/core/RiskGame.java` and `Domination/src/net/yura/domination/engine/Risk.java`, preserved in this repository.

This document records observations from the original source, not a claim of complete WARG parity.

## Confirmed mechanics

- `RiskGame.startGame(..., maxDefendDice, minimumThreeReinforcements)` accepts **1–3** as the maximum defender dice setting; other values throw `IllegalArgumentException`. The minimum reinforcement option is **3** when enabled and **0** otherwise.
- `RiskGame.getExtraArmiesForPlayer` calculates `max(minimumNewArmies, territoriesOwned / 3)`, then adds the army value of every fully controlled continent. Integer division truncates.
- `RiskGame.attack` requires the attack phase, valid distinct ownership, a directed neighbor relation from the attacking territory to the defending territory, and **more than one army** in the attacking territory.
- `RiskGame.rollA` permits 1 to `min(3, attackerArmies - 1)` attacker dice; `rollD` permits 1 to `min(maxDefendDice, defenderArmies)` defender dice.
- `RiskGame.rollDice` generates **zero-based values 0–5** via `nextInt(6)` and sorts each roll **descending** before it is used.
- `RiskGame.battle` compares attacker and defender arrays positionally; an **equal** result causes an attacker loss. Each compared pair removes one army from one side. It also performs state transitions and elimination/card-transfer handling, not implemented by the initial `BattleRound` helper.
- `RiskGame.moveArmy` permits a fortification in the fortifying phase only between neighboring territories belonging to the current player, transferring a strictly positive number of armies while leaving at least one in the source.

## Initial WARG implementation

- `warg-core/src/main/java/org/warg/core/Rules.java`: attack/defense dice caps and territory-only reinforcement formula.
- `warg-core/src/main/java/org/warg/core/BattleRound.java`: descending dice comparison and losses, with validation of die values and roll order. Validation is a deliberate new API boundary; legacy `battle()` receives ordered results from its caller and does not perform that validation.
- `warg-core/src/test/java/org/warg/core/ParitySmokeTest.java`: small no-library test suite.
- `scripts/test-warg-core.sh`: compile and run smoke tests with a locally available JDK.

## Not yet implemented

No full turn state machine, maps, continent evaluation, player model, attack/conquest transitions, AI, persistence, networking, graphics or user interface exists in `warg-core` yet. Compatibility is therefore **partial and unverified as a whole**.

## Next tests

1. Separate the source-side battle result array fields (success, army losses, conquest, minimum/maximum move) from the pure dice-resolution layer.
2. Exhaustively compare legal dice combinations against a Java test harness derived from observed reference behavior.
3. Test configured 1/2/3 defender dice and 0/3 reinforcement minimums.
4. Test continent bonuses and territorial ownership with representative map fixtures.
5. Record baseline commands and full turn transitions for the new engine before adding graphical features.
