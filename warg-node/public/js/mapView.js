// warg - vector map (SVG). Each territory is a polygon filled with the colour of the player that
// owns it, with a border of the same colour. While a territory is being attacked it is split in
// half: the half that faces the attacking territory takes the attacker's colour. GPLv3.

import * as RG from '/engine/RiskGame.js';
import { RiskGame } from '/engine/RiskGame.js';
import { getTextColorFor, getRed, getGreen, getBlue } from '/engine/colors.js';

export const NO_COUNTRY = 255;
export const VIEW_CONTINENTS = 0;
export const VIEW_OWNERSHIP = 1;
export const VIEW_BORDER_THREAT = 2;
export const VIEW_CARD_OWNERSHIP = 3;
export const VIEW_TROOP_STRENGTH = 4;
export const VIEW_CONNECTED_EMPIRE = 5;

const SVGNS = 'http://www.w3.org/2000/svg';
const FILL_OPACITY = 0.62;
const UNOWNED = 'rgb(118,118,110)';

export function svg(tag, attrs = {}, parent = null) {
  const e = document.createElementNS(SVGNS, tag);
  for (const [k, v] of Object.entries(attrs)) if (v != null) e.setAttribute(k, v);
  if (parent) parent.appendChild(e);
  return e;
}

const css = (c) => `rgb(${getRed(c)},${getGreen(c)},${getBlue(c)})`;
const rgbCss = (a) => `rgb(${a[0] | 0},${a[1] | 0},${a[2] | 0})`;

export function ringsToPath(rings) {
  let d = '';
  for (const r of rings) {
    if (r.length < 6) continue;
    d += 'M' + r[0] + ' ' + r[1];
    for (let i = 2; i < r.length; i += 2) d += 'L' + r[i] + ' ' + r[i + 1];
    d += 'Z';
  }
  return d;
}

function ringSignedArea(r) {
  let a = 0;
  const n = r.length / 2;
  for (let i = 0, j = n - 1; i < n; j = i++) a += r[j * 2] * r[i * 2 + 1] - r[i * 2] * r[j * 2 + 1];
  return a / 2;
}

/** keeps the part of a ring where (p · u) <= t (Sutherland-Hodgman with one plane) */
function clipRing(r, ux, uy, t) {
  const out = [];
  const n = r.length / 2;
  for (let i = 0; i < n; i++) {
    const j = (i + 1) % n;
    const ax = r[i * 2], ay = r[i * 2 + 1], bx = r[j * 2], by = r[j * 2 + 1];
    const da = ax * ux + ay * uy - t, db = bx * ux + by * uy - t;
    if (da <= 0) out.push(ax, ay);
    if ((da < 0 && db > 0) || (da > 0 && db < 0)) {
      const k = da / (da - db);
      out.push(ax + (bx - ax) * k, ay + (by - ay) * k);
    }
  }
  return out;
}

function areaBelow(rings, ux, uy, t) {
  let a = 0;
  for (const r of rings) {
    const c = clipRing(r, ux, uy, t);
    if (c.length >= 6) a += ringSignedArea(c);
  }
  return a;
}

/** centroid of a set of rings (holes subtract) */
export function centroid(rings) {
  let A = 0, cx = 0, cy = 0;
  for (const r of rings) {
    const n = r.length / 2;
    for (let i = 0, j = n - 1; i < n; j = i++) {
      const f = r[j * 2] * r[i * 2 + 1] - r[i * 2] * r[j * 2 + 1];
      A += f;
      cx += (r[j * 2] + r[i * 2]) * f;
      cy += (r[j * 2 + 1] + r[i * 2 + 1]) * f;
    }
  }
  if (A === 0) return [r0(rings, 0), r0(rings, 1)];
  return [cx / (3 * A), cy / (3 * A)];
}
function r0(rings, k) { return rings.length && rings[0].length ? rings[0][k] : 0; }

export class MapView {
  constructor(svgEl, risk) {
    this.svg = svgEl;
    this.canvas = svgEl; // kept for the code that used the old canvas
    this.risk = risk;
    this.view = VIEW_OWNERSHIP;
    this.c1 = NO_COUNTRY;
    this.c2 = NO_COUNTRY;
    this.cc = NO_COUNTRY;
    this.showNames = false;
    this.balls = null;
    this.loadedFor = null;
    this.vector = null;
  }

  async load(game) {
    const key = game.getMapFile();
    if (this.loadedFor === key && this.vector) { this.build(game); return; }
    const r = await fetch('/vector/' + encodeURIComponent(key) + '.json');
    if (!r.ok) throw new Error('no vector map for ' + key + ' (' + r.status + ')');
    this.vector = await r.json();
    this.loadedFor = key;
    this.stopAni();
    this.build(game);
  }

  /** creates the svg elements for every territory */
  build(game) {
    const v = this.vector;
    this.w = v.width;
    this.h = v.height;
    const s = this.svg;
    s.replaceChildren();
    s.setAttribute('viewBox', `0 0 ${v.width} ${v.height}`);
    s.setAttribute('preserveAspectRatio', 'xMidYMid meet');
    const defs = svg('defs', {}, s);
    this.clip = svg('clipPath', { id: 'warg-dispute-clip' }, defs);
    this.clipPoly = svg('polygon', { points: '' }, this.clip);
    svg('rect', { x: 0, y: 0, width: v.width, height: v.height, class: 'sea' }, s);
    const linkLayer = svg('g', { class: 'links' }, s);
    for (const l of v.links || []) {
      const [, , x1, y1, x2, y2, wrapped] = l;
      svg('line', { x1, y1, x2, y2 }, linkLayer);
      if (wrapped) {
        // the route goes off one side of the map and comes back on the other
        const shift = x2 > v.width ? v.width : x2 < 0 ? -v.width : (x1 < x2 ? v.width : -v.width);
        svg('line', { x1: x1 - shift, y1, x2: x2 - shift, y2 }, linkLayer);
      }
    }
    const landLayer = svg('g', { class: 'land' }, s);
    this.paths = [];
    this.rings = [];
    this.centers = [];
    for (const c of v.countries) {
      const d = ringsToPath(c.rings);
      const p = svg('path', { d, 'data-id': c.id, 'fill-rule': 'evenodd', class: 'terr' }, landLayer);
      this.paths[c.id] = p;
      this.rings[c.id] = c.rings;
      this.centers[c.id] = centroid(c.rings);
    }
    this.disputeLayer = svg('g', { class: 'dispute', 'clip-path': 'url(#warg-dispute-clip)' }, s);
    // opaque base first so the attacker's colour does not mix with the defender's
    this.disputeBase = svg('path', { 'fill-rule': 'evenodd', 'pointer-events': 'none', class: 'base' }, this.disputeLayer);
    this.disputePath = svg('path', { 'fill-rule': 'evenodd', 'pointer-events': 'none' }, this.disputeLayer);
    this.cutLine = svg('line', { class: 'cut', 'pointer-events': 'none' }, s);
    this.hlLayer = svg('g', { class: 'hl', 'pointer-events': 'none' }, s);
    this.labelLayer = svg('g', { class: 'labels' }, s);
    this.labels = [];
    const ball = game.getCircleSize();
    this.ball = ball;
    for (const country of game.getCountries()) {
      const id = country.getColor();
      const g = svg('g', { 'data-id': id, class: 'army', transform: `translate(${country.getX()} ${country.getY()})` }, this.labelLayer);
      const ring = svg('circle', { r: ball / 2 + ball / 10, class: 'capital-ring', fill: 'none' }, g);
      const circle = svg('circle', { r: ball / 2 }, g);
      const text = svg('text', { 'text-anchor': 'middle', 'dominant-baseline': 'central', 'font-size': Math.floor((ball + 2) / 2) }, g);
      const name = svg('text', { class: 'cname', 'text-anchor': 'middle', y: ball / 2 + 8, 'font-size': 8 }, g);
      name.textContent = country.getName();
      this.labels[id] = { g, circle, text, ring, name };
    }
    this.svg.classList.toggle('names', this.showNames);
    this.repaintCountries();
  }

  resize() { /* svg scales by itself */ }

  setShowNames(on) {
    this.showNames = on;
    this.svg.classList.toggle('names', on);
  }

  /** territory under a screen point, or NO_COUNTRY */
  countryAt(clientX, clientY) {
    const e = document.elementFromPoint(clientX, clientY);
    const t = e && e.closest && e.closest('[data-id]');
    if (!t || !this.svg.contains(t)) return NO_COUNTRY;
    return parseInt(t.getAttribute('data-id'), 10);
  }

  setView(v) {
    this.view = v;
    this.repaintCountries();
  }

  /** colour of each territory for the current view (same rules as PicturePanel) */
  colorFor(country, ctx) {
    const owner = country.getOwner();
    const cur = ctx.cur;
    switch (this.view) {
      case VIEW_CONTINENTS: return css(country.getContinent().getColor());
      case VIEW_OWNERSHIP: return owner ? css(owner.getColor()) : UNOWNED;
      case VIEW_BORDER_THREAT: {
        if (owner !== cur) return 'rgb(128,128,128)';
        let threat = 0;
        for (const n of country.getNeighbours()) if (n.getOwner() !== owner) threat++;
        return rgbCss([Math.min(255, threat * 40), 0, 0]);
      }
      case VIEW_CARD_OWNERSHIP: {
        const mine = owner === cur;
        if (!ctx.showCards) return 'rgb(192,192,192)';
        if (ctx.myCards.has(country)) return mine ? 'rgb(0,0,255)' : 'rgb(255,255,0)';
        return mine ? 'rgb(64,64,64)' : 'rgb(192,192,192)';
      }
      case VIEW_TROOP_STRENGTH: {
        const arm = Math.min(255, country.getArmies() * 25);
        if (owner == null) return 'rgb(128,128,128)';
        if (owner !== cur) return rgbCss([255, 255 - arm, 255 - arm]);
        return rgbCss([0, arm, 0]);
      }
      default: {
        if (owner == null) return 'rgb(192,192,192)';
        return ctx.empires.has(country) ? css(owner.getColor()) : 'rgb(64,64,64)';
      }
    }
  }

  repaintCountries() {
    const game = this.risk.getGame();
    if (!this.vector || !game || !game.getCountries() || !this.paths) return;
    const ctx = { cur: game.getCurrentPlayer(), showCards: false, myCards: new Set(), empires: null };
    if (this.view === VIEW_CONNECTED_EMPIRE) {
      ctx.empires = new Set();
      for (const p of game.getPlayers()) for (const c of RiskGame.getConnectedEmpire(p)) ctx.empires.add(c);
    } else if (this.view === VIEW_CARD_OWNERSHIP) {
      ctx.showCards = this.risk.showHumanCurrentPlayerInfo();
      if (ctx.showCards) ctx.myCards = new Set(game.getCurrentPlayer().getCards().map((c) => c.getCountry()).filter(Boolean));
    }
    for (const country of game.getCountries()) {
      const p = this.paths[country.getColor()];
      if (!p) continue;
      const col = this.colorFor(country, ctx);
      p.setAttribute('fill', col);
      p.setAttribute('stroke', col);
    }
    this.paint();
  }

  highlighted() {
    const game = this.risk.getGame();
    const set = new Map();
    if (this.cc !== NO_COUNTRY) set.set(this.cc, 'hover');
    if (!game) return set;
    if (game.getState() === RG.STATE_TRADE_CARDS && this.risk.showHumanCurrentPlayerInfo()) {
      const me = game.getCurrentPlayer();
      for (const card of me.getCards()) {
        const c = card.getCountry();
        if (c && c.getOwner() === me) set.set(c.getColor(), 'card');
      }
    }
    if (this.c2 !== NO_COUNTRY) set.set(this.c2, 'sel');
    if (this.c1 !== NO_COUNTRY) set.set(this.c1, 'sel');
    return set;
  }

  paint() {
    const game = this.risk.getGame();
    if (!game || !this.paths || !game.getCountries()) return;
    // highlights
    this.hlLayer.replaceChildren();
    for (const [id, kind] of this.highlighted()) {
      const p = this.paths[id];
      if (p) svg('path', { d: p.getAttribute('d'), class: 'hl-' + kind, 'fill-rule': 'evenodd' }, this.hlLayer);
    }
    this.paintDispute(game);
    this.paintArmies(game);
  }

  /** split the attacked territory in half, attacker's colour on the side it attacks from */
  paintDispute(game) {
    const st = game.getState();
    const a = game.getAttacker();
    const d = game.getDefender();
    if (!((st === RG.STATE_ROLLING || st === RG.STATE_DEFEND_YOURSELF) && a && d && a.getOwner() !== d.getOwner())) {
      this.disputeLayer.style.display = 'none';
      this.cutLine.style.display = 'none';
      return;
    }
    const rings = this.rings[d.getColor()];
    const [dx0, dy0] = this.centers[d.getColor()];
    let [ax, ay] = this.centers[a.getColor()];
    // attacks across the edge of the map (e.g. Alaska - Kamchatka)
    if (ax - dx0 > this.w / 2) ax -= this.w;
    else if (dx0 - ax > this.w / 2) ax += this.w;
    let ux = dx0 - ax, uy = dy0 - ay;
    const len = Math.hypot(ux, uy) || 1;
    ux /= len; uy /= len;
    // find the cut so that both halves have the same area
    let lo = Infinity, hi = -Infinity;
    for (const r of rings) for (let i = 0; i < r.length; i += 2) {
      const p = r[i] * ux + r[i + 1] * uy;
      if (p < lo) lo = p;
      if (p > hi) hi = p;
    }
    const total = areaBelow(rings, ux, uy, hi + 1);
    for (let k = 0; k < 30; k++) {
      const mid = (lo + hi) / 2;
      if (areaBelow(rings, ux, uy, mid) < total / 2) lo = mid; else hi = mid;
    }
    const t = (lo + hi) / 2;
    const big = (this.w + this.h) * 2;
    // point on the cut line closest to the centroid
    const off = dx0 * ux + dy0 * uy - t;
    const bx = dx0 - ux * off, by = dy0 - uy * off;
    const px = -uy, py = ux;
    const pts = [
      [bx + px * big, by + py * big],
      [bx - px * big, by - py * big],
      [bx - px * big - ux * big, by - py * big - uy * big],
      [bx + px * big - ux * big, by + py * big - uy * big],
    ];
    this.clipPoly.setAttribute('points', pts.map((q) => q[0] + ',' + q[1]).join(' '));
    const col = css(a.getOwner().getColor());
    this.disputePath.setAttribute('d', this.paths[d.getColor()].getAttribute('d'));
    this.disputeBase.setAttribute('d', this.paths[d.getColor()].getAttribute('d'));
    this.disputePath.setAttribute('fill', col);
    this.disputePath.setAttribute('stroke', col);
    this.disputeLayer.style.display = '';
    // the cut itself: where the line crosses the outline
    let mn = Infinity, mx = -Infinity;
    for (const r of rings) {
      const n = r.length / 2;
      for (let i = 0; i < n; i++) {
        const j = (i + 1) % n;
        const da = r[i * 2] * ux + r[i * 2 + 1] * uy - t, db = r[j * 2] * ux + r[j * 2 + 1] * uy - t;
        if ((da <= 0 && db > 0) || (da > 0 && db <= 0)) {
          const k = da / (da - db);
          const x = r[i * 2] + (r[j * 2] - r[i * 2]) * k, y = r[i * 2 + 1] + (r[j * 2 + 1] - r[i * 2 + 1]) * k;
          const q = (x - bx) * px + (y - by) * py;
          if (q < mn) mn = q;
          if (q > mx) mx = q;
        }
      }
    }
    if (Number.isFinite(mn) && mx > mn) {
      this.cutLine.setAttribute('x1', bx + px * mn); this.cutLine.setAttribute('y1', by + py * mn);
      this.cutLine.setAttribute('x2', bx + px * mx); this.cutLine.setAttribute('y2', by + py * mx);
      this.cutLine.style.display = '';
    } else this.cutLine.style.display = 'none';
  }

  paintArmies(game) {
    const st = game.getState();
    const capitals = new Map();
    if (game.getGameMode() === RG.MODE_CAPITAL && game.getSetupDone() && st !== RG.STATE_SELECT_CAPITAL) {
      for (const p of game.getPlayers()) if (p.getCapital()) capitals.set(p.getCapital(), p);
    }
    const countries = game.getCountries();
    for (let i = 0; i < countries.length; i++) {
      const t = countries[i];
      const lb = this.labels[t.getColor()];
      if (!lb) continue;
      const owner = t.getOwner();
      lb.g.style.display = owner ? '' : 'none';
      if (!owner) continue;
      const x = this.balls ? this.balls[i].x : t.getX();
      const y = this.balls ? this.balls[i].y : t.getY();
      lb.g.setAttribute('transform', `translate(${x} ${y})`);
      lb.circle.setAttribute('fill', css(owner.getColor()));
      lb.text.setAttribute('fill', css(getTextColorFor(owner.getColor())));
      lb.text.textContent = String(t.getArmies());
      const cap = capitals.get(t);
      if (cap) {
        lb.ring.style.display = '';
        lb.ring.setAttribute('stroke', css(cap.getColor()));
      } else lb.ring.style.display = 'none';
    }
  }

  /** small svg with the shape of one territory (battle dialog) */
  countryImage(id) {
    const rings = this.rings && this.rings[id];
    if (!rings || !rings.length) return null;
    let x1 = Infinity, y1 = Infinity, x2 = -Infinity, y2 = -Infinity;
    for (const r of rings) for (let i = 0; i < r.length; i += 2) {
      x1 = Math.min(x1, r[i]); x2 = Math.max(x2, r[i]); y1 = Math.min(y1, r[i + 1]); y2 = Math.max(y2, r[i + 1]);
    }
    const s = svg('svg', { viewBox: `${x1 - 2} ${y1 - 2} ${x2 - x1 + 4} ${y2 - y1 + 4}`, class: 'shape' });
    const p = this.paths[id];
    svg('path', { d: p.getAttribute('d'), fill: p.getAttribute('fill'), stroke: p.getAttribute('stroke'), 'fill-rule': 'evenodd', 'fill-opacity': FILL_OPACITY, 'stroke-width': 1.5, 'vector-effect': 'non-scaling-stroke' }, s);
    return s;
  }

  // ---------- game over animation (BallWorld) ----------
  startAni() {
    const game = this.risk.getGame();
    if (this.balls || !game || !game.getCountries() || !this.vector) return;
    if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;
    const r = game.getCircleSize() / 2;
    this.balls = game.getCountries().map((c) => ({ x: c.getX(), y: c.getY(), vx: (Math.random() - 0.5) * 0.4, vy: 2, r }));
    const step = () => {
      if (!this.balls) return;
      this.updateBalls();
      this.paintArmies(this.risk.getGame());
      this.aniTimer = setTimeout(step, 1000 / 30);
    };
    step();
  }

  stopAni() {
    if (this.balls) {
      this.balls = null;
      clearTimeout(this.aniTimer);
      const g = this.risk.getGame();
      if (g && g.getCountries() && this.labels) this.paintArmies(g);
    }
  }

  toggleAni() {
    if (this.balls) this.stopAni(); else this.startAni();
  }

  updateBalls() {
    const b = this.balls;
    for (const p of b) {
      p.x += p.vx; p.y += p.vy;
      if (p.x < p.r) { p.x = p.r; p.vx = Math.abs(p.vx); }
      if (p.x > this.w - p.r) { p.x = this.w - p.r; p.vx = -Math.abs(p.vx); }
      if (p.y < p.r) { p.y = p.r; p.vy = Math.abs(p.vy); }
      if (p.y > this.h - p.r) { p.y = this.h - p.r; p.vy = -Math.abs(p.vy); }
    }
    for (let i = 0; i < b.length; i++) {
      for (let j = i + 1; j < b.length; j++) {
        const dx = b[j].x - b[i].x, dy = b[j].y - b[i].y;
        const dist = Math.hypot(dx, dy), min = b[i].r + b[j].r;
        if (dist > 0 && dist < min) {
          const nx = dx / dist, ny = dy / dist;
          const p = (b[i].vx - b[j].vx) * nx + (b[i].vy - b[j].vy) * ny;
          if (p > 0) { b[i].vx -= p * nx; b[i].vy -= p * ny; b[j].vx += p * nx; b[j].vy += p * ny; }
          const push = (min - dist) / 2;
          b[i].x -= nx * push; b[i].y -= ny * push;
          b[j].x += nx * push; b[j].y += ny * push;
        }
      }
    }
  }
}
