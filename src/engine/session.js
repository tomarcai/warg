import {Game,STATE,MODE} from './game.js';
export const ACTIONS=Object.freeze(['addPlayer','start','place','endTurn','attack','rollAttack','rollDefend','battle','moveConquered','retreat','endAttack','fortify','skipFortify','setCapital','trade','endTrade']);
/**
 * Local game coordinator. Never accept fabricated dice arrays via the public API.
 * Domination's Risk.java coordinates random CARD and DICE commands.
 */
export class Session {
 constructor(map,deck,options={}) {
  this.map=map;this.cards=deck;this.options=options;
  this.game=new Game(map,{...options,cards:deck.cards,missions:deck.missions});
  this.events=[];
 }
 log(action,result){this.events.push({action,player:this.game.active?.name??null,result,at:new Date().toISOString()});if(this.events.length>150)this.events.shift();}
 shuffle(items){for(let i=items.length-1;i>0;i--){const j=Math.floor(Math.random()*(i+1));[items[i],items[j]]=[items[j],items[i]];}return items;}
 draw() {
  const game=this.game;
  if(!game.captured)return null;
  if(!game.deck.length&&this.options.recycleCards!==false){
   game.deck=this.shuffle(game.discard.splice(0));
  }
  if(!game.deck.length)return null;
  const index=Math.floor(Math.random()*game.deck.length);
  return game.deck.splice(index,1)[0];
 }
 execute(action,args={}) {
  if(!ACTIONS.includes(action))throw Error('Unknown action');
  const g=this.game;
  let result;
  switch(action){
   case 'addPlayer':result=g.addPlayer(args.name,args.color,args.type);break;
   case 'start':
    result=g.start();
    if(result){
     this.shuffle(g.deck);
     g.current=Math.floor(Math.random()*g.players.length);
     if(g.mode===MODE.MISSION){
      const missions=this.shuffle([...g.missions]);
      for(const player of g.players)player.mission=missions.pop();
     }
     if(g.mode===MODE.MISSION||this.options.autoplaceAll){
      for(const c of this.shuffle([...g.countries])){
       if(!g.place(c.id,1))throw Error('Initial auto-placement failed');
       if(!g.endTurn())throw Error('Initial turn rotation failed');
      }
     }
    }
    break;
   case 'place':result=g.place(args.countryId,args.armies??1);break;
   case 'endTurn':{
    if(g.state!==STATE.END){result=false;break;}
    const previous=g.active;
    const reward=this.draw();
    if(reward)previous.cards.push(reward);
    result=g.endTurn();
    break;
   }
   case 'attack':result=g.attack(args.from,args.to);break;
   case 'rollAttack':result=g.rollAttack(args.dice);break;
   case 'rollDefend':result=g.rollDefend(args.dice);break;
   case 'battle':result=g.resolveBattle();break;
   case 'moveConquered':result=g.moveConquered(args.armies);break;
   case 'retreat':result=g.retreat();break;
   case 'endAttack':result=g.endAttack();break;
   case 'fortify':result=g.fortify(args.from,args.to,args.armies);break;
   case 'skipFortify':result=g.skipFortify();break;
   case 'setCapital':
    result=g.setCapital(args.countryId);
    if(result)g.deck=g.deck.filter(c=>c.countryId!==args.countryId);
    break;
   case 'trade':result=g.trade(args.indices);break;
   case 'endTrade':result=g.endTrade();break;
  }
  if(result===false||result===null||result===0)return {ok:false,result,state:g.snapshot()};
  this.log(action,result);
  return {ok:true,result,state:g.snapshot()};
 }
 snapshot(){return {...this.game.snapshot(),map:{name:this.map.name,continents:this.map.continents},events:this.events};}
}
