import test from 'node:test';
import assert from 'node:assert/strict';
import {Game,STATE} from '../src/engine/game.js';
const map={continents:[{id:1,bonus:5},{id:2,bonus:2}],countries:[
 {id:1,continent:1,neighbours:[2]},{id:2,continent:1,neighbours:[1,3]},
 {id:3,continent:2,neighbours:[2,4]},{id:4,continent:2,neighbours:[3]}]};
const create=()=>{const g=new Game(map);assert.equal(g.addPlayer('A','#aaa'),true);assert.equal(g.addPlayer('B','#bbb'),true);return g;};
test('player uniqueness and initial deployment constraints',()=>{
 const g=create();assert.equal(g.addPlayer('A','#bbb'),false);assert.equal(g.start(),true);
 // A 4-country fixture produces 0 original starting armies; provide reserves to test placement separately.
 g.players.forEach(p=>p.reserve=5);
 assert.equal(g.place(1,2),false);assert.equal(g.place(1),true);
 assert.equal(g.state,STATE.END);assert.equal(g.endTurn(),true);
 assert.equal(g.place(1),false);assert.equal(g.place(2),true);
});
test('continent and territory reinforcements honor minimum',()=>{
 const g=create();g.country(1).owner=0;g.country(2).owner=0;
 assert.equal(g.reinforcements(0),8);assert.equal(g.reinforcements(1),3);
});
test('attacking requires adjacency, dice limits and transfers control to defender',()=>{
 const g=create();g.state=STATE.ATTACK;g.country(1).owner=0;g.country(2).owner=1;
 g.country(1).armies=4;g.country(2).armies=2;
 assert.equal(g.attack(1,4),false);assert.equal(g.attack(1,2),true);
 assert.equal(g.rollAttack(4),false);assert.equal(g.rollAttack(3),true);
 assert.equal(g.current,1);assert.equal(g.rollDefend(3),false);assert.equal(g.rollDefend(2),true);
 assert.equal(g.current,0);
 const outcome=g.battle([6,5,1],[5,1]);assert.equal(outcome.conquered,true);
 assert.equal(g.state,STATE.CONQUER);assert.equal(g.mustMove,3);
 assert.equal(g.moveConquered(2),false);assert.equal(g.moveConquered(3),true);
});
test('defender wins equal dice, attack retreat and one fortification',()=>{
 const g=create();g.country(1).owner=0;g.country(2).owner=1;g.country(3).owner=0;
 g.country(1).armies=4;g.country(2).armies=2;g.country(3).armies=1;
 g.state=STATE.ATTACK;assert.equal(g.attack(1,2),true);
 assert.equal(g.rollAttack(2),true);assert.equal(g.rollDefend(2),true);
 const r=g.battle([4,2],[4,1]);assert.equal(r.attackerLost,1);assert.equal(r.defenderLost,1);
 assert.equal(g.state,STATE.ROLL);assert.equal(g.retreat(),true);
 assert.equal(g.endAttack(),true);assert.equal(g.fortify(1,3,1),false);
 assert.equal(g.skipFortify(),true);assert.equal(g.state,STATE.END);
});
test('capital requires own territory',()=>{
 const g=create();g.state=STATE.CAPITAL;g.country(1).owner=0;
 assert.equal(g.setCapital(2),false);assert.equal(g.setCapital(1),true);
 assert.equal(g.active.capital,1);
});
