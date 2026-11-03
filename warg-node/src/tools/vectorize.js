// warg - turns a Domination "_map.gif" (one palette index per country) into polygons. GPLv3.
//
// 1. every country pixel region is traced along the pixel edges (exact outline)
// 2. outlines are cut at junctions (points where 3 regions meet), so each piece of border
//    between two countries is simplified only once and both countries reuse it: no gaps
//    or overlaps between neighbours
// 3. tiny specks and small holes (letters drawn into the map image) are dropped
// 4. borders listed in the .map file that do not touch get a "link" line (sea routes)

import { decodeGif } from './gif.js';

const SEA = 255;

/** parse the parts of a .map file we need (countries and borders) */
export function parseMapFile(text) {
  const out = { countries: [], continents: [], borders: [], files: {}, props: {} };
  let mode = null;
  for (const raw of text.split(/\r\n|\r|\n/)) {
    const line = raw.trim();
    if (!line || line[0] === ';') continue;
    if (line[0] === '[' && line.endsWith(']')) { mode = line.slice(1, -1); continue; }
    const t = line.split(/\s+/);
    if (mode === 'files') out.files[t[0]] = line.substring(t[0].length + 1);
    else if (mode === 'continents') out.continents.push({ id: t[0], bonus: parseInt(t[1], 10), color: t[2] });
    else if (mode === 'countries') out.countries.push({ n: parseInt(t[0], 10), id: t[1], continent: parseInt(t[2], 10), x: parseInt(t[3], 10), y: parseInt(t[4], 10) });
    else if (mode === 'borders') out.borders.push(t.map((v) => parseInt(v, 10)));
    else if (mode == null) { const i = line.indexOf(' '); if (i > 0) out.props[line.substring(0, i)] = line.substring(i + 1); }
  }
  return out;
}

/** label image: country id (1..noc) or SEA */
export function labelsFromGif(buf, noc) {
  const g = decodeGif(buf);
  const n = g.width * g.height;
  const labels = new Uint8Array(n);
  for (let i = 0; i < n; i++) {
    // the original game uses the blue channel of the palette colour (getRGB() & 0xff)
    const v = g.palette ? g.palette[g.indices[i] * 3 + 2] : g.indices[i];
    labels[i] = v >= 1 && v <= noc ? v : SEA;
  }
  return { width: g.width, height: g.height, labels };
}

function dpSimplify(pts, eps) {
  // pts: flat [x0,y0,x1,y1,...]; keeps first and last
  const n = pts.length / 2;
  if (n <= 2) return pts.slice();
  const keep = new Uint8Array(n);
  keep[0] = keep[n - 1] = 1;
  const stack = [[0, n - 1]];
  const eps2 = eps * eps;
  while (stack.length) {
    const [a, b] = stack.pop();
    const ax = pts[a * 2], ay = pts[a * 2 + 1], bx = pts[b * 2], by = pts[b * 2 + 1];
    const dx = bx - ax, dy = by - ay;
    const len2 = dx * dx + dy * dy;
    let best = -1, bestD = eps2;
    for (let i = a + 1; i < b; i++) {
      const px = pts[i * 2] - ax, py = pts[i * 2 + 1] - ay;
      let d2;
      if (len2 === 0) d2 = px * px + py * py;
      else { const c = px * dy - py * dx; d2 = (c * c) / len2; }
      if (d2 > bestD) { bestD = d2; best = i; }
    }
    if (best >= 0) { keep[best] = 1; stack.push([a, best], [best, b]); }
  }
  const out = [];
  for (let i = 0; i < n; i++) if (keep[i]) out.push(pts[i * 2], pts[i * 2 + 1]);
  return out;
}

function ringArea(r) {
  let a = 0;
  const n = r.length / 2;
  for (let i = 0, j = n - 1; i < n; j = i++) a += r[j * 2] * r[i * 2 + 1] - r[i * 2] * r[j * 2 + 1];
  return a / 2;
}

/**
 * @returns {{width, height, countries: Array<{id, rings: number[][]}>, links: Array}}
 */
export function vectorize(labelImg, map, options = {}) {
  const eps = options.epsilon ?? 1.0;
  const { width: W, height: H, labels } = labelImg;
  const noc = map.countries.length;
  const lab = (x, y) => (x < 0 || y < 0 || x >= W || y >= H ? SEA : labels[y * W + x]);
  const VW = W + 1;
  const vkey = (x, y) => y * VW + x;

  // junction vertices: 3+ different labels around, or a diagonal (saddle) pattern
  const isJunction = (x, y) => {
    const a = lab(x - 1, y - 1), b = lab(x, y - 1), c = lab(x - 1, y), d = lab(x, y);
    const s = new Set([a, b, c, d]);
    if (s.size >= 3) return true;
    if (s.size === 2 && a === d && b === c) return true;
    return false;
  };

  // directed boundary edges with the country on the right (screen coordinates, y down)
  const edges = Array.from({ length: noc + 1 }, () => new Map());
  const add = (c, x1, y1, x2, y2, other) => {
    const k = vkey(x1, y1);
    let list = edges[c].get(k);
    if (!list) { list = []; edges[c].set(k, list); }
    list.push({ x1, y1, x2, y2, other, used: false });
  };
  for (let y = 0; y < H; y++) {
    for (let x = 0; x < W; x++) {
      const c = labels[y * W + x];
      if (c === SEA) continue;
      let o;
      if ((o = lab(x, y - 1)) !== c) add(c, x, y, x + 1, y, o); // top
      if ((o = lab(x + 1, y)) !== c) add(c, x + 1, y, x + 1, y + 1, o); // right
      if ((o = lab(x, y + 1)) !== c) add(c, x + 1, y + 1, x, y + 1, o); // bottom
      if ((o = lab(x - 1, y)) !== c) add(c, x, y + 1, x, y, o); // left
    }
  }

  // trace loops per country
  const loops = Array.from({ length: noc + 1 }, () => []);
  for (let c = 1; c <= noc; c++) {
    for (const list of edges[c].values()) {
      for (const e0 of list) {
        if (e0.used) continue;
        const pts = [];
        const others = [];
        let e = e0;
        while (e && !e.used) {
          e.used = true;
          pts.push(e.x1, e.y1);
          others.push(e.other);
          const outs = edges[c].get(vkey(e.x2, e.y2)) || [];
          const cand = outs.filter((n) => !n.used);
          if (cand.length <= 1) e = cand[0];
          else {
            // saddle: take the right turn so the loop hugs this country's pixels
            const dx = e.x2 - e.x1, dy = e.y2 - e.y1;
            const rx = -dy, ry = dx; // right turn in y-down coordinates
            e = cand.find((n) => n.x2 - n.x1 === rx && n.y2 - n.y1 === ry) || cand[0];
          }
        }
        loops[c].push({ pts, others });
      }
    }
  }

  // cut loops into chains at junctions; simplify each shared chain only once
  const cache = new Map();
  const chainKey = (lo, hi, pts) => {
    const n = pts.length / 2;
    return `${lo}|${hi}|${pts[0]},${pts[1]}|${pts[2]},${pts[3]}|${pts[(n - 1) * 2]},${pts[(n - 1) * 2 + 1]}`;
  };
  const reverse = (pts) => {
    const r = [];
    for (let i = pts.length - 2; i >= 0; i -= 2) r.push(pts[i], pts[i + 1]);
    return r;
  };
  const simplifyClosed = (pts) => {
    // split at the point farthest from the start, simplify both halves
    const n = pts.length / 2;
    let far = 0, best = -1;
    for (let i = 1; i < n; i++) {
      const d = (pts[i * 2] - pts[0]) ** 2 + (pts[i * 2 + 1] - pts[1]) ** 2;
      if (d > best) { best = d; far = i; }
    }
    const a = dpSimplify(pts.slice(0, far * 2 + 2), eps);
    const b = dpSimplify([...pts.slice(far * 2), pts[0], pts[1]], eps);
    return [...a, ...b.slice(2, -2)];
  };
  const getChain = (c, other, pts, closed) => {
    // canonical orientation: the one traced by the lower country id (sea chains: as traced)
    const flip = other !== SEA && other < c;
    let canon = flip ? reverse(pts) : pts;
    if (closed) {
      // canonical start: smallest vertex
      const n = canon.length / 2;
      let m = 0;
      for (let i = 1; i < n; i++) {
        const k = canon[i * 2 + 1] * VW + canon[i * 2], km = canon[m * 2 + 1] * VW + canon[m * 2];
        if (k < km) m = i;
      }
      canon = [...canon.slice(m * 2), ...canon.slice(0, m * 2)];
    }
    const lo = Math.min(c, other), hi = Math.max(c, other);
    const key = (closed ? 'C' : 'O') + chainKey(lo, hi, canon);
    let simp = cache.get(key);
    if (!simp) {
      simp = closed ? simplifyClosed(canon) : dpSimplify(canon, eps);
      cache.set(key, simp);
    }
    return flip ? reverse(simp) : simp;
  };

  const countries = [];
  for (let c = 1; c <= noc; c++) {
    const rings = [];
    for (const loop of loops[c]) {
      const { pts, others } = loop;
      const n = pts.length / 2;
      if (n < 4) continue;
      const junctions = [];
      for (let i = 0; i < n; i++) {
        // the right label changes only at junctions, but also cut where it changes (safety)
        if (isJunction(pts[i * 2], pts[i * 2 + 1]) || others[i] !== others[(i + n - 1) % n]) junctions.push(i);
      }
      let ring = [];
      const borders = new Set(others);
      if (junctions.length === 0) {
        ring = getChain(c, others[0], pts, true);
      } else {
        for (let k = 0; k < junctions.length; k++) {
          const s = junctions[k];
          const e = junctions[(k + 1) % junctions.length];
          const seg = [];
          const steps = (e - s + n) % n || n;
          for (let t = 0; t <= steps; t++) {
            const i = (s + t) % n;
            seg.push(pts[i * 2], pts[i * 2 + 1]);
          }
          const simp = getChain(c, others[s], seg, false);
          ring.push(...simp.slice(0, -2)); // last point is the next chain's first
        }
      }
      if (ring.length < 6) continue;
      const area = ringArea(ring);
      const onlySea = borders.size === 1 && borders.has(SEA);
      // y-down + country on the right: outer rings have positive area
      if (Math.abs(area) < 3) continue;
      if (area < 0 && onlySea && -area < (options.minHole ?? 60)) continue; // letters / specks of sea inside
      rings.push(ring);
    }
    countries.push({ id: c, rings });
  }

  // borders that do not touch: draw a link (sea route), checking the wrap around the map edge
  const touching = new Set();
  const R = options.touchRadius ?? 4;
  for (let y = 0; y < H; y++) {
    for (let x = 0; x < W; x++) {
      const a = labels[y * W + x];
      if (a === SEA) continue;
      for (let dy = 0; dy <= R; dy++) {
        for (let dx = -R; dx <= R; dx++) {
          if (dy === 0 && dx <= 0) continue;
          if (dx * dx + dy * dy > R * R) continue;
          const b = lab(x + dx, y + dy);
          if (b !== SEA && b !== a) touching.add(a < b ? a * 1000 + b : b * 1000 + a);
        }
      }
    }
  }
  const links = [];
  const seen = new Set();
  for (const line of map.borders) {
    const a = line[0];
    for (const b of line.slice(1)) {
      const k = a < b ? a * 1000 + b : b * 1000 + a;
      if (seen.has(k) || a === b) continue;
      seen.add(k);
      if (touching.has(k)) continue;
      const ra = countries[a - 1] && countries[a - 1].rings;
      const rb = countries[b - 1] && countries[b - 1].rings;
      if (!ra || !rb || !ra.length || !rb.length) continue;
      let best = null;
      for (const shift of [0, W, -W]) {
        for (const r1 of ra) for (let i = 0; i < r1.length; i += 2) {
          for (const r2 of rb) for (let j = 0; j < r2.length; j += 2) {
            const d = (r1[i] - (r2[j] + shift)) ** 2 + (r1[i + 1] - r2[j + 1]) ** 2;
            if (!best || d < best.d) best = { d, x1: r1[i], y1: r1[i + 1], x2: r2[j] + shift, y2: r2[j + 1], shift };
          }
        }
      }
      links.push([Math.min(a, b), Math.max(a, b), ...(a < b ? [best.x1, best.y1, best.x2, best.y2] : [best.x2 - best.shift, best.y2, best.x1 - best.shift, best.y1]), best.shift !== 0 ? 1 : 0]);
    }
  }
  return { width: W, height: H, countries, links };
}

/** vectorize a map given the text of the .map file and a function returning file buffers */
export async function vectorizeMapFile(mapText, readBuffer, options) {
  const map = parseMapFile(mapText);
  const mapImage = map.files.map;
  if (!mapImage) throw new Error('the map has no "map" image');
  const buf = await readBuffer(mapImage);
  const img = labelsFromGif(buf, map.countries.length);
  return vectorize(img, map, options);
}
