import test from 'node:test';
import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';
import {parseMap} from '../src/engine/map.js';
import {parseCards} from '../src/engine/cards.js';
const root=new URL('../Domination/swingUI/game/Domination/maps/',import.meta.url);
test('original world map preserves 42 countries and six continents',async()=>{
 const map=parseMap(await readFile(new URL('world.map',root),'utf8'));
 assert.equal(map.countries.length,42);
 assert.equal(map.continents.length,6);
 assert.equal(map.countries.find(c=>c.id===1).name,'Alaska');
 assert.deepEqual(map.countries.find(c=>c.id===1).neighbours,[2,3,38]);
 assert.equal(map.continents.find(c=>c.name==='Asia').bonus,7);
});
test('original risk.cards parses deck and secret missions',async()=>{
 const map=parseMap(await readFile(new URL('world.map',root),'utf8'));
 const deck=parseCards(await readFile(new URL('risk.cards',root),'utf8'),new Set(map.countries.map(c=>c.id)));
 assert.equal(deck.cards.length,44);
 assert.ok(deck.missions.length>=6);
 assert.equal(deck.cards.filter(c=>c.type==='wildcard').length,2);
});
test('invalid map references are rejected instead of silently changed',()=>{
 assert.throws(()=>parseMap('[continents]\nA 5 red\n[countries]\n1 A 1 1 1\n[borders]\n1 9'),/Invalid neighbour/);
});
