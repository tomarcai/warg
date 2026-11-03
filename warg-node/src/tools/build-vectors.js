#!/usr/bin/env node
// warg - builds vector/<map>.json for every original map that has a "_map.gif". GPLv3.
// Usage: node src/tools/build-vectors.js
import fs from 'node:fs/promises';
import path from 'node:path';
import { PATHS, ROOT, decodeMapBuffer } from '../server/nodeIo.js';
import { vectorizeMapFile } from './vectorize.js';

const outDir = path.join(ROOT, 'vector');
await fs.mkdir(outDir, { recursive: true });
for (const file of (await fs.readdir(PATHS.maps)).filter((f) => f.endsWith('.map')).sort()) {
  const text = decodeMapBuffer(await fs.readFile(path.join(PATHS.maps, file)));
  const t0 = Date.now();
  try {
    const v = await vectorizeMapFile(text, (name) => fs.readFile(path.join(PATHS.maps, name)));
    const json = JSON.stringify(v);
    await fs.writeFile(path.join(outDir, file.replace(/\.map$/, '.json')), json);
    const pts = v.countries.reduce((s, c) => s + c.rings.reduce((a, r) => a + r.length / 2, 0), 0);
    const empty = v.countries.filter((c) => c.rings.length === 0).map((c) => c.id);
    console.log(file, 'countries', v.countries.length, 'points', pts, 'links', v.links.length, 'kB', (json.length / 1024).toFixed(0), 'ms', Date.now() - t0, empty.length ? 'EMPTY ' + empty : '');
  } catch (e) {
    console.log(file, 'ERROR', e.message);
  }
}
