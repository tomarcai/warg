// warg - local web server. Serves the browser game, the original Domination maps,
// translations and sounds, and stores saved games on disk. GPLv3.

import http from 'node:http';
import fs from 'node:fs/promises';
import path from 'node:path';
import { PATHS, ROOT, safeJoin, decodeMapBuffer } from './nodeIo.js';

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
async function listMaps() {
  const files = (await fs.readdir(PATHS.maps)).filter((f) => f.endsWith('.map')).sort();
  const maps = [];
  for (const file of files) {
    try {
      const text = decodeMapBuffer(await fs.readFile(path.join(PATHS.maps, file)));
      const info = { file, name: null, prv: null, crd: null, pic: null, countries: 0, continents: 0, missions: false };
      let mode = null;
      for (const line of text.split(/\r\n|\r|\n/)) {
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
        try {
          const cards = decodeMapBuffer(await fs.readFile(path.join(PATHS.maps, info.crd)));
          info.missions = /^\[missions\]/m.test(cards);
        } catch (e) { /* no cards */ }
      }
      maps.push(info);
    } catch (e) {
      // skip broken map
    }
  }
  return maps;
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
