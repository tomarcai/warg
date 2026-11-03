import test from 'node:test';
import assert from 'node:assert/strict';
import { CARD_MODE, tradeValue, nextTradeValue, bestTrade, parseCards } from '../src/engine/cards.js';
test('progressive trade values follow Domination r2844', () => {
  assert.deepEqual([0,4,6,8,10,12,15,20].map(nextTradeValue), [4,6,8,10,12,15,20,25]);
  assert.equal(tradeValue(['Infantry','Infantry','Cavalry'],0),0);
  assert.equal(tradeValue(['Infantry','Cannon','Cavalry'],0),4);
});
test('fixed trade values and wildcard substitution', () => {
  assert.equal(tradeValue(['Infantry','Infantry','Infantry'],1),4);
  assert.equal(tradeValue(['Cavalry','Cavalry','wildcard'],1),6);
  assert.equal(tradeValue(['Cannon','Cannon','wildcard'],1),8);
  assert.equal(tradeValue(['Cannon','Cavalry','Infantry'],1),10);
  assert.equal(tradeValue(['wildcard','wildcard','wildcard'],1),12);
  assert.equal(tradeValue(['Infantry','Infantry','Cavalry'],1),0);
});
test('Italian rules handle wildcards differently', () => {
  assert.equal(tradeValue(['Cavalry','Cavalry','Cavalry'],2),8);
  assert.equal(tradeValue(['Infantry','Infantry','Infantry'],2),6);
  assert.equal(tradeValue(['Cannon','Cannon','Cannon'],2),4);
  assert.equal(tradeValue(['Cannon','Cavalry','Infantry'],2),10);
  assert.equal(tradeValue(['Cannon','Cannon','wildcard'],2),12);
  assert.equal(tradeValue(['wildcard','wildcard','Cannon'],2),0);
});
test('best card combination is selected without modifying hand', () => {
  const cards=[{type:'Infantry'},{type:'Infantry'},{type:'Cavalry'},{type:'wildcard'}];
  assert.equal(bestTrade(cards,CARD_MODE.FIXED).value,10);
  assert.equal(cards.length,4);
});
test('original card format reads both card deck and missions',()=>{
  const x=parseCards('[cards]\nInfantry 1\nwildcard\n[missions]\n0 24 1 0 0 0 Occupy 24 countries',new Set([1]));
  assert.equal(x.cards.length,2); assert.equal(x.missions[0].countries,24);
});
