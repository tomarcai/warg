// warg engine tests. Several of these reuse the original Domination test data
// (CardsTradeTest.java assertions and the recorded games in Domination/res/test_scripts).

import { test } from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import { Risk } from '../src/engine/Risk.js';
import * as RG from '../src/engine/RiskGame.js';
import { RiskGame } from '../src/engine/RiskGame.js';
import { serializeGame, deserializeGame } from '../src/engine/save.js';
import { createNodeIo, PATHS, REPO } from '../src/server/nodeIo.js';
import { javaHashMapKeyOrder, JavaPriorityQueue, binarySearchDouble } from '../src/engine/util.js';
import { PLAYER_AI_EASY, PLAYER_AI_AVERAGE, PLAYER_AI_HARD } from '../src/engine/ai/AIDomination.js';

const io = createNodeIo();

function newRisk() {
  return new Risk(io, { aiWait: 0, showDice: false, language: 'en' });
}

async function waitForGameOver(risk, timeout = 120000) {
  const start = Date.now();
  for (;;) {
    await new Promise((r) => setTimeout(r, 10));
    const g = risk.getGame();
    if (g && g.getState() === RG.STATE_GAME_OVER && !risk.processing && risk.inbox.length === 0) return g;
    if (Date.now() - start > timeout) throw new Error('game did not finish, state ' + (g && g.getState()));
  }
}

test('card trade values match CardsTradeTest.java', async () => {
  // runs every getTradeAbsValue assertion of the original java test, keeping track of
  // the int variables the test (re)assigns as it goes
  const javaTest = fs.readFileSync(path.join(REPO, 'Domination/test/net/yura/domination/engine/core/CardsTradeTest.java'), 'utf8');
  const game = await RiskGame.create(io);
  const vars = {};
  const cardName = { 'Card.CANNON': 'Cannon', 'Card.INFANTRY': 'Infantry', 'Card.CAVALRY': 'Cavalry', 'Card.WILDCARD': 'wildcard' };
  const modes = { 'RiskGame.CARD_INCREASING_SET': 0, 'RiskGame.CARD_FIXED_SET': 1, 'RiskGame.CARD_ITALIANLIKE_SET': 2 };
  let checked = 0;
  for (const line of javaTest.split('\n')) {
    const assign = /^\s*(?:int\s+)?(\w+)\s*=\s*(\d+)\s*;/.exec(line);
    if (assign) { vars[assign[1]] = parseInt(assign[2], 10); continue; }
    const m = /assertEquals\((\w+), instance\.getTradeAbsValue\((Card\.\w+), (Card\.\w+), (Card\.\w+), (RiskGame\.\w+)\) \);/.exec(line);
    if (!m) continue;
    const expected = /^\d+$/.test(m[1]) ? parseInt(m[1], 10) : vars[m[1]];
    assert.notEqual(expected, undefined, 'unknown variable in ' + line);
    assert.equal(game.getTradeAbsValue(cardName[m[2]], cardName[m[3]], cardName[m[4]], modes[m[5]]), expected, line.trim());
    checked++;
  }
  assert.equal(checked, 192, 'checked ' + checked + ' assertions');
});

test('italian card values', async () => {
  const game = await RiskGame.create(io);
  const I = 2;
  assert.equal(game.getTradeAbsValue('Cavalry', 'Cavalry', 'Cavalry', I), 8);
  assert.equal(game.getTradeAbsValue('Infantry', 'Infantry', 'Infantry', I), 6);
  assert.equal(game.getTradeAbsValue('Cannon', 'Cannon', 'Cannon', I), 4);
  assert.equal(game.getTradeAbsValue('Cannon', 'Infantry', 'Cavalry', I), 10);
  assert.equal(game.getTradeAbsValue('wildcard', 'Cannon', 'Cannon', I), 12);
  assert.equal(game.getTradeAbsValue('wildcard', 'Cannon', 'Infantry', I), 0);
  assert.equal(game.getTradeAbsValue('wildcard', 'wildcard', 'Infantry', I), 0);
  assert.equal(game.getTradeAbsValue('wildcard', 'wildcard', 'wildcard', I), 0);
});

test('increasing card set sequence 4 6 8 10 12 15 20 25', async () => {
  const game = await RiskGame.create(io);
  const seq = [];
  for (let i = 0; i < 8; i++) {
    const v = game.getNewCardState();
    seq.push(v);
    game.cardState = v;
  }
  assert.deepEqual(seq, [4, 6, 8, 10, 12, 15, 20, 25]);
});

test('java helpers: HashMap order, PriorityQueue, binarySearch', () => {
  // java.util.HashMap iterates these card names in this order
  assert.deepEqual(javaHashMapKeyOrder(['wildcard', 'Infantry', 'Cavalry', 'Cannon']).length, 4);
  const pq = new JavaPriorityQueue((a, b) => a - b);
  [5, 1, 4, 1, 3].forEach((x) => pq.add(x));
  const out = [];
  while (!pq.isEmpty()) out.push(pq.poll());
  assert.deepEqual(out, [1, 1, 3, 4, 5]);
  assert.equal(binarySearchDouble([-3, -2, -1], -2), 1);
  assert.equal(binarySearchDouble([-3, -2, -1], -2.5), -2);
});

test('starting armies and reinforcements follow the rules', async () => {
  const risk = newRisk();
  await risk.ready();
  await risk.parser('newgame');
  for (const p of ['human cyan a', 'human green b', 'human red c']) await risk.parser('newplayer ' + p);
  await risk.parser('startgame domination increasing');
  const g = risk.getGame();
  // (10 - 3 players) * round(42 * 0.12) = 7 * 5
  for (const p of g.getPlayers()) assert.equal(p.getExtraArmies(), 35);
  assert.equal(g.getState(), RG.STATE_PLACE_ARMIES);
  assert.equal(g.getMaxDefendDice(), 2);
});

const scripts = fs.readdirSync(PATHS.scripts).filter((f) => f.endsWith('.log'));
for (const file of scripts) {
  test('replays original recorded game ' + file, async () => {
    const risk = newRisk();
    await risk.ready();
    const origInfo = console.info;
    const invalid = [];
    console.info = (...a) => invalid.push(a.join(' '));
    try {
      await risk.parser('play ' + path.join(PATHS.scripts, file));
      await risk.idle();
    } finally {
      console.info = origInfo;
    }
    const g = risk.getGame();
    assert.equal(risk.scriptError, undefined);
    assert.equal(g.getState(), RG.STATE_GAME_OVER, 'game should be over');
    if (g.getGameMode() === RG.MODE_DOMINATION) {
      assert.equal(g.getCurrentPlayer().getNoTerritoriesOwned(), g.getNoCountries());
    }
    // every command of the log is accepted (the only exception is a RENAME sent while the
    // game was already over, which the original also rejects)
    assert.ok(invalid.every((l) => l.includes('RENAME')), invalid.join('\n'));
  });
}

test('save and load keeps the game identical', async () => {
  const risk = newRisk();
  await risk.ready();
  await risk.parser('play ' + path.join(PATHS.scripts, 'domination-fixed-recycle.log'));
  await risk.idle();
  const g = risk.getGame();
  const json = serializeGame(g);
  const g2 = await deserializeGame(JSON.parse(JSON.stringify(json)), io, {});
  assert.deepEqual(serializeGame(g2), json);
});

for (const mode of ['domination increasing recycle', 'capital fixed recycle', 'mission italianlike recycle', 'domination fixed autoplaceall']) {
  test('AI only game finishes: ' + mode, async () => {
    const risk = newRisk();
    await risk.ready();
    const errors = [];
    const origWarn = console.warn;
    console.warn = (...a) => errors.push(a.join(' '));
    try {
      await risk.parser('newgame');
      for (const p of ['ai easy 6 6', 'ai average 2 2', 'ai hard 1 1', 'ai easy 3 3', 'ai average 4 4', 'ai hard 5 5']) await risk.parser('newplayer ' + p);
      risk.parser('startgame ' + mode);
      const g = await waitForGameOver(risk);
      assert.ok([PLAYER_AI_EASY, PLAYER_AI_AVERAGE, PLAYER_AI_HARD].includes(g.getCurrentPlayer().getType()));
    } finally {
      console.warn = origWarn;
    }
    assert.deepEqual(errors, []);
  });
}
