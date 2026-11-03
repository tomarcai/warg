// warg - map editor: draw territories as polygons, group them in continents, connect neighbours
// and save the map so it can be played. GPLv3.

import { svg, ringsToPath, centroid } from './mapView.js';
import { validateMap, shapeDistance } from '/tools/mapWriter.js';
import { parseMapFile } from '/tools/vectorize.js';
import * as ColorUtil from '/engine/colors.js';

const PALETTE = ['#c8962e', '#3f8fd2', '#5aa34a', '#c4453a', '#8a5cc8', '#d97a2b', '#2fa39b', '#c25b97', '#7d8b3a', '#4b5fbf'];

export function initEditor(deps) {
  const { t, show, openDialog, el, alertDialog, confirmDialog, onSaved } = deps;
  const $ = (id) => document.getElementById(id);
  const svgEl = $('ed-svg');

  let ed = null;
  const view = { x: 0, y: 0, w: 800, h: 500 };
  let drag = null; // current mouse drag
  let pointer = null; // last map position of the mouse
  let spaceDown = false;
  let bgUrl = null;
  let historyStack = [];

  // ---------------------------------------------------------------- model

  function blank() {
    return {
      name: t('editor.newname'),
      file: null,
      width: 800,
      height: 500,
      continents: [{ name: t('editor.continent') + ' 1', bonus: 3, color: PALETTE[0] }],
      terrs: [],
      sel: -1,
      tool: 'select',
      drawing: null,
      dirty: false,
    };
  }

  function snapshot() {
    historyStack.push(JSON.stringify({ c: ed.continents, t: ed.terrs.map((x) => ({ ...x, neighbors: [...x.neighbors] })), w: ed.width, h: ed.height, n: ed.name }));
    if (historyStack.length > 60) historyStack.shift();
    ed.dirty = true;
  }

  function undo() {
    const s = historyStack.pop();
    if (!s) return;
    const o = JSON.parse(s);
    ed.continents = o.c;
    ed.terrs = o.t.map((x) => ({ ...x, neighbors: new Set(x.neighbors) }));
    ed.width = o.w; ed.height = o.h; ed.name = o.n;
    if (ed.sel >= ed.terrs.length) ed.sel = -1;
    renderAll();
  }

  function toData() {
    return {
      name: ed.name,
      file: ed.file,
      width: ed.width,
      height: ed.height,
      continents: ed.continents.map((c) => ({ name: c.name, bonus: c.bonus, color: c.color })),
      territories: ed.terrs.map((x) => ({ name: x.name, continent: x.continent, rings: x.rings, label: x.label, neighbors: [...x.neighbors] })),
    };
  }

  // ---------------------------------------------------------------- geometry helpers

  const pxToMap = () => view.w / Math.max(1, svgEl.clientWidth || 1);

  function toMap(e) {
    const pt = svgEl.createSVGPoint();
    pt.x = e.clientX; pt.y = e.clientY;
    const m = svgEl.getScreenCTM();
    if (!m) return { x: 0, y: 0 };
    const p = pt.matrixTransform(m.inverse());
    return { x: p.x, y: p.y };
  }

  const round1 = (v) => Math.round(v * 10) / 10;

  /** nearest existing vertex (of any territory, or the drawing) within a few screen pixels */
  function snap(p, skip) {
    const r = 9 * pxToMap();
    let best = null, bd = r * r;
    ed.terrs.forEach((tr, ti) => {
      tr.rings.forEach((ring, ri) => {
        for (let i = 0; i < ring.length; i += 2) {
          if (skip && skip.has(ti + ':' + ri + ':' + i)) continue;
          const d = (ring[i] - p.x) ** 2 + (ring[i + 1] - p.y) ** 2;
          if (d < bd) { bd = d; best = { x: ring[i], y: ring[i + 1] }; }
        }
      });
    });
    return best || { x: round1(p.x), y: round1(p.y) };
  }

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

  function territoryAt(p) {
    for (let i = ed.terrs.length - 1; i >= 0; i--) if (pointInRings(ed.terrs[i].rings, p.x, p.y)) return i;
    return -1;
  }

  /** neighbours by touching shapes (shared or nearly shared borders) */
  function detectNeighbors(i) {
    const a = ed.terrs[i];
    const tol = 2;
    ed.terrs.forEach((b, j) => {
      if (j === i) return;
      if (!bboxNear(a.rings, b.rings, tol)) return;
      if (shapeDistance(a.rings, b.rings).dist <= tol) {
        a.neighbors.add(j);
        b.neighbors.add(i);
      }
    });
  }

  function bbox(rings) {
    let x1 = Infinity, y1 = Infinity, x2 = -Infinity, y2 = -Infinity;
    for (const r of rings) for (let i = 0; i < r.length; i += 2) {
      x1 = Math.min(x1, r[i]); x2 = Math.max(x2, r[i]); y1 = Math.min(y1, r[i + 1]); y2 = Math.max(y2, r[i + 1]);
    }
    return { x1, y1, x2, y2 };
  }
  function bboxNear(ra, rb, tol) {
    const a = bbox(ra), b = bbox(rb);
    return !(a.x2 + tol < b.x1 || b.x2 + tol < a.x1 || a.y2 + tol < b.y1 || b.y2 + tol < a.y1);
  }

  function nextName() {
    const used = new Set(ed.terrs.map((x) => x.name.toLowerCase()));
    for (let i = ed.terrs.length + 1; ; i++) {
      const n = t('editor.territory') + ' ' + i;
      if (!used.has(n.toLowerCase())) return n;
    }
  }

  // ---------------------------------------------------------------- rendering

  function applyView() {
    svgEl.setAttribute('viewBox', `${view.x} ${view.y} ${view.w} ${view.h}`);
  }

  function fit() {
    const pad = 20;
    view.x = -pad; view.y = -pad; view.w = ed.width + pad * 2; view.h = ed.height + pad * 2;
    applyView();
  }

  function render() {
    svgEl.replaceChildren();
    applyView();
    svg('rect', { x: 0, y: 0, width: ed.width, height: ed.height, class: 'ed-sea' }, svgEl);
    if (bgUrl) svg('image', { href: bgUrl, x: 0, y: 0, width: ed.width, height: ed.height, opacity: $('ed-bg-op').value, preserveAspectRatio: 'none', 'pointer-events': 'none' }, svgEl);
    const land = svg('g', {}, svgEl);
    ed.terrs.forEach((tr, i) => {
      const c = ed.continents[tr.continent];
      const col = c ? c.color : '#888';
      svg('path', { d: ringsToPath(tr.rings), fill: col, stroke: col, class: 'ed-terr' + (i === ed.sel ? ' sel' : ''), 'data-i': i, 'fill-rule': 'evenodd' }, land);
    });
    // neighbour lines of the selected territory (all of them in connect mode)
    const lines = svg('g', { class: 'ed-links' }, svgEl);
    ed.terrs.forEach((tr, i) => {
      if (ed.tool !== 'connect' && i !== ed.sel) return;
      for (const j of tr.neighbors) {
        if (ed.tool === 'connect' && j < i && ed.sel !== i) continue;
        const b = ed.terrs[j];
        if (b) svg('line', { x1: tr.label[0], y1: tr.label[1], x2: b.label[0], y2: b.label[1], class: i === ed.sel || j === ed.sel ? 'on' : '' }, lines);
      }
    });
    const labels = svg('g', { class: 'ed-labels' }, svgEl);
    ed.terrs.forEach((tr, i) => {
      const g = svg('g', { transform: `translate(${tr.label[0]} ${tr.label[1]})`, class: 'ed-label' + (i === ed.sel ? ' sel' : ''), 'data-label': i }, labels);
      svg('circle', { r: 7 * Math.min(1, Math.max(0.5, pxToMap())) + 3 }, g);
      const tx = svg('text', { y: -12, 'text-anchor': 'middle' }, g);
      tx.textContent = tr.name;
    });
    // vertices of the selected territory
    if (ed.sel >= 0 && ed.tool === 'select') {
      const hs = svg('g', { class: 'ed-handles' }, svgEl);
      const r = 4 * pxToMap();
      ed.terrs[ed.sel].rings.forEach((ring, ri) => {
        for (let i = 0; i < ring.length; i += 2) {
          svg('circle', { cx: ring[i], cy: ring[i + 1], r, 'data-v': ri + ':' + i }, hs);
        }
      });
    }
    // polygon being drawn
    if (ed.drawing && ed.drawing.pts.length) {
      const pts = ed.drawing.pts;
      const all = pointer ? [...pts, pointer.x, pointer.y] : pts;
      svg('polyline', { points: all.join(' '), class: 'ed-drawing' }, svgEl);
      svg('circle', { cx: pts[0], cy: pts[1], r: 6 * pxToMap(), class: 'ed-first' }, svgEl);
    }
  }

  function hint() {
    const key = {
      select: 'editor.hint.select', draw: 'editor.hint.draw', part: 'editor.hint.part', connect: 'editor.hint.connect', label: 'editor.hint.label',
    }[ed.tool];
    $('ed-hint').textContent = t(key);
  }

  function renderPanel() {
    $('ed-name').value = ed.name;
    $('ed-w').value = ed.width;
    $('ed-h').value = ed.height;
    document.querySelectorAll('input[name=ed-tool]').forEach((r) => { r.checked = r.value === ed.tool; });
    // continents
    const box = $('ed-conts');
    box.replaceChildren(...ed.continents.map((c, i) => {
      const color = el('input', { type: 'color', value: c.color, 'aria-label': t('editor.color') });
      const name = el('input', { value: c.name, maxlength: 30, 'aria-label': t('editor.mapname') });
      const bonus = el('input', { type: 'number', min: 0, max: 99, value: c.bonus, title: t('editor.bonus'), 'aria-label': t('editor.bonus') });
      const count = ed.terrs.filter((x) => x.continent === i).length;
      color.addEventListener('change', () => { snapshot(); c.color = color.value; renderAll(); });
      name.addEventListener('change', () => { snapshot(); c.name = name.value.trim(); renderAll(); });
      bonus.addEventListener('change', () => { snapshot(); c.bonus = Math.max(0, Math.min(99, parseInt(bonus.value, 10) || 0)); renderAll(); });
      const del = el('button', {
        type: 'button', class: 'icon', title: t('editor.delete'), disabled: ed.continents.length === 1,
        onclick: async () => {
          if (count > 0 && !(await confirmDialog(t('editor.delete'), t('editor.deletecontinent', c.name, count), t('editor.delete'), t('newgame.cancel')))) return;
          snapshot();
          ed.continents.splice(i, 1);
          for (const x of ed.terrs) { if (x.continent === i) x.continent = 0; else if (x.continent > i) x.continent--; }
          renderAll();
        },
      }, '✕');
      return el('div', { class: 'ed-cont' }, color, name, bonus, el('span', { class: 'muted', title: t('editor.territories') }, String(count)), del);
    }));
    // selected territory
    const box2 = $('ed-terr');
    if (ed.sel < 0) {
      box2.replaceChildren(el('h3', {}, t('editor.selected')), el('p', { class: 'muted' }, t('editor.noselection')));
    } else {
      const tr = ed.terrs[ed.sel];
      const name = el('input', { value: tr.name, maxlength: 40 });
      name.addEventListener('change', () => { snapshot(); tr.name = name.value.trim(); renderAll(); });
      const cont = el('select', {}, ed.continents.map((c, i) => el('option', { value: i, selected: i === tr.continent }, c.name)));
      cont.addEventListener('change', () => { snapshot(); tr.continent = parseInt(cont.value, 10); renderAll(); });
      const chips = el('div', { class: 'ed-chips' }, [...tr.neighbors].sort((a, b) => a - b).map((j) => el('span', { class: 'chipbtn' },
        el('button', { type: 'button', onclick: () => select(j) }, ed.terrs[j].name),
        el('button', { type: 'button', class: 'x', title: t('editor.disconnect'), onclick: () => { snapshot(); tr.neighbors.delete(j); ed.terrs[j].neighbors.delete(ed.sel); renderAll(); } }, '✕'))));
      box2.replaceChildren(
        el('h3', {}, t('editor.selected')),
        el('label', { class: 'field' }, el('span', {}, t('editor.mapname')), name),
        el('label', { class: 'field' }, el('span', {}, t('editor.continent')), cont),
        el('div', { class: 'field' }, el('span', {}, t('editor.neighbors') + ' (' + tr.neighbors.size + ')'), tr.neighbors.size ? chips : el('span', { class: 'muted' }, t('editor.noneighbors'))),
        el('p', { class: 'muted' }, t('editor.parts', tr.rings.length)),
        el('div', { class: 'add-row' },
          el('button', { class: 'btn', type: 'button', onclick: () => { snapshot(); detectNeighbors(ed.sel); renderAll(); } }, t('editor.detect')),
          el('button', { class: 'btn danger', type: 'button', onclick: () => deleteTerritory(ed.sel) }, t('editor.deleteterritory'))));
    }
    // list
    $('ed-list').replaceChildren(...ed.terrs.map((x, i) => el('li', {},
      el('button', { type: 'button', class: i === ed.sel ? 'on' : '', onclick: () => select(i) },
        el('span', { class: 'chip', style: { background: (ed.continents[x.continent] || {}).color || '#888' } }), x.name))));
    // problems
    const { errors, warnings } = validateMap(toData());
    const items = [...errors.map((e) => ({ ...e, kind: 'err' })), ...warnings.map((w) => ({ ...w, kind: 'warn' }))];
    $('ed-problems').replaceChildren(...(items.length
      ? items.slice(0, 40).map((p) => el('li', { class: p.kind }, t(p.key, ...(p.args || []))))
      : [el('li', { class: 'ok' }, t('editor.ok'))]));
    $('ed-save').disabled = errors.length > 0;
    $('ed-save').title = errors.length ? t('editor.fixfirst') : '';
  }

  function renderAll() {
    render();
    renderPanel();
    hint();
  }

  function select(i) {
    ed.sel = i;
    renderAll();
  }

  async function deleteTerritory(i) {
    if (!(await confirmDialog(t('editor.deleteterritory'), t('editor.deleteconfirm', ed.terrs[i].name), t('editor.delete'), t('newgame.cancel')))) return;
    snapshot();
    ed.terrs.splice(i, 1);
    for (const x of ed.terrs) {
      const nb = new Set();
      for (const j of x.neighbors) if (j !== i) nb.add(j > i ? j - 1 : j);
      x.neighbors = nb;
    }
    ed.sel = -1;
    renderAll();
  }

  // ---------------------------------------------------------------- drawing

  function finishDrawing() {
    const d = ed.drawing;
    if (!d || d.pts.length < 6) return;
    snapshot();
    const ring = d.pts.slice();
    if (ed.tool === 'part' && ed.sel >= 0) {
      ed.terrs[ed.sel].rings.push(ring);
      detectNeighbors(ed.sel);
    } else {
      const c = centroid([ring]);
      const contIdx = ed.sel >= 0 ? ed.terrs[ed.sel].continent : 0;
      ed.terrs.push({ name: nextName(), continent: contIdx, rings: [ring], label: [round1(c[0]), round1(c[1])], neighbors: new Set() });
      ed.sel = ed.terrs.length - 1;
      detectNeighbors(ed.sel);
    }
    ed.drawing = null;
    renderAll();
  }

  function cancelDrawing() {
    ed.drawing = null;
    render();
  }

  // ---------------------------------------------------------------- mouse

  svgEl.addEventListener('contextmenu', (e) => e.preventDefault());

  svgEl.addEventListener('wheel', (e) => {
    e.preventDefault();
    const p = toMap(e);
    const k = e.deltaY > 0 ? 1.15 : 1 / 1.15;
    const nw = Math.max(40, Math.min(ed.width * 4, view.w * k));
    const f = nw / view.w;
    view.x = p.x - (p.x - view.x) * f;
    view.y = p.y - (p.y - view.y) * f;
    view.w = nw;
    view.h *= f;
    render();
  }, { passive: false });

  svgEl.addEventListener('pointerdown', (e) => {
    const p = toMap(e);
    if (e.button === 1 || spaceDown) {
      drag = { kind: 'pan', sx: e.clientX, sy: e.clientY, vx: view.x, vy: view.y, k: pxToMap() };
      svgEl.setPointerCapture(e.pointerId);
      return;
    }
    const tool = ed.tool;
    if (tool === 'draw' || tool === 'part') {
      if (e.button === 2) { // right click removes the last point
        if (ed.drawing) { ed.drawing.pts.splice(-2, 2); if (!ed.drawing.pts.length) ed.drawing = null; render(); }
        return;
      }
      if (tool === 'part' && ed.sel < 0) { $('ed-hint').textContent = t('editor.hint.partfirst'); return; }
      const q = snap(p);
      if (!ed.drawing) ed.drawing = { pts: [] };
      const pts = ed.drawing.pts;
      const r = 9 * pxToMap();
      if (pts.length >= 6 && Math.hypot(q.x - pts[0], q.y - pts[1]) <= r) { finishDrawing(); return; }
      if (pts.length >= 2 && q.x === pts[pts.length - 2] && q.y === pts[pts.length - 1]) return;
      pts.push(q.x, q.y);
      render();
      return;
    }
    if (tool === 'connect') {
      // first click picks the origin, each following click connects / disconnects it
      const i = territoryAt(p);
      if (i < 0) { ed.connectFrom = -1; select(-1); return; }
      if (ed.connectFrom == null || ed.connectFrom < 0 || ed.connectFrom === i) {
        ed.connectFrom = ed.connectFrom === i ? -1 : i;
        select(ed.connectFrom);
        return;
      }
      snapshot();
      const from = ed.connectFrom;
      const a = ed.terrs[from], b = ed.terrs[i];
      if (a.neighbors.has(i)) { a.neighbors.delete(i); b.neighbors.delete(from); } else { a.neighbors.add(i); b.neighbors.add(from); }
      ed.sel = from;
      renderAll();
      return;
    }
    if (tool === 'label') {
      const i = territoryAt(p);
      const target = i >= 0 ? i : ed.sel;
      if (target < 0) return;
      snapshot();
      ed.sel = target;
      ed.terrs[target].label = [round1(p.x), round1(p.y)];
      renderAll();
      return;
    }
    // select tool
    const v = e.target.closest && e.target.closest('[data-v]');
    if (v && ed.sel >= 0) {
      const [ri, idx] = v.getAttribute('data-v').split(':').map(Number);
      const ring = ed.terrs[ed.sel].rings[ri];
      if (e.button === 2) { deleteVertex(ri, idx, e.altKey); return; }
      // vertices shared with other territories move together (keeps borders glued), Alt moves only this one
      const ox = ring[idx], oy = ring[idx + 1];
      const group = [];
      if (e.altKey) group.push([ed.sel, ri, idx]);
      else ed.terrs.forEach((tr, ti) => tr.rings.forEach((rg, rj) => { for (let k = 0; k < rg.length; k += 2) if (rg[k] === ox && rg[k + 1] === oy) group.push([ti, rj, k]); }));
      snapshot();
      drag = { kind: 'vertex', group, skip: new Set(group.map((g) => g.join(':'))) };
      svgEl.setPointerCapture(e.pointerId);
      return;
    }
    const lb = e.target.closest && e.target.closest('[data-label]');
    if (lb) {
      const i = parseInt(lb.getAttribute('data-label'), 10);
      ed.sel = i;
      snapshot();
      drag = { kind: 'label', i };
      svgEl.setPointerCapture(e.pointerId);
      renderAll();
      return;
    }
    const i = territoryAt(p);
    if (i !== ed.sel) select(i);
  });

  svgEl.addEventListener('pointermove', (e) => {
    const p = toMap(e);
    pointer = p;
    if (drag && drag.kind === 'pan') {
      view.x = drag.vx - (e.clientX - drag.sx) * drag.k;
      view.y = drag.vy - (e.clientY - drag.sy) * drag.k;
      applyView();
      return;
    }
    if (drag && drag.kind === 'vertex') {
      const q = snap(p, drag.skip);
      for (const [ti, ri, k] of drag.group) { const rg = ed.terrs[ti].rings[ri]; rg[k] = q.x; rg[k + 1] = q.y; }
      render();
      return;
    }
    if (drag && drag.kind === 'label') {
      ed.terrs[drag.i].label = [round1(p.x), round1(p.y)];
      render();
      return;
    }
    if (ed.drawing) {
      const q = snap(p);
      pointer = q;
      render();
    }
  });

  const endDrag = () => {
    if (drag && (drag.kind === 'vertex' || drag.kind === 'label')) renderAll();
    drag = null;
  };
  svgEl.addEventListener('pointerup', endDrag);
  svgEl.addEventListener('pointercancel', endDrag);

  svgEl.addEventListener('dblclick', (e) => {
    if (ed.tool !== 'select' || ed.sel < 0) return;
    // insert a vertex on the nearest edge of the selected territory
    const p = toMap(e);
    const tr = ed.terrs[ed.sel];
    let best = null;
    tr.rings.forEach((ring, ri) => {
      const n = ring.length / 2;
      for (let k = 0; k < n; k++) {
        const j = (k + 1) % n;
        const ax = ring[k * 2], ay = ring[k * 2 + 1], bx = ring[j * 2], by = ring[j * 2 + 1];
        const dx = bx - ax, dy = by - ay, l = dx * dx + dy * dy;
        const u = l ? Math.max(0, Math.min(1, ((p.x - ax) * dx + (p.y - ay) * dy) / l)) : 0;
        const x = ax + u * dx, y = ay + u * dy;
        const d = Math.hypot(p.x - x, p.y - y);
        if (!best || d < best.d) best = { d, ri, at: j === 0 ? n : j, x: round1(x), y: round1(y) };
      }
    });
    if (best && best.d < 12 * pxToMap()) {
      snapshot();
      tr.rings[best.ri].splice(best.at * 2, 0, best.x, best.y);
      renderAll();
    }
  });

  function deleteVertex(ri, idx, onlyThis) {
    const tr = ed.terrs[ed.sel];
    const ring = tr.rings[ri];
    if (ring.length <= 6) { $('ed-hint').textContent = t('editor.hint.minvertices'); return; }
    snapshot();
    const ox = ring[idx], oy = ring[idx + 1];
    const targets = onlyThis ? [[tr, ring, idx]] : [];
    if (!onlyThis) ed.terrs.forEach((x) => x.rings.forEach((rg) => { for (let k = 0; k < rg.length; k += 2) if (rg[k] === ox && rg[k + 1] === oy && rg.length > 6) targets.push([x, rg, k]); }));
    for (const [, rg, k] of targets) rg.splice(k, 2);
    renderAll();
  }

  // ---------------------------------------------------------------- keyboard

  document.addEventListener('keydown', (e) => {
    if ($('screen-editor').hidden || e.target.matches('input, select, textarea') || document.getElementById('modal-root').children.length) return;
    if (e.key === ' ') { spaceDown = true; e.preventDefault(); return; }
    if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'z') { e.preventDefault(); undo(); return; }
    if (e.key === 'Escape') { if (ed.drawing) cancelDrawing(); else select(-1); return; }
    if (e.key === 'Enter' && ed.drawing) { finishDrawing(); return; }
    if (e.key === 'Backspace' && ed.drawing) { e.preventDefault(); ed.drawing.pts.splice(-2, 2); if (!ed.drawing.pts.length) ed.drawing = null; render(); return; }
    const tools = { v: 'select', d: 'draw', p: 'part', c: 'connect', t: 'label' };
    const tool = tools[e.key.toLowerCase()];
    if (tool && !e.ctrlKey && !e.metaKey && !e.altKey) setTool(tool);
  });
  document.addEventListener('keyup', (e) => { if (e.key === ' ') spaceDown = false; });

  function setTool(tool) {
    if (ed.drawing && tool !== ed.tool) ed.drawing = null;
    if (tool === 'connect' && ed.tool !== 'connect') ed.connectFrom = -1;
    ed.tool = tool;
    renderAll();
  }

  // ---------------------------------------------------------------- panel inputs

  document.querySelectorAll('input[name=ed-tool]').forEach((r) => r.addEventListener('change', () => setTool(r.value)));
  $('ed-name').addEventListener('change', () => { snapshot(); ed.name = $('ed-name').value.trim(); renderPanel(); });
  const sizeChange = () => {
    snapshot();
    ed.width = Math.max(100, Math.min(4000, parseInt($('ed-w').value, 10) || 800));
    ed.height = Math.max(100, Math.min(4000, parseInt($('ed-h').value, 10) || 500));
    fit();
    renderAll();
  };
  $('ed-w').addEventListener('change', sizeChange);
  $('ed-h').addEventListener('change', sizeChange);
  $('ed-bg').addEventListener('change', () => {
    const f = $('ed-bg').files[0];
    if (bgUrl) URL.revokeObjectURL(bgUrl);
    bgUrl = f ? URL.createObjectURL(f) : null;
    render();
  });
  $('ed-bg-op').addEventListener('input', render);
  $('ed-add-cont').addEventListener('click', () => {
    snapshot();
    const n = ed.continents.length;
    ed.continents.push({ name: t('editor.continent') + ' ' + (n + 1), bonus: 2, color: PALETTE[n % PALETTE.length] });
    renderAll();
  });
  $('ed-fit').addEventListener('click', () => { fit(); render(); });
  $('ed-new').addEventListener('click', async () => {
    if (ed.dirty && !(await confirmDialog(t('editor.new'), t('editor.discard'), t('editor.new'), t('newgame.cancel')))) return;
    start(blank());
  });
  $('ed-exit').addEventListener('click', async () => {
    if (ed.dirty && !(await confirmDialog(t('editor.exit'), t('editor.discard'), t('editor.exit'), t('newgame.cancel')))) return;
    show('screen-menu');
  });
  $('ed-open').addEventListener('click', openDialogMaps);
  $('ed-save').addEventListener('click', save);

  async function save() {
    const data = toData();
    const { errors } = validateMap(data);
    if (errors.length) { alertDialog(t('editor.save'), t('editor.fixfirst'), t('about.okbutton')); return; }
    const r = await fetch('/api/maps', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data) });
    const body = await r.json().catch(() => ({}));
    if (!r.ok) {
      const msg = (body.errors || []).map((x) => t(x.key, ...(x.args || []))).join('\n') || ('HTTP ' + r.status);
      alertDialog(t('editor.save'), msg, t('about.okbutton'));
      return;
    }
    ed.file = body.file;
    ed.dirty = false;
    if (onSaved) onSaved(body.file);
    alertDialog(t('editor.save'), t('editor.saved', body.file), t('about.okbutton'));
  }

  // ---------------------------------------------------------------- open / duplicate

  async function openDialogMaps() {
    const maps = await (await fetch('/api/maps')).json();
    const list = el('ul', { class: 'list-saves' });
    let dlg;
    for (const m of maps) {
      const actions = [];
      if (m.custom) {
        actions.push(el('button', { class: 'btn ghost', type: 'button', onclick: async () => {
          if (!(await confirmDialog(t('editor.delete'), t('editor.deletemap', m.name || m.file), t('editor.delete'), t('newgame.cancel')))) return;
          await fetch('/api/maps/' + encodeURIComponent(m.file), { method: 'DELETE' });
          dlg.close();
          if (onSaved) onSaved(null);
          openDialogMaps();
        } }, t('editor.delete')));
        actions.push(el('button', { class: 'btn primary', type: 'button', onclick: () => { dlg.close(); loadMap(m.file, true); } }, t('editor.edit')));
      } else {
        actions.push(el('button', { class: 'btn', type: 'button', onclick: () => { dlg.close(); loadMap(m.file, false); } }, t('editor.duplicate')));
      }
      list.append(el('li', {}, el('span', {}, m.name || m.file, ' ', el('span', { class: 'when' }, m.custom ? t('editor.yours') : m.file)), el('span', {}, ...actions)));
    }
    dlg = openDialog({ title: t('editor.open'), body: el('div', {}, el('p', { class: 'muted' }, t('editor.openhelp')), list), actions: [{ label: t('newgame.cancel'), ghost: true, onClick: (d) => d.close() }] });
  }

  /** loads a map (its .map file + polygons) into the editor */
  async function loadMap(file, edit) {
    if (ed.dirty && !(await confirmDialog(t('editor.open'), t('editor.discard'), t('editor.open'), t('newgame.cancel')))) return;
    const [mapText, vector] = await Promise.all([
      fetch('/maps/' + encodeURIComponent(file)).then((r) => r.arrayBuffer()).then(decodeMap),
      fetch('/vector/' + encodeURIComponent(file) + '.json').then((r) => r.json()),
    ]);
    const map = parseMapFile(mapText);
    const nb = map.countries.map(() => new Set());
    for (const line of map.borders) {
      const a = line[0] - 1;
      for (const b of line.slice(1)) { if (nb[a] && nb[b - 1] && a !== b - 1) { nb[a].add(b - 1); nb[b - 1].add(a); } }
    }
    const e = blank();
    e.name = edit ? (map.props.name || file) : t('editor.copyof', map.props.name || file);
    e.file = edit ? file : null;
    e.width = vector.width;
    e.height = vector.height;
    e.continents = map.continents.map((c, i) => {
      const col = ColorUtil.getColor(c.color);
      return { name: c.id.replace(/_/g, ' '), bonus: c.bonus, color: col ? ColorUtil.getHexForColor(col) : PALETTE[i % PALETTE.length] };
    });
    e.terrs = map.countries.map((c, i) => ({
      name: c.id.replace(/_/g, ' '),
      continent: Math.max(0, c.continent - 1),
      rings: ((vector.countries[i] || {}).rings || []).map((r) => r.slice()),
      label: [c.x, c.y],
      neighbors: nb[i],
    }));
    start(e);
  }

  function decodeMap(buf) {
    const b = new Uint8Array(buf);
    if (b[0] === 0xef && b[1] === 0xbb && b[2] === 0xbf) return new TextDecoder('utf-8').decode(b.subarray(3));
    let s = '';
    for (let i = 0; i < b.length; i += 8192) s += String.fromCharCode.apply(null, b.subarray(i, i + 8192));
    return s;
  }

  function start(model) {
    ed = model;
    historyStack = [];
    drag = null;
    show('screen-editor');
    fit();
    renderAll();
  }

  return {
    openNew() { start(blank()); },
    openFile(file, edit) { show('screen-editor'); if (!ed) ed = blank(); ed.dirty = false; loadMap(file, edit); },
    retranslate() { if (ed && !$('screen-editor').hidden) renderAll(); },
  };
}
