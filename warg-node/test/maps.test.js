// warg tests for the vector maps and the map editor files.

import { test } from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { PATHS, decodeMapBuffer, createNodeIo } from '../src/server/nodeIo.js';
import { parseMapFile, labelsFromGif, vectorize } from '../src/tools/vectorize.js';
import { buildMapFiles, validateMap } from '../src/tools/mapWriter.js';
import { Risk } from '../src/engine/Risk.js';
import * as RG from '../src/engine/RiskGame.js';

const mapFiles = fs.readdirSync(PATHS.maps).filter((f) => f.endsWith('.map')).sort();

function pointInRings(rings, x, y) {
  let inside = false;
  for (const r of rings) {
    const n = r.length / 2;
    for (let i = 0, j = n - 1; i < n; j = i++) {
      const xi = r[i * 2], yi = r[i * 2 + 1], xj = r[j * 2], yj = r[j * 2 + 1];
      if ((yi > y) !== (yj > y) && x < ((xj - xi) * (y - yi)) / (yj - yi) + xi) inside = !inside;
    }
  }
  return inside;
}

for (const file of mapFiles) {
  test('vector map is faithful: ' + file, () => {
    const text = decodeMapBuffer(fs.readFileSync(path.join(PATHS.maps, file)));
    const map = parseMapFile(text);
    const img = labelsFromGif(fs.readFileSync(path.join(PATHS.maps, map.files.map)), map.countries.length);
    const v = vectorize(img, map);
    // the committed file is up to date
    const committed = JSON.parse(fs.readFileSync(path.join(PATHS.vector, file.replace(/\.map$/, '.json')), 'utf8'));
    assert.deepEqual(committed, JSON.parse(JSON.stringify(v)), 'run: node src/tools/build-vectors.js');
    assert.equal(v.countries.length, map.countries.length);
    for (const c of v.countries) assert.ok(c.rings.length > 0, 'country ' + c.id + ' has a shape');
    // sample pixels: the polygon under a pixel centre is the country of that pixel
    let ok = 0, total = 0;
    const W = img.width, H = img.height;
    for (let y = 2; y < H; y += 7) {
      for (let x = 2; x < W; x += 7) {
        const id = img.labels[y * W + x];
        if (id === 255) continue;
        total++;
        if (pointInRings(v.countries[id - 1].rings, x + 0.5, y + 0.5)) ok++;
      }
    }
    assert.ok(ok / total > 0.97, `${file}: ${(100 * ok / total).toFixed(1)}% of sampled pixels inside their polygon`);
  });
}

function square(x, y, s) {
  return [x, y, x + s, y, x + s, y + s, x, y + s];
}

function sampleMap() {
  const terrs = [];
  for (let i = 0; i < 8; i++) {
    const x = 50 + (i % 4) * 100, y = 50 + Math.floor(i / 4) * 100;
    terrs.push({ name: 'Zona ' + (i + 1) + (i === 0 ? ' Ñandú' : ''), continent: i < 4 ? 0 : 1, rings: [square(x, y, 100)], label: [x + 50, y + 50], neighbors: [] });
  }
  const link = (a, b) => { terrs[a].neighbors.push(b); terrs[b].neighbors.push(a); };
  link(0, 1); link(1, 2); link(2, 3); link(4, 5); link(5, 6); link(6, 7); link(0, 4); link(3, 7);
  return {
    name: 'Prueba Ñ', width: 500, height: 300,
    continents: [{ name: 'Norte', bonus: 3, color: '#c8962e' }, { name: 'Sur', bonus: 2, color: '#3f8fd2' }],
    territories: terrs,
  };
}

test('editor validation catches problems', () => {
  const data = sampleMap();
  assert.deepEqual(validateMap(data).errors, []);
  const bad = sampleMap();
  bad.territories[2].neighbors = [];
  bad.territories[1].neighbors = [0];
  bad.territories[3].neighbors = [7];
  const keys = validateMap(bad).errors.map((e) => e.key);
  assert.ok(keys.includes('editor.err.noneighbors'));
  const few = sampleMap();
  few.territories = few.territories.slice(0, 4);
  assert.ok(validateMap(few).errors.some((e) => e.key === 'editor.err.fewterritories'));
});

test('a map made in the editor can be played to the end', async () => {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'warg-maps-'));
  const files = buildMapFiles(sampleMap(), 'prueba');
  fs.writeFileSync(path.join(dir, 'prueba.map'), files.map);
  fs.writeFileSync(path.join(dir, 'prueba.cards'), files.cards);
  const io = createNodeIo({ ...PATHS, customMaps: dir });
  const risk = new Risk(io, { aiWait: 0, showDice: false, language: 'es' });
  await risk.ready();
  await risk.parser('newgame');
  await risk.parser('choosemap prueba.map');
  const g0 = risk.getGame();
  assert.equal(g0.getMapFile(), 'prueba.map');
  for (const p of ['ai easy 6 6', 'ai hard 1 1', 'ai average 2 2']) await risk.parser('newplayer ' + p);
  risk.parser('startgame mission increasing recycle');
  const start = Date.now();
  for (;;) {
    await new Promise((r) => setTimeout(r, 10));
    const g = risk.getGame();
    if (g && g.getState() === RG.STATE_GAME_OVER && !risk.processing && risk.inbox.length === 0) break;
    if (Date.now() - start > 60000) throw new Error('game did not finish');
  }
  const g = risk.getGame();
  assert.equal(g.getNoCountries(), 8);
  assert.equal(g.getCountryInt(1).getName(), 'Zona 1 Ñandú'); // UTF-8 names survive
  assert.ok(g.getMissions().length >= 3);
  fs.rmSync(dir, { recursive: true, force: true });
});
