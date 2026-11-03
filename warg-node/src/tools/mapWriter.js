// warg - writes a map made in the editor as Domination files (.map + .cards) plus the
// polygons (.json, same format as the generated vector files). GPLv3.

const MAX_NAME = 40;

export function sanitizeBase(name) {
  const base = String(name || '')
    .normalize('NFD').replace(/[̀-ͯ]/g, '')
    .toLowerCase().replace(/[^a-z0-9]+/g, '_').replace(/^_+|_+$/g, '')
    .substring(0, MAX_NAME);
  return base || 'mapa';
}

/** ids in .map files can not contain white space */
export function toId(name) {
  return String(name).trim().replace(/\s+/g, '_');
}

function hexColor(c) {
  const m = /^#?([0-9a-f]{6})$/i.exec(String(c || '').trim());
  return m ? '#' + m[1].toLowerCase() : null;
}

/**
 * checks an editor map, returns { errors: string[], warnings: string[] } with message keys
 * data: { name, width, height, continents:[{name, bonus, color}], territories:[{name, continent, rings, label, neighbors}] }
 */
export function validateMap(data) {
  const errors = [];
  const warnings = [];
  const err = (key, ...args) => errors.push({ key, args });
  const warn = (key, ...args) => warnings.push({ key, args });
  if (!data || typeof data !== 'object') { err('editor.err.data'); return { errors, warnings }; }
  if (!String(data.name || '').trim()) err('editor.err.mapname');
  const w = data.width, h = data.height;
  if (!(w >= 100 && w <= 4000 && h >= 100 && h <= 4000)) err('editor.err.size');
  const conts = Array.isArray(data.continents) ? data.continents : [];
  const terrs = Array.isArray(data.territories) ? data.territories : [];
  if (conts.length === 0) err('editor.err.nocontinents');
  // fewer than 6 gives 0 starting armies with the original rule (10 - players) * round(n * 0.12)
  if (terrs.length < 6) err('editor.err.fewterritories');
  if (terrs.length > 254) err('editor.err.toomany');
  const cnames = new Set();
  conts.forEach((c) => {
    const n = String(c.name || '').trim();
    if (!n) err('editor.err.continentname');
    else if (cnames.has(n.toLowerCase())) err('editor.err.continentdup', n);
    cnames.add(n.toLowerCase());
    if (!(Number.isInteger(c.bonus) && c.bonus >= 0 && c.bonus <= 99)) err('editor.err.bonus', n);
    if (!hexColor(c.color)) err('editor.err.color', n);
  });
  const used = new Array(conts.length).fill(0);
  const tnames = new Set();
  terrs.forEach((t, i) => {
    const n = String(t.name || '').trim();
    if (!n) err('editor.err.territoryname', i + 1);
    else if (tnames.has(n.toLowerCase())) err('editor.err.territorydup', n);
    tnames.add(n.toLowerCase());
    if (!(Number.isInteger(t.continent) && t.continent >= 0 && t.continent < conts.length)) err('editor.err.nocontinent', n);
    else used[t.continent]++;
    const rings = Array.isArray(t.rings) ? t.rings : [];
    if (rings.length === 0 || rings.some((r) => !Array.isArray(r) || r.length < 6 || r.some((v) => typeof v !== 'number' || !Number.isFinite(v)))) err('editor.err.shape', n);
    if (!Array.isArray(t.label) || t.label.length !== 2) err('editor.err.label', n);
    const nb = Array.isArray(t.neighbors) ? t.neighbors : [];
    if (nb.length === 0) err('editor.err.noneighbors', n);
    for (const j of nb) {
      if (!Number.isInteger(j) || j < 0 || j >= terrs.length || j === i) err('editor.err.badneighbor', n);
      else if (!(terrs[j].neighbors || []).includes(i)) err('editor.err.oneway', n, terrs[j].name);
    }
  });
  used.forEach((u, i) => { if (u === 0 && conts[i]) err('editor.err.emptycontinent', conts[i].name); });
  // every territory reachable from the first one
  if (terrs.length > 1 && errors.length === 0) {
    const seen = new Set([0]);
    const stack = [0];
    while (stack.length) {
      const i = stack.pop();
      for (const j of terrs[i].neighbors) if (!seen.has(j)) { seen.add(j); stack.push(j); }
    }
    if (seen.size !== terrs.length) warn('editor.warn.disconnected');
  }
  return { errors, warnings };
}

/** returns { map, cards, vector } file contents */
export function buildMapFiles(data, base) {
  const conts = data.continents;
  const terrs = data.territories;
  const W = Math.round(data.width), H = Math.round(data.height);
  const lines = [];
  lines.push('; ' + data.name);
  lines.push('; map made with the warg map editor');
  lines.push('');
  lines.push('name ' + String(data.name).trim());
  lines.push('ver 1');
  lines.push('');
  lines.push('[files]');
  lines.push('crd ' + base + '.cards');
  lines.push('');
  lines.push('[continents]');
  for (const c of conts) lines.push(`${toId(c.name)} ${c.bonus} ${hexColor(c.color)}`);
  lines.push('');
  lines.push('[countries]');
  terrs.forEach((t, i) => {
    const x = Math.max(0, Math.min(W, Math.round(t.label[0])));
    const y = Math.max(0, Math.min(H, Math.round(t.label[1])));
    lines.push(`${i + 1} ${toId(t.name)} ${t.continent + 1} ${x} ${y}`);
  });
  lines.push('');
  lines.push('[borders]');
  terrs.forEach((t, i) => {
    const nb = [...new Set(t.neighbors)].sort((a, b) => a - b).map((j) => j + 1);
    lines.push([i + 1, ...nb].join(' '));
  });
  lines.push('');
  // UTF-8 with BOM so accents work (RiskUtil.readMap reads UTF-8 when there is a BOM)
  const map = '﻿' + lines.join('\r\n');

  // cards: one per territory (infantry, cavalry, cannon in turn) and 2 wildcards, like risk.cards
  const kinds = ['Infantry', 'Cavalry', 'Cannon'];
  const c = ['; cards for ' + data.name, '', '[cards]'];
  terrs.forEach((t, i) => c.push(`${kinds[i % 3]} ${i + 1}`));
  c.push('wildcard', 'wildcard', '', '[missions]');
  const n = terrs.length;
  const big = Math.max(2, Math.round(n * 24 / 42));
  const mid = Math.max(2, Math.round(n * 18 / 42));
  c.push(`0\t${big} 1\t0 0 0\tOccupy ${big} territories of your choice and occupy each with at least 1 army.`);
  c.push(`0\t${mid} 2\t0 0 0\tOccupy ${mid} territories of your choice and occupy each with at least 2 armies.`);
  for (let p = 1; p <= 6; p++) {
    c.push(`${p}\t${big} 1\t0 0 0\tDestroy all of PLAYER${p}'s TROOPS. If they are yours or they have already been destroyed by another player then your mission is: Occupy ${big} territories.`);
  }
  c.push('');
  const cards = '﻿' + c.join('\r\n');

  // polygons (same format as vector/*.json)
  const round = (v) => Math.round(v * 10) / 10;
  const links = [];
  const vector = {
    width: W,
    height: H,
    editor: true,
    countries: terrs.map((t, i) => ({ id: i + 1, rings: t.rings.map((r) => r.map(round)) })),
    links,
  };
  // links: neighbours whose shapes do not touch (drawn as dashed lines in the game)
  terrs.forEach((t, i) => {
    for (const j of t.neighbors) {
      if (j <= i) continue;
      const d = shapeDistance(t.rings, terrs[j].rings);
      if (d.dist > 3) links.push([i + 1, j + 1, round(d.x1), round(d.y1), round(d.x2), round(d.y2), 0]);
    }
  });
  return { map, cards, vector };
}

function segDist(px, py, ax, ay, bx, by) {
  const dx = bx - ax, dy = by - ay;
  const l = dx * dx + dy * dy;
  let t = l ? ((px - ax) * dx + (py - ay) * dy) / l : 0;
  t = Math.max(0, Math.min(1, t));
  const x = ax + t * dx, y = ay + t * dy;
  return { d: Math.hypot(px - x, py - y), x, y };
}

/** smallest distance between two shapes (vertex to segment), with the closest points */
export function shapeDistance(ringsA, ringsB) {
  let best = { dist: Infinity, x1: 0, y1: 0, x2: 0, y2: 0 };
  const test = (ra, rb, swap) => {
    for (const r1 of ra) for (let i = 0; i < r1.length; i += 2) {
      const px = r1[i], py = r1[i + 1];
      for (const r2 of rb) {
        const n = r2.length / 2;
        for (let k = 0; k < n; k++) {
          const j = (k + 1) % n;
          const s = segDist(px, py, r2[k * 2], r2[k * 2 + 1], r2[j * 2], r2[j * 2 + 1]);
          if (s.d < best.dist) best = swap ? { dist: s.d, x1: s.x, y1: s.y, x2: px, y2: py } : { dist: s.d, x1: px, y1: py, x2: s.x, y2: s.y };
        }
      }
    }
  };
  test(ringsA, ringsB, false);
  test(ringsB, ringsA, true);
  return best;
}
