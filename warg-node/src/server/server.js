// warg - local web server. Serves the browser game, the original Domination maps,
// translations and sounds, and stores saved games on disk. GPLv3.

import http from 'node:http';
import fs from 'node:fs/promises';
import path from 'node:path';
import { PATHS, ROOT, safeJoin, decodeMapBuffer } from './nodeIo.js';
import { vectorizeMapFile, parseMapFile } from '../tools/vectorize.js';
import { validateMap, buildMapFiles, sanitizeBase } from '../tools/mapWriter.js';

const PORT = parseInt(process.env.PORT || '8080', 10);
const HOST = process.env.HOST || '127.0.0.1';

const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.mjs': 'text/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.gif': 'image/gif',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.svg': 'image/svg+xml',
  '.mp3': 'audio/mpeg',
  '.htm': 'text/html; charset=iso-8859-1',
  '.map': 'text/plain; charset=iso-8859-1',
  '.cards': 'text/plain; charset=iso-8859-1',
  '.properties': 'text/plain; charset=iso-8859-1',
  '.log': 'text/plain; charset=iso-8859-1',
};

const STATIC = [
  ['/engine/', path.join(ROOT, 'src', 'engine')],
  ['/tools/', path.join(ROOT, 'src', 'tools')],
  ['/maps/', PATHS.maps],
  ['/translation/', PATHS.translations],
  ['/sound/', PATHS.sounds],
  ['/help/', PATHS.help],
  ['/ui/', PATHS.uiArt],
  ['/scripts/', PATHS.scripts],
  ['/', path.join(ROOT, 'public')],
];

function send(res, status, body, type = 'text/plain; charset=utf-8') {
  res.writeHead(status, { 'Content-Type': type, 'Cache-Control': 'no-cache' });
  res.end(body);
}

function sendJson(res, status, obj) {
  send(res, status, JSON.stringify(obj), MIME['.json']);
}

async function serveFile(res, file) {
  try {
    const data = await fs.readFile(file);
    send(res, 200, data, MIME[path.extname(file).toLowerCase()] || 'application/octet-stream');
    return true;
  } catch (e) {
    return false;
  }
}

async function readBody(req, limit = 20 * 1024 * 1024) {
  const chunks = [];
  let size = 0;
  for await (const c of req) {
    size += c.length;
    if (size > limit) throw new Error('too big');
    chunks.push(c);
  }
  return Buffer.concat(chunks).toString('utf8');
}

/** reads the header of every .map file: name, preview, cards, number of countries */
async function readMapInfo(dir, file, custom) {
  const text = decodeMapBuffer(await fs.readFile(path.join(dir, file)));
  const info = { file, name: null, prv: null, crd: null, pic: null, countries: 0, continents: 0, missions: false, custom };
  let mode = null;
  for (const raw of text.split(/\r\n|\r|\n/)) {
    const line = raw.trim();
    if (!line || line[0] === ';') continue;
    if (line[0] === '[' && line.endsWith(']')) { mode = line.slice(1, -1); continue; }
    if (mode == null && line.startsWith('name ')) info.name = line.substring(5);
    else if (mode === 'files') {
      if (line.startsWith('prv ')) info.prv = line.substring(4);
      else if (line.startsWith('crd ')) info.crd = line.substring(4);
      else if (line.startsWith('pic ')) info.pic = line.substring(4);
    } else if (mode === 'countries') info.countries++;
    else if (mode === 'continents') info.continents++;
  }
  if (info.crd) {
    const cards = await readMapText(info.crd);
    if (cards != null) info.missions = /^\[missions\]/m.test(cards);
  }
  return info;
}

async function listDir(dir) {
  try { return (await fs.readdir(dir)).filter((f) => f.endsWith('.map')).sort(); } catch (e) { return []; }
}

async function listMaps() {
  const maps = [];
  const customFiles = await listDir(PATHS.customMaps);
  for (const file of customFiles) {
    try { maps.push(await readMapInfo(PATHS.customMaps, file, true)); } catch (e) { /* skip broken map */ }
  }
  for (const file of await listDir(PATHS.maps)) {
    if (customFiles.includes(file)) continue;
    try { maps.push(await readMapInfo(PATHS.maps, file, false)); } catch (e) { /* skip broken map */ }
  }
  return maps;
}

/** a file from the custom maps folder, or the original maps folder */
async function readMapText(name) {
  for (const dir of [PATHS.customMaps, PATHS.maps]) {
    try { return decodeMapBuffer(await fs.readFile(safeJoin(dir, name))); } catch (e) { /* next */ }
  }
  return null;
}

const vectorCache = new Map();

/** polygons of a map: editor maps keep them next to the .map, originals are pre-built in vector/ */
async function getVector(mapFile) {
  const base = mapFile.replace(/\.map$/, '');
  for (const f of [safeJoin(PATHS.customMaps, base + '.json'), safeJoin(PATHS.vector, base + '.json')]) {
    try { return JSON.parse(await fs.readFile(f, 'utf8')); } catch (e) { /* next */ }
  }
  if (vectorCache.has(mapFile)) return vectorCache.get(mapFile);
  // a map dropped into the maps folder without a pre-built vector file: build it now
  const text = await readMapText(mapFile);
  if (text == null) return null;
  const v = await vectorizeMapFile(text, async (name) => {
    for (const dir of [PATHS.customMaps, PATHS.maps]) {
      try { return await fs.readFile(safeJoin(dir, name)); } catch (e) { /* next */ }
    }
    throw new Error('missing ' + name);
  });
  vectorCache.set(mapFile, v);
  return v;
}

function escapeXml(s) {
  return String(s).replace(/[<>&"]/g, (c) => ({ '<': '&lt;', '>': '&gt;', '&': '&amp;', '"': '&quot;' }[c]));
}

/** small picture of a map, coloured by continent */
async function previewSvg(mapFile) {
  const text = await readMapText(mapFile);
  const v = await getVector(mapFile);
  if (text == null || v == null) return null;
  const map = parseMapFile(text);
  const colorOf = (c) => {
    const named = { black: '#000000', blue: '#0000ff', cyan: '#00ffff', darkgray: '#404040', gray: '#808080', green: '#00ff00', lightgray: '#c0c0c0', magenta: '#ff00ff', orange: '#ffc800', pink: '#ffafaf', red: '#ff0000', white: '#ffffff', yellow: '#ffff00' };
    if (named[c]) return named[c];
    if (/^(#|0x)[0-9a-f]{6}$/i.test(c)) return '#' + c.replace(/^(#|0x)/i, '');
    return '#888888';
  };
  let out = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${v.width} ${v.height}"><rect width="100%" height="100%" fill="#1f2b30"/>`;
  for (const c of v.countries) {
    const info = map.countries[c.id - 1];
    const cont = info && map.continents[info.continent - 1];
    const col = escapeXml(colorOf(cont ? cont.color : 'gray'));
    const d = c.rings.map((r) => 'M' + r.join(' ') + 'Z').join('');
    out += `<path d="${d}" fill="${col}" fill-opacity=".7" stroke="${col}" stroke-width="1" fill-rule="evenodd"/>`;
  }
  return out + '</svg>';
}

async function saveCustomMap(data) {
  const { errors, warnings } = validateMap(data);
  if (errors.length) return { status: 400, body: { errors, warnings } };
  let base;
  if (data.file) {
    // overwrite an existing editor map
    base = sanitizeBase(String(data.file).replace(/\.map$/, ''));
  } else {
    base = sanitizeBase(data.name);
    const taken = new Set([...(await listDir(PATHS.customMaps)), ...(await listDir(PATHS.maps))].map((f) => f.replace(/\.map$/, '')));
    let b = base;
    for (let i = 2; taken.has(b); i++) b = base + '_' + i;
    base = b;
  }
  if ((await listDir(PATHS.maps)).includes(base + '.map')) return { status: 400, body: { errors: [{ key: 'editor.err.original' }] } };
  const files = buildMapFiles(data, base);
  await fs.mkdir(PATHS.customMaps, { recursive: true });
  await fs.writeFile(safeJoin(PATHS.customMaps, base + '.map'), files.map, 'utf8');
  await fs.writeFile(safeJoin(PATHS.customMaps, base + '.cards'), files.cards, 'utf8');
  await fs.writeFile(safeJoin(PATHS.customMaps, base + '.json'), JSON.stringify(files.vector));
  vectorCache.delete(base + '.map');
  return { status: 200, body: { ok: true, file: base + '.map', warnings } };
}

async function listSaves() {
  try {
    const files = (await fs.readdir(PATHS.saves)).filter((f) => f.endsWith('.json'));
    const out = [];
    for (const f of files) {
      const st = await fs.stat(path.join(PATHS.saves, f));
      out.push({ name: f.slice(0, -5), modified: st.mtime.toISOString() });
    }
    return out.sort((a, b) => b.modified.localeCompare(a.modified));
  } catch (e) {
    return [];
  }
}

async function handleApi(req, res, url) {
  if (url.pathname === '/api/maps' && req.method === 'GET') return sendJson(res, 200, await listMaps());
  if (url.pathname === '/api/maps' && req.method === 'POST') {
    let data;
    try { data = JSON.parse(await readBody(req)); } catch (e) { return sendJson(res, 400, { errors: [{ key: 'editor.err.data' }] }); }
    const r = await saveCustomMap(data);
    return sendJson(res, r.status, r.body);
  }
  const dm = /^\/api\/maps\/([^/]+\.map)$/.exec(url.pathname);
  if (dm && req.method === 'DELETE') {
    const base = sanitizeBase(decodeURIComponent(dm[1]).replace(/\.map$/, ''));
    for (const ext of ['.map', '.cards', '.json']) await fs.rm(safeJoin(PATHS.customMaps, base + ext), { force: true });
    return sendJson(res, 200, { ok: true });
  }
  const pv = /^\/api\/preview\/([^/]+\.map)\.svg$/.exec(url.pathname);
  if (pv) {
    const svg = await previewSvg(decodeURIComponent(pv[1]));
    return svg ? send(res, 200, svg, 'image/svg+xml') : send(res, 404, 'not found');
  }
  if (url.pathname === '/api/saves' && req.method === 'GET') return sendJson(res, 200, await listSaves());
  const m = /^\/api\/saves\/([^/]+)$/.exec(url.pathname);
  if (m) {
    const name = decodeURIComponent(m[1]).replace(/[\\/:*?"<>|]/g, '_').trim();
    if (!name) return sendJson(res, 400, { error: 'bad name' });
    const file = safeJoin(PATHS.saves, name + '.json');
    if (req.method === 'GET') {
      try { return send(res, 200, await fs.readFile(file), MIME['.json']); } catch (e) { return sendJson(res, 404, { error: 'not found' }); }
    }
    if (req.method === 'PUT' || req.method === 'POST') {
      const body = await readBody(req);
      JSON.parse(body); // validate
      await fs.mkdir(PATHS.saves, { recursive: true });
      await fs.writeFile(file, body);
      return sendJson(res, 200, { ok: true, name });
    }
    if (req.method === 'DELETE') {
      await fs.rm(file, { force: true });
      return sendJson(res, 200, { ok: true });
    }
  }
  return sendJson(res, 404, { error: 'unknown api' });
}

async function handle(req, res) {
  const url = new URL(req.url, 'http://localhost');
  if (url.pathname.startsWith('/api/')) return handleApi(req, res, url);
  if (req.method !== 'GET' && req.method !== 'HEAD') return send(res, 405, 'method not allowed');
  let pathname = decodeURIComponent(url.pathname);
  if (pathname === '/') pathname = '/index.html';
  const vm = /^\/vector\/([^/]+\.map)\.json$/.exec(pathname);
  if (vm) {
    try {
      const v = await getVector(vm[1]);
      return v ? sendJson(res, 200, v) : send(res, 404, 'not found');
    } catch (e) {
      return send(res, 500, 'can not build the vector map: ' + e.message);
    }
  }
  // map files: maps made with the editor first
  if (pathname.startsWith('/maps/') && !pathname.startsWith('/maps/preview/')) {
    try {
      if (await serveFile(res, safeJoin(PATHS.customMaps, pathname.substring(6)))) return undefined;
    } catch (e) { return send(res, 400, 'bad path'); }
  }
  for (const [prefix, dir] of STATIC) {
    if (!pathname.startsWith(prefix)) continue;
    const rel = pathname.substring(prefix.length);
    let file;
    try { file = safeJoin(dir, rel); } catch (e) { return send(res, 400, 'bad path'); }
    // our own translations override / extend the originals
    if (prefix === '/translation/' && await serveFile(res, safeJoin(PATHS.extraTranslations, rel))) return undefined;
    if (await serveFile(res, file)) return undefined;
    if (prefix !== '/') return send(res, 404, 'not found');
  }
  return send(res, 404, 'not found');
}

export function startServer(port = PORT, host = HOST) {
  const server = http.createServer((req, res) => {
    handle(req, res).catch((e) => {
      console.error(e);
      if (!res.headersSent) send(res, 500, 'error: ' + e.message);
    });
  });
  return new Promise((resolve) => {
    server.listen(port, host, () => {
      console.log(`warg listo: abre http://${host === '0.0.0.0' ? 'localhost' : host}:${server.address().port}/ en tu navegador`);
      resolve(server);
    });
  });
}

if (import.meta.url === `file://${process.argv[1]}` || process.argv[1]?.endsWith('server.js')) {
  startServer();
}
