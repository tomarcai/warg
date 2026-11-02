#!/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
BUILD=$(mktemp -d)
trap 'rm -rf "$BUILD"' EXIT HUP INT TERM
javac -encoding UTF-8 -d "$BUILD" \
  "$ROOT/warg-core/src/main/java/org/warg/core/Rules.java" \
  "$ROOT/warg-core/src/main/java/org/warg/core/BattleRound.java" \
  "$ROOT/warg-core/src/test/java/org/warg/core/ParitySmokeTest.java"
java -cp "$BUILD" org.warg.core.ParitySmokeTest
