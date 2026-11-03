// warg - JavaScript port of net.yura.domination.engine.Risk (Yura Mamyrin). GPLv3.
// The command parser that drives a game. Every move in the game is a text command, exactly
// like in the original ("placearmies 12 3", "attack 4 7", "roll 3", ...). This version only
// implements local play (no network / lobby).

import * as RG from './RiskGame.js';
import { RiskGame, config } from './RiskGame.js';
import { Card, WILDCARD, Player, PLAYER_HUMAN, PLAYER_AI_CRAP } from './model.js';
import * as ColorUtil from './colors.js';
import { AIManager } from './ai/AIManager.js';
import { tokenize, sleep, replaceAll, Random } from './util.js';
import { serializeGame, deserializeGame } from './save.js';
import { loadBundle, Bundle } from './i18n.js';

export const STARTGAME_OPTION_MODE_DOMINATION = 'domination';
export const STARTGAME_OPTION_MODE_CAPITAL = 'capital';
export const STARTGAME_OPTION_MODE_SECRET_MISSION = 'mission';
export const STARTGAME_OPTION_CARD_INCREASING_SET = 'increasing';
export const STARTGAME_OPTION_CARD_FIXED_SET = 'fixed';
export const STARTGAME_OPTION_CARD_ITALIAN_LIKE_SET = 'italianlike';
export const STARTGAME_OPTION_AUTO_PLACE_ALL = 'autoplaceall';
export const STARTGAME_OPTION_RECYCLE_CARDS = 'recycle';
export const STARTGAME_OPTION_DEFENDER_DICE = 'defenderdice=';

export const GAME_NAME = 'Domination';
export const RISK_VERSION = 'warg 0.1 (Domination 1.3.6 rules)';

const NL = '\n';

/** default players (game.ini) */
export const DEFAULT_PLAYERS = [
  { type: 'human', color: 'cyan', name: 'player' },
  { type: 'ai easy', color: 'green', name: 'bob' },
  { type: 'ai easy', color: 'magenta', name: 'fred' },
  { type: 'ai easy', color: 'red', name: 'ted' },
  { type: 'ai average', color: 'blue', name: 'yura' },
  { type: 'ai average', color: 'yellow', name: 'lala' },
];

/**
 * io: {
 *   readMap(name) -> Promise<string|null>   text of a file in the maps folder
 *   readResource(name) -> Promise<string|null> text of a translation .properties file
 *   saveGame(name, json) -> Promise<void>    (optional)
 *   loadGame(name) -> Promise<object>        (optional)
 *   readScript(name) -> Promise<string>      (optional, for "play")
 * }
 */
export class Risk {
  constructor(io, options = {}) {
    this.rawIo = io;
    const cache = new Map();
    // map files are re-read on undo / load, so keep them in memory
    this.io = {
      ...io,
      readMap: async (name) => {
        if (!cache.has(name)) cache.set(name, await io.readMap(name));
        return cache.get(name);
      },
      readResource: async (name) => {
        const key = 'res:' + name;
        if (!cache.has(key)) cache.set(key, await io.readResource(name));
        return cache.get(key);
      },
    };
    this.language = options.language || 'en';
    this.listeners = [];
    this.inbox = [];
    this.processing = false;
    this.game = null;
    this.replay = false;
    this.battle = false;
    this.unlimitedLocalMode = true;
    this.myAddress = 'local#' + Math.floor(Math.random() * 1e9);
    this.undo = null;
    this.skipUndo = false;
    this.autoplaceall = false;
    this.showDiceSleep = options.showDice === false ? 0 : 1000;
    this.rollDiceSleep = options.showDice === false ? 0 : 500;
    this.random = options.random || null; // seeded random for tests
    this.ai = new AIManager();
    if (options.aiWait !== undefined) this.ai.wait = options.aiWait;
    this.riskconfig = new Map([
      ['default.autoplaceall', 'false'],
      ['default.recyclecards', 'true'],
    ]);
    this.resb = new Bundle([], this.language);
    this._ready = this.setLanguage(this.language);
  }

  async ready() { return this._ready; }

  async setLanguage(language) {
    this.language = language;
    const b = await loadBundle((n) => this.io.readResource(n), 'Risk', language);
    if (b) this.resb = b;
    return this;
  }

  getResourceBundle() { return this.resb; }

  // ------------------------------------------------------------------ listeners (RiskController)

  addRiskListener(l) {
    this.listeners.push(l);
    this.setHelp();
  }
  deleteRiskListener(l) {
    this.listeners = this.listeners.filter((x) => x !== l);
  }
  emit(name, ...args) {
    for (const l of this.listeners) {
      if (typeof l[name] === 'function') {
        try { l[name](...args); } catch (e) { console.error('listener error in ' + name, e); }
      }
    }
  }
  sendMessage(output, redrawNeeded, repaintNeeded) { this.emit('sendMessage', output, redrawNeeded, repaintNeeded); }
  showMessageDialog(a) { this.emit('showMessageDialog', a); }

  t(key, ...args) { return this.resb.t(key, ...args); }

  // ------------------------------------------------------------------ inbox

  /** queue a command typed by the user (or sent by the UI) */
  parser(command) {
    return new Promise((resolve) => {
      this.inbox.push({ type: 'UI', command, resolve });
      this._pump();
    });
  }

  async _pump() {
    if (this.processing) return;
    this.processing = true;
    try {
      while (this.inbox.length > 0) {
        const m = this.inbox.shift();
        try {
          if (m.type === 'UI') await this.processFromUI(m.command);
          else await this.inGameParser(m.command);
        } catch (ex) {
          console.warn('ERROR processing: ' + m.command + (this.game ? ' (gameState=' + this.game.getState() + ')' : ''), ex);
        } finally {
          m.resolve();
        }
      }
    } finally {
      this.processing = false;
    }
  }

  /** resolves when nothing is left to process (handy for tests and the CLI) */
  async idle() {
    while (this.processing || this.inbox.length > 0) await sleep(5);
  }

  // ------------------------------------------------------------------ commands from the UI

  async processFromUI(message) {
    if (message.trim().length === 0) {
      this.sendMessage('>', false, false);
      await this.getInput();
      return;
    }
    if (message === 'ver') {
      this.sendMessage('>' + message, false, false);
      this.sendMessage(GAME_NAME + ' Game Engine [Version ' + RISK_VERSION + ']', false, false);
      await this.getInput();
    } else if (message.startsWith('rem ')) {
      this.sendMessage('>' + message, false, false);
      await this.getInput();
    } else if (this.game == null) {
      await this.noGameParser(message);
    } else {
      const st = tokenize(message);
      const input = st[0];
      let output;
      if (input === 'closegame') {
        if (st.length === 1) {
          this.closeGame();
          output = this.t('core.close.closed');
        } else output = this.t('core.error.syntax', 'closegame');
      } else if (input === 'savegame') {
        if (st.length >= 2) {
          const filename = message.substring(message.indexOf(' ') + 1).trim();
          try {
            if (!this.io.saveGame) throw new Error('saving is not supported here');
            await this.io.saveGame(filename, this.getSaveData());
            output = this.t('core.save.saved');
          } catch (ex) {
            output = this.t('core.save.error.unable') + ' ' + ex.message;
            this.showMessageDialog(output);
          }
        } else output = this.t('core.error.syntax', 'savegame filename');
      } else if (input === 'replay') {
        if (st.length === 1) {
          try {
            const replayCommands = this.game.getCommands();
            this.saveGameToUndoObject();
            this.game = await this.newRiskGame();
            this.replay = true;
            for (const command of replayCommands) {
              try {
                await this.inGameParser(command);
              } catch (error) {
                if (!(this.game.getCurrentPlayer() != null && this.game.getCurrentPlayer().getType() === PLAYER_HUMAN)) throw error;
              }
            }
            output = 'replay of game finished';
          } catch (e) {
            output = 'error with replay ' + e;
          } finally {
            this.replay = false;
          }
        } else output = this.t('core.error.syntax', 'replay');
      } else {
        await this.inGameParser(this.myAddress + ' ' + message);
        output = null;
      }
      if (output != null) {
        this.sendMessage('game>' + message, false, false);
        this.sendMessage(output, false, false);
        await this.getInput();
      }
    }
  }

  async newRiskGame() {
    const opts = { language: this.language, resb: this.resb };
    if (this.random) opts.random = this.random;
    return RiskGame.create(this.io, opts);
  }

  async noGameParser(message) {
    const st = tokenize(message);
    const input = st[0];
    let output;
    this.sendMessage('>' + message, false, false);
    if (input === 'newgame') {
      if (st.length === 1) {
        try {
          this.game = await this.newRiskGame();
          this.unlimitedLocalMode = true;
          this.emit('newGame', true);
          await this.setupPreviews(await this.doesMapHaveMission());
          output = this.t('core.newgame.created');
        } catch (e) {
          console.warn('newgame error', e);
          output = this.t('core.newgame.error') + ' ' + e;
        }
      } else output = this.t('core.error.syntax', 'newgame');
    } else if (input === 'loadgame') {
      if (st.length >= 2) {
        const filename = message.substring(message.indexOf(' ') + 1).trim();
        try {
          if (!this.io.loadGame) throw new Error('loading is not supported here');
          const data = await this.io.loadGame(filename);
          await this.loadGameFromData(data);
          output = this.t('core.loadgame.loaded');
          const player = this.game.getCurrentPlayer();
          if (player != null) {
            this.saveGameToUndoObject();
            output = output + NL + this.t('core.loadgame.currentplayer') + ' ' + player.getName();
          }
          if (this.game.getState() === RG.STATE_NEW_GAME) {
            this.emit('newGame', true);
            await this.setupPreviews(await this.doesMapHaveMission());
          } else {
            this.emit('startGame', this.unlimitedLocalMode);
          }
        } catch (ex) {
          console.warn('error loading game from file: ' + filename, ex);
          output = this.t('core.loadgame.error.load') + ' ' + ex.message;
          this.game = null;
          this.showMessageDialog(output);
        }
      } else output = this.t('core.error.syntax', 'loadgame filename');
    } else if (input === 'play') {
      if (st.length >= 2) {
        const filename = message.substring(message.indexOf(' ') + 1).trim();
        try {
          if (!this.io.readScript) throw new Error('scripts are not supported here');
          const script = await this.io.readScript(filename);
          this.replay = true;
          this.game = await this.newRiskGame();
          this.unlimitedLocalMode = true;
          this.emit('newGame', true);
          await this.setupPreviews(await this.doesMapHaveMission());
          output = 'playing "' + filename + '"';
          this.sendMessage(output, false, true);
          await this.playScript(script);
          return;
        } catch (error) {
          this.replay = false;
          output = 'unable to play "' + filename + '" ' + error;
        }
      } else output = this.t('core.error.syntax', 'play filename');
    } else {
      output = this.t('core.loadgame.nogame');
    }
    this.sendMessage(output, false, true);
    await this.getInput();
  }

  /** replays a game log line by line (the "play" command) */
  async playScript(script) {
    let line = 0;
    try {
      for (const input of script.split(/\r\n|\r|\n/)) {
        line++;
        if (input.length === 0) continue;
        try {
          await this.inGameParser(input);
        } catch (error) {
          if (this.game.getCurrentPlayer() != null && this.game.getCurrentPlayer().getType() === PLAYER_HUMAN) {
            console.info('invalid script user command on line: ' + line + ' input: ' + input, error.message);
          } else {
            throw error;
          }
        }
      }
    } catch (error) {
      console.warn('script error on line ' + line, error);
      this.scriptError = { line, error };
    } finally {
      this.replay = false;
    }
    await this.getInput();
  }

  getSaveData() {
    return serializeGame(this.game);
  }

  async loadGameFromData(data) {
    const opts = { language: this.language, resb: this.resb };
    this.game = await deserializeGame(data, this.io, opts);
    this.unlimitedLocalMode = true;
  }

  // ------------------------------------------------------------------ the game parser

  async inGameParser(message) {
    this.emit('sendDebug', message);
    let needInput = true;
    let output = null;
    const st = tokenize(message);
    const Addr = st[0];
    let pos = 1;
    const hasMore = () => pos < st.length;
    const next = () => st[pos++];
    const countTokens = () => st.length - pos;
    const rest = () => st.slice(pos).join(' ');

    if (Addr !== 'ERROR') this.game.addCommand(message);

    if (Addr === 'ERROR') {
      this.showMessageDialog(rest());
    } else if (Addr === 'DICE') {
      const game = this.game;
      const attSize = RiskGame.getNumber(next());
      const defSize = RiskGame.getNumber(next());
      output = this.t('core.dice.rolling') + NL + this.t('core.dice.results');
      const att = [];
      output += ' ' + this.t('core.dice.attacker');
      for (let c = 0; c < attSize; c++) {
        att[c] = RiskGame.getNumber(next());
        output += ' ' + (att[c] + 1);
      }
      const def = [];
      output += ' ' + this.t('core.dice.defender');
      for (let c = 0; c < defSize; c++) {
        def[c] = RiskGame.getNumber(next());
        output += ' ' + (def[c] + 1);
      }
      output += NL;
      const attacker = game.getAttacker();
      const result = game.battle(att, def);
      if (result[0] === 1) {
        output += replaceAll(replaceAll(this.t('core.dice.result'), '{0}', String(result[2])), '{1}', String(result[1]));
        if (result[3] === 0) {
          let n = attacker.getArmies() - 1;
          output += NL + this.t('core.dice.notdefeated') + ' ';
          if (n > 0) {
            if (n > 3) n = 3;
            output += this.t('core.dice.attackagain', n);
          } else {
            output += this.t('core.dice.noattackagain');
          }
        } else {
          output += NL + this.t('core.dice.defeated') + ' ';
          if (result[3] === 2) output += this.t('core.dice.eliminated') + ' ';
          if (result[4] === result[5]) {
            const noa = game.moveAll();
            const ma = game.moveArmies(noa);
            output += this.t('core.dice.armiesmoved', noa);
            if (ma === 2) output += this.whoWon();
          } else {
            output += this.t('core.dice.howmanyarmies', result[4], result[5]);
          }
        }
        if (this.battle) {
          const weAreAttacker = this.showHumanPlayerThereInfo(attacker.getOwner());
          const defenderWon = result[3] === 0 && attacker.getArmies() === 1;
          this.emit('showDiceResults', att, def, weAreAttacker, defenderWon ? -1 : result[3]);
          if (this.showDiceSleep > 0) await sleep(this.showDiceSleep);
        }
      } else {
        output = this.t('core.dice.error.unabletoroll');
      }
      if (game.getState() !== RG.STATE_ROLLING && game.getState() !== RG.STATE_DEFEND_YOURSELF) this.closeBattle();
    } else if (Addr === 'PLAYER') {
      const index = parseInt(next(), 10);
      const p = this.game.setCurrentPlayer(index);
      this.sendMessage('Game started', false, false);
      output = this.t('core.player.randomselected', p.getName());
      if (this.game.getGameMode() === RG.MODE_SECRET_MISSION || this.autoplaceall === true) needInput = false;
      else this.saveGameToUndoObject();
    } else if (Addr === 'CARD') {
      const game = this.game;
      if (hasMore()) {
        const name = next();
        const card = game.findCardAndRemoveIt(name);
        game.getCurrentPlayer().giveCard(card);
        if (this.showHumanCurrentPlayerInfo()) {
          const cardName = name === WILDCARD ? name : card.getName() + ' ' + game.getCountryInt(parseInt(name, 10)).getName();
          this.emit('playerGotCard', card);
          this.sendMessage('You got a new card: "' + cardName + '"', false, false);
        }
      }
      const newplayer = game.endGo();
      output = this.t('core.player.newselected', newplayer.getName());
      // this is not a bug! (Easter egg)
      if (this.unlimitedLocalMode && game.getSetupDone() && newplayer.getName() === 'Theo') newplayer.addArmies(newplayer.getExtraArmies());
      this.saveGameToUndoObject();
    } else if (Addr === 'PLACE') {
      const c = this.game.getCountryInt(parseInt(next(), 10));
      this.game.placeArmy(c, 1);
      this.sendMessage(this.t('core.place.oneplacedin', c.getName()), false, false);
      output = this.t('core.place.autoplaceok');
    } else if (Addr === 'PLACEALL') {
      const game = this.game;
      for (let c = 0; c < game.getNoCountries(); c++) {
        const t = game.getCountryInt(parseInt(next(), 10));
        game.placeArmy(t, 1);
        this.sendMessage(this.t('core.place.getcountry', game.getCurrentPlayer().getName(), t.getName()), false, false);
        game.endGo();
      }
      this.saveGameToUndoObject();
      this.sendMessage('Auto place all successful.', false, false);
      output = this.t('core.player.newselected', this.game.getCurrentPlayer().getName());
    } else if (Addr === 'LEAVE') {
      // a network player left: their humans become "crap" AI (kept for log compatibility)
      const id = next();
      const cur = this.game.getCurrentPlayer();
      if (cur != null && !(cur.getAddress() === id && cur.getType() === PLAYER_HUMAN)) needInput = false;
      output = 'someone has gone: ';
      const leavers = this.game.getPlayers();
      const other = leavers.find((p) => p.getAddress() !== id);
      const newPlayerAddress = other ? other.getAddress() : null;
      for (let c = 0; c < leavers.length; c++) {
        let patc = leavers[c];
        if (patc.getAddress() === id) {
          if (patc.getType() === PLAYER_HUMAN) {
            output += patc.getName() + ' ';
            if (this.game.getState() === RG.STATE_NEW_GAME) {
              if (this.game.delPlayer(patc.getName())) {
                c--;
                this.emit('delPlayer', patc.getName());
                patc = null;
              }
            } else {
              patc.setType(PLAYER_AI_CRAP);
            }
          }
          if (patc != null && newPlayerAddress != null) patc.setAddress(newPlayerAddress);
        }
      }
    } else if (Addr === 'RENAME') {
      const map = new Map();
      for (const part of message.substring(Addr.length + 1).split('&')) {
        const i = part.indexOf('=');
        const dec = (s) => decodeURIComponent(s.replace(/\+/g, ' '));
        map.set(dec(part.substring(0, i)), dec(part.substring(i + 1)));
      }
      try {
        this.renamePlayer(map.get('oldName'), map.get('newName'), map.get('newAddress'), parseInt(map.get('newType'), 10));
      } catch (ex) {
        console.warn('rename error', ex.message);
      }
    } else if (Addr === 'MISSION') {
      const m = [...this.game.getMissions()];
      const p = this.game.getPlayers();
      for (let c = 0; c < p.length; c++) {
        const i = RiskGame.getNumber(next());
        p[c].setMission(m[i]);
        m.splice(i, 1);
      }
      output = null;
      needInput = false;
    } else {
      // parse this normal command
      const game = this.game;
      const echo = message.substring(Addr.length + 1);
      if (game != null && game.getCurrentPlayer() != null && game.getState() !== RG.STATE_GAME_OVER) {
        const type = game.getCurrentPlayer().getType();
        const key = type === PLAYER_HUMAN ? 'newgame.player.type.human' : 'newgame.player.type.' + this.ai.getCommandFromType(type) + 'ai';
        this.sendMessage(game.getCurrentPlayer().getName() + '(' + this.t(key) + ')>' + echo, false, false);
      } else {
        this.sendMessage('game>' + echo, false, false);
      }

      const input = next();
      output = '';
      if (game.getState() === RG.STATE_NEW_GAME) {
        const r = await this.newGameCommand(input, st, pos, Addr);
        output = r.output;
        if (r.needInput === false) needInput = false;
      } else {
        const aiPlayer = game.getCurrentPlayer().getType() !== PLAYER_HUMAN;
        try {
          const r = await this.inGameCommand(input, { next, hasMore, countTokens, Addr });
          output = r.output;
          if (r.needInput === false) needInput = false;
        } catch (ex) {
          if (!ex.userError) throw ex;
          if (aiPlayer) console.warn('bad command from ai: "' + message + '" player=' + game.getCurrentPlayer() + ' state=' + game.getState(), ex.message);
          if (this.replay) throw ex;
          output = ex.message;
        }
      }
      this.updateBattleState();
    }

    if (output != null) {
      const game = this.game;
      if (game == null || game.getState() === RG.STATE_NEW_GAME) this.sendMessage(output, false, true);
      else if (game.getState() === RG.STATE_GAME_OVER) this.sendMessage(output, true, true);
      else if (game.getState() === RG.STATE_END_TURN) {
        if (game.getCurrentPlayer().getAutoEndGo()) this.sendMessage(output, false, false);
        else this.sendMessage(output, true, true);
      } else this.sendMessage(output, true, true);
    }

    if (this.game != null && this.game.getState() === RG.STATE_END_TURN && this.game.getCurrentPlayer().getAutoEndGo()) {
      needInput = false;
      await this.doEndGo();
    }

    if (needInput) await this.getInput();
  }

  /** commands only valid before the game starts. returns the output, or undefined if handled */
  async newGameCommand(input, st, pos, Addr) {
    const game = this.game;
    const args = st.slice(pos);
    const restStr = args.join(' ');
    if (input === 'choosemap') {
      if (args.length >= 1) {
        try {
          const yesmissions = await game.setMapfile(restStr);
          await this.setupPreviews(yesmissions);
          this.sendMessage(this.t('core.choosemap.mapselected', restStr), false, true);
        } catch (e) {
          this.showMessageDialog('Error: ' + e.message);
        }
        return { output: null };
      }
      return { output: this.t('core.error.syntax', 'choosemap filename') };
    }
    if (input === 'choosecards') {
      if (args.length >= 1) {
        try {
          const yesmissions = await game.setCardsfile(restStr);
          this.emit('showCardsFile', game.getCardsFile(), yesmissions);
          return { output: this.t('core.choosecards.chosen', restStr) };
        } catch (e) {
          return { output: this.t('core.choosecards.error.unable') };
        }
      }
      return { output: this.t('core.error.syntax', 'choosecards filename') };
    }
    if (input === 'newplayer') {
      if (args.length >= 3) {
        let i = 0;
        let type = args[i++];
        if (type === 'ai') type = type + ' ' + args[i++];
        const c = args[i++];
        const name = args.slice(i).join(' ');
        const t = this.getType(type);
        const color = ColorUtil.getColor(c);
        if (color !== 0 && t !== -1 && name !== '' && game.addPlayer(t, name, color, this.unlimitedLocalMode ? 'LOCALGAME' : Addr)) {
          this.emit('addPlayer', t, name, color, Addr);
          return { output: this.t('core.newplayer.created', name, c) };
        }
        return { output: this.t('core.newplayer.error.unable') };
      }
      return { output: this.t('core.error.syntax', 'newplayer type (skill) color name') };
    }
    if (input === 'delplayer') {
      if (args.length >= 1) {
        if (game.delPlayer(restStr)) {
          this.emit('delPlayer', restStr);
          return { output: this.t('core.delplayer.deleted', restStr) };
        }
        return { output: this.t('core.delplayer.error.unable') };
      }
      return { output: this.t('core.error.syntax', 'delplayer name') };
    }
    if (input === 'info') {
      if (args.length === 0) {
        let output = this.t('core.info.title') + NL;
        for (const p of game.getPlayers()) output += this.t('core.info.player') + ' ' + p.getName() + NL;
        output += this.t('core.info.mapfile') + ' ' + game.getMapFile() + NL;
        output += this.t('core.info.cardsfile') + ' ' + game.getCardsFile();
        return { output: output };
      }
      return { output: this.t('core.error.syntax', 'info') };
    }
    if (input === 'autosetup') {
      if (args.length === 0) {
        if (game.getPlayers().length === 0) {
          if (!this.replay) {
            for (let c = 1; c <= config.MAX_PLAYERS; c++) {
              const p = this.getDefaultPlayer(c);
              // the original queues these with parser(), so they run after this command
              if (p) this.parser('newplayer ' + p.type + ' ' + p.color + ' ' + p.name);
            }
            return { output: this.t('core.info.autosetup') };
          }
          return { output: 'replay mode, nothing done' };
        }
        return { output: this.t('core.info.autosetup.error') };
      }
      return { output: this.t('core.error.syntax', 'autosetup') };
    }
    if (input === 'startgame') {
      if (args.length >= 2 && args.length <= 5) {
        const n = game.getPlayers().length;
        let newgameType = -1;
        let newgameCardType = -1;
        let maxFiveCards = true;
        let newgameAutoplaceall = false;
        let newgameRecycle = false;
        let defenderDice = null;
        let minimumThreeArmies = true;
        let crap = null;
        for (const newOption of args) {
          if (newOption === STARTGAME_OPTION_MODE_DOMINATION) newgameType = RG.MODE_DOMINATION;
          else if (newOption === STARTGAME_OPTION_MODE_CAPITAL) newgameType = RG.MODE_CAPITAL;
          else if (newOption === STARTGAME_OPTION_MODE_SECRET_MISSION) newgameType = RG.MODE_SECRET_MISSION;
          else if (newOption === STARTGAME_OPTION_CARD_INCREASING_SET) newgameCardType = RG.CARD_INCREASING_SET;
          else if (newOption === STARTGAME_OPTION_CARD_FIXED_SET) newgameCardType = RG.CARD_FIXED_SET;
          else if (newOption === STARTGAME_OPTION_CARD_ITALIAN_LIKE_SET) {
            newgameCardType = RG.CARD_ITALIANLIKE_SET;
            minimumThreeArmies = false;
            maxFiveCards = false;
          } else if (newOption === STARTGAME_OPTION_AUTO_PLACE_ALL) newgameAutoplaceall = true;
          else if (newOption === STARTGAME_OPTION_RECYCLE_CARDS) newgameRecycle = true;
          else if (newOption.startsWith(STARTGAME_OPTION_DEFENDER_DICE)) defenderDice = parseInt(newOption.substring(STARTGAME_OPTION_DEFENDER_DICE.length), 10);
          else crap = newOption;
        }
        if (crap == null) {
          defenderDice = defenderDice == null ? (newgameCardType === RG.CARD_ITALIANLIKE_SET ? 3 : 2) : defenderDice;
          if (newgameType !== -1 && newgameCardType !== -1 && n >= 2 && n <= config.MAX_PLAYERS && defenderDice >= 1 && defenderDice <= 3) {
            this.autoplaceall = newgameAutoplaceall;
            try {
              await game.startGame(newgameType, newgameCardType, newgameRecycle, maxFiveCards, defenderDice, minimumThreeArmies);
            } catch (e) {
              console.warn('startgame error', e);
            }
          }
          if (game.getState() !== RG.STATE_NEW_GAME) {
            this.emit('noInput');
            this.emit('startGame', this.unlimitedLocalMode);
            if (this.shouldGameCommand(Addr)) {
              await this.gameCommand(Addr, 'PLAYER', String(game.getRandomPlayer()));
              if (game.getGameMode() === RG.MODE_SECRET_MISSION) {
                const r = this.random || new Random();
                let a = game.getNoMissions();
                const b = game.getNoPlayers();
                const out = [];
                for (let c = 0; c < b; c++) {
                  out.push(r.nextInt(a));
                  a--;
                }
                await this.gameCommand(Addr, 'MISSION', out.join(' '));
              }
              if (game.getGameMode() === RG.MODE_SECRET_MISSION || this.autoplaceall) {
                const a = game.shuffleCountries();
                await this.gameCommand(Addr, 'PLACEALL', a.map((c) => c.getColor()).join(' '));
              }
            }
            return { output: null, needInput: false };
          }
          if (newgameType === RG.MODE_SECRET_MISSION && game.getNoMissions() < game.getPlayers().length) {
            return { output: 'map does not have enough missions for players' };
          }
          return { output: this.t('core.start.error.players') };
        }
        return { output: 'unknown option: ' + crap };
      }
      return { output: this.t('core.error.syntax', 'startgame gametype cardtype (autoplaceall recycle)') };
    }
    return { output: this.t('core.error.incorrect', 'newplayer, delplayer, startgame, choosemap, choosecards, info, autosetup') };
  }

  renamePlayer(name, newName, newAddress, newType) {
    if (!name || !newName || !newAddress || newType === -1 || Number.isNaN(newType)) {
      throw new Error('bad rename ' + name + ' ' + newName + ' ' + newAddress + ' ' + newType);
    }
    let leaver = null;
    let newNamePlayer = null;
    for (const player of this.game.getPlayers()) {
      if (player.getName() === newName) newNamePlayer = player;
      if (player.getName() === name) leaver = player;
    }
    if (leaver == null) throw new Error('can not find player with name "' + name + '"');
    if (newNamePlayer != null && name !== newName) throw new Error('can not rename "' + name + '". someone with new name "' + newName + '" is already in this game');
    leaver.rename(newName);
    leaver.setType(newType);
    leaver.setAddress(newAddress);
  }

  getDefaultPlayer(c) {
    const d = DEFAULT_PLAYERS[c - 1] || {};
    const type = this.riskconfig.get('default.player' + c + '.type') || d.type;
    const color = this.riskconfig.get('default.player' + c + '.color') || d.color;
    const name = this.riskconfig.get('default.player' + c + '.name') || d.name;
    if (type && color && name) return { type, color, name };
    return null;
  }

  /** commands once the game is running. throws userError for invalid input */
  async inGameCommand(input, tk) {
    const game = this.game;
    const err = (msg) => { const e = new Error(msg); e.userError = true; return e; };
    const syntax = (s) => err(this.t('core.error.syntax', s));
    const aiPlayer = game.getCurrentPlayer().getType() !== PLAYER_HUMAN;
    let output = '';

    if (input === 'undo') {
      if (game.getState() !== RG.STATE_GAME_OVER && aiPlayer) throw new Error('ai is trying to call undo');
      if (!tk.hasMore()) {
        if (this.unlimitedLocalMode) {
          if (game.getState() !== RG.STATE_DEFEND_YOURSELF && this.undo != null) {
            this.closeBattle();
            this.game = await deserializeGame(this.undo, this.io, { language: this.language, resb: this.resb });
            output = this.t('core.undo.undone');
          } else {
            output = this.t('core.undo.error.unable');
          }
        } else output = this.t('core.undo.error.network');
      } else output = this.t('core.error.syntax', 'undo');
      return { output };
    }
    if (input === 'showmission') {
      if (tk.hasMore()) throw syntax('showmission');
      if (this.showHumanCurrentPlayerInfo()) output = this.t('core.showmission.mission') + ' ' + this.getHumanPlayerMission();
      else output = this.t('core.showmission.error');
      return { output };
    }
    if (input === 'showarmies') {
      if (tk.hasMore()) throw syntax('showarmies');
      if (game.getState() !== RG.STATE_NEW_GAME) {
        output = this.t('core.showarmies.countries') + NL;
        for (const v of game.getCountries()) {
          output += v.getColor() + ' ' + v.getName() + ' - ';
          if (v.getOwner() != null) {
            output += v.getOwner().getName() + ' (' + v.getArmies() + ')';
            if (game.getGameMode() === RG.MODE_CAPITAL && game.getSetupDone() && game.getState() !== RG.STATE_SELECT_CAPITAL) {
              for (const p of game.getPlayers()) {
                if (p.getCapital() != null && p.getCapital() === v) output += ' ' + this.t('core.showarmies.captial', p.getName());
              }
            }
            output += NL;
          } else {
            output += this.t('core.showarmies.noowner') + NL;
          }
        }
      } else output = this.t('core.showarmies.error.unable');
      return { output };
    }
    if (input === 'showcards') {
      if (tk.hasMore()) throw syntax('showcards');
      if (this.showHumanCurrentPlayerInfo()) {
        const c = game.getCurrentPlayer().getCards();
        if (c.length === 0) output = this.t('core.showcards.nocards');
        else {
          output = this.t('core.showcards.youhave');
          for (const card of c) {
            if (card.getName() === WILDCARD) output += ' ' + WILDCARD;
            else output += ' "' + card.getName() + ' ' + card.getCountry().getName() + ' (' + card.getCountry().getColor() + ')"';
          }
        }
        if (game.getCardMode() === RG.CARD_FIXED_SET) output += NL + this.t('cards.fixed');
        else if (game.getCardMode() === RG.CARD_ITALIANLIKE_SET) output += NL + this.t('cards.italianlike');
        else output += NL + this.t('cards.nexttrade', game.getNewCardState());
      } else output = this.t('core.showcards.error.unable');
      return { output };
    }
    if (input === 'autoendgo' || input === 'autodefend') {
      const isEndGo = input === 'autoendgo';
      const p = game.getCurrentPlayer();
      const prefix = isEndGo ? 'core.autoendgo' : 'core.autodefend';
      if (!tk.hasMore()) {
        const on = isEndGo ? p.getAutoEndGo() : p.getAutoDefend();
        output = this.t(prefix + '.setto', this.t(prefix + (on ? '.on' : '.off')));
      } else if (tk.countTokens() === 1) {
        const option = tk.next();
        if (option === 'on' || option === 'off') {
          if (isEndGo) p.setAutoEndGo(option === 'on'); else p.setAutoDefend(option === 'on');
          output = this.t(prefix + '.setto', this.t(prefix + '.' + option));
        } else output = this.t(prefix + '.error.unknown', option);
      } else output = this.t('core.error.syntax', input + ' on/off');
      return { output };
    }

    const state = game.getState();
    if (state === RG.STATE_TRADE_CARDS) {
      if (input === 'trade') {
        if (tk.countTokens() !== 3) throw syntax('trade card card card');
        let noa = 0;
        const cards = game.getCardsByName(tk.next(), tk.next(), tk.next());
        if (cards[0] != null && cards[1] != null && cards[2] != null) noa = game.trade(cards[0], cards[1], cards[2]);
        if (noa === 0) throw err(this.t('core.trade.error.unable'));
        return { output: this.t('core.trade.traded', noa) };
      }
      if (input === 'endtrade') {
        if (tk.hasMore()) throw syntax('endtrade');
        if (game.endTrade()) return { output: this.t('core.trade.endtrade') };
        throw err(this.t('core.trade.end.error.unable'));
      }
      throw err(this.t('core.error.incorrect', 'showcards, trade' + (game.canEndTrade() ? ', endtrade' : '')));
    }
    if (state === RG.STATE_PLACE_ARMIES) {
      if (input === 'placearmies') {
        if (tk.countTokens() !== 2) throw syntax('placearmies country number');
        const c = RiskGame.getNumber(tk.next());
        const num = RiskGame.getNumber(tk.next());
        const t = c !== -1 ? game.getCountryInt(c) : null;
        if (t != null && num !== -1 && !(game.getGameMode() === RG.MODE_SECRET_MISSION && t.getOwner() == null)) {
          const result = game.placeArmy(t, num);
          if (result !== 0) {
            output = this.t('core.place.placed', num, t.getName());
            if (result === 2) output += this.whoWon();
            return { output };
          }
          throw err(this.t('core.place.error.unable'));
        }
        throw err(this.t('core.place.error.invalid'));
      }
      if (input === 'autoplace') {
        if (tk.hasMore()) throw syntax('autoplace');
        if (this.shouldGameCommand(tk.Addr)) await this.gameCommand(tk.Addr, 'PLACE', String(game.getRandomCountry()));
        return { output: null, needInput: false };
      }
      throw err(this.t('core.error.incorrect', 'showarmies, placearmies, autoplace'));
    }
    if (state === RG.STATE_ATTACKING) {
      if (input === 'attack') {
        if (tk.countTokens() !== 2) throw syntax('attack country country');
        const a1 = RiskGame.getNumber(tk.next());
        const a2 = RiskGame.getNumber(tk.next());
        const country1 = a1 !== -1 ? game.getCountryInt(a1) : null;
        const country2 = a2 !== -1 ? game.getCountryInt(a2) : null;
        if (game.attack(country1, country2)) {
          return { output: this.t('core.attack.attacking', country2.getName(), country2.getArmies(), country1.getName(), country1.getArmies(), game.getNoAttackDice()) };
        }
        throw err(this.t('core.attack.error.unable'));
      }
      if (input === 'endattack') {
        if (tk.hasMore()) throw syntax('endattack');
        if (game.endAttack()) return { output: this.t('core.attack.end.ended') };
        throw err(this.t('core.attack.end.error.unable'));
      }
      throw err(this.t('core.error.incorrect', 'attack, endattack'));
    }
    if (state === RG.STATE_ROLLING) {
      if (input === 'roll') {
        if (tk.countTokens() !== 1) throw syntax('roll number');
        const dice = RiskGame.getNumber(tk.next());
        if (dice !== -1 && game.rollA(dice)) {
          if (this.battle) this.emit('setNODAttacker', dice);
          const n = game.getNoDefendDice();
          return { output: this.t('core.roll.rolled', game.getCurrentPlayer().getName(), n) };
        }
        throw err(this.t('core.roll.error.unable'));
      }
      if (input === 'retreat') {
        if (tk.hasMore()) throw syntax('retreat');
        if (game.retreat()) return { output: this.t('core.retreat.retreated') };
        throw err(this.t('core.retreat.error.unable'));
      }
      throw err(this.t('core.error.incorrect', 'roll, retreat'));
    }
    if (state === RG.STATE_BATTLE_WON) {
      if (input === 'move') {
        if (tk.countTokens() !== 1) throw syntax('move number');
        const num = tk.next();
        const noa = num === 'all' ? game.moveAll() : RiskGame.getNumber(num);
        const mov = game.moveArmies(noa);
        if (mov !== 0) {
          output = this.t('core.move.moved', noa);
          if (mov === 2) output += this.whoWon();
          return { output };
        }
        throw err(this.t('core.move.error.unable'));
      }
      throw err(this.t('core.error.incorrect', 'move'));
    }
    if (state === RG.STATE_FORTIFYING) {
      if (input === 'movearmies') {
        if (tk.countTokens() !== 3) throw syntax('movearmies country country number');
        const a1 = RiskGame.getNumber(tk.next());
        const a2 = RiskGame.getNumber(tk.next());
        const country1 = a1 !== -1 ? game.getCountryInt(a1) : null;
        const country2 = a2 !== -1 ? game.getCountryInt(a2) : null;
        const noa = RiskGame.getNumber(tk.next());
        if (game.moveArmy(country1, country2, noa)) {
          return { output: this.t('core.tacmove.movedfromto', noa, country1.getName(), country2.getName()) };
        }
        throw err(this.t('core.tacmove.error.unable'));
      }
      if (input === 'nomove') {
        if (tk.hasMore()) throw syntax('nomove');
        if (game.noMove()) return { output: this.t('core.tacmove.no.nomoves') };
        throw err(this.t('core.tacmove.no.error.unable'));
      }
      throw err(this.t('core.error.incorrect', 'movearmies, nomove'));
    }
    if (state === RG.STATE_END_TURN) {
      if (input === 'endgo') {
        if (tk.hasMore()) throw syntax('endgo');
        this.sendMessage(this.t('core.endgo.ended'), false, false);
        await this.doEndGo();
        return { output: null, needInput: false };
      }
      throw err(this.t('core.error.incorrect', 'endgo'));
    }
    if (state === RG.STATE_GAME_OVER) {
      if (input === 'continue') {
        if (tk.hasMore()) throw syntax('continue');
        if (game.continuePlay()) return { output: this.t('core.continue.successful') };
        throw err(this.t('core.continue.error.unable'));
      }
      throw err(this.t('core.gameover.won', game.getCurrentPlayer().getName()));
    }
    if (state === RG.STATE_SELECT_CAPITAL) {
      if (input === 'capital') {
        if (tk.countTokens() !== 1) throw syntax('capital country');
        const id = RiskGame.getNumber(tk.next());
        const t = id !== -1 ? game.getCountryInt(id) : null;
        if (t != null && game.setCapital(t)) {
          if (this.showHumanCurrentPlayerInfo()) return { output: this.t('core.capital.selected', t.getName()) };
          return { output: this.t('core.capital.hasbeenselected') };
        }
        throw err(this.t('core.capital.error.unable'));
      }
      throw err(this.t('core.error.incorrect', 'capital'));
    }
    if (state === RG.STATE_DEFEND_YOURSELF) {
      if (input === 'roll') {
        if (tk.countTokens() !== 1) throw syntax('roll number');
        const dice = RiskGame.getNumber(tk.next());
        if (dice !== -1 && game.rollD(dice)) {
          if (this.battle) {
            this.emit('setNODDefender', dice);
            if (this.rollDiceSleep > 0) await sleep(this.rollDiceSleep);
          }
          if (this.shouldGameCommand(tk.Addr)) {
            const attackerResults = game.rollDice(game.getAttackerDice());
            const defenderResults = game.rollDice(game.getDefenderDice());
            const serverRoll = attackerResults.length + ' ' + defenderResults.length + ' ' +
              attackerResults.join(' ') + (attackerResults.length ? ' ' : '') + defenderResults.join(' ');
            await this.gameCommand(tk.Addr, 'DICE', serverRoll.trim());
          }
          return { output: null, needInput: false };
        }
        throw err(this.t('core.roll.error.unable'));
      }
      throw err(this.t('core.error.incorrect', 'roll'));
    }
    throw new Error(this.t('core.error.unknownstate'));
  }

  /** works out what input is required next, asks the AI or the UI */
  async getInput() {
    if (this.inbox.length > 0) return;
    this.setHelp();
    const game = this.game;
    if (game == null) {
      this.emit('needInput', -1);
    } else if (game.getCurrentPlayer() != null && game.getState() !== RG.STATE_GAME_OVER) {
      this.updateBattleState();
      if (game.getState() === RG.STATE_TRADE_CARDS) {
        this.sendMessage(this.t('core.input.newarmies', game.getCurrentPlayer().getExtraArmies()), false, false);
      } else if (game.getState() === RG.STATE_PLACE_ARMIES) {
        this.sendMessage(this.t('core.input.armiesleft', game.getCurrentPlayer().getExtraArmies()), false, false);
      }
      if (this.shouldGameCommand(game.getCurrentPlayer().getAddress())) {
        if (game.getState() === RG.STATE_DEFEND_YOURSELF && game.getCurrentPlayer().getAutoDefend()) {
          await this.gameCommand(this.myAddress, this.myAddress, this.getBasicPassiveGo());
        } else if (game.getCurrentPlayer().getType() === PLAYER_HUMAN) {
          this.emit('needInput', game.getState());
        } else {
          await this.aiPlay();
        }
      }
    } else {
      this.emit('needInput', game.getState());
    }
  }

  async aiPlay() {
    const game = this.game;
    const output = this.ai.getOutput(game, game.getCurrentPlayer().getType());
    if (this.ai.wait > 0) await sleep(this.ai.wait);
    if (this.game !== game) return; // game closed while waiting
    this.parser(output);
  }

  async doEndGo() {
    this.emit('noInput');
    const Addr = this.game.getCurrentPlayer().getAddress();
    if (this.shouldGameCommand(Addr)) {
      await this.gameCommand(Addr, 'CARD', this.game.getDesrvedCard());
    }
  }

  shouldGameCommand(/* Addr */) {
    return !this.replay;
  }

  async gameCommand(address, gameCommandOrAddress, options) {
    if (this.replay) throw new Error('game command sent during replay');
    await this.inGameParser(gameCommandOrAddress + ' ' + options);
  }

  getBasicPassiveGo() {
    return this.ai.getOutput(this.game, PLAYER_AI_CRAP);
  }

  getAICommands() { return this.ai.getAICommands(); }
  getCommandFromType(type) { return this.ai.getCommandFromType(type); }

  getType(type) {
    if (type === 'human') return PLAYER_HUMAN;
    if (type != null && type.startsWith('ai ')) {
      try { return this.ai.getTypeFromCommand(type.substring(3)); } catch (e) { return -1; }
    }
    return -1;
  }

  getTypeString(type) {
    return type === PLAYER_HUMAN ? 'human' : 'ai ' + this.ai.getCommandFromType(type);
  }

  whoWon() {
    const winner = this.getWinner();
    const iWon = winner.getType() === PLAYER_HUMAN && this.unlimitedLocalMode;
    this.emit('gameOver', iWon);
    let text = NL + this.t('core.whowon.hehaswon', winner.getName());
    if (this.game.getGameMode() === RG.MODE_SECRET_MISSION) {
      text += NL + this.t('core.whowon.mission', winner.getMission().getDiscription());
    }
    return text;
  }

  getWinner() {
    if (this.game.getState() === RG.STATE_GAME_OVER) return this.game.getCurrentPlayer();
    return null;
  }

  setHelp() {
    const game = this.game;
    let help = '';
    if (game != null && game.getCurrentPlayer() != null) {
      const type = game.getCurrentPlayer().getType();
      const strId = type === PLAYER_HUMAN ? 'core.help.move.human' : 'core.help.move.ai.' + this.ai.getCommandFromType(type);
      if (this.resb.has(strId)) help = this.t(strId, game.getCurrentPlayer().getName()) + ' ';
      else help = strId + ': (' + game.getCurrentPlayer().getName() + ') ';
    }
    if (game == null) help = this.t('core.help.newgame');
    else {
      const s = game.getState();
      if (s === RG.STATE_NEW_GAME) help = this.t('core.help.createplayers');
      else if (s === RG.STATE_TRADE_CARDS) help += this.t('core.help.trade');
      else if (s === RG.STATE_PLACE_ARMIES) {
        if (game.getSetupDone()) help += this.t('core.help.placearmies');
        else if (game.NoEmptyCountries()) help += this.t('core.help.placearmy');
        else help += this.t('core.help.placearmyempty');
      } else if (s === RG.STATE_ATTACKING) help += this.t('core.help.attack');
      else if (s === RG.STATE_ROLLING) help += this.t('core.help.rollorretreat');
      else if (s === RG.STATE_BATTLE_WON) help += this.t('core.help.youhavewon');
      else if (s === RG.STATE_FORTIFYING) help += this.t('core.help.fortifyposition');
      else if (s === RG.STATE_END_TURN) help += this.t('core.help.endgo');
      else if (s === RG.STATE_GAME_OVER) {
        const w = game.getCurrentPlayer();
        help = this.t('core.help.gameover', w == null ? 'null' : w.getName());
      } else if (s === RG.STATE_SELECT_CAPITAL) help += this.t('core.help.selectcapital');
      else if (s === RG.STATE_DEFEND_YOURSELF) help += this.t('core.help.defendyourself');
      else help = this.t('core.help.error.unknownstate');
    }
    this.emit('setGameStatus', help);
  }

  saveGameToUndoObject() {
    if (this.skipUndo) return;
    if (this.unlimitedLocalMode) {
      try {
        this.undo = serializeGame(this.game);
      } catch (e) {
        this.undo = null;
        this.skipUndo = true;
        console.warn('undo error', e);
      }
    }
  }

  canUndo() {
    return this.undo != null;
  }

  updateBattleState() {
    const game = this.game;
    if (game == null) return;
    if ((game.getState() === RG.STATE_ROLLING || game.getState() === RG.STATE_DEFEND_YOURSELF) && !this.battle) {
      const attackingPlayer = game.getAttacker().getOwner();
      const defendingPlayer = game.getDefender().getOwner();
      if (this.showHumanPlayerThereInfo(attackingPlayer) || this.showHumanPlayerThereInfo(defendingPlayer)) {
        this.emit('openBattle', game.getAttacker().getColor(), game.getDefender().getColor());
        if (game.getState() === RG.STATE_DEFEND_YOURSELF) this.emit('setNODAttacker', game.getAttackerDice());
        this.battle = true;
      }
    } else if (game.getState() !== RG.STATE_ROLLING && game.getState() !== RG.STATE_DEFEND_YOURSELF) {
      this.closeBattle();
    }
  }

  closeBattle() {
    if (this.battle) {
      this.emit('closeBattle');
      this.battle = false;
    }
  }

  closeGame() {
    this.inbox.length = 0;
    if (this.game != null) {
      this.closeBattle();
      this.emit('closeGame');
      this.ai.closeGame();
      this.game = null;
      this.undo = null;
    }
  }

  async doesMapHaveMission() {
    try {
      const text = await this.io.readMap(this.game.getCardsFile());
      return /^\[missions\]/m.test(text || '');
    } catch (e) {
      return false;
    }
  }

  async setupPreviews(yesmissions) {
    this.emit('showMapPic', this.game);
    this.emit('showCardsFile', this.game.getCardsFile(), yesmissions);
  }

  /** true ONLY if info of this Player p should be disclosed on this computer */
  showHumanPlayerThereInfo(p) {
    return this.game.getState() === RG.STATE_GAME_OVER || (p != null && p.getType() === PLAYER_HUMAN && this.unlimitedLocalMode);
  }

  showHumanCurrentPlayerInfo() {
    return this.showHumanPlayerThereInfo(this.game.getCurrentPlayer());
  }

  getHumanPlayerMission() {
    const game = this.game;
    switch (game.getGameMode()) {
      case RG.MODE_DOMINATION: return this.t('core.mission.conquerworld');
      case RG.MODE_CAPITAL: return this.t('core.mission.capturecapitals');
      case RG.MODE_SECRET_MISSION: {
        const human = this.getSingleLocalHumanPlayer();
        if (human != null) return human.getMission().getDiscription();
        return game.getCurrentPlayer().getMission().getDiscription();
      }
      default: return this.t('core.mission.error.cantshow');
    }
  }

  getSingleLocalHumanPlayer() {
    const game = this.game;
    if (game == null || game.getState() === RG.STATE_NEW_GAME || game.getState() === RG.STATE_GAME_OVER) return null;
    let human1 = null;
    let tooMany1 = false;
    for (const player of game.getPlayers()) {
      if (player.isAlive() && player.getType() === PLAYER_HUMAN) {
        if (human1 == null) human1 = player; else tooMany1 = true;
      }
    }
    if (human1 != null && !tooMany1) return human1;
    return null;
  }

  /** colours of the players still in the game, starting with the current one (bottom bar) */
  getPlayerColors() {
    const g = this.game;
    if (g == null) return [];
    if (g.getState() === RG.STATE_DEFEND_YOURSELF && g.getDefender() != null) return [g.getDefender().getOwner().getColor()];
    const players = g.getPlayers();
    const setup = g.getSetupDone();
    let start = 0;
    const alive = [];
    for (let c = 0; c < players.length; c++) {
      if (players[c] === g.getCurrentPlayer()) start = c;
    }
    for (let i = 0; i < players.length; i++) {
      const p = players[(start + i) % players.length];
      if (p.getNoTerritoriesOwned() > 0 || !setup) alive.push(p.getColor());
    }
    return alive;
  }

  isOwnedCurrentPlayerInt(id) {
    const g = this.game;
    if (g == null || g.getCurrentPlayer() == null) return false;
    const c = g.getCountryInt(id);
    return c != null && (c.getOwner() == null || c.getOwner() === g.getCurrentPlayer());
  }

  hasArmiesInt(id) {
    return this.game.getCountryInt(id).getArmies();
  }

  canAttack(from, to) {
    return this.game.getCountryInt(from).isNeighbours(this.game.getCountryInt(to));
  }

  canTrade(c1, c2, c3) {
    if (this.game.getState() === RG.STATE_TRADE_CARDS) {
      const cards = this.game.getCardsByName(c1, c2, c3);
      if (cards[0] == null || cards[1] == null || cards[2] == null) return false;
      return this.game.checkTrade(cards[0], cards[1], cards[2]);
    }
    return false;
  }

  getGame() { return this.game; }
  getLocalGame() { return this.unlimitedLocalMode; }
  getAutoEndGo() {
    return this.game != null && this.game.getCurrentPlayer() != null ? this.game.getCurrentPlayer().getAutoEndGo() : false;
  }
  getAutoDefend() {
    return this.game != null && this.game.getCurrentPlayer() != null ? this.game.getCurrentPlayer().getAutoDefend() : false;
  }
  getCountryCapital(id) {
    const t = this.game.getCountryInt(id);
    return this.game.getPlayers().find((p) => p.getCapital() === t) || null;
  }
}

export { Card, Player };
