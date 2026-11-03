import {bestTrade,CARD_MODE} from './cards.js';
export const STATE=Object.freeze({NEW:0,TRADE:1,PLACE:2,ATTACK:3,ROLL:4,CONQUER:5,FORTIFY:6,END:7,OVER:8,CAPITAL:9,DEFEND:10});
export const MODE=Object.freeze({DOMINATION:0,CAPITAL:2,MISSION:3});
const positive=n=>Number.isSafeInteger(n)&&n>0;
/** Headless game rules adapted from Domination r2844 RiskGame. */
export class Game {
 constructor(map,options={}) {
  if(!map?.countries?.length||!map?.continents?.length)throw Error('Missing map');
  this.map=map;this.countries=map.countries.map(c=>({...c,neighbours:[...c.neighbours],owner:null,armies:0}));
  this.byId=new Map(this.countries.map(c=>[c.id,c]));this.players=[];this.state=STATE.NEW;
  this.mode=options.mode??MODE.DOMINATION;this.cardMode=options.cardMode??CARD_MODE.INCREASING;
  this.maxDefendDice=options.maxDefendDice??(this.cardMode===CARD_MODE.ITALIAN?3:2);this.minReinforcements=options.minReinforcements??(this.cardMode===CARD_MODE.ITALIAN?0:3);
  this.maxCards=options.maxCards??(this.cardMode===CARD_MODE.ITALIAN?Number.MAX_SAFE_INTEGER:5);this.current=0;this.setupCompleted=0;
  this.attacker=null;this.defender=null;this.attackerDice=0;this.defenderDice=0;this.mustMove=0;
  this.captured=false;this.tradeCap=false;this.cardState=0;
  this.deck=options.cards?.map(c=>({...c}))??[];this.discard=[];this.missions=options.missions??[];
  if(![0,2,3].includes(this.mode)||![0,1,2].includes(this.cardMode))throw RangeError('Invalid game mode');
  if(!Number.isInteger(this.maxDefendDice)||this.maxDefendDice<1||this.maxDefendDice>3)throw RangeError('Invalid defender dice limit');
 }
 get active(){return this.players[this.current]??null;}
 get setupDone(){return this.setupCompleted===this.players.length;}
 country(id){return this.byId.get(Number(id))??null;}
 addPlayer(name,color,type='human'){
  if(this.state!==STATE.NEW||!name||this.players.some(p=>p.name===name||p.color===color))return false;
  this.players.push({id:this.players.length,name,color,type,reserve:0,cards:[],capital:null,eliminated:[],turns:0});return true;
 }
 owned(id){return this.countries.filter(c=>c.owner===id);}
 ownsContinent(id,continent){
  const members=this.countries.filter(c=>c.continent===continent);
  return members.length>0&&members.every(c=>c.owner===id);
 }
 reinforcements(id){
  return Math.max(this.minReinforcements,Math.floor(this.owned(id).length/3))+
   this.map.continents.reduce((sum,c)=>sum+(this.ownsContinent(id,c.id)?c.bonus:0),0);
 }
 start(){
  if(this.state!==STATE.NEW||this.players.length<2||this.players.length>6)return false;
  if(this.mode===MODE.MISSION&&this.missions.length<this.players.length)return false;
  const armies=Math.max(1,10-this.players.length)*Math.round(this.countries.length*.12);
  for(const p of this.players)p.reserve=armies;
  this.state=STATE.PLACE;return true;
 }
 place(id,n=1){
  const c=this.country(id),p=this.active;
  if(this.state!==STATE.PLACE||!c||!positive(n)||!p||p.reserve<n)return false;
  if(!this.setupDone){
   if(n!==1||(c.owner!==null&&c.owner!==p.id)||(c.owner===p.id&&this.countries.some(x=>x.owner===null)))return false;
   c.owner=p.id;
  }else if(c.owner!==p.id)return false;
  c.armies+=n;p.reserve-=n;
  if(this.setupDone)this.state=p.reserve?STATE.PLACE:STATE.ATTACK;
  else{if(!p.reserve)this.setupCompleted++;this.state=STATE.END;}
  this.checkWin();return true;
 }
 hasMoves(id,attack){
  return this.owned(id).some(c=>c.armies>1&&c.neighbours.some(k=>{
   const target=this.country(k);return target&&(attack?target.owner!==id:target.owner===id);
  }));
 }
 canTrade(p=this.active){return !!bestTrade(p.cards,this.cardMode,this.cardState);}
 nextPlaceState(){
  if(this.active.reserve>0)return STATE.PLACE;
  if(this.hasMoves(this.active.id,true))return STATE.ATTACK;
  return this.hasMoves(this.active.id,false)?STATE.FORTIFY:STATE.END;
 }
 endTurn(){
  if(this.state!==STATE.END)return false;
  let next=null;
  for(let i=1;i<=this.players.length;i++){
   const index=(this.current+i)%this.players.length,p=this.players[index];
   if(!this.setupDone||(this.owned(p.id).length&&(this.reinforcements(p.id)>0||this.canTrade(p)||this.hasMoves(p.id,true)||this.hasMoves(p.id,false)))){next=index;break;}
  }
  if(next===null){this.state=STATE.OVER;return true;}
  this.current=next;const p=this.active;
  const capital=this.mode===MODE.CAPITAL&&p.capital===null;
  if(this.setupDone&&!capital){p.turns++;p.reserve+=this.reinforcements(p.id);}
  this.state=this.setupDone&&capital?STATE.CAPITAL:this.canTrade(p)?STATE.TRADE:this.nextPlaceState();
  this.captured=false;this.tradeCap=false;return true;
 }
 attack(from,to){
  const a=this.country(from),d=this.country(to);
  if(this.state!==STATE.ATTACK||!a||!d||a.owner!==this.active.id||d.owner===a.owner||d.owner===null||!a.neighbours.includes(d.id)||a.armies<=1)return false;
  this.attacker=a.id;this.defender=d.id;this.state=STATE.ROLL;return true;
 }
 rollAttack(n){
  const a=this.country(this.attacker);
  if(this.state!==STATE.ROLL||!a||!positive(n)||n>Math.min(3,a.armies-1))return false;
  this.attackerDice=n;this.current=this.players.findIndex(p=>p.id===this.country(this.defender).owner);
  this.state=STATE.DEFEND;return true;
 }
 rollDefend(n){
  const d=this.country(this.defender),a=this.country(this.attacker);
  if(this.state!==STATE.DEFEND||!d||!positive(n)||n>Math.min(this.maxDefendDice,d.armies))return false;
  this.defenderDice=n;this.current=this.players.findIndex(p=>p.id===a.owner);return true;
 }
 battle(attacking,defending){
  const a=this.country(this.attacker),d=this.country(this.defender);
  if(this.state!==STATE.DEFEND||!this.defenderDice||!a||!d)return null;
  if(!Array.isArray(attacking)||!Array.isArray(defending)||attacking.length!==this.attackerDice||defending.length!==this.defenderDice||
    ![...attacking,...defending].every(n=>Number.isInteger(n)&&n>=1&&n<=6))return null;
  const aa=[...attacking].sort((x,y)=>y-x),dd=[...defending].sort((x,y)=>y-x);
  const result={attackerLost:0,defenderLost:0,conquered:false,eliminated:false,mustMove:0,maxMove:0};
  for(let i=0;i<Math.min(aa.length,dd.length);i++){
   if(aa[i]>dd[i]){d.armies--;result.defenderLost++;}else{a.armies--;result.attackerLost++;}
  }
  if(!d.armies){
   const defeated=d.owner;d.owner=a.owner;this.captured=true;this.mustMove=this.attackerDice;this.state=STATE.CONQUER;
   result.conquered=true;result.mustMove=this.mustMove;result.maxMove=a.armies-1;
   if(!this.owned(defeated).length){
    const loser=this.players[defeated],winner=this.active;
    winner.eliminated.push(defeated);winner.cards.push(...loser.cards.splice(0));
    this.tradeCap=winner.cards.length>this.maxCards;result.eliminated=true;
   }
  }else this.state=a.armies===1?STATE.ATTACK:STATE.ROLL;
  this.attackerDice=0;this.defenderDice=0;return result;
 }
 resolveBattle(rng=Math.random){
  if(this.state!==STATE.DEFEND||!this.defenderDice)return null;
  const roll=n=>Array.from({length:n},()=>Math.floor(rng()*6)+1);
  return this.battle(roll(this.attackerDice),roll(this.defenderDice));
 }
 moveConquered(n){
  const a=this.country(this.attacker),d=this.country(this.defender);
  if(this.state!==STATE.CONQUER||!a||!d||!positive(n)||n<this.mustMove||n>=a.armies)return false;
  a.armies-=n;d.armies+=n;this.state=this.tradeCap?STATE.TRADE:STATE.ATTACK;
  this.attacker=null;this.defender=null;this.mustMove=0;this.checkWin();return true;
 }
 retreat(){if(this.state!==STATE.ROLL)return false;this.attacker=null;this.defender=null;this.state=STATE.ATTACK;return true;}
 endAttack(){if(this.state!==STATE.ATTACK)return false;this.state=STATE.FORTIFY;return true;}
 fortify(from,to,n){
  const a=this.country(from),b=this.country(to);
  if(this.state!==STATE.FORTIFY||!a||!b||a.owner!==this.active.id||b.owner!==this.active.id||!a.neighbours.includes(b.id)||!positive(n)||a.armies<=n)return false;
  a.armies-=n;b.armies+=n;this.state=STATE.END;this.checkWin();return true;
 }
 skipFortify(){if(this.state!==STATE.FORTIFY)return false;this.state=STATE.END;return true;}
 setCapital(id){
  const c=this.country(id);
  if(this.state!==STATE.CAPITAL||!c||c.owner!==this.active.id)return false;
  this.active.capital=c.id;this.state=STATE.END;return true;
 }
 trade(indices){
  if(this.state!==STATE.TRADE||!Array.isArray(indices)||indices.length!==3||new Set(indices).size!==3||
    indices.some(i=>!Number.isInteger(i)||i<0||i>=this.active.cards.length))return 0;
  const cards=indices.map(i=>this.active.cards[i]),chosen=bestTrade(cards,this.cardMode,this.cardState);
  if(!chosen)return 0;
  let value=chosen.value;
  if(this.cardMode===CARD_MODE.INCREASING)this.cardState=value;
  const owned=cards.filter(c=>c.countryId!=null&&this.country(c.countryId)?.owner===this.active.id);
  if(this.cardMode===CARD_MODE.ITALIAN)value+=2*owned.length;
  else if(owned.length)this.country(owned[0].countryId).armies+=2;
  for(const i of [...indices].sort((a,b)=>b-a))this.discard.push(...this.active.cards.splice(i,1));
  this.active.reserve+=value;
  if(!this.canTrade()||(this.tradeCap&&this.active.cards.length<this.maxCards)){this.tradeCap=false;this.state=STATE.PLACE;}
  return value;
 }
 endTrade(){
  if(this.state!==STATE.TRADE||this.active.cards.length>=this.maxCards||this.tradeCap)return false;
  this.state=this.nextPlaceState();return true;
 }
 checkWin(){
  const p=this.active;if(!p||!this.setupDone)return false;
  let won=this.map.continents.every(c=>this.ownsContinent(p.id,c.id));
  if(!won&&this.mode===MODE.CAPITAL&&p.capital!=null)
   won=this.country(p.capital)?.owner===p.id&&this.players.every(x=>x.capital!=null&&this.country(x.capital)?.owner===p.id);
  if(!won&&this.mode===MODE.MISSION&&p.mission){
   const m=p.mission,eliminated=m.player>0?this.players[m.player-1]:null;
   if(eliminated&&eliminated.id!==p.id&&!this.owned(eliminated.id).length&&p.eliminated.includes(eliminated.id))won=true;
   if(!won&&m.countries>0&&m.armies>0&&(!eliminated||eliminated.id===p.id||!this.owned(eliminated.id).length))
    won=this.owned(p.id).filter(c=>c.armies>=m.armies).length>=m.countries;
   if(!won&&m.continents?.[0]&&m.continents[0]!=='0')
    won=m.continents.every((id,i)=>id==='0'||(id==='*'?this.map.continents.filter(c=>this.ownsContinent(p.id,c.id)).length>=i+1:this.ownsContinent(p.id,Number(id))));
  }
  if(won)this.state=STATE.OVER;return won;
 }
 snapshot(){return JSON.parse(JSON.stringify({state:this.state,mode:this.mode,cardMode:this.cardMode,current:this.current,players:this.players,countries:this.countries,captured:this.captured,setupCompleted:this.setupCompleted,attacker:this.attacker,defender:this.defender,mustMove:this.mustMove,attackerDice:this.attackerDice,defenderDice:this.defenderDice,cardState:this.cardState}));}
}
