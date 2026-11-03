// warg - game sounds, same files and names as the original sound/medieval.xml. GPLv3.

const FILES = {
  button: 'medieval/button1.mp3',
  back_button: 'medieval/return button3.mp3',
  button_start_game: 'medieval/game starting.mp3',
  cards_receive: 'medieval/receiving card.mp3',
  cards_trade: 'medieval/trade-cards-in-medieval-map.mp3',
  place_army: 'medieval/adding pieces.mp3',
  place_armies: 'medieval/adding_soldiers1.mp3',
  attack: 'medieval/attack.mp3',
  battle_retreat: 'medieval/retreating.mp3',
  dice_roll: 'medieval/batttle_medieval_map1.mp3',
  dice_win: 'medieval/Victory2.mp3',
  dice_lose: 'medieval/losing medieval battle1.mp3',
  dice_draw: 'medieval/medieval-draw-battle.mp3',
  battle_win: 'medieval/won-against-all-troops-medieval.mp3',
  battle_defeat: 'medieval/lost-all-troops-medieval.mp3',
  move_armies: 'medieval/moving soldiers after to win and conquer territory.mp3',
  move_tactical: 'medieval/moving troops1.mp3',
};

const cache = new Map();

export const sound = {
  enabled: (() => { try { return localStorage.getItem('warg.sound') !== 'off'; } catch (e) { return true; } })(),
  setEnabled(on) {
    this.enabled = on;
    try { localStorage.setItem('warg.sound', on ? 'on' : 'off'); } catch (e) { /* ignore */ }
  },
  play(name) {
    if (!this.enabled || !FILES[name]) return;
    let a = cache.get(name);
    if (!a) {
      a = new Audio('/sound/' + FILES[name].split('/').map(encodeURIComponent).join('/'));
      a.volume = 0.6;
      cache.set(name, a);
    }
    try {
      a.currentTime = 0;
      const p = a.play();
      if (p && p.catch) p.catch(() => {});
    } catch (e) { /* ignore */ }
  },
};
