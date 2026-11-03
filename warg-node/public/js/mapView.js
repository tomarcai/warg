// warg - the map, port of the original PicturePanel. GPLv3.
// The "_map.gif" of every map has one colour index per country (blue channel = country id,
// 255 = sea). The "_pic" image is what we show; countries get tinted per map view.

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

function loadImage(src) {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.onload = () => resolve(img);
    img.onerror = () => reject(new Error('can not load ' + src));
    img.src = src;
  });
}

const rgb = (c) => [getRed(c), getGreen(c), getBlue(c)];

export class MapView {
  constructor(canvas, risk) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d');
    this.risk = risk;
    this.view = VIEW_CONTINENTS;
    this.c1 = NO_COUNTRY;
    this.c2 = NO_COUNTRY;
    this.cc = NO_COUNTRY;
    this.balls = null;
    this.loadedFor = null;
    this.ro = new ResizeObserver(() => this.resize());
    this.ro.observe(canvas.parentElement);
  }

  async load(game) {
    const key = game.getImageMap() + '|' + game.getImagePic();
    if (this.loadedFor === key) return;
    const [mapImg, picImg] = await Promise.all([
      loadImage('/maps/' + game.getImageMap()),
      loadImage('/maps/' + game.getImagePic()),
    ]);
    const w = mapImg.naturalWidth;
    const h = mapImg.naturalHeight;
    this.w = w;
    this.h = h;
    const tmp = document.createElement('canvas');
    tmp.width = w;
    tmp.height = h;
    const tctx = tmp.getContext('2d', { willReadFrequently: true });
    tctx.drawImage(mapImg, 0, 0);
    const md = tctx.getImageData(0, 0, w, h).data;
    tctx.clearRect(0, 0, w, h);
    tctx.drawImage(picImg, 0, 0, w, h);
    this.pic = tctx.getImageData(0, 0, w, h);

    const noc = game.getNoCountries();
    this.ids = new Uint8Array(w * h);
    const counts = new Array(noc + 1).fill(0);
    for (let i = 0; i < w * h; i++) {
      const id = md[i * 4 + 2];
      this.ids[i] = id;
      if (id !== NO_COUNTRY && id <= noc) counts[id]++;
    }
    this.pixels = counts.map((n) => new Int32Array(n));
    const fill = new Array(noc + 1).fill(0);
    for (let i = 0; i < w * h; i++) {
      const id = this.ids[i];
      if (id !== NO_COUNTRY && id <= noc) this.pixels[id][fill[id]++] = i;
    }
    const p = this.pic.data;
    this.gray = new Uint8ClampedArray(w * h);
    for (let i = 0; i < w * h; i++) this.gray[i] = 0.299 * p[i * 4] + 0.587 * p[i * 4 + 1] + 0.114 * p[i * 4 + 2];

    this.base = new ImageData(new Uint8ClampedArray(this.pic.data), w, h);
    this.frame = new ImageData(w, h);
    this.off = document.createElement('canvas');
    this.off.width = w;
    this.off.height = h;
    this.offCtx = this.off.getContext('2d');
    this.loadedFor = key;
    this.stopAni();
    this.resize();
  }

  resize() {
    const parent = this.canvas.parentElement;
    const dpr = window.devicePixelRatio || 1;
    const cw = parent.clientWidth;
    const ch = parent.clientHeight;
    this.canvas.width = Math.max(1, Math.round(cw * dpr));
    this.canvas.height = Math.max(1, Math.round(ch * dpr));
    this.dpr = dpr;
    this.cw = cw;
    this.ch = ch;
    this.paint();
  }

  get scale() {
    return Math.min(this.cw / this.w, this.ch / this.h);
  }

  /** map pixel -> country id (or 255) for a mouse event */
  countryAt(clientX, clientY) {
    if (!this.ids) return NO_COUNTRY;
    const r = this.canvas.getBoundingClientRect();
    const s = this.scale;
    const ox = (this.cw - this.w * s) / 2;
    const oy = (this.ch - this.h * s) / 2;
    const x = Math.floor((clientX - r.left - ox) / s);
    const y = Math.floor((clientY - r.top - oy) / s);
    if (x < 0 || y < 0 || x >= this.w || y >= this.h) return NO_COUNTRY;
    return this.ids[y * this.w + x];
  }

  setView(v) {
    this.view = v;
    this.repaintCountries();
  }

  /** recolours every country for the current view (PicturePanel.repaintCountries) */
  repaintCountries() {
    const game = this.risk.getGame();
    if (!this.pic || !game || !game.getCountries()) return;
    const out = this.base.data;
    out.set(this.pic.data);
    const view = this.view;
    if (view !== VIEW_CONTINENTS) {
      let empires = null;
      let showCards = false;
      let myCards = null;
      if (view === VIEW_CONNECTED_EMPIRE) {
        empires = new Set();
        for (const p of game.getPlayers()) for (const c of RiskGame.getConnectedEmpire(p)) empires.add(c);
      } else if (view === VIEW_CARD_OWNERSHIP) {
        showCards = this.risk.showHumanCurrentPlayerInfo();
        if (showCards) myCards = new Set(game.getCurrentPlayer().getCards().map((c) => c.getCountry()).filter(Boolean));
      }
      const cur = game.getCurrentPlayer();
      const countries = game.getCountries();
      for (let id = 1; id <= countries.length; id++) {
        const country = countries[id - 1];
        const owner = country.getOwner();
        let col;
        let a;
        if (view === VIEW_OWNERSHIP) {
          col = owner ? rgb(owner.getColor()) : [128, 128, 128];
          a = 100;
        } else if (view === VIEW_BORDER_THREAT) {
          if (owner !== cur) col = [128, 128, 128];
          else {
            let threat = 0;
            for (const n of country.getNeighbours()) if (n.getOwner() !== owner) threat++;
            col = [Math.min(255, threat * 40), 0, 0];
          }
          a = 200;
        } else if (view === VIEW_CARD_OWNERSHIP) {
          const mine = owner === cur;
          if (showCards) {
            if (myCards.has(country)) col = mine ? [0, 0, 255] : [255, 255, 0];
            else col = mine ? [64, 64, 64] : [192, 192, 192];
          } else col = [192, 192, 192];
          a = 100;
        } else if (view === VIEW_TROOP_STRENGTH) {
          const arm = Math.min(255, country.getArmies() * 25);
          if (owner == null) col = [128, 128, 128];
          else if (owner !== cur) col = [255, 255 - arm, 255 - arm];
          else col = [0, arm, 0];
          a = 200;
        } else {
          if (owner == null) col = [192, 192, 192];
          else if (empires.has(country)) col = rgb(owner.getColor());
          else col = [64, 64, 64];
          a = 100;
        }
        const al = a / 255;
        const r = col[0] * al, g = col[1] * al, b = col[2] * al, k = 1 - al;
        const px = this.pixels[id];
        for (let j = 0; j < px.length; j++) {
          const i = px[j];
          const gy = this.gray[i] * k;
          const o = i * 4;
          out[o] = gy + r;
          out[o + 1] = gy + g;
          out[o + 2] = gy + b;
        }
      }
    }
    this.paint();
  }

  highlighted() {
    const game = this.risk.getGame();
    const set = new Set();
    for (const c of [this.c1, this.c2, this.cc]) if (c !== NO_COUNTRY) set.add(c);
    if (!game) return set;
    const st = game.getState();
    if (st === RG.STATE_TRADE_CARDS && this.risk.showHumanCurrentPlayerInfo()) {
      const me = game.getCurrentPlayer();
      for (const card of me.getCards()) {
        const c = card.getCountry();
        if (c && c.getOwner() === me) set.add(c.getColor());
      }
    }
    if ((st === RG.STATE_ROLLING || st === RG.STATE_BATTLE_WON || st === RG.STATE_DEFEND_YOURSELF) && game.getAttacker() && game.getDefender()) {
      set.add(game.getAttacker().getColor());
      set.add(game.getDefender().getColor());
    }
    return set;
  }

  paint() {
    const ctx = this.ctx;
    ctx.setTransform(1, 0, 0, 1, 0, 0);
    ctx.fillStyle = '#000';
    ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
    const game = this.risk.getGame();
    if (!this.base || !game) return;

    // compose base + highlights (RescaleOp 1.5, +1)
    const f = this.frame.data;
    f.set(this.base.data);
    for (const id of this.highlighted()) {
      const px = this.pixels[id];
      if (!px) continue;
      for (let j = 0; j < px.length; j++) {
        const o = px[j] * 4;
        f[o] = f[o] * 1.5 + 1;
        f[o + 1] = f[o + 1] * 1.5 + 1;
        f[o + 2] = f[o + 2] * 1.5 + 1;
      }
    }
    this.offCtx.putImageData(this.frame, 0, 0);

    const s = this.scale;
    const ox = (this.cw - this.w * s) / 2;
    const oy = (this.ch - this.h * s) / 2;
    const d = this.dpr;
    ctx.setTransform(d * s, 0, 0, d * s, d * ox, d * oy);
    ctx.imageSmoothingEnabled = s < 1.5;
    ctx.drawImage(this.off, 0, 0);
    ctx.imageSmoothingEnabled = true;
    if (game.getCountries()) this.drawArmies(ctx, game);
  }

  drawArmies(ctx, game) {
    const st = game.getState();
    const ball = game.getCircleSize();
    if ((st === RG.STATE_ROLLING || st === RG.STATE_BATTLE_WON || st === RG.STATE_DEFEND_YOURSELF) && game.getAttacker() && game.getDefender()) {
      const a = game.getAttacker();
      const b = game.getDefender();
      const c = a.getOwner().getColor();
      ctx.fillStyle = `rgba(${getRed(c)},${getGreen(c)},${getBlue(c)},${150 / 255})`;
      const W = this.w;
      if (Math.abs(a.getX() - b.getX()) > W / 2) {
        if (a.getX() > W / 2) {
          this.fillArrow(ctx, a.getX(), a.getY(), b.getX() + W, b.getY(), ball);
          this.fillArrow(ctx, a.getX() - W, a.getY(), b.getX(), b.getY(), ball);
        } else {
          this.fillArrow(ctx, a.getX(), a.getY(), b.getX() - W, b.getY(), ball);
          this.fillArrow(ctx, a.getX() + W, a.getY(), b.getX(), b.getY(), ball);
        }
      } else {
        this.fillArrow(ctx, a.getX(), a.getY(), b.getX(), b.getY(), ball);
      }
    }
    const capitals = new Map();
    if (game.getGameMode() === RG.MODE_CAPITAL && game.getSetupDone() && st !== RG.STATE_SELECT_CAPITAL) {
      for (const p of game.getPlayers()) if (p.getCapital()) capitals.set(p.getCapital(), p);
    }
    ctx.font = `${Math.floor((ball + 2) / 2)}px Arial, Helvetica, sans-serif`;
    ctx.textBaseline = 'alphabetic';
    const countries = game.getCountries();
    for (let i = 0; i < countries.length; i++) {
      const t = countries[i];
      if (t.getOwner() == null) continue;
      let x = t.getX();
      let y = t.getY();
      if (this.balls) { x = this.balls[i].x; y = this.balls[i].y; }
      this.drawArmy(ctx, t.getOwner().getColor(), t.getArmies(), x, y, ball, capitals.get(t));
    }
  }

  drawArmy(ctx, color, armies, x, y, ballSize, capital) {
    const r = ballSize / 2;
    ctx.fillStyle = `rgb(${getRed(color)},${getGreen(color)},${getBlue(color)})`;
    ctx.beginPath();
    ctx.arc(x, y, r, 0, Math.PI * 2);
    ctx.fill();
    const noa = String(armies);
    const m = ctx.measureText(noa);
    const ascent = m.actualBoundingBoxAscent || ballSize * 0.45;
    const tc = getTextColorFor(color);
    ctx.fillStyle = `rgb(${getRed(tc)},${getGreen(tc)},${getBlue(tc)})`;
    // the original centres using font ascent * 2/5
    ctx.fillText(noa, x - m.width / 2, y + (ascent * 1.1) * 2 / 5 + 0.5);
    if (capital) {
      const stroke = ballSize / 10;
      ctx.lineWidth = stroke;
      const c1 = getTextColorFor(capital.getColor());
      ctx.strokeStyle = `rgb(${getRed(c1)},${getGreen(c1)},${getBlue(c1)})`;
      ctx.beginPath();
      ctx.arc(x - 0.5, y - 0.5, r - 0.5, 0, Math.PI * 2);
      ctx.stroke();
      const c2 = capital.getColor();
      ctx.strokeStyle = `rgb(${getRed(c2)},${getGreen(c2)},${getBlue(c2)})`;
      ctx.beginPath();
      ctx.arc(x - 0.5, y - 0.5, (ballSize + stroke * 2) / 2 - 0.5, 0, Math.PI * 2);
      ctx.stroke();
    }
  }

  /** PicturePanel.makeArrow */
  fillArrow(ctx, x1, y1, x2, y2, d) {
    const xd = x2 - x1;
    const yd = y1 - y2;
    const r = d / 2;
    const l = Math.sqrt(xd * xd + yd * yd);
    if (l <= r) return;
    const a = Math.acos(r / l);
    const b = Math.atan(yd / xd);
    const c = Math.atan(xd / yd);
    const x3 = r * Math.cos(a - b);
    const y3 = r * Math.sin(a - b);
    const x4 = r * Math.sin(a - c);
    const y4 = r * Math.cos(a - c);
    let xs, ys;
    if (x2 >= x1 && y2 <= y1) {
      xs = [x1, x1 + x3, x2, x1 - x4]; ys = [y1, y1 + y3, y2, y1 - y4];
    } else if (x2 >= x1 && y2 >= y1) {
      xs = [x1, x1 + x3, x2, x1 + x4]; ys = [y1, y1 + y3, y2, y1 + y4];
    } else if (x2 <= x1 && y2 <= y1) {
      xs = [x1, x1 - x3, x2, x1 - x4]; ys = [y1, y1 - y3, y2, y1 - y4];
    } else {
      xs = [x1, x1 - x3, x2, x1 + x4]; ys = [y1, y1 - y3, y2, y1 + y4];
    }
    ctx.beginPath();
    ctx.moveTo(xs[0], ys[0]);
    for (let i = 1; i < 4; i++) ctx.lineTo(xs[i], ys[i]);
    ctx.closePath();
    ctx.fill();
  }

  /** a small copy of the image of one country (used by the battle and move dialogs) */
  countryImage(id) {
    const px = this.pixels && this.pixels[id];
    if (!px || px.length === 0) return null;
    let x1 = Infinity, y1 = Infinity, x2 = -1, y2 = -1;
    for (const i of px) {
      const x = i % this.w, y = (i / this.w) | 0;
      if (x < x1) x1 = x; if (x > x2) x2 = x; if (y < y1) y1 = y; if (y > y2) y2 = y;
    }
    const cw = x2 - x1 + 1, ch = y2 - y1 + 1;
    const c = document.createElement('canvas');
    c.width = cw; c.height = ch;
    const img = c.getContext('2d').createImageData(cw, ch);
    const src = this.base.data;
    for (const i of px) {
      const x = (i % this.w) - x1, y = ((i / this.w) | 0) - y1;
      const o = (y * cw + x) * 4, s = i * 4;
      img.data[o] = Math.min(255, src[s] * 1.5 + 1);
      img.data[o + 1] = Math.min(255, src[s + 1] * 1.5 + 1);
      img.data[o + 2] = Math.min(255, src[s + 2] * 1.5 + 1);
      img.data[o + 3] = 255;
    }
    c.getContext('2d').putImageData(img, 0, 0);
    return c;
  }

  // ---------- game over animation (BallWorld) ----------
  startAni() {
    const game = this.risk.getGame();
    if (this.balls || !game || !game.getCountries()) return;
    if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;
    const r = game.getCircleSize() / 2;
    this.balls = game.getCountries().map((c) => ({ x: c.getX(), y: c.getY(), vx: (Math.random() - 0.5) * 0.4, vy: 2, r }));
    const step = () => {
      if (!this.balls) return;
      this.updateBalls();
      this.paint();
      this.aniTimer = setTimeout(step, 1000 / 30);
    };
    step();
  }

  stopAni() {
    if (this.balls) {
      this.balls = null;
      clearTimeout(this.aniTimer);
      this.paint();
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
    // elastic collisions between equal balls
    for (let i = 0; i < b.length; i++) {
      for (let j = i + 1; j < b.length; j++) {
        const dx = b[j].x - b[i].x, dy = b[j].y - b[i].y;
        const dist = Math.hypot(dx, dy), min = b[i].r + b[j].r;
        if (dist > 0 && dist < min) {
          const nx = dx / dist, ny = dy / dist;
          const p = (b[i].vx - b[j].vx) * nx + (b[i].vy - b[j].vy) * ny;
          if (p > 0) {
            b[i].vx -= p * nx; b[i].vy -= p * ny;
            b[j].vx += p * nx; b[j].vy += p * ny;
          }
          const push = (min - dist) / 2;
          b[i].x -= nx * push; b[i].y -= ny * push;
          b[j].x += nx * push; b[j].y += ny * push;
        }
      }
    }
  }
}
