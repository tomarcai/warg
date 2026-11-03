// warg - JavaScript port of net.yura.domination.engine.core.RiskGame (Yura Mamyrin). GPLv3.
// The game model and every rule of the game live here. Behaviour is kept identical to the
// original Java engine (NETWORK_VERSION 16).

import { Random, javaHashMapKeyOrder, tokenize } from './util.js';
import * as ColorUtil from './colors.js';
import {
  Country, Continent, Card, Mission, Player,
  CAVALRY, INFANTRY, CANNON, WILDCARD,
} from './model.js';
import { loadBundle } from './i18n.js';

export const STATE_NEW_GAME = 0;
export const STATE_TRADE_CARDS = 1;
export const STATE_PLACE_ARMIES = 2;
export const STATE_ATTACKING = 3;
export const STATE_ROLLING = 4;
export const STATE_BATTLE_WON = 5;
export const STATE_FORTIFYING = 6;
export const STATE_END_TURN = 7;
export const STATE_GAME_OVER = 8;
export const STATE_SELECT_CAPITAL = 9;
export const STATE_DEFEND_YOURSELF = 10;

export const MODE_DOMINATION = 0;
export const MODE_CAPITAL = 2;
export const MODE_SECRET_MISSION = 3;

export const CARD_INCREASING_SET = 0;
export const CARD_FIXED_SET = 1;
export const CARD_ITALIANLIKE_SET = 2;

export const DEFAULT_MAX_CARDS = 5;
export const DEFAULT_MINIMUM_NEW_ARMIES = 3;
export const DEFAULT_EXTRA_ARMIES_FOR_CARD = 2;

export const NETWORK_VERSION = '16';
export const SAVE_VERSION = 'warg-1';

export const ANY_CONTINENT = new Continent('any', 'any', 0, 0);

export const config = {
  MAX_PLAYERS: 6,
  defaultMap: 'luca.map',
  defaultCards: 'risk.cards',
};

/** Returns the directory part of a map path ("RiskUtil.getContext") */
function getContext(f) {
  const i = f.lastIndexOf('/');
  return i >= 0 ? f.substring(0, i + 1) : '';
}

/**
 * MapTranslator: looks for map_xx.properties next to the map, otherwise the DefaultMaps bundle.
 */
export class MapTranslator {
  constructor(io, language) {
    this.io = io;
    this.language = language;
    this.mapResb = null;
    this.cardsResb = null;
  }
  async setMap(strFile) {
    if (strFile == null || !this.language) { this.mapResb = null; return; }
    const strName = strFile.substring(0, strFile.lastIndexOf('.'));
    const text = await this.io.readMap(strName + '_' + this.language.split(/[_-]/)[0] + '.properties');
    if (text != null) {
      this.mapResb = await loadBundle(async (n) => (n.endsWith('.properties') && n.startsWith('__map') ? text : null), '__map', null);
    } else {
      this.mapResb = await loadBundle((n) => this.io.readResource(n), 'DefaultMaps', this.language);
    }
  }
  getTranslatedMapName(s) {
    if (this.mapResb == null) return s;
    try { return this.mapResb.getString(s); } catch (e) { return s; }
  }
  async setCards(file) {
    if (!this.language) { this.cardsResb = null; return; }
    const strName = file.substring(0, file.lastIndexOf('.'));
    const text = await this.io.readMap(strName + '_' + this.language.split(/[_-]/)[0] + '.properties');
    if (text != null) {
      this.cardsResb = await loadBundle(async (n) => (n.startsWith('__cards') ? text : null), '__cards', null);
    } else if (file === 'risk.cards') {
      this.cardsResb = await loadBundle((n) => this.io.readResource(n), 'DefaultCards', this.language);
    } else {
      this.cardsResb = null;
    }
  }
  getTranslatedMissionName(id) {
    if (this.cardsResb == null) return null;
    try { return this.cardsResb.getString(id); } catch (e) { return null; }
  }
}

function lines(text) {
  return text.split(/\r\n|\r|\n/);
}

export class RiskGame {
  /**
   * @param io object with async readMap(name) and readResource(name) returning text or null
   * @param options {language, resb (Risk bundle used for color names in missions), random}
   */
  constructor(io, options = {}) {
    this.io = io;
    this.language = options.language || null;
    this.resb = options.resb || null;
    this.r = options.random || new Random();

    this.mapfile = null;
    this.cardsfile = null;
    this.ImagePic = null;
    this.ImageMap = null;
    this.previewPic = null;
    this.properties = new Map();

    this.gameMode = 0;
    this.cardMode = 0;
    this.recycleCards = false;
    this.maxDefendDice = 0;
    this.minimumNewArmies = DEFAULT_MINIMUM_NEW_ARMIES;
    this.maxCardsPerPlayer = DEFAULT_MAX_CARDS;
    this.runmaptest = false;
    this.replayCommands = [];

    this.Players = [];
    this.Countries = null;
    this.Continents = null;
    this.Cards = [];
    this.usedCards = [];
    this.Missions = [];

    this.currentPlayer = null;
    this.setup = 0;
    this.gameState = STATE_NEW_GAME;
    this.cardState = 0;
    this.mustmove = 0;
    this.capturedCountry = false;
    this.tradeCap = false;
    this.attacker = null;
    this.defender = null;
    this.attackerDice = 0;
    this.defenderDice = 0;
    this.battleRounds = 0;
  }

  /** async factory, same as "new RiskGame()" in Java (selects the default map and cards) */
  static async create(io, options = {}) {
    const g = new RiskGame(io, options);
    await g.setMapfile('default');
    await g.setCardsfile('default');
    return g;
  }

  addCommand(a) { this.replayCommands.push(a); }
  getCommands() { return this.replayCommands; }
  setCommands(c) { this.replayCommands = c; }
  getMaxDefendDice() { return this.maxDefendDice; }
  getMaxCardsPerPlayer() { return this.maxCardsPerPlayer; }

  addPlayer(type, name, color, address) {
    if (this.gameState === STATE_NEW_GAME) {
      for (const p of this.Players) {
        if (name === p.getName() || color === p.getColor()) return false;
      }
      this.Players.push(new Player(type, name, color, address));
      return true;
    }
    return false;
  }

  delPlayer(name) {
    if (this.gameState === STATE_NEW_GAME) {
      let n = -1;
      for (let c = 0; c < this.Players.length; c++) {
        if (name === this.Players[c].getName()) n = c;
      }
      if (n === -1) return false;
      this.Players.splice(n, 1);
      return true;
    }
    return false;
  }

  async startGame(mode, card, recycle, maxFiveCards, maxDefendDice, minimumThreeReinforcements) {
    if (maxDefendDice < 1 || maxDefendDice > 3) throw new Error('bad max defend dice ' + maxDefendDice);
    if (this.gameState === STATE_NEW_GAME) {
      this.gameMode = mode;
      this.cardMode = card;
      this.recycleCards = recycle;
      this.maxDefendDice = maxDefendDice;
      this.minimumNewArmies = minimumThreeReinforcements ? DEFAULT_MINIMUM_NEW_ARMIES : 0;
      this.maxCardsPerPlayer = maxFiveCards ? DEFAULT_MAX_CARDS : Number.MAX_SAFE_INTEGER;

      if (this.mapfile != null && this.cardsfile != null) {
        await this.loadMap();
        try {
          await this.loadCards(false);
        } catch (e) {
          if (this.runmaptest) {
            throw new Error('LOAD FILE ERROR: ' + e.message + '\n(This normally means you have selected the wrong set of cards for this map)');
          }
          console.log(e);
          return;
        }
        if (this.runmaptest) this.testMap();
      }

      if (this.Countries == null) return;
      if (this.gameMode === MODE_SECRET_MISSION && this.Missions.length < this.Players.length) return;

      // Math.round(float) in java == floor(x + 0.5)
      const armies = Math.max(1, 10 - this.Players.length) * Math.floor(this.Countries.length * Math.fround(0.12) + 0.5);
      for (const p of this.Players) p.addArmies(armies);

      this.gameState = STATE_PLACE_ARMIES;
      this.capturedCountry = false;
      this.tradeCap = false;
    }
  }

  testMap() {
    for (const c1 of this.Countries) {
      const c1n = c1.getNeighbours();
      if (c1n.includes(c1)) throw new Error('Error: ' + c1.getName() + ' neighbours with itself');
      for (const c2 of c1n) {
        if (!c2.getNeighbours().includes(c1)) {
          throw new Error('Borders error with: ' + c1.getName() + ' (' + c1.getColor() + ') and ' + c2.getName() + ' (' + c2.getColor() + ')');
        }
      }
    }
  }

  setCurrentPlayer(c) {
    this.currentPlayer = this.Players[c];
    return this.currentPlayer;
  }

  getRandomPlayer() { return this.r.nextInt(this.Players.length); }

  getDesrvedCard() {
    if (this.capturedCountry === true && this.Cards.length > 0) {
      const c = this.Cards[this.r.nextInt(this.Cards.length)];
      if (c.getCountry() == null) return WILDCARD;
      return String(c.getCountry().getColor());
    }
    return '';
  }

  isCapturedCountry() { return this.capturedCountry; }

  endGo() {
    if (this.gameState !== STATE_END_TURN) return null;

    const setupDone = this.getSetupDone();
    const base = this.Players.indexOf(this.currentPlayer);
    let found = false;
    for (let i = 1; i <= this.Players.length && !found; i++) {
      this.currentPlayer = this.Players[(base + i) % this.Players.length];
      found = !setupDone || (this.currentPlayer.getNoTerritoriesOwned() > 0 && (
        this.getExtraArmiesForPlayer(this.currentPlayer) > 0 ||
        this.canTrade() ||
        RiskGame.canAttackOrMove(this.currentPlayer, true) ||
        RiskGame.canAttackOrMove(this.currentPlayer, false)));
    }

    if (!found) {
      this.gameState = STATE_GAME_OVER;
    } else {
      const pickingCapital = this.gameMode === MODE_CAPITAL && this.currentPlayer.getCapital() == null;
      if (setupDone && !pickingCapital) {
        this.workOutEndGoStats(this.currentPlayer);
        this.currentPlayer.nextTurn();
        this.currentPlayer.addArmies(this.getExtraArmiesForPlayer(this.currentPlayer));
      }
      if (setupDone && pickingCapital) {
        this.gameState = STATE_SELECT_CAPITAL;
      } else if (this.canTrade()) {
        this.gameState = STATE_TRADE_CARDS;
      } else {
        this.goIntoPlaceArmiesState();
      }
    }
    this.capturedCountry = false;
    this.tradeCap = false;
    return this.currentPlayer;
  }

  getExtraArmiesForPlayer(p) {
    let extraArmies = Math.trunc(p.getNoTerritoriesOwned() / 3);
    if (extraArmies < this.minimumNewArmies) extraArmies = this.minimumNewArmies;
    for (const co of this.Continents) {
      if (co.isOwned(p)) extraArmies += co.getArmyValue();
    }
    return extraArmies;
  }

  goIntoPlaceArmiesState() {
    if (this.currentPlayer.getExtraArmies() > 0) {
      this.gameState = STATE_PLACE_ARMIES;
    } else if (RiskGame.canAttackOrMove(this.currentPlayer, true)) {
      this.gameState = STATE_ATTACKING;
    } else if (RiskGame.canAttackOrMove(this.currentPlayer, false)) {
      this.gameState = STATE_FORTIFYING;
    } else {
      this.gameState = STATE_END_TURN;
    }
  }

  static canAttackOrMove(player, attack) {
    for (const source of player.getTerritoriesOwned()) {
      if (source.getArmies() > 1) {
        for (const target of source.getNeighbours()) {
          if (attack && target.getOwner() !== player) return true;
          if (!attack && target.getOwner() === player) return true;
        }
      }
    }
    return false;
  }

  trade(card1, card2, card3) {
    if (this.gameState !== STATE_TRADE_CARDS) return 0;
    if (this.tradeCap && this.currentPlayer.getCards().length < this.maxCardsPerPlayer) {
      throw new Error('trying to do a trade when less then ' + this.maxCardsPerPlayer + ' cards and tradeCap is on');
    }
    let armies = this.getTradeAbsValue(card1.getName(), card2.getName(), card3.getName(), this.cardMode);
    if (armies <= 0) return 0;
    if (this.cardMode === CARD_INCREASING_SET) this.cardState = armies;

    const italian = this.cardMode === CARD_ITALIANLIKE_SET;
    const cardCountriesOwned = this.currentPlayer.tradeInCards(card1, card2, card3, italian ? 0 : DEFAULT_EXTRA_ARMIES_FOR_CARD);
    if (italian) armies = armies + cardCountriesOwned * DEFAULT_EXTRA_ARMIES_FOR_CARD;

    this.usedCards.push(card1, card2, card3);
    this.recycleUsedCards();
    this.currentPlayer.addArmies(armies);

    if (!this.canTrade() || (this.tradeCap && this.currentPlayer.getCards().length < this.maxCardsPerPlayer)) {
      this.gameState = STATE_PLACE_ARMIES;
      this.tradeCap = false;
    }
    return armies;
  }

  getTradeAbsValue(c1, c2, c3, cardMode) {
    let armies = 0;
    // shift wildcards to the front
    if (c1 !== WILDCARD) { const n4 = c3; c3 = c1; c1 = n4; }
    if (c2 !== WILDCARD) { const n4 = c3; c3 = c2; c2 = n4; }
    if (c1 !== WILDCARD) { const n4 = c2; c2 = c1; c1 = n4; }

    if (cardMode === CARD_INCREASING_SET) {
      if (c1 === WILDCARD || (c1 === c2 && c1 === c3) || (c1 !== c2 && c1 !== c3 && c2 !== c3)) {
        armies = this.getNewCardState();
      }
    } else if (cardMode === CARD_FIXED_SET) {
      if ((c1 === c2 || c1 === WILDCARD) && c2 === c3) {
        if (c3 === INFANTRY) armies = 4;
        else if (c3 === CAVALRY) armies = 6;
        else if (c3 === CANNON) armies = 8;
        else armies = 12;
      } else if ((c1 === WILDCARD && c2 === WILDCARD) || (c1 !== c2 && c2 !== c3 && c1 !== c3)) {
        armies = 10;
      }
    } else {
      if (c1 === c2 && c1 === c3) {
        if (c1 === CAVALRY) armies = 8;
        else if (c1 === INFANTRY) armies = 6;
        else if (c1 === CANNON) armies = 4;
        else armies = 0;
      } else if (c1 !== c2 && c2 !== c3 && c1 !== c3 && c1 !== WILDCARD) {
        armies = 10;
      } else if (c1 === WILDCARD && c2 === c3) {
        armies = 12;
      } else {
        armies = 0;
      }
    }
    return armies;
  }

  canTrade() {
    return this.getBestTrade(this.currentPlayer.getCards(), null) > 0;
  }

  /** greedy search for the best trade (same iteration order as the Java HashMap) */
  getBestTrade(cards, bestResult) {
    const cardTypes = new Map();
    for (const card of cards) {
      let list = cardTypes.get(card.getName());
      if (!list) { list = []; cardTypes.set(card.getName(), list); }
      list.push(card);
    }
    const getCard = (name) => { const t = cardTypes.get(name); return t ? t[0] : null; };
    let carda = null, cardb = null, cardc = null;
    let bestValue = 0;
    if (cardTypes.size >= 3) {
      carda = getCard(CANNON) || getCard(WILDCARD);
      cardb = getCard(CAVALRY) || getCard(WILDCARD);
      cardc = getCard(INFANTRY) || getCard(WILDCARD);
      bestValue = this.getTradeAbsValue(carda.getName(), cardb.getName(), cardc.getName(), this.cardMode);
      if (bestValue > 0) {
        if (bestResult == null) return bestValue;
        bestResult[0] = carda; bestResult[1] = cardb; bestResult[2] = cardc;
      }
    }
    const wildCards = cardTypes.get(WILDCARD);
    const wildCardCount = wildCards ? wildCards.length : 0;
    for (const key of javaHashMapKeyOrder([...cardTypes.keys()])) {
      const cardList = cardTypes.get(key);
      carda = null;
      if (key === WILDCARD) {
        if (wildCardCount >= 3) {
          carda = wildCards[0]; cardb = wildCards[1]; cardc = wildCards[2];
        }
      } else if (cardList.length + wildCardCount >= 3) {
        carda = cardList[0];
        cardb = cardList.length > 1 ? cardList[1] : wildCards[0];
        cardc = cardList.length > 2 ? cardList[2] : wildCards[2 - cardList.length];
      }
      if (carda != null) {
        const val = this.getTradeAbsValue(carda.getName(), cardb.getName(), cardc.getName(), this.cardMode);
        if (val > bestValue) {
          bestValue = val;
          if (bestResult == null) return bestValue;
          bestResult[0] = carda; bestResult[1] = cardb; bestResult[2] = cardc;
        }
      }
    }
    return bestValue;
  }

  getNewCardState() {
    const cs = this.cardState;
    if (cs < 4) return cs + 4;
    if (cs < 12) return cs + 2;
    if (cs < 15) return cs + 3;
    return cs + 5;
  }

  checkTrade(card1, card2, card3) {
    return this.getTradeAbsValue(card1.getName(), card2.getName(), card3.getName(), this.cardMode) > 0;
  }

  endTrade() {
    if (this.canEndTrade()) {
      if (this.tradeCap) throw new Error('endTrade worked when tradeCap was true');
      this.goIntoPlaceArmiesState();
      return true;
    }
    return false;
  }

  canEndTrade() {
    if (this.gameState === STATE_TRADE_CARDS) {
      if (this.currentPlayer.getCards().length < this.maxCardsPerPlayer) return true;
    }
    return false;
  }

  placeArmy(t, n) {
    let done = 0;
    if (this.gameState === STATE_PLACE_ARMIES) {
      if (!this.getSetupDone()) {
        if (n !== 1) return 0;
        if (t.getOwner() === this.currentPlayer) {
          if (this.NoEmptyCountries()) {
            t.addArmy();
            this.currentPlayer.loseExtraArmy(1);
            done = 1;
          }
        } else if (t.getOwner() == null) {
          t.setOwner(this.currentPlayer);
          this.currentPlayer.newCountry(t);
          t.addArmy();
          this.currentPlayer.loseExtraArmy(1);
          done = 1;
        }
      } else if (t.getOwner() === this.currentPlayer && this.currentPlayer.getExtraArmies() >= n) {
        this.currentPlayer.currentStatistic.addReinforcements(n);
        t.addArmies(n);
        this.currentPlayer.loseExtraArmy(n);
        done = 1;
      }

      if (done === 1) {
        if (this.getSetupDone()) {
          if (this.currentPlayer.getExtraArmies() !== 0) this.gameState = STATE_PLACE_ARMIES;
          else this.gameState = STATE_ATTACKING;
        } else {
          if (this.currentPlayer.getExtraArmies() === 0) this.setup++;
          this.gameState = STATE_END_TURN;
        }
        if (this.checkPlayerWon()) done = 2;
      }
    }
    return done;
  }

  getRandomCountry() {
    if (this.gameState === STATE_PLACE_ARMIES) {
      if (this.NoEmptyCountries()) {
        const countries = this.currentPlayer.getTerritoriesOwned();
        return countries[this.r.nextInt(countries.length)].getColor();
      }
      const a = this.r.nextInt(this.Countries.length);
      let done = false;
      for (let c = a; c < this.Countries.length; c++) {
        if (this.Countries[c].getOwner() == null) return this.Countries[c].getColor();
        else if (c === this.Countries.length - 1 && !done) { c = -1; done = true; }
        else if (c === this.Countries.length - 1 && done) break;
      }
    }
    throw new Error('IllegalStateException');
  }

  attack(t1, t2) {
    let result = false;
    if (this.gameState === STATE_ATTACKING) {
      if (t1 != null && t2 != null && t1.getOwner() === this.currentPlayer && t2.getOwner() !== this.currentPlayer &&
          t1.isNeighbours(t2) && t1.getArmies() > 1) {
        this.currentPlayer.currentStatistic.addAttack();
        t2.getOwner().currentStatistic.addAttacked();
        result = true;
        this.attacker = t1;
        this.defender = t2;
        this.battleRounds = 0;
        this.gameState = STATE_ROLLING;
      }
    }
    return result;
  }

  endAttack() {
    if (this.gameState === STATE_ATTACKING) {
      this.gameState = STATE_FORTIFYING;
      return true;
    }
    return false;
  }

  rollA(dice1) {
    if (this.gameState === STATE_ROLLING) {
      if (this.attacker.getArmies() > 4) {
        if (dice1 <= 0 || dice1 > 3) return false;
      } else if (dice1 <= 0 || dice1 > (this.attacker.getArmies() - 1)) return false;
      this.attackerDice = dice1;
      this.currentPlayer = this.defender.getOwner();
      this.gameState = STATE_DEFEND_YOURSELF;
      return true;
    }
    return false;
  }

  rollD(dice2) {
    if (this.gameState === STATE_DEFEND_YOURSELF) {
      if (this.defender.getArmies() > this.maxDefendDice) {
        if (dice2 <= 0 || dice2 > this.maxDefendDice) return false;
      } else if (dice2 <= 0 || dice2 > this.defender.getArmies()) return false;
      this.currentPlayer = this.attacker.getOwner();
      this.defenderDice = dice2;
      return true;
    }
    return false;
  }

  getAttackerDice() { return this.attackerDice; }
  getDefenderDice() { return this.defenderDice; }
  getBattleRounds() { return this.battleRounds; }

  /**
   * result[0] worked, [1] attacker lost, [2] defender lost,
   * [3] 0 still armies left, 1 all killed, 2 player eliminated, [4] min move, [5] max move
   */
  battle(attackerResults, defenderResults) {
    const result = [0, 0, 0, 0, 0, 0];
    if (this.gameState === STATE_DEFEND_YOURSELF) {
      this.battleRounds++;
      for (const a of attackerResults) this.attacker.getOwner().currentStatistic.addDice(a);
      for (const d of defenderResults) this.defender.getOwner().currentStatistic.addDice(d);

      for (let c = 0; c < Math.min(attackerResults.length, defenderResults.length); c++) {
        if (attackerResults[c] > defenderResults[c]) {
          this.defender.looseArmy();
          this.defender.getOwner().currentStatistic.addCasualty();
          this.attacker.getOwner().currentStatistic.addKill();
          result[2]++;
        } else {
          this.attacker.looseArmy();
          this.attacker.getOwner().currentStatistic.addCasualty();
          this.defender.getOwner().currentStatistic.addKill();
          result[1]++;
        }
      }

      if (this.defender.getArmies() === 0) {
        this.attacker.getOwner().currentStatistic.addCountriesWon();
        this.defender.getOwner().currentStatistic.addCountriesLost();
        result[5] = this.attacker.getArmies() - 1;
        this.capturedCountry = true;
        const lostPlayer = this.defender.getOwner();
        lostPlayer.lostCountry(this.defender);
        this.currentPlayer.newCountry(this.defender);
        this.defender.setOwner(this.attacker.getOwner());
        result[3] = 1;
        this.gameState = STATE_BATTLE_WON;
        this.mustmove = attackerResults.length;
        result[4] = this.mustmove;

        if (lostPlayer.getNoTerritoriesOwned() === 0) {
          result[3] = 2;
          this.currentPlayer.addPlayersEliminated(lostPlayer);
          while (lostPlayer.getCards().length > 0) {
            this.currentPlayer.giveCard(lostPlayer.takeCard());
          }
          if (this.currentPlayer.getCards().length > this.maxCardsPerPlayer) {
            this.tradeCap = true;
          }
        }
      } else if (this.attacker.getArmies() === 1) {
        this.gameState = STATE_ATTACKING;
        this.currentPlayer.currentStatistic.addRetreat();
      } else {
        this.gameState = STATE_ROLLING;
      }
      this.defenderDice = 0;
      this.attackerDice = 0;
      result[0] = 1;
    }
    return result;
  }

  moveArmies(noa) {
    if (this.gameState === STATE_BATTLE_WON && this.mustmove > 0 && noa >= this.mustmove && noa < this.attacker.getArmies()) {
      this.attacker.removeArmies(noa);
      this.defender.addArmies(noa);
      this.gameState = this.tradeCap ? STATE_TRADE_CARDS : STATE_ATTACKING;
      this.attacker = null;
      this.defender = null;
      this.mustmove = 0;
      return this.checkPlayerWon() ? 2 : 1;
    }
    return 0;
  }

  moveAll() {
    if (this.gameState === STATE_BATTLE_WON && this.mustmove > 0) {
      return this.attacker.getArmies() - 1;
    }
    return -1;
  }

  getMustMove() { return this.mustmove; }

  retreat() {
    if (this.gameState === STATE_ROLLING) {
      this.currentPlayer.currentStatistic.addRetreat();
      this.gameState = STATE_ATTACKING;
      this.attacker = null;
      this.defender = null;
      return true;
    }
    return false;
  }

  moveArmy(t1, t2, noa) {
    if (this.gameState === STATE_FORTIFYING) {
      if (t1 != null && t2 != null && t1.getOwner() === this.currentPlayer && t2.getOwner() === this.currentPlayer &&
          t1.isNeighbours(t2) && t1.getArmies() > noa && noa > 0) {
        t1.removeArmies(noa);
        t2.addArmies(noa);
        this.gameState = STATE_END_TURN;
        this.checkPlayerWon();
        return true;
      }
    }
    return false;
  }

  noMove() {
    if (this.gameState === STATE_FORTIFYING) {
      this.gameState = STATE_END_TURN;
      return true;
    }
    return false;
  }

  workOutEndGoStats(p) {
    const countries = p.getNoTerritoriesOwned();
    const armies = p.getNoArmies();
    const continents = this.getNoContinentsOwned(p);
    const conectedEmpire = RiskGame.getConnectedEmpire(p).length;
    const cards = p.getCards().length;
    p.currentStatistic.endGoStatistics(countries, armies, continents, conectedEmpire, cards);
  }

  /** largest group of connected territories owned by a player */
  static getConnectedEmpire(p) {
    const t = [...p.getTerritoriesOwned()];
    let a = [];
    let b = [];
    while (t.length > 0) {
      const country = t.shift();
      a.push(country);
      RiskGame._getConnectedEmpire(t, a, country.getNeighbours(), p);
      if (a.length > b.length) b = a;
      a = [];
    }
    return b;
  }

  static _getConnectedEmpire(t, a, n, p) {
    for (let i = 0; i < n.length; i++) {
      const country = n[i];
      if (country.getOwner() === p && t.includes(country)) {
        t.splice(t.indexOf(country), 1);
        a.push(country);
        RiskGame._getConnectedEmpire(t, a, country.getNeighbours(), p);
      }
    }
  }

  setCapital(c) {
    if (this.gameState === STATE_SELECT_CAPITAL && this.gameMode === MODE_CAPITAL && c.getOwner() === this.currentPlayer && this.currentPlayer.getCapital() == null) {
      this.currentPlayer.setCapital(c);
      // note: the original removes while iterating forward (skips the element after a removed one)
      for (let b = 0; b < this.Cards.length; b++) {
        if (c === this.Cards[b].getCountry()) this.Cards.splice(b, 1);
      }
      this.gameState = STATE_END_TURN;
      return true;
    }
    return false;
  }

  checkPlayerWon() {
    let result = false;
    let won = 0;
    for (const co of this.Continents) if (co.isOwned(this.currentPlayer)) won++;

    if (won === this.Continents.length) {
      result = true;
    } else if (this.getSetupDone() && this.gameMode === MODE_CAPITAL && this.currentPlayer.getCapital() != null) {
      let capitalcount = 0;
      if (this.currentPlayer === this.currentPlayer.getCapital().getOwner()) {
        for (const p of this.Players) {
          if (this.currentPlayer.getTerritoriesOwned().includes(p.getCapital())) capitalcount++;
        }
      }
      if (capitalcount === this.Players.length) result = true;
    } else if (this.getSetupDone() && this.gameMode === MODE_SECRET_MISSION) {
      const m = this.currentPlayer.getMission();
      if (m.getPlayer() != null && m.getPlayer() !== this.currentPlayer && m.getPlayer().getNoTerritoriesOwned() === 0 &&
          this.currentPlayer.getPlayersEliminated().includes(m.getPlayer())) {
        result = true;
      } else if (m.getNoofcountries() !== 0 && m.getNoofarmies() !== 0 &&
          (m.getPlayer() == null || m.getPlayer().getNoTerritoriesOwned() === 0 || m.getPlayer() === this.currentPlayer) &&
          m.getNoofcountries() <= this.currentPlayer.getNoTerritoriesOwned()) {
        let n = 0;
        for (const c of this.currentPlayer.getTerritoriesOwned()) {
          if (c.getArmies() >= m.getNoofarmies()) n++;
        }
        if (n >= m.getNoofcountries()) result = true;
      } else if (m.getContinent1() != null &&
          this.checkPlayerOwnesContinentForMission(m.getContinent1(), 1) &&
          this.checkPlayerOwnesContinentForMission(m.getContinent2(), 2) &&
          this.checkPlayerOwnesContinentForMission(m.getContinent3(), 3)) {
        result = true;
      }
    }

    if (result === true) this.gameState = STATE_GAME_OVER;
    return result;
  }

  checkPlayerOwnesContinentForMission(c, n) {
    if (ANY_CONTINENT.equals(c)) return this.getNoContinentsOwned(this.currentPlayer) >= n;
    if (c != null) return c.isOwned(this.currentPlayer);
    return true;
  }

  canContinue() {
    if (this.gameState === STATE_GAME_OVER && this.gameMode !== MODE_DOMINATION && this.gameMode !== 1) {
      const oldGameMode = this.gameMode;
      const oldState = this.gameState;
      this.gameMode = MODE_DOMINATION;
      const playerWon = this.checkPlayerWon();
      this.gameMode = oldGameMode;
      this.gameState = oldState;
      return !playerWon;
    }
    return false;
  }

  continuePlay() {
    if (this.canContinue()) {
      this.gameMode = MODE_DOMINATION;
      if (this.tradeCap === true) this.gameState = STATE_TRADE_CARDS;
      else if (this.currentPlayer.getExtraArmies() !== 0) this.gameState = STATE_PLACE_ARMIES;
      else this.gameState = STATE_ATTACKING;
      return true;
    }
    return false;
  }

  getClosestCountry(x, y) {
    let closest = null;
    let closestDistance = Infinity;
    for (const c of this.Countries) {
      const d = c.getDistanceTo(x, y);
      if (d < closestDistance) { closest = c; closestDistance = d; }
    }
    return closest.getColor();
  }

  // ------------------------------------------------------------------ map files

  async _readMapText(name) {
    const text = await this.io.readMap(name);
    if (text == null) throw new Error('file not found: ' + name);
    return text;
  }

  async loadMap(cleanLoad = true, text = null) {
    const mapTranslator = new MapTranslator(this.io, this.language);
    await mapTranslator.setMap(this.mapfile);

    const countries = cleanLoad ? [] : [...this.Countries];
    const continents = cleanLoad ? [] : [...this.Continents];
    let mapVer = 1;
    let countryCount = 0;
    if (text == null) text = await this._readMapText(this.mapfile);
    let mode = 'none';
    for (const input of lines(text)) {
      if (input === '' || input[0] === ';') continue;
      let st = null;
      if (input[0] === '[' && input[input.length - 1] === ']') mode = 'newsection';
      else st = tokenize(input);

      if (mode === 'files') {
        if (input.startsWith('pic ')) this.ImagePic = input.substring(4);
        else if (input.startsWith('map ')) this.ImageMap = input.substring(4);
        else if (input.startsWith('crd ')) { /* nothing */ }
        else if (input.startsWith('prv ')) { /* nothing */ }
        else throw new Error('error with files section in map file: ' + input);
      } else if (mode === 'continents') {
        const id = st[0];
        const name = mapTranslator.getTranslatedMapName(id).replace(/_/g, ' ');
        const noa = parseJavaInt(st[1]);
        let color = ColorUtil.getColor(st[2]);
        if (color === 0) color = RiskGame.getRandomColor();
        if (st.length > 3) throw new Error('unknown item found in map file: ' + st[3]);
        if (cleanLoad) continents.push(new Continent(id, name, noa, color));
      } else if (mode === 'countries') {
        const color = parseJavaInt(st[0]);
        const id = st[1];
        const name = mapTranslator.getTranslatedMapName(id).replace(/_/g, ' ');
        const continent = parseJavaInt(st[2]);
        const x = parseJavaInt(st[3]);
        const y = parseJavaInt(st[4]);
        if (st.length > 5) throw new Error('unknown item found in map file: ' + st[5]);
        if (++countryCount !== color) throw new Error('unexpected number found in map file: ' + color);
        let country;
        if (cleanLoad) {
          country = new Country(color, id, name, continents[continent - 1], x, y);
          countries.push(country);
          continents[continent - 1].addTerritoriesContained(country);
        } else {
          country = countries[color - 1];
          country.color = color;
          country.continent = continents[continent - 1];
          country.idString = id;
          country.name = name;
          country.x = x;
          country.y = y;
        }
      } else if (mode === 'borders') {
        if (cleanLoad) {
          const country = parseJavaInt(st[0]);
          for (let k = 1; k < st.length; k++) {
            countries[country - 1].addNeighbour(countries[parseJavaInt(st[k]) - 1]);
          }
        }
      } else if (mode === 'newsection') {
        mode = input.substring(1, input.length - 1);
        if (mode === 'files') { this.ImagePic = null; this.ImageMap = null; }
        else if (mode === 'continents' || mode === 'countries' || mode === 'borders') { /* ok */ }
        else throw new Error('unknown section found in map file: ' + mode);
      } else if (input.startsWith('ver ')) {
        mapVer = parseJavaInt(input.substring(4));
      }
    }
    const gameVer = this.getVersion();
    if (gameVer > mapVer) throw new Error(this.mapfile + ' too old, ver ' + mapVer + '. game saved with ver ' + gameVer);
    if (cleanLoad) {
      this.Countries = countries;
      this.Continents = continents;
    }
  }

  /** returns true if the cards file of the map has missions */
  async setMapfile(f) {
    if (f === 'default') f = config.defaultMap;
    const text = await this._readMapText(f);
    this.runmaptest = false;
    this.previewPic = null;
    this.properties = new Map();
    let mode = null;
    let yesmap = false;
    let returnvalue = false;
    let yescards = false;
    for (const input of lines(text)) {
      if (input === '' || input[0] === ';') continue;
      if (input[0] === '[' && input[input.length - 1] === ']') mode = 'newsection';
      if (mode === 'files') {
        if (input.startsWith('pic ')) this.ImagePic = input.substring(4);
        else if (input.startsWith('prv ')) this.previewPic = input.substring(4);
        else if (input.startsWith('crd ')) { yescards = true; returnvalue = await this.setCardsfile(getContext(f) + input.substring(4)); }
      } else if (mode === 'borders') {
        yesmap = true;
      } else if (mode === 'newsection') {
        mode = input.substring(1, input.length - 1);
      } else if (mode == null) {
        const space = input.indexOf(' ');
        if (input === 'test') this.runmaptest = true;
        else if (space >= 0) this.properties.set(input.substring(0, space), input.substring(space + 1));
      }
    }
    if (yesmap === false) throw new Error('borders not found in map ' + f);
    if (yescards === false) throw new Error('cards file not specified in map ' + f);
    this.mapfile = f;
    return returnvalue;
  }

  async loadCards(rawLoad) {
    this.Cards = [];
    this.usedCards = [];
    this.Missions = [];
    const text = await this._readMapText(this.cardsfile);
    const mapTranslator = new MapTranslator(this.io, this.language);
    await mapTranslator.setCards(this.cardsfile);
    let mode = 'none';
    for (const input of lines(text)) {
      if (input === '' || input[0] === ';') continue;
      let st = null;
      if (input[0] === '[' && input[input.length - 1] === ']') mode = 'newsection';
      else st = tokenize(input);

      if (mode === 'cards') {
        const name = st[0];
        if (name === WILDCARD) {
          this.Cards.push(new Card(name, null));
        } else if (name === CAVALRY || name === INFANTRY || name === CANNON) {
          const country = parseJavaInt(st[1]);
          this.Cards.push(new Card(name, this.Countries[country - 1]));
          if (st.length > 2) throw new Error('unknown item found in cards file: ' + st[2]);
          continue;
        } else {
          throw new Error('unknown item found in cards file: ' + name);
        }
        if (st.length > 1) throw new Error('unknown item found in cards file: ' + st[1]);
      } else if (mode === 'missions') {
        const s1 = parseJavaInt(st[0]);
        const p = (s1 === 0 || s1 > this.Players.length) ? null : this.Players[s1 - 1];
        const noc = parseJavaInt(st[1]);
        const noa = parseJavaInt(st[2]);
        const s4 = st[3], s5 = st[4], s6 = st[5];
        const c1 = this.getMissionContinentfromString(s4);
        const c2 = this.getMissionContinentfromString(s5);
        const c3 = this.getMissionContinentfromString(s6);
        const missioncode = `${s1}-${noc}-${noa}-${s4}-${s5}-${s6}`;
        let description = rawLoad ? null : mapTranslator.getTranslatedMissionName(missioncode);
        if (description == null) description = st.slice(6).join(' ');
        if (p != null && !rawLoad) {
          let name = p.getName();
          const color = 'color.' + ColorUtil.getStringForColor(p.getColor());
          if (this.resb) {
            try { name = this.resb.getString(color) + ' ' + name; } catch (e) { /* not found */ }
          }
          const oldkey = 'PLAYER' + s1;
          const newkey = '{' + oldkey + '}';
          if (description.indexOf(newkey) >= 0) description = description.split(newkey).join(name);
          else if (description.indexOf(oldkey) >= 0) description = description.split(oldkey).join(name);
        }
        if (rawLoad || s1 <= this.Players.length) {
          this.Missions.push(new Mission(p, noc, noa, c1, c2, c3, description));
        }
      } else if (mode === 'newsection') {
        mode = input.substring(1, input.length - 1);
        if (mode !== 'cards' && mode !== 'missions') throw new Error('unknown section found in cards file: ' + mode);
      } else {
        throw new Error('unknown item found in cards file: ' + input);
      }
    }
  }

  getMissionContinentfromString(a) {
    if (a === '*') return ANY_CONTINENT;
    const s = parseJavaInt(a);
    if (s === 0) return null;
    return this.Continents[s - 1];
  }

  async setCardsfile(f) {
    if (f === 'default') f = config.defaultCards;
    const text = await this._readMapText(f);
    let mode = 'none';
    let yesmissions = false;
    let yescards = false;
    for (const input of lines(text)) {
      if (input === '' || input[0] === ';') continue;
      if (input[0] === '[' && input[input.length - 1] === ']') mode = 'newsection';
      if (mode === 'newsection') {
        mode = input.substring(1, input.length - 1);
        if (mode === 'cards') yescards = true;
        else if (mode === 'missions') yesmissions = true;
      }
    }
    if (yescards === false) throw new Error('error with cards file');
    this.cardsfile = f;
    return yesmissions;
  }

  shuffleCountries() {
    return this.r.shuffle([...this.Countries]);
  }

  getState() { return this.gameState; }

  NoEmptyCountries() {
    for (const c of this.Countries) if (c.getOwner() == null) return false;
    return true;
  }

  getSetupDone() { return this.setup === this.Players.length; }
  getTradeCap() { return this.tradeCap; }
  getGameMode() { return this.gameMode; }
  getCurrentPlayer() { return this.currentPlayer; }
  getPlayers() { return this.Players; }
  getPlayersStats() {
    for (const p of this.Players) this.workOutEndGoStats(p);
    return this.Players;
  }
  getAttacker() { return this.attacker; }
  getDefender() { return this.defender; }
  getImagePic() { return this.ImagePic; }
  getImageMap() { return this.ImageMap; }
  getPreviewPic() { return this.previewPic; }
  getProperties() { return this.properties; }
  getIntProperty(name, def) {
    const v = this.properties.get(name);
    return v != null ? parseJavaInt(String(v)) : def;
  }
  getCircleSize() { return this.getIntProperty('circle', 20); }
  getVersion() { return this.getIntProperty('ver', 1); }
  getMapName() { return this.properties.get('name') || null; }
  getCardsFile() { return this.cardsfile; }
  getMapFile() { return this.mapfile; }
  getCards() { return this.Cards; }
  getUsedCards() { return this.usedCards; }

  /** rolls dice, values 0-5, sorted biggest first (selection sort like the original) */
  rollDice(nod) {
    const dice = [];
    for (let j = 0; j < nod; j++) dice[j] = this.r.nextInt(6);
    for (let i = 0; i < nod - 1; i++) {
      let pos = i;
      for (let j = i + 1; j < nod; j++) if (dice[j] > dice[pos]) pos = j;
      const temp = dice[i]; dice[i] = dice[pos]; dice[pos] = temp;
    }
    return dice;
  }

  getNoContinentsOwned(p) {
    let total = 0;
    for (const co of this.Continents) if (co.isOwned(p)) total++;
    return total;
  }

  getCountryInt(color) {
    if (!(color > 0) || color > this.Countries.length) return null;
    return this.Countries[color - 1];
  }

  /** finds the given cards (country ids or "wildcard") in the current player's hand */
  getCardsByName(name1, name2, name3) {
    const c = [null, null, null];
    const playersCards = [...this.currentPlayer.getCards()];
    const names = [name1, name2, name3];
    for (let a = 0; a < 3; a++) {
      const name = names[a];
      for (let b = 0; b < playersCards.length; b++) {
        const card = playersCards[b];
        if (name === WILDCARD && name === card.getName()) {
          c[a] = playersCards.splice(b, 1)[0];
          break;
        } else if (card.getCountry() != null && name === String(card.getCountry().getColor())) {
          c[a] = playersCards.splice(b, 1)[0];
          break;
        }
      }
    }
    return c;
  }

  findCardAndRemoveIt(name) {
    let cardIndex = -1;
    for (let c = 0; c < this.Cards.length; c++) {
      const theCard = this.Cards[c];
      if (name === WILDCARD && name === theCard.getName()) { cardIndex = c; break; }
      else if (theCard.getCountry() != null && name === String(theCard.getCountry().getColor())) { cardIndex = c; break; }
    }
    if (cardIndex === -1) throw new Error('card not found ' + name);
    const theCard = this.Cards.splice(cardIndex, 1)[0];
    this.recycleUsedCards();
    return theCard;
  }

  recycleUsedCards() {
    if (this.Cards.length === 0 && this.recycleCards && this.usedCards.length > 0) {
      this.Cards.push(...this.usedCards);
      this.usedCards.length = 0;
      return true;
    }
    return false;
  }

  static getNumber(s) {
    const v = parseJavaInt(s);
    return v === null ? -1 : v;
  }

  getNoPlayers() { return this.Players.length; }
  getCountries() { return this.Countries; }
  getContinents() { return this.Continents; }
  getNoCountries() { return this.Countries.length; }
  getNoContinents() { return this.Continents.length; }
  getMissions() { return this.Missions; }
  getNoMissions() { return this.Missions.length; }
  getNoCards() { return this.Cards.length; }
  isRecycleCards() { return this.recycleCards; }
  getCardMode() { return this.cardMode; }

  static getRandomColor() {
    return ColorUtil.HSBtoRGB(Math.random(), 0.5, 1.0);
  }

  getNoAttackDice() {
    return this.attacker.getArmies() > 4 ? 3 : this.attacker.getArmies() - 1;
  }

  getNoDefendDice() {
    return this.defender.getArmies() > this.maxDefendDice ? this.maxDefendDice : this.defender.getArmies();
  }

  getPlayer(name) {
    return this.Players.find((p) => p.getName() === name) || null;
  }
}

/** Integer.parseInt, returns null when it is not a valid int */
export function parseJavaInt(s) {
  if (s == null) return null;
  const str = String(s);
  if (!/^[+-]?\d+$/.test(str)) return null;
  const v = parseInt(str, 10);
  if (v > 2147483647 || v < -2147483648) return null;
  return v;
}
