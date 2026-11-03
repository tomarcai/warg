// warg - browser user interface for a local game (port of the Domination "flash" GUI). GPLv3.

import { Risk } from '/engine/Risk.js';
import * as RG from '/engine/RiskGame.js';
import { config } from '/engine/RiskGame.js';
import { loadBundle, Bundle } from '/engine/i18n.js';
import * as ColorUtil from '/engine/colors.js';
import { PLAYER_HUMAN, StatType, STAT_TYPES } from '/engine/model.js';
import { browserIo, listMaps, listSaves, deleteSave } from './browserIo.js';
import { MapView, NO_COUNTRY } from './mapView.js';
import { openDialog, el, alertDialog, confirmDialog } from './dialogs.js';
import { sound } from './sound.js';
import { initEditor } from './editor.js';

// ------------------------------------------------------------------ preferences

const prefs = {
  get(k, d) {
    try { const v = localStorage.getItem('warg.' + k); return v == null ? d : JSON.parse(v); } catch (e) { return d; }
  },
  set(k, v) {
    try { localStorage.setItem('warg.' + k, JSON.stringify(v)); } catch (e) { /* ignore */ }
  },
};

let lang = prefs.get('lang', (navigator.language || 'es').toLowerCase().startsWith('en') ? 'en' : 'es');

const risk = new Risk(browserIo, {
  language: lang,
  aiWait: prefs.get('aiWait', 500),
  showDice: prefs.get('showDice', true),
});
window.warg = risk; // handy from the browser console

let T = new Bundle([], lang);
const t = (key, ...args) => T.t(key, ...args);

async function setLanguage(l) {
  lang = l;
  prefs.set('lang', l);
  document.documentElement.lang = l;
  const read = (n) => browserIo.readResource(n);
  // ordering: language specific (Risk, Warg) first, then the English base files
  const names = [`Risk_${l}`, `Warg_${l}`, 'Risk', 'Warg'];
  const maps = [];
  for (const n of names) {
    const b = await loadBundle(async (f) => (f === n + '.properties' ? read(f) : null), n, null);
    if (b) maps.push(...b.maps);
  }
  T = new Bundle(maps, l);
  risk.language = l;
  risk.resb = T;
  document.querySelectorAll('[data-t]').forEach((e) => { e.textContent = t(e.dataset.t); });
  document.querySelectorAll('[data-t-title]').forEach((e) => { e.title = t(e.dataset.tTitle); });
  document.getElementById('cmd').placeholder = t('warg.console.hint');
  document.getElementById('lang-select').value = l;
  if (typeof editor !== 'undefined') editor.retranslate();
}

// ------------------------------------------------------------------ screens

const $ = (id) => document.getElementById(id);
function show(screen) {
  for (const s of ['screen-menu', 'screen-setup', 'screen-game', 'screen-editor']) $(s).hidden = s !== screen;
  if (screen === 'screen-game') mapView.resize();
}

const mapView = new MapView($('map-canvas'), risk);
const editor = initEditor({
  t: (...a) => t(...a), show, openDialog, el, alertDialog, confirmDialog,
  onSaved: () => { setup.maps = []; },
});
window.wargMap = mapView;

// ------------------------------------------------------------------ state of the game screen

const ui = {
  gameState: 0, // what we asked the human for, 0 when input is blocked (like GameFrame)
  note: '',
  help: '',
  quickPlace: new Map(),
  battle: null,
  move: null,
  stats: null,
  cards: null,
  menuOpen: false,
  repaintQueued: false,
};
window.wargUi = ui;

function scheduleRepaint() {
  if (ui.repaintQueued) return;
  ui.repaintQueued = true;
  requestAnimationFrame(() => {
    ui.repaintQueued = false;
    const g = risk.getGame();
    if (!g || !g.getCountries()) return;
    mapView.repaintCountries();
    updateBar();
    if (ui.stats) ui.stats.redraw();
  });
}

function colorCss(c) {
  return ColorUtil.toCss(c);
}

function updateBar() {
  const g = risk.getGame();
  const chits = $('turn-chits');
  chits.replaceChildren();
  if (g && g.getState() !== RG.STATE_NEW_GAME) {
    const cols = risk.getPlayerColors();
    cols.forEach((c, i) => {
      const p = g.getPlayers().find((x) => x.getColor() === c);
      chits.append(el('span', { class: 'chit' + (i === 0 ? ' current' : ''), style: { background: colorCss(c) }, title: p ? p.getName() : '' }));
    });
  }
  $('status-note').textContent = ui.note;
  $('status-help').textContent = ui.help;
  const canUseCards = g && g.getState() !== RG.STATE_NEW_GAME && (risk.getSingleLocalHumanPlayer() != null || ui.gameState !== 0);
  $('t-cards').disabled = !canUseCards;
  $('t-mission').disabled = !canUseCards;
  $('t-cards').classList.toggle('attention', ui.gameState === RG.STATE_TRADE_CARDS);
  $('t-undo').disabled = !(ui.gameState !== 0 && ui.gameState !== RG.STATE_DEFEND_YOURSELF && risk.canUndo());
  $('t-stats').disabled = !g || g.getState() === RG.STATE_NEW_GAME;
}

function setGo(text) {
  const go = $('go');
  if (text) {
    go.disabled = false;
    go.textContent = text;
  } else {
    go.disabled = true;
    go.textContent = '';
  }
}

function armiesLeftText() {
  return t('game.note.armiesleft', risk.getGame().getCurrentPlayer().getExtraArmies());
}

/** GameFrame.needInput */
function needInput(s) {
  const game = risk.getGame();
  if (!game) return;
  if (game.getState() === RG.STATE_NEW_GAME) { setupNeedInput(); return; }
  ui.gameState = s;
  if (s !== RG.STATE_BATTLE_WON && ui.move && !ui.move.tactical) closeMove();
  let goText = null;
  switch (s) {
    case RG.STATE_PLACE_ARMIES: {
      const me = game.getCurrentPlayer();
      if (!game.getSetupDone() && ui.quickPlace.has(me)) {
        const country = ui.quickPlace.get(me);
        mapView.c1 = country;
        scheduleRepaint();
        setTimeout(() => { mapView.c1 = NO_COUNTRY; go('placearmies ' + country + ' 1'); }, Math.min(risk.ai.wait, 300));
        return;
      }
      if (!game.NoEmptyCountries()) goText = t('game.button.go.autoplace');
      ui.note = armiesLeftText();
      break;
    }
    case RG.STATE_TRADE_CARDS:
      mapView.c1 = NO_COUNTRY; mapView.c2 = NO_COUNTRY;
      if (game.canEndTrade()) goText = t('game.button.go.endtrade');
      ui.note = armiesLeftText();
      break;
    case RG.STATE_ATTACKING:
      mapView.c1 = NO_COUNTRY; mapView.c2 = NO_COUNTRY;
      ui.note = t('game.note.selectattacker');
      goText = t('game.button.go.endattack');
      break;
    case RG.STATE_FORTIFYING:
      ui.note = t('game.note.selectsource');
      goText = t('game.button.go.nomove');
      break;
    case RG.STATE_END_TURN:
      goText = t('game.button.go.endgo');
      break;
    case RG.STATE_GAME_OVER:
      goText = game.canContinue() ? t('game.button.go.continue') : t('game.button.go.closegame');
      break;
    case RG.STATE_SELECT_CAPITAL:
      ui.note = t('game.note.happyok');
      goText = t('game.button.go.ok');
      break;
    case RG.STATE_BATTLE_WON:
      openMove(game.getMustMove(), game.getAttacker().getColor(), game.getDefender().getColor(), false);
      break;
    case RG.STATE_ROLLING:
      if (ui.battle) ui.battle.needInput(game.getNoAttackDice(), true);
      break;
    case RG.STATE_DEFEND_YOURSELF:
      if (ui.battle) ui.battle.needInput(game.getNoDefendDice(), false);
      break;
    default:
      break;
  }
  setGo(goText);
  scheduleRepaint();
}

function noInput() {
  if (ui.move) closeMove();
  ui.gameState = 0;
  ui.note = '';
  setGo(null);
  updateBar();
}

/** sends a command for the human player (GameFrame.go) */
function go(command) {
  blockInput();
  risk.parser(command);
}

function blockInput() {
  mapView.cc = NO_COUNTRY;
  $('hover-name').hidden = true;
  if (ui.gameState === RG.STATE_ROLLING || ui.gameState === RG.STATE_DEFEND_YOURSELF) {
    if (ui.battle) ui.battle.blockInput();
  }
  if (ui.gameState === RG.STATE_BATTLE_WON || ui.gameState === RG.STATE_FORTIFYING) closeMove();
  const game = risk.getGame();
  if (ui.gameState !== RG.STATE_PLACE_ARMIES || !(game && game.getSetupDone())) noInput();
}

/** the GO button (GameFrame.goOn) */
function goOn() {
  const game = risk.getGame();
  const s = ui.gameState;
  if (s === RG.STATE_TRADE_CARDS) go('endtrade');
  else if (s === RG.STATE_PLACE_ARMIES) {
    sound.play('place_army');
    const quick = mapView.c1;
    if (quick !== NO_COUNTRY) {
      ui.quickPlace.set(game.getCurrentPlayer(), quick);
      mapView.c1 = NO_COUNTRY;
      go('placearmies ' + quick + ' 1');
    } else go('autoplace');
  } else if (s === RG.STATE_ATTACKING) { mapView.c1 = NO_COUNTRY; go('endattack'); }
  else if (s === RG.STATE_FORTIFYING) { mapView.c1 = NO_COUNTRY; go('nomove'); }
  else if (s === RG.STATE_END_TURN) go('endgo');
  else if (s === RG.STATE_GAME_OVER) {
    if (game && game.canContinue()) go('continue');
    else closeGameAndReturn(false);
  } else if (s === RG.STATE_SELECT_CAPITAL) {
    const c = mapView.c1;
    if (c === NO_COUNTRY) return;
    mapView.c1 = NO_COUNTRY;
    go('capital ' + c);
  }
}

// ------------------------------------------------------------------ map mouse (MapMouseListener)

function onMapRelease(id, secondary) {
  const game = risk.getGame();
  const s = ui.gameState;
  if (!game || id === NO_COUNTRY || id > game.getNoCountries()) return null;
  const own = risk.isOwnedCurrentPlayerInt(id);
  if (s === RG.STATE_PLACE_ARMIES) {
    if (own && (game.NoEmptyCountries() || risk.hasArmiesInt(id) === 0)) {
      if (!secondary) {
        mapView.c1 = NO_COUNTRY;
        sound.play('place_army');
        go('placearmies ' + id + ' 1');
      } else if (game.getSetupDone()) {
        sound.play('place_armies');
        go('placearmies ' + id + ' 10');
      } else if (game.NoEmptyCountries()) {
        mapView.c1 = mapView.c1 === id ? NO_COUNTRY : id;
        setGo(mapView.c1 !== NO_COUNTRY ? t('newgame.autoplace') : null);
        scheduleRepaint();
      }
    }
  } else if (s === RG.STATE_ATTACKING) {
    if (id === mapView.c1) {
      mapView.c1 = NO_COUNTRY; mapView.c2 = NO_COUNTRY;
      ui.note = t('game.note.selectattacker');
    } else if (own && risk.hasArmiesInt(id) > 1) {
      mapView.c1 = id; mapView.c2 = NO_COUNTRY;
      ui.note = t('game.note.selectdefender');
    } else if (mapView.c1 !== NO_COUNTRY && !own && risk.canAttack(mapView.c1, id)) {
      mapView.c2 = id;
      sound.play('attack');
      const from = mapView.c1;
      go('attack ' + from + ' ' + id);
      ui.note = t('game.note.selectattacker');
    }
  } else if (s === RG.STATE_FORTIFYING) {
    if (mapView.c1 !== NO_COUNTRY && mapView.c2 === NO_COUNTRY && id === mapView.c1) {
      mapView.c1 = NO_COUNTRY; mapView.c2 = NO_COUNTRY;
      ui.note = t('game.note.selectsource');
    } else if (own && risk.hasArmiesInt(id) > 1 && mapView.c1 === NO_COUNTRY) {
      mapView.c1 = id; mapView.c2 = NO_COUNTRY;
      ui.note = t('game.note.selectdestination');
    } else if (mapView.c1 !== NO_COUNTRY && own && mapView.c2 !== id && risk.canAttack(mapView.c1, id)) {
      mapView.c2 = id;
      ui.note = '';
      openMove(1, mapView.c1, id, true);
    } else if (own && mapView.c1 !== NO_COUNTRY && mapView.c2 !== NO_COUNTRY && risk.hasArmiesInt(id) > 1) {
      mapView.c1 = id; mapView.c2 = NO_COUNTRY;
      ui.note = t('game.note.selectdestination');
    }
  } else if (s === RG.STATE_SELECT_CAPITAL) {
    if (own) mapView.c1 = id;
  }
  updateBar();
  scheduleRepaint();
  return null;
}

function hoverCountry(id) {
  const game = risk.getGame();
  const s = ui.gameState;
  let cc = NO_COUNTRY;
  if (game && id !== NO_COUNTRY && id <= game.getNoCountries()) {
    const own = risk.isOwnedCurrentPlayerInt(id);
    if (s === RG.STATE_PLACE_ARMIES) {
      if (own && (game.NoEmptyCountries() || risk.hasArmiesInt(id) === 0)) cc = id;
    } else if (s === RG.STATE_ATTACKING) {
      if (own && risk.hasArmiesInt(id) > 1) cc = id;
      else if (!own && mapView.c1 !== NO_COUNTRY && risk.canAttack(mapView.c1, id)) cc = id;
    } else if (s === RG.STATE_FORTIFYING) {
      if (own && risk.hasArmiesInt(id) > 1 && mapView.c1 === NO_COUNTRY) cc = id;
      else if (own && mapView.c1 !== NO_COUNTRY && risk.canAttack(mapView.c1, id)) cc = id;
      else if (own && mapView.c1 !== NO_COUNTRY && mapView.c2 !== NO_COUNTRY && risk.hasArmiesInt(id) > 1) cc = id;
    } else if (s === RG.STATE_SELECT_CAPITAL) {
      if (own) cc = id;
    }
  }
  if (mapView.cc !== cc) {
    mapView.cc = cc;
    mapView.paint();
  }
  // the country name is always shown, like the original tooltip area
  const hn = $('hover-name');
  if (game && game.getCountries() && id !== NO_COUNTRY && id <= game.getNoCountries()) {
    const c = game.getCountryInt(id);
    hn.textContent = t('picturepanel.country') + ' ' + c.getName() + (c.getOwner() ? ' (' + c.getOwner().getName() + ')' : '');
    hn.hidden = false;
  } else hn.hidden = true;
}

function setupMapEvents() {
  const canvas = $('map-canvas');
  canvas.addEventListener('mousemove', (e) => hoverCountry(mapView.countryAt(e.clientX, e.clientY)));
  canvas.addEventListener('mouseleave', () => { $('hover-name').hidden = true; if (mapView.cc !== NO_COUNTRY) { mapView.cc = NO_COUNTRY; mapView.paint(); } });
  canvas.addEventListener('contextmenu', (e) => e.preventDefault());
  canvas.addEventListener('mouseup', (e) => {
    const game = risk.getGame();
    if (game && game.getState() === RG.STATE_GAME_OVER) { mapView.toggleAni(); return; }
    if (e.button !== 0 && e.button !== 2) return;
    onMapRelease(mapView.countryAt(e.clientX, e.clientY), e.button === 2 || e.shiftKey);
  });
}

// ------------------------------------------------------------------ battle dialog (BattleDialog)

function openBattle(c1num, c2num) {
  closeBattle();
  const game = risk.getGame();
  const country1 = game.getCountryInt(c1num);
  const country2 = game.getCountryInt(c2num);
  const b = {
    noda: 0, nodd: 0, max: 0, canRetreat: false, att: null, def: null, spinA: false, spinD: false, annihilate: false,
  };
  const side = (country, label) => {
    const img = mapView.countryImage(country.getColor());
    const owner = country.getOwner();
    const badge = el('span', { class: 'army-badge' });
    const node = el('div', { class: 'side' },
      el('div', { class: 'muted' }, label),
      img || el('div'),
      el('div', { class: 'cname' }, country.getName()),
      el('div', { class: 'owner' }, owner.getName()),
      badge);
    return { node, badge, country };
  };
  const s1 = side(country1, t('warg.battle.attacker'));
  const s2 = side(country2, t('warg.battle.defender'));
  const diceA = el('div', { class: 'dice-row' });
  const diceD = el('div', { class: 'dice-row' });
  s1.node.append(diceA);
  s2.node.append(diceD);
  const msg = el('div', { class: 'battle-msg' });
  const body = el('div', {}, el('div', { class: 'battle' }, s1.node, el('div', { class: 'vs' }, 'vs'), s2.node), msg);
  const annihilateBox = el('input', { type: 'checkbox' });
  const annihilateLabel = el('label', { class: 'toggle' }, annihilateBox, t('battle.annihilate'));
  annihilateBox.addEventListener('change', () => {
    b.annihilate = annihilateBox.checked;
    if (b.annihilate && b.canRetreat && b.max > 0) doRoll();
  });
  const doRoll = () => {
    sound.play('dice_roll');
    go('roll ' + (b.canRetreat ? b.noda : b.nodd));
  };
  const dlg = openDialog({
    title: t('battle.title'),
    body,
    passive: true,
    closeOnEscape: false,
    actions: [],
  });
  b.dlg = dlg;

  const renderDice = () => {
    const make = (row, isAtt, results, count, spin) => {
      row.replaceChildren();
      if (results) {
        results.forEach((v, i) => {
          const other = isAtt ? b.def : b.att;
          let lost = false;
          if (other && i < other.length) lost = isAtt ? other[i] >= v : other[i] > v;
          row.append(el('span', { class: 'die ' + (isAtt ? 'att' : 'def') + (lost ? ' lost' : '') }, String(v + 1)));
        });
      } else if (spin) {
        for (let i = 0; i < count; i++) row.append(el('span', { class: 'die spin ' + (isAtt ? 'att' : 'def') }, '?'));
      } else if ((isAtt && b.canRetreat) || (!isAtt && !b.canRetreat && b.max > 0)) {
        // choose how many dice to use
        for (let i = 1; i <= b.max; i++) {
          const n = i;
          const on = n <= (isAtt ? b.noda : b.nodd);
          row.append(el('button', {
            class: 'die pick' + (on ? ' on' : ''),
            type: 'button',
            'aria-label': n + '',
            onclick: () => { if (isAtt) b.noda = n; else b.nodd = n; renderDice(); },
          }, on ? '●' : ''));
        }
      }
    };
    make(diceA, true, b.att, b.noda, b.spinA);
    make(diceD, false, b.def, b.nodd, b.spinD);
    s1.badge.textContent = country1.getArmies();
    s2.badge.textContent = country2.getArmies();
    for (const [s, c] of [[s1, country1], [s2, country2]]) {
      const col = c.getOwner().getColor();
      s.badge.style.background = colorCss(col);
      s.badge.style.color = colorCss(ColorUtil.getTextColorFor(col));
    }
  };

  b.renderActions = () => {
    const list = [];
    if (b.canRetreat) {
      list.push({ node: annihilateLabel });
      list.push({ label: t('battle.retreat'), ghost: true, onClick: () => { sound.play('battle_retreat'); go('retreat'); } });
    }
    list.push({ label: t('battle.roll'), primary: true, id: 'roll', disabled: b.max === 0, onClick: doRoll });
    dlg.setActions(list);
  };
  b.needInput = (n, canRetreat) => {
    b.max = n;
    b.canRetreat = canRetreat;
    if (canRetreat) { if (b.noda === 0 || b.noda > n) b.noda = n; } else if (b.nodd === 0 || b.nodd > n) b.nodd = n;
    b.att = null; b.def = null;
    dlg.el.querySelector('h2').textContent = t(canRetreat ? 'battle.select.attack' : 'battle.select.defend');
    msg.textContent = '';
    renderDice();
    b.renderActions();
    if (canRetreat && b.annihilate) setTimeout(doRoll, 0);
  };
  b.blockInput = () => {
    b.canRetreat = false;
    b.max = 0;
    dlg.el.querySelector('h2').textContent = t('battle.title');
    b.renderActions();
  };
  b.setNODAttacker = (n) => { b.att = null; b.def = null; b.noda = n; b.spinA = true; renderDice(); };
  b.setNODDefender = (n) => { b.nodd = n; b.spinD = true; renderDice(); };
  b.showDiceResults = (att, def, weAreAttacker, result) => {
    b.att = att; b.def = def; b.spinA = false; b.spinD = false;
    renderDice();
    let attWins = 0;
    for (let i = 0; i < Math.min(att.length, def.length); i++) if (att[i] > def[i]) attWins++;
    const losses = Math.min(att.length, def.length) - attWins;
    if (result >= 1) { msg.textContent = t('warg.battle.won'); sound.play(weAreAttacker ? 'battle_win' : 'battle_defense_defeat'); }
    else if (result === -1) { msg.textContent = t('warg.battle.lost'); sound.play(weAreAttacker ? 'battle_defeat' : 'battle_defense_win'); }
    else sound.play(attWins > losses ? (weAreAttacker ? 'dice_win' : 'dice_lose') : attWins === losses ? 'dice_draw' : (weAreAttacker ? 'dice_lose' : 'dice_win'));
  };
  b.blockInput();
  renderDice();
  ui.battle = b;
}

function closeBattle() {
  if (ui.battle) {
    ui.battle.dlg.close();
    ui.battle = null;
  }
}

// ------------------------------------------------------------------ move dialog (MoveDialog)

function openMove(min, c1num, c2num, tactical) {
  closeMove();
  const game = risk.getGame();
  const country1 = game.getCountryInt(c1num);
  const country2 = game.getCountryInt(c2num);
  const csrc = country1.getArmies();
  const cdes = country2.getArmies();
  const max = csrc - 1;
  let move = min;
  const range = el('input', { type: 'range', min, max, value: min, step: 1, 'aria-label': t('move.numberofarmies') });
  const left = el('span');
  const right = el('span');
  const amount = el('div', { class: 'battle-msg' });
  const render = () => {
    left.textContent = String(csrc - move);
    right.textContent = String(cdes + move);
    amount.textContent = t('move.move') + ': ' + move;
    range.value = move;
  };
  range.addEventListener('input', () => { move = parseInt(range.value, 10); render(); });
  const set = (v) => { move = Math.max(min, Math.min(max, v)); render(); };
  const body = el('div', { class: 'move' },
    el('div', { class: 'from-to' }, el('span', {}, country1.getName()), el('span', {}, country2.getName())),
    el('div', { class: 'counts' }, left, right),
    range,
    el('div', { class: 'stepper' },
      el('button', { class: 'btn', type: 'button', onclick: () => set(min) }, t('move.min')),
      el('button', { class: 'btn', type: 'button', onclick: () => set(move - 1) }, t('move.minus')),
      el('button', { class: 'btn', type: 'button', onclick: () => set(move + 1) }, t('move.plus')),
      el('button', { class: 'btn', type: 'button', onclick: () => set(max) }, t('move.max'))),
    amount);
  const send = (n) => {
    if (tactical) { sound.play('move_tactical'); go('movearmies ' + c1num + ' ' + c2num + ' ' + n); }
    else { sound.play('move_armies'); go('move ' + n); }
  };
  const actions = [];
  if (tactical) actions.push({ label: t('move.cancel'), ghost: true, onClick: () => closeMove(true) });
  actions.push({ label: t('move.moveall'), onClick: () => send(csrc - 1) });
  actions.push({ label: t('move.move'), primary: true, onClick: () => send(move) });
  const dlg = openDialog({
    title: t(tactical ? 'move.title.tactical' : 'move.title.captured'),
    body,
    actions,
    closeOnEscape: tactical,
    onClose: () => {
      if (ui.move && ui.move.dlg === dlg) ui.move = null;
      if (tactical) {
        mapView.c1 = NO_COUNTRY; mapView.c2 = NO_COUNTRY;
        if (ui.gameState === RG.STATE_FORTIFYING) ui.note = t('game.note.selectsource');
        scheduleRepaint();
      }
    },
  });
  ui.move = { dlg, tactical };
  render();
}

function closeMove() {
  if (ui.move) {
    const m = ui.move;
    ui.move = null;
    m.dlg.close();
  }
}

// ------------------------------------------------------------------ cards (CardsDialog)

function cardSymbol(name) {
  // sprites from the original cards.jpg (flash GUI), scaled to fit a 70px box
  const pos = { wildcard: [630, 0, 50, 145], Cavalry: [630, 145, 50, 70], Infantry: [630, 215, 50, 70], Cannon: [630, 285, 70, 50] }[name];
  const [x, y, w, h] = pos;
  const k = Math.min(1, 70 / h);
  return el('span', { class: 'csym', style: { height: '72px' } }, el('span', {
    style: {
      display: 'inline-block', width: w + 'px', height: h + 'px',
      background: `url(/ui/cards.jpg) -${x}px -${y}px`, transform: `scale(${k})`, transformOrigin: 'center',
      margin: `${-(h - h * k) / 2}px ${-(w - w * k) / 2}px`,
    },
    'aria-hidden': 'true',
  }));
}

function showCards() {
  const game = risk.getGame();
  let human = risk.getSingleLocalHumanPlayer();
  const current = game.getCurrentPlayer();
  if (human == null) human = current;
  const canTrade = human === current && ui.gameState === RG.STATE_TRADE_CARDS;
  const selected = [];
  const grid = el('div', { class: 'cards' });
  const info = el('p', { class: 'muted' });
  const modeText = () => {
    if (game.getCardMode() === RG.CARD_FIXED_SET) return t('cards.fixed');
    if (game.getCardMode() === RG.CARD_ITALIANLIKE_SET) return t('cards.italianlike');
    return t('cards.nexttrade', game.getNewCardState());
  };
  let dlg;
  const render = () => {
    grid.replaceChildren();
    const cards = human.getCards();
    if (cards.length === 0) grid.append(el('p', {}, t('warg.cards.none')));
    cards.forEach((card) => {
      const idx = selected.indexOf(card);
      const country = card.getCountry();
      const owned = country && country.getOwner() === human;
      const node = el('div', {
        class: 'card' + (idx >= 0 ? ' sel' : '') + (owned ? ' owned' : '') + (canTrade ? '' : ' disabled'),
        role: canTrade ? 'button' : null,
        tabindex: canTrade ? '0' : null,
        onclick: () => {
          if (!canTrade) return;
          if (idx >= 0) selected.splice(idx, 1);
          else if (selected.length < 3) selected.push(card);
          render();
        },
      },
      el('span', { class: 'ctype' }, t('warg.card.' + card.getName()), idx >= 0 ? el('span', { class: 'order' }, ' ' + (idx + 1)) : null),
      cardSymbol(card.getName()),
      el('span', { class: 'cname' }, country ? country.getName() : ''));
      grid.append(node);
    });
    info.textContent = canTrade ? t('warg.cards.pick', modeText()) : modeText();
    const names = selected.map((c) => (c.getCountry() ? String(c.getCountry().getColor()) : 'wildcard'));
    const valid = canTrade && selected.length === 3 && risk.canTrade(names[0], names[1], names[2]);
    if (dlg) {
      dlg.setActions([
        { label: t('cards.done'), ghost: !canTrade, onClick: (d) => d.close() },
        ...(canTrade ? [{
          label: t('cards.trade'), primary: true, disabled: !valid,
          onClick: (d) => { sound.play('cards_trade'); d.close(); risk.parser('trade ' + names.join(' ')); },
        }] : []),
      ]);
    }
  };
  dlg = openDialog({
    title: t('cards.yourcards') + ' (' + human.getName() + ')',
    body: el('div', {}, info, canTrade ? el('p', { class: 'muted' }, t('cards.note')) : null, grid),
    wide: true,
    onClose: () => { ui.cards = null; },
  });
  ui.cards = dlg;
  render();
}

// ------------------------------------------------------------------ other dialogs

function showMission() {
  openDialog({
    title: t('game.button.mission'),
    body: el('p', { class: 'mission-text' }, risk.getHumanPlayerMission()),
    actions: [{ label: t('about.okbutton'), primary: true, onClick: (d) => d.close() }],
  });
}

const SHOWN_STATS = [StatType.COUNTRIES, StatType.ARMIES, StatType.KILLS, StatType.CASUALTIES, StatType.REINFORCEMENTS,
  StatType.CONTINENTS, StatType.CONNECTED_EMPIRE, StatType.ATTACKS, StatType.COUNTRIES_WON, StatType.COUNTRIES_LOST,
  StatType.CARDS, StatType.DICE];

function showStats() {
  if (ui.stats) { ui.stats.dlg.close(); return; }
  let current = prefs.get('statType', StatType.COUNTRIES);
  const hidden = new Set();
  const canvas = el('canvas', { id: 'stats-canvas' });
  const tabs = el('div', { class: 'stats-tabs' });
  const legend = el('div', { class: 'legend' });
  const redraw = () => {
    tabs.replaceChildren(...SHOWN_STATS.map((s) => el('button', {
      type: 'button', 'aria-pressed': String(s === current),
      onclick: () => { current = s; prefs.set('statType', s); redraw(); },
    }, t('swing.toolbar.' + STAT_TYPES[s].key))));
    const game = risk.getGame();
    if (!game) return;
    const players = game.getPlayersStats();
    legend.replaceChildren(...players.map((p) => {
      const cb = el('input', { type: 'checkbox', checked: !hidden.has(p) });
      cb.addEventListener('change', () => { if (cb.checked) hidden.delete(p); else hidden.add(p); redraw(); });
      return el('label', {}, cb, el('span', { class: 'chip', style: { background: colorCss(p.getColor()) } }), p.getName());
    }));
    drawGraph(canvas, players, current, hidden);
  };
  const dlg = openDialog({
    title: t('swing.tab.statistics'),
    body: el('div', {}, tabs, canvas, legend),
    wide: true,
    actions: [{ label: t('warg.close'), primary: true, onClick: (d) => d.close() }],
    onClose: () => { ui.stats = null; },
  });
  ui.stats = { dlg, redraw };
  requestAnimationFrame(redraw);
}

/** StatsPanel.repaintStats */
function drawGraph(canvas, players, type, hidden) {
  const dpr = window.devicePixelRatio || 1;
  const W = canvas.clientWidth, H = canvas.clientHeight;
  canvas.width = W * dpr; canvas.height = H * dpr;
  const g = canvas.getContext('2d');
  g.setTransform(dpr, 0, 0, dpr, 0, 0);
  g.fillStyle = '#000'; g.fillRect(0, 0, W, H);
  g.font = '12px Arial, sans-serif';
  const summable = STAT_TYPES[type].summable;
  let maxValue = 0, maxTurns = 0;
  for (const p of players) {
    const s = p.getStatisticsFor(type);
    maxTurns = Math.max(maxTurns, s.length);
    let sum = 0;
    for (const v of s) {
      if (summable) sum += v;
      else if (v > maxValue) maxValue = Math.trunc(v);
    }
    if (sum > maxValue) maxValue = Math.trunc(sum);
  }
  maxValue++; maxTurns++;
  const xOff = 35, yOff = 25;
  const gx = (W - xOff - 20) / maxTurns, gy = (H - yOff - 20) / maxValue;
  const zx = xOff, zy = H - yOff;
  const bob = Math.round(15 / gy);
  for (let i = 0; i <= maxValue; i++) {
    if (i === maxValue || bob === 0 || i % bob === 0) {
      g.strokeStyle = 'gray'; g.beginPath(); g.moveTo(zx, zy - i * gy); g.lineTo(maxTurns * gx + zx, zy - i * gy); g.stroke();
      g.fillStyle = 'white'; const lw = g.measureText(String(i)).width; g.fillText(String(i), zx - 6 - lw, zy - i * gy + 5);
    }
  }
  const fred = Math.round(g.measureText(String(maxTurns)).width / gx);
  for (let i = 0; i <= maxTurns; i++) {
    g.strokeStyle = 'gray'; g.beginPath(); g.moveTo(zx + i * gx, zy); g.lineTo(zx + i * gx, zy - maxValue * gy); g.stroke();
    if (i === maxTurns || fred === 0 || i % fred === 0) { g.fillStyle = 'white'; g.fillText(String(i), i * gx + zx - 3, zy + 15); }
  }
  g.strokeStyle = 'white';
  g.beginPath(); g.moveTo(zx, zy); g.lineTo(zx + maxTurns * gx, zy); g.moveTo(zx, zy); g.lineTo(zx, zy - maxValue * gy); g.stroke();
  g.lineCap = 'round';
  for (const p of players) {
    if (hidden.has(p)) continue;
    const pts = p.getStatisticsFor(type);
    const col = colorCss(p.getColor());
    const black = p.getColor() === ColorUtil.BLACK;
    let old = NaN, nw = 0, i;
    for (i = 0; i < pts.length; i++) {
      nw = summable ? nw + pts[i] : pts[i];
      if (!Number.isNaN(nw)) {
        let x1, y1;
        if (!Number.isNaN(old)) { x1 = zx + i * gx; y1 = zy - old * gy; } else { x1 = zx + (i + 1) * gx - 3; y1 = zy - nw * gy; }
        const x2 = zx + (i + 1) * gx, y2 = zy - nw * gy;
        if (black) { g.strokeStyle = 'white'; g.lineWidth = 3; g.beginPath(); g.moveTo(x1, y1); g.lineTo(x2, y2); g.stroke(); }
        g.strokeStyle = col; g.lineWidth = 2; g.beginPath(); g.moveTo(x1, y1); g.lineTo(x2, y2); g.stroke();
      }
      old = nw;
    }
    const x = zx + i * gx, y = zy - old * gy + 11;
    g.fillStyle = col;
    if (black) { g.strokeStyle = 'white'; g.lineWidth = 3; g.strokeText(p.getName(), Math.min(x, W - g.measureText(p.getName()).width), y); }
    g.fillText(p.getName(), Math.min(x, W - g.measureText(p.getName()).width), y);
  }
}

function showContinents() {
  const game = risk.getGame();
  const rows = game.getContinents().map((c) => {
    const owner = c.getOwner();
    return el('tr', {},
      el('td', {}, el('span', { class: 'chip', style: { display: 'inline-block', verticalAlign: 'middle', marginRight: '6px', background: colorCss(c.getColor()) } }), c.getName()),
      el('td', { class: 'num' }, String(c.getArmyValue())),
      el('td', { class: 'num' }, String(c.getTerritoriesContained().length)),
      el('td', {}, owner ? owner.getName() : ''));
  });
  openDialog({
    title: t('swing.button.continents'),
    body: el('table', { class: 'plain' },
      el('thead', {}, el('tr', {}, el('th', {}, t('warg.continents.name')), el('th', {}, t('warg.continents.bonus')), el('th', {}, t('warg.continents.countries')), el('th', {}, t('warg.continents.owner')))),
      el('tbody', {}, rows)),
    actions: [{ label: t('warg.close'), primary: true, onClick: (d) => d.close() }],
  });
}

function showOptions() {
  const soundBox = el('input', { type: 'checkbox', checked: sound.enabled });
  const diceBox = el('input', { type: 'checkbox', checked: risk.showDiceSleep > 0 });
  const wait = el('input', { type: 'number', min: 0, max: 5000, step: 100, value: risk.ai.wait });
  openDialog({
    title: t('swing.menu.options'),
    body: el('div', {},
      el('label', { class: 'check' }, soundBox, t('game.menu.sound')),
      el('label', { class: 'check' }, diceBox, t('warg.showdice')),
      el('label', { class: 'field' }, t('warg.aispeed'), wait)),
    actions: [{
      label: t('about.okbutton'), primary: true,
      onClick: (d) => {
        sound.setEnabled(soundBox.checked);
        const show = diceBox.checked;
        risk.showDiceSleep = show ? 1000 : 0;
        risk.rollDiceSleep = show ? 500 : 0;
        prefs.set('showDice', show);
        const w = Math.max(0, Math.min(5000, parseInt(wait.value, 10) || 0));
        risk.ai.wait = w;
        prefs.set('aiWait', w);
        d.close();
      },
    }],
  });
}

function showMenu() {
  const game = risk.getGame();
  if (!game) return;
  const active = ui.gameState !== 0;
  const autoEnd = el('input', { type: 'checkbox', checked: risk.getAutoEndGo(), disabled: !active });
  const autoDef = el('input', { type: 'checkbox', checked: risk.getAutoDefend(), disabled: !active });
  autoEnd.addEventListener('change', () => risk.parser('autoendgo ' + (autoEnd.checked ? 'on' : 'off')));
  autoDef.addEventListener('change', () => risk.parser('autodefend ' + (autoDef.checked ? 'on' : 'off')));
  const dlg = openDialog({
    title: t('game.menu.title'),
    body: el('div', {},
      el('label', { class: 'check' }, autoEnd, t('game.menu.autoendgo')),
      el('label', { class: 'check' }, autoDef, t('game.menu.autodefend')),
      el('p', { class: 'muted' }, game.getCurrentPlayer() ? game.getCurrentPlayer().getName() : '')),
    actions: [
      { label: t('game.menu.save'), disabled: !active, onClick: (d) => { d.close(); saveDialog(); } },
      { label: t('swing.button.continents'), onClick: (d) => { d.close(); showContinents(); } },
      { label: t('swing.menu.options'), onClick: (d) => { d.close(); showOptions(); } },
      { label: t('game.menu.manual'), onClick: () => window.open('/help/index.htm', '_blank', 'noopener') },
      { label: t('game.menu.close'), danger: true, onClick: (d) => { d.close(); closeGameAndReturn(true); } },
      { label: t('game.menu.closemenu'), primary: true, onClick: (d) => d.close() },
    ],
    onClose: () => { ui.menuOpen = false; },
  });
  ui.menuOpen = dlg;
}

function saveDialog() {
  const game = risk.getGame();
  const def = (game.getMapName() || game.getMapFile()).replace(/\s+/g, '-') + '-' + new Date().toISOString().slice(0, 16).replace(/[:T]/g, '-');
  const input = el('input', { value: def, maxlength: 60 });
  openDialog({
    title: t('game.menu.save'),
    body: el('label', { class: 'field' }, t('warg.save.name'), input),
    actions: [
      { label: t('newgame.cancel'), ghost: true, onClick: (d) => d.close() },
      { label: t('warg.save.ok'), primary: true, onClick: (d) => { const n = input.value.trim(); if (!n) return; d.close(); risk.parser('savegame ' + n); } },
    ],
  });
}

async function closeGameAndReturn(confirmFirst) {
  if (confirmFirst) {
    const ok = await confirmDialog(t('game.menu.close'), t('warg.close.confirm'), t('game.menu.close'), t('newgame.cancel'));
    if (!ok) return;
  }
  risk.parser('closegame');
}

function toast(text) {
  const tEl = $('toast');
  tEl.textContent = text;
  tEl.hidden = false;
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => { tEl.hidden = true; }, 3500);
}

// ------------------------------------------------------------------ console

function log(text) {
  const pre = $('log');
  pre.append(text + '\n');
  if (pre.childNodes.length > 1500) pre.firstChild.remove();
  pre.scrollTop = pre.scrollHeight;
}

// ------------------------------------------------------------------ new game setup (NewGameFrame)

const setup = { maps: [], selectedType: 'human', color: null };
const TYPES = ['human', 'ai easy', 'ai average', 'ai hard', 'ai crap'];
const typeLabel = (type) => t(type === 'human' ? 'newgame.player.type.human' : 'newgame.player.type.' + type.substring(3) + 'ai');

async function openSetup() {
  if (setup.maps.length === 0) setup.maps = await listMaps();
  renderMaps();
  const sel = $('opt-cards');
  sel.replaceChildren();
  const cards = [...new Set(setup.maps.map((m) => m.crd).filter(Boolean))].sort();
  for (const c of cards) sel.append(el('option', { value: c }, c));
  $('np-type').replaceChildren(...TYPES.map((ty) => el('option', { value: ty }, typeLabel(ty))));
  $('np-name').value = '';
  const game = risk.getGame();
  if (game.getPlayers().length === 0) {
    const saved = prefs.get('players', null);
    if (saved && saved.length) {
      for (const p of saved) await risk.parser(`newplayer ${p.type} ${p.color} ${p.name}`);
    } else {
      await resetPlayers();
    }
  }
  const opts = prefs.get('setupOptions', null);
  if (opts) {
    document.querySelector(`input[name=mode][value=${opts.mode}]`).checked = true;
    document.querySelector(`input[name=cardmode][value=${opts.cardmode}]`).checked = true;
    $('opt-autoplace').checked = opts.autoplace;
    $('opt-recycle').checked = opts.recycle;
    if (opts.map && opts.map !== game.getMapFile()) await risk.parser('choosemap ' + opts.map);
  }
  if ([...sel.options].some((o) => o.value === game.getCardsFile())) sel.value = game.getCardsFile();
  renderPlayers();
  syncModeOptions();
}

async function resetPlayers() {
  const game = risk.getGame();
  for (const p of [...game.getPlayers()]) await risk.parser('delplayer ' + p.getName());
  const human = prefs.get('humanName', null) || t('warg.player.default');
  risk.riskconfig.set('default.player1.name', human.replace(/\s+/g, '_'));
  await risk.parser('autosetup');
  await risk.idle();
}

function renderMaps() {
  const game = risk.getGame();
  const list = $('map-list');
  const preview = (m) => (m.prv ? '/maps/preview/' + encodeURIComponent(m.prv) : '/api/preview/' + encodeURIComponent(m.file) + '.svg');
  list.replaceChildren(...setup.maps.map((m) => el('button', {
    class: 'map-card' + (m.custom ? ' custom' : ''),
    type: 'button',
    role: 'option',
    'aria-selected': String(game && game.getMapFile() === m.file),
    onclick: () => risk.parser('choosemap ' + m.file),
  },
  el('img', { src: preview(m), alt: '', loading: 'lazy' }),
  el('span', { class: 'mc-name' }, m.name || m.file),
  el('span', { class: 'mc-meta' }, t('warg.mapmeta', m.countries, m.continents) + (m.custom ? ' — ' + t('editor.yours') : '')))),
  el('button', {
    class: 'map-card new-map', type: 'button',
    onclick: () => { risk.parser('closegame'); setTimeout(() => editor.openNew(), 50); },
  }, el('span', { class: 'plus', 'aria-hidden': 'true' }, '+'), el('span', { class: 'mc-name' }, t('editor.create'))));
}

function renderPlayers() {
  const game = risk.getGame();
  if (!game || game.getState() !== RG.STATE_NEW_GAME) return;
  const list = $('player-list');
  list.replaceChildren(...game.getPlayers().map((p) => el('li', {},
    el('span', { class: 'chip', style: { background: colorCss(p.getColor()) } }),
    el('span', {}, p.getName()),
    el('span', { class: 'ptype' }, typeLabel(risk.getTypeString(p.getType()))),
    el('button', { type: 'button', title: t('newgame.removeplayer'), onclick: () => risk.parser('delplayer ' + p.getName()) }, '✕'))));
  // colour swatches, used colours are disabled
  const used = new Set(game.getPlayers().map((p) => p.getColor()));
  if (!setup.color || used.has(ColorUtil.getColor(setup.color))) setup.color = ColorUtil.PLAYER_COLORS.find((c) => !used.has(ColorUtil.getColor(c))) || null;
  $('np-colors').replaceChildren(...ColorUtil.PLAYER_COLORS.map((c) => el('button', {
    type: 'button',
    class: 'swatch',
    role: 'radio',
    title: t('color.' + c),
    'aria-label': t('color.' + c),
    'aria-checked': String(setup.color === c),
    disabled: used.has(ColorUtil.getColor(c)),
    style: { background: colorCss(ColorUtil.getColor(c)) },
    onclick: () => { setup.color = c; renderPlayers(); },
  })));
  const full = game.getPlayers().length >= config.MAX_PLAYERS;
  $('add-player').querySelector('button[type=submit]').disabled = full;
}

function syncModeOptions() {
  const mission = document.querySelector('input[name=mode][value=mission]');
  const mode = document.querySelector('input[name=mode]:checked').value;
  $('opt-autoplace').disabled = mode === 'mission';
  if (mission.disabled && mission.checked) document.querySelector('input[name=mode][value=domination]').checked = true;
}

function setupNeedInput() {
  $('setup-start').disabled = false;
  renderPlayers();
  renderMaps();
}

async function startFromSetup() {
  const game = risk.getGame();
  const n = game.getPlayers().length;
  if (n < 2 || n > config.MAX_PLAYERS) {
    await alertDialog(t('newgame.error.title'), t('warg.error.players'), t('about.okbutton'));
    return;
  }
  prefs.set('players', game.getPlayers().map((p) => ({ type: risk.getTypeString(p.getType()), color: ColorUtil.getStringForColor(p.getColor()), name: p.getName() })));
  const human = game.getPlayers().find((p) => p.getType() === PLAYER_HUMAN);
  if (human) prefs.set('humanName', human.getName());
  const mode = document.querySelector('input[name=mode]:checked').value;
  const cardmode = document.querySelector('input[name=cardmode]:checked').value;
  const autoplace = $('opt-autoplace').checked && mode !== 'mission';
  const recycle = $('opt-recycle').checked;
  const dd = $('opt-defdice').value;
  prefs.set('setupOptions', { mode, cardmode, autoplace, recycle, map: game.getMapFile() });
  sound.play('button_start_game');
  $('setup-start').disabled = true;
  ui.quickPlace.clear();
  risk.parser(`startgame ${mode} ${cardmode}${autoplace ? ' autoplaceall' : ''}${recycle ? ' recycle' : ''}${dd ? ' defenderdice=' + dd : ''}`);
}

// ------------------------------------------------------------------ load game

async function showLoad() {
  const saves = await listSaves();
  const list = el('ul', { class: 'list-saves' });
  let dlg;
  const render = (items) => {
    list.replaceChildren(...items.map((s) => el('li', {},
      el('span', {}, s.name, ' ', el('span', { class: 'when' }, new Date(s.modified).toLocaleString(lang))),
      el('span', {},
        el('button', { class: 'btn ghost', type: 'button', onclick: async () => { await deleteSave(s.name); render(await listSaves()); } }, t('warg.load.delete')),
        ' ',
        el('button', { class: 'btn primary', type: 'button', onclick: () => { dlg.close(); risk.parser('loadgame ' + s.name); } }, t('mainmenu.loadgame.loadbutton'))))));
    if (items.length === 0) list.append(el('li', {}, t('warg.load.empty')));
  };
  render(saves);
  dlg = openDialog({
    title: t('mainmenu.loadgame'),
    body: list,
    actions: [{ label: t('newgame.cancel'), ghost: true, onClick: (d) => d.close() }],
  });
}

function showAbout() {
  openDialog({
    title: t('mainmenu.about'),
    body: el('div', { class: 'about' },
      el('p', {}, t('warg.about.text')),
      el('p', { class: 'muted' }, 'Domination © Yura Mamyrin, yura.net. GNU GPL v3.'),
      el('p', {}, el('a', { href: '/help/index.htm', target: '_blank', rel: 'noopener' }, t('mainmenu.help')), ' · ',
        el('a', { href: '/help/rules.htm', target: '_blank', rel: 'noopener' }, t('warg.rules')))),
    actions: [{ label: t('about.okbutton'), primary: true, onClick: (d) => d.close() }],
  });
}

// ------------------------------------------------------------------ engine listener

risk.addRiskListener({
  newGame() {
    closeAllDialogs();
    show('screen-setup');
    openSetup();
  },
  showMapPic() {
    renderMaps();
  },
  showCardsFile(file, hasMissions) {
    const sel = $('opt-cards');
    if (![...sel.options].some((o) => o.value === file)) sel.append(el('option', { value: file }, file));
    sel.value = file;
    document.querySelector('input[name=mode][value=mission]').disabled = !hasMissions;
    syncModeOptions();
  },
  addPlayer() { renderPlayers(); },
  delPlayer() { renderPlayers(); },
  async startGame() {
    closeAllDialogs();
    show('screen-game');
    $('status-help').textContent = t('warg.loading');
    updateBar();
    const game = risk.getGame();
    try {
      await mapView.load(game);
    } catch (e) {
      alertDialog('Error', e.message);
    }
    $('status-help').textContent = ui.help;
    mapView.repaintCountries();
    scheduleRepaint();
  },
  closeGame() {
    closeAllDialogs();
    mapView.stopAni();
    noInput();
    show('screen-menu');
  },
  sendMessage(output, redraw) {
    log(output);
    scheduleRepaint();
  },
  setGameStatus(help) {
    ui.help = help;
    $('status-help').textContent = help;
  },
  needInput(s) { needInput(s); },
  noInput() { noInput(); },
  openBattle(c1, c2) { openBattle(c1, c2); },
  setNODAttacker(n) { if (ui.battle) ui.battle.setNODAttacker(n); },
  setNODDefender(n) { if (ui.battle) ui.battle.setNODDefender(n); },
  showDiceResults(att, def, weAreAttacker, result) { if (ui.battle) ui.battle.showDiceResults(att, def, weAreAttacker, result); scheduleRepaint(); },
  closeBattle() { closeBattle(); },
  playerGotCard(card) {
    sound.play('cards_receive');
    toast(t('warg.newcard', t('warg.card.' + card.getName()) + (card.getCountry() ? ' · ' + card.getCountry().getName() : '')));
  },
  gameOver() {
    setTimeout(() => {
      const game = risk.getGame();
      if (!game) return;
      const w = game.getCurrentPlayer();
      mapView.startAni();
      toast(t('warg.gameover.won', w.getName()));
    }, 50);
  },
  showMessageDialog(text) { alertDialog(t('newgame.error.title'), text, t('about.okbutton')); },
});

function closeAllDialogs() {
  closeBattle();
  closeMove();
  if (ui.stats) ui.stats.dlg.close();
  if (ui.cards) ui.cards.close();
  if (ui.menuOpen) ui.menuOpen.close();
  $('modal-root').replaceChildren();
}

// ------------------------------------------------------------------ wiring

function wire() {
  $('btn-new').addEventListener('click', () => { sound.play('button'); risk.parser('newgame'); });
  $('btn-load').addEventListener('click', () => { sound.play('button'); showLoad(); });
  $('btn-help').addEventListener('click', () => window.open('/help/index.htm', '_blank', 'noopener'));
  $('btn-about').addEventListener('click', showAbout);
  $('btn-editor').addEventListener('click', () => { sound.play('button'); editor.openNew(); });
  $('lang-select').addEventListener('change', (e) => setLanguage(e.target.value));

  $('setup-cancel').addEventListener('click', () => { sound.play('back_button'); risk.parser('closegame'); });
  $('setup-start').addEventListener('click', startFromSetup);
  $('reset-players').addEventListener('click', resetPlayers);
  $('add-player').addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = $('np-name').value.trim().replace(/\s+/g, ' ');
    if (!name || !setup.color) return;
    sound.play('button');
    await risk.parser(`newplayer ${$('np-type').value} ${setup.color} ${name}`);
    $('np-name').value = '';
  });
  document.querySelectorAll('input[name=mode]').forEach((r) => r.addEventListener('change', syncModeOptions));
  document.querySelectorAll('input[name=cardmode]').forEach((r) => r.addEventListener('change', () => {
    const it = document.querySelector('input[name=cardmode]:checked').value === 'italianlike';
    $('opt-defdice').options[0].textContent = t('warg.default') + ' (' + (it ? 3 : 2) + ')';
  }));
  $('opt-cards').addEventListener('change', (e) => risk.parser('choosecards ' + e.target.value));

  document.querySelectorAll('.views button').forEach((b) => b.addEventListener('click', () => {
    document.querySelectorAll('.views button').forEach((x) => x.setAttribute('aria-selected', String(x === b)));
    mapView.setView(parseInt(b.dataset.view, 10));
  }));
  document.querySelector('.views button[data-view="1"]').setAttribute('aria-selected', 'true');
  const namesBtn = $('t-names');
  const names = prefs.get('showNames', false);
  mapView.setShowNames(names);
  namesBtn.setAttribute('aria-pressed', String(names));
  namesBtn.addEventListener('click', () => {
    const on = namesBtn.getAttribute('aria-pressed') !== 'true';
    namesBtn.setAttribute('aria-pressed', String(on));
    mapView.setShowNames(on);
    prefs.set('showNames', on);
  });

  $('go').addEventListener('click', goOn);
  $('t-stats').addEventListener('click', showStats);
  $('t-cards').addEventListener('click', showCards);
  $('t-mission').addEventListener('click', showMission);
  $('t-undo').addEventListener('click', () => { mapView.c1 = NO_COUNTRY; mapView.c2 = NO_COUNTRY; go('undo'); });
  $('t-menu').addEventListener('click', showMenu);
  $('cmd-form').addEventListener('submit', (e) => {
    e.preventDefault();
    const v = $('cmd').value.trim();
    if (!v) return;
    $('cmd').value = '';
    risk.parser(v);
  });
  document.addEventListener('keydown', (e) => {
    if (e.target.matches('input, select, textarea')) return;
    if ((e.key === 'Enter' || e.key === ' ') && !$('screen-game').hidden && !$('go').disabled && $('modal-root').children.length === 0) {
      e.preventDefault();
      goOn();
    }
  });
  setupMapEvents();
}

await risk.ready();
await setLanguage(lang);
wire();
show('screen-menu');
