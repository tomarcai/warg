// warg - port of net.yura.domination.engine.ai.AISubmissive and AITest (Yura Mamyrin). GPLv3.
// AISubmissive ("crap") never attacks, it just follows the rules.

import { Random } from '../util.js';
import { PLAYER_AI_CRAP, PLAYER_AI_TEST } from '../model.js';

export class AISubmissive {
  constructor(random) {
    this.r = random || new Random();
    this.game = null;
    this.player = null;
  }
  getType() { return PLAYER_AI_CRAP; }
  getCommand() { return 'crap'; }

  setGame(game) {
    this.game = game;
    this.player = game == null ? null : game.getCurrentPlayer();
  }

  getBattleWon() { return 'move all'; }
  getTacMove() { return 'nomove'; }

  getTrade() {
    const cards = this.player.getCards();
    if (cards.length < 3) return 'endtrade';
    const result = [null, null, null];
    if (this.game.getBestTrade(cards, result) > 0) return this.getTradeCommand(result);
    return 'endtrade';
  }

  getTradeCommand(result) {
    return 'trade ' + result.map((c) => (c.getName() === 'wildcard' ? c.getName() : String(c.getCountry().getColor()))).join(' ');
  }

  getPlaceArmies() {
    if (this.game.NoEmptyCountries() === false) return 'autoplace';
    const extra = this.player.getExtraArmies();
    return this.getPlaceCommand(this.randomCountry(this.player.getTerritoriesOwned()), Math.trunc(extra / 3) + (extra % 3));
  }

  getAttack() { return 'endattack'; }
  getRoll() { return 'retreat'; }

  getCapital() {
    return 'capital ' + this.randomCountry(this.player.getTerritoriesOwned()).getColor();
  }

  randomCountry(countries) {
    if (countries.length === 0) return null;
    return countries[this.r.nextInt(countries.length)];
  }

  getAutoDefendString() {
    const n = this.game.getDefender().getArmies();
    return 'roll ' + Math.min(this.game.getMaxDefendDice(), n);
  }

  getPlaceCommand(country, armies) {
    return 'placearmies ' + country.getColor() + ' ' + (!this.game.getSetupDone() ? 1 : Math.max(1, Math.min(this.player.getExtraArmies(), armies)));
  }
}

/** AI that attacks as much as it can all the time (AITest) */
export class AITest extends AISubmissive {
  getType() { return PLAYER_AI_TEST; }
  getCommand() { return 'test'; }

  getPlaceArmies() {
    if (this.game.NoEmptyCountries() === false) return 'autoplace';
    const t = this.player.getTerritoriesOwned();
    const name = AITest.findAttackableTerritory(this.player);
    if (name == null) return 'placearmies ' + t[0].getColor() + ' ' + this.player.getExtraArmies();
    if (this.game.getSetupDone()) return 'placearmies ' + name + ' ' + this.player.getExtraArmies();
    return 'placearmies ' + name + ' 1';
  }

  getAttack() {
    const outputs = AITest.findAttackableNeighbors(this.player, 0);
    if (outputs.length > 0) {
      const move = outputs[Math.round(Math.random() * (outputs.length - 1))];
      return 'attack ' + move.source.getColor() + ' ' + move.destination.getColor();
    }
    return 'endattack';
  }

  getRoll() {
    const n = this.game.getAttacker().getArmies() - 1;
    if (n > 3) return 'roll 3';
    return 'roll ' + n;
  }

  static findAttackableTerritory(p) {
    for (const country of p.getTerritoriesOwned()) {
      for (const n of country.getNeighbours()) {
        if (n.getOwner() !== p) {
          if (p.getCapital() == null || country.getColor() !== p.getCapital().getColor()) {
            return String(country.getColor());
          }
        }
      }
    }
    return null;
  }

  static findAttackableNeighbors(player, ratio) {
    const output = [];
    if (ratio < 0) ratio = 0;
    for (const source of player.getTerritoriesOwned()) {
      if (source.getOwner() === player && source.getArmies() > 1) {
        for (const target of source.getNeighbours()) {
          if (target.getOwner() !== player && source.getArmies() / target.getArmies() > ratio) {
            output.push({ source, destination: target });
          }
        }
      }
    }
    return output;
  }
}
