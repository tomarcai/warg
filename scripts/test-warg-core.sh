#!/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
BUILD=$(mktemp -d)
trap 'rm -rf "$BUILD"' EXIT HUP INT TERM
javac -encoding UTF-8 -d "$BUILD" \
  "$ROOT/warg-core/src/main/java/org/warg/core/Rules.java" \
  "$ROOT/warg-core/src/main/java/org/warg/core/BattleRound.java" \
  "$ROOT/warg-core/src/main/java/org/warg/core/Board.java" \
  "$ROOT/warg-core/src/main/java/org/warg/core/TurnController.java" \
  "$ROOT/warg-core/src/main/java/org/warg/core/CombatResult.java" \
  "$ROOT/warg-core/src/test/java/org/warg/core/ParitySmokeTest.java" \
  "$ROOT/warg-core/src/test/java/org/warg/core/BoardTest.java" \
  "$ROOT/warg-core/src/test/java/org/warg/core/TurnControllerTest.java" \
  "$ROOT/warg-core/src/test/java/org/warg/core/CombatIntegrationTest.java"
java -cp "$BUILD" org.warg.core.ParitySmokeTest
java -cp "$BUILD" org.warg.core.BoardTest
java -cp "$BUILD" org.warg.core.TurnControllerTest
java -cp "$BUILD" org.warg.core.CombatIntegrationTest
