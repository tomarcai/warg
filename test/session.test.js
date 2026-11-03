import test from 'node:test';
import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';
import {parseMap} from '../src/engine/map.js';
import {parseCards} from '../src/engine/cards.js';
import {Session} from '../src/engine/session.js';
import {STATE} from '../src/engine/game.js';
const resource=new URL('../Domination/swingUI/game/Domination/maps/',import.meta.url);
async function create(opts={}){
 const map=parseMap(await readFile(new URL('world.map',resource),'utf8'));
 const cards=parseCards(await readFile(new URL('risk.cards',resource),'utf8'));
 const s=new Session(map,cards,opts);
 assert.ok(s.execute('addPlayer',{name:'Red',color:'red'}).ok);
 assert.ok(s.execute('addPlayer',{name:'Blue',color:'blue'}).ok);
 return s;
}
test('mission mode auto-distributes all 42 original territories',async()=>{
 const s=await create({mode:3});assert.equal(s.execute('start').ok,true);
 assert.equal(s.game.countries.filter(c=>c.owner!==null).length,42);
 assert.equal(s.game.countries.reduce((n,c)=>n+c.armies,0),42);
 assert.ok(s.game.players.every(p=>p.mission&&s.game.owned(p.id).length===21));
 assert.equal(s.game.state,STATE.PLACE);
});
test('turn-end rewards exactly one card for a successful conquest',async()=>{
 const s=await create();s.execute('start');const g=s.game;
 g.state=STATE.END;g.captured=true;const previous=g.active;
 assert.equal(previous.cards.length,0);assert.equal(s.execute('endTurn').ok,true);
 assert.equal(previous.cards.length,1);assert.equal(g.captured,false);
});
test('trade adds two armies only to the first owned card territory (standard)',async()=>{
 const s=await create({cardMode:1});s.execute('start');const g=s.game;const p=g.active;
 g.country(1).owner=p.id;g.country(2).owner=p.id;g.country(1).armies=1;g.country(2).armies=1;
 p.cards=[{type:'Infantry',countryId:1},{type:'Infantry',countryId:2},{type:'Infantry',countryId:3}];g.state=STATE.TRADE;
 const r=s.execute('trade',{indices:[0,1,2]});assert.equal(r.ok,true);
 assert.equal(p.reserve>=4,true);assert.equal(g.country(1).armies,3);assert.equal(g.country(2).armies,1);
});

test('configuration preserves pregame players and selects Italian rules',async()=>{
 const s=await create();const r=s.execute('configure',{mode:2,cardMode:2,autoplaceAll:true});
 assert.equal(r.ok,true);assert.equal(r.state.mode,2);
 assert.equal(s.game.players.length,2);assert.equal(s.game.minReinforcements,0);
 assert.equal(s.game.maxDefendDice,3);assert.equal(s.game.maxCards,Number.MAX_SAFE_INTEGER);
 assert.equal(s.execute('start').ok,true);
 assert.equal(s.game.countries.filter(c=>c.owner!=null).length,42);
 assert.equal(s.execute('configure',{mode:0}).ok,false);
});
