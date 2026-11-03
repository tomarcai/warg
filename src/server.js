import http from 'node:http';
import { readFile, stat } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseMap } from './engine/map.js';
import { parseCards } from './engine/cards.js';
import { Session } from './engine/session.js';
const ROOT=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const MAPS=path.join(ROOT,'Domination/swingUI/game/Domination/maps');
const PUBLIC=path.join(ROOT,'public');
const mapText=await readFile(path.join(MAPS,'world.map'),'utf8');
const map=parseMap(mapText);
const deck=parseCards(await readFile(path.join(MAPS,'risk.cards'),'utf8'),new Set(map.countries.map(c=>c.id)));
let session=new Session(map,deck);
const PORT=Number(process.env.PORT??3000);
const HOST=process.env.HOST??'127.0.0.1';
function json(res,status,payload){
 res.writeHead(status,{'content-type':'application/json; charset=utf-8','cache-control':'no-store','x-content-type-options':'nosniff'});
 res.end(JSON.stringify(payload));
}
async function body(req){
 let text='';
 for await(const chunk of req){text+=chunk;if(text.length>32768)throw Error('Request too large');}
 const value=JSON.parse(text||'{}');
 if(!value||typeof value!=='object'||Array.isArray(value))throw Error('JSON object required');
 return value;
}
const allowed={'/':'index.html','/app.css':'app.css','/app.js':'app.js','/assets/world_pic.jpg':'../Domination/swingUI/game/Domination/maps/world_pic.jpg'};
const types={'.html':'text/html; charset=utf-8','.css':'text/css; charset=utf-8','.js':'text/javascript; charset=utf-8','.jpg':'image/jpeg'};
http.createServer(async(req,res)=>{
 try{
  const url=new URL(req.url,'http://localhost');
  if(req.method==='GET'&&url.pathname==='/api/state')return json(res,200,session.snapshot());
  if(req.method==='POST'&&url.pathname==='/api/action'){
   const {action,...args}=await body(req);
   const result=session.execute(action,args);
   return json(res,result.ok?200:409,result);
  }
  if(req.method==='POST'&&url.pathname==='/api/reset'){
   session=new Session(map,deck);
   return json(res,200,session.snapshot());
  }
  if(req.method==='GET'&&Object.hasOwn(allowed,url.pathname)){
   const p=url.pathname==='/assets/world_pic.jpg'?path.join(MAPS,'world_pic.jpg'):path.join(PUBLIC,allowed[url.pathname]);
   const bytes=await readFile(p);
   res.writeHead(200,{'content-type':types[path.extname(p)],'x-content-type-options':'nosniff'});
   return res.end(bytes);
  }
  json(res,404,{error:'Not found'});
 }catch(e){json(res,400,{error:e.message});}
}).listen(PORT,HOST,()=>console.log(`WARG local: http://${HOST}:${PORT}`));
