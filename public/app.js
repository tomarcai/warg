const $=id=>document.getElementById(id);
const labels=['CONFIGURACIÓN','CANJE DE CARTAS','DESPLIEGUE','ATAQUE','PREPARAR DADOS','OCUPAR TERRITORIO','FORTIFICAR','FIN DE TURNO','OPERACIÓN TERMINADA','ELEGIR CAPITAL','DEFENSA'];
let state=null,selected=[],selectedCards=new Set(),status='';
const esc=v=>String(v??'').replace(/[&<>"']/g,s=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[s]));
async function refresh(){
 const r=await fetch('/api/state');state=await r.json();render();
}
async function command(action,args={}){
 try{
  const r=await fetch('/api/action',{method:'POST',headers:{'content-type':'application/json'},body:JSON.stringify({action,...args})});
  const answer=await r.json();
  if(!r.ok||!answer.ok){status='Orden rechazada: '+(answer.error??'acción no válida en esta fase');}
  else{status='';selected=[];selectedCards.clear();}
  state=answer.state??state;render();
 }catch(e){status=e.message;render();}
}
function player(id){return state.players.find(p=>p.id===id);}
function current(){return state.players[state.current];}
function ownerColor(c){return c.owner===null?'#acb9a7':player(c.owner)?.color??'#acb9a7';}
function territoryName(id){return state.countries.find(c=>c.id===id)?.name?.replaceAll('-',' ')??'—';}
function select(id){
 const stage=state.state;
 if(stage===0||stage===7||stage===8)return;
 if(stage===3||stage===6){if(selected.length>=2||selected[0]===id)selected=[];selected.push(id);}
 else selected=[id];
 render();
}
function btn(label,act,args={},kind=''){
 return '<button class="'+kind+'" data-action="'+esc(act)+'" data-args="'+esc(JSON.stringify(args))+'">'+esc(label)+'</button>';
}
function render(){
 if(!state)return;
 const stage=state.state,p=current(),territories=state.countries;
 $('phase').textContent=labels[stage]??'—';
 $('turn').textContent=p?.name??'—';$('reserve').textContent=p?.reserve??'—';
 $('ownTerritories').textContent=p?territories.filter(c=>c.owner===p.id).length:'—';
 $('setup').style.display=stage===0?'block':'none';
 $('start').disabled=state.players.length<2;
 $('players').innerHTML=state.players.map(x=>'<div class="player '+(x.id===p?.id&&stage!==0?'active':'')+'"><span class="swatch" style="background:'+esc(x.color)+'"></span><span class="name">'+esc(x.name)+'</span><span class="count">'+territories.filter(c=>c.owner===x.id).length+' / '+x.reserve+'</span></div>').join('')||'<p class="muted">Sin comandantes</p>';
 $('objective').textContent=status||({
  0:'Configura los comandantes y comienza la operación.',
  1:'Selecciona tres cartas válidas para obtener refuerzos o termina el canje.',
  2:'Selecciona un territorio válido y despliega ejércitos.',
  3:'Selecciona el territorio atacante y después el objetivo enemigo.',
  4:'El atacante elige los dados que utilizará.',
  5:'Tras la conquista, desplaza los ejércitos obligatorios.',
  6:'Puedes mover ejércitos una vez entre dos territorios vecinos propios.',
  7:'Termina el turno para ceder el mando.',
  8:'La operación ha concluido.',
  9:'Selecciona un territorio propio para establecer la capital.',
  10:'El defensor elige sus dados antes de resolver el combate.'
 })[stage];
 const source=selected[0],target=selected[1];
 let actionHTML='';
 if(stage===2)actionHTML='<label>TERRITORIO DESTINO</label><strong>'+esc(territoryName(source))+'</strong><label>EJÉRCITOS A DESPLEGAR</label><input id="armyCount" type="number" min="1" value="1">'+btn('DESPLEGAR','place',{countryId:source},'primary');
 if(stage===3)actionHTML='<label>ORIGEN → DESTINO</label><strong>'+esc(territoryName(source))+' → '+esc(territoryName(target))+'</strong>'+btn('ATACAR','attack',{from:source,to:target},'primary')+btn('TERMINAR ATAQUES','endAttack');
 if(stage===4)actionHTML='<label>DADOS DEL ATACANTE</label><input id="diceCount" type="number" min="1" max="3" value="3">'+btn('LANZAR DADOS','rollAttack',{},'primary')+btn('RETIRARSE','retreat');
 if(stage===10)actionHTML='<label>DADOS DEL DEFENSOR</label><input id="diceCount" type="number" min="1" max="3" value="2">'+btn('DEFENDER','rollDefend',{},'primary')+(state.defenderDice?btn('RESOLVER COMBATE','battle',{},'primary'):'');
 if(stage===5)actionHTML='<label>EJÉRCITOS MÍNIMOS: '+state.mustMove+'</label><input id="armyCount" type="number" min="'+state.mustMove+'" value="'+state.mustMove+'">'+btn('OCUPAR TERRITORIO','moveConquered',{},'primary');
 if(stage===6)actionHTML='<label>ORIGEN → DESTINO</label><strong>'+esc(territoryName(source))+' → '+esc(territoryName(target))+'</strong><input id="armyCount" type="number" min="1" value="1">'+btn('FORTIFICAR','fortify',{from:source,to:target},'primary')+btn('OMITIR MOVIMIENTO','skipFortify');
 if(stage===7)actionHTML=btn('FINALIZAR TURNO','endTurn',{},'primary');
 if(stage===9)actionHTML='<strong>'+esc(territoryName(source))+'</strong>'+btn('DESIGNAR CAPITAL','setCapital',{countryId:source},'primary');
 if(stage===1){
  actionHTML='<label>CARTAS EN MANO</label>'+p.cards.map((c,i)=>'<label class="card-choice"><input type="checkbox" class="trade-card" value="'+i+'" '+(selectedCards.has(i)?'checked':'')+'>'+esc(c.type)+' '+esc(territoryName(c.countryId))+'</label>').join('');
  actionHTML+=btn('CANJEAR 3 CARTAS','trade',{},'primary')+btn('TERMINAR CANJE','endTrade');
 }
 $('actions').innerHTML=actionHTML;
 $('events').innerHTML=state.events.slice(-10).reverse().map(e=>'<div class="event"><b>'+esc(e.player)+'</b> '+esc(e.action)+' · '+esc(typeof e.result==='object'?JSON.stringify(e.result):e.result)+'</div>').join('')||'<p class="muted">Sin eventos</p>';
 $('world').innerHTML=territories.map(c=>'<button class="country '+(c.owner===null?'':'occupied')+' '+(selected.includes(c.id)?'selected':'')+'" data-country="'+c.id+'" style="left:'+(c.x/8)+'%;top:'+(c.y/5)+'%;--owner:'+esc(ownerColor(c))+'" title="'+esc(c.name)+' · '+c.armies+' ejércitos">'+(c.armies||'·')+'</button>').join('');
 $('territories').innerHTML=territories.map(c=>'<button class="territory '+(selected.includes(c.id)?'selected':'')+'" data-country="'+c.id+'"><span>'+esc(c.name.replaceAll('-',' '))+'</span><small style="color:'+esc(ownerColor(c))+'">'+(c.armies||'–')+'</small></button>').join('');
 $('mapSummary').textContent=territories.filter(c=>c.owner!==null).length+' / '+territories.length+' TERRITORIOS OCUPADOS';
 $('help').textContent=stage===0?'Partidas locales para entre 2 y 6 comandantes.':stage===8?'La partida ha terminado. Reinicia para jugar de nuevo.':'Selecciona territorios en el mapa o en la lista. Los ejércitos y los dados se introducen en el panel de órdenes.';
}
document.addEventListener('click',event=>{
 const t=event.target.closest('button');if(!t)return;
 if(t.dataset.country){select(Number(t.dataset.country));return;}
 if(!t.dataset.action)return;
 const action=t.dataset.action,args=JSON.parse(t.dataset.args||'{}');
 if(['place','fortify','moveConquered'].includes(action))args.armies=Number($('armyCount')?.value);
 if(['rollAttack','rollDefend'].includes(action))args.dice=Number($('diceCount')?.value);
 if(action==='trade')args.indices=[...selectedCards];
 command(action,args);
});
document.addEventListener('change',e=>{
 if(e.target.classList.contains('trade-card')){
  const index=Number(e.target.value);
  if(e.target.checked)selectedCards.add(index);else selectedCards.delete(index);
 }
});
$('addPlayer').addEventListener('click',()=>{
 const name=$('playerName').value.trim(),color=$('playerColor').value;
 if(!name)return;
 command('addPlayer',{name,color});$('playerName').value='';
});
$('start').addEventListener('click',()=>command('start'));
$('reset').addEventListener('click',async()=>{
 if(!confirm('¿Reiniciar la partida actual?'))return;
 const r=await fetch('/api/reset',{method:'POST'});state=await r.json();selected=[];status='';render();
});
refresh();
