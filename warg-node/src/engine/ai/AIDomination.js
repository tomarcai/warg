// warg - port of net.yura.domination.engine.ai.logic.AIDomination (Steven Hawkins). GPLv3.
// The Easy / Average / Hard AI. Kept as close as possible to the Java source, including
// integer arithmetic, iteration orders and the java.util.PriorityQueue used by the search.

import { AISubmissive } from './AISubmissive.js';
import {
  idiv, toInt, INT_MIN, INT_MAX, isub, binarySearchDouble, JavaPriorityQueue,
} from '../util.js';
import { StatType, PLAYER_HUMAN } from '../model.js';
import {
  CARD_INCREASING_SET, CARD_FIXED_SET, CARD_ITALIANLIKE_SET, DEFAULT_MAX_CARDS,
} from '../RiskGame.js';

export const MAX_AI_TURNS = 300;
export const PLAYER_AI_AVERAGE = 4;
export const PLAYER_AI_HARD = 2;
export const PLAYER_AI_EASY = 1;

const EMPTY_SET = new Set();

function playerStateCompare(a, b) {
  if (a.playerValue !== b.playerValue) return Math.sign(a.playerValue - b.playerValue) | 0;
  return a.p.getCards().length - b.p.getCards().length;
}
function reverse(cmp) {
  return (a, b) => cmp(b, a);
}

export class AttackTarget {
  constructor(fromCountries, targetCountry) {
    this.remaining = INT_MIN;
    this.routeRemaining = new Array(fromCountries).fill(INT_MIN);
    this.eliminationScore = null;
    this.attackPath = new Array(fromCountries).fill(null);
    this.targetCountry = targetCountry;
    this.depth = 0;
  }
  compareTo(obj) {
    const diff = isub(this.remaining, obj.remaining);
    if (diff !== 0) return diff;
    return this.targetCountry.getColor() - obj.targetCountry.getColor();
  }
  /** shallow clone like Object.clone (arrays are shared) */
  clone() {
    const c = Object.create(AttackTarget.prototype);
    Object.assign(c, this);
    return c;
  }
  toString() {
    return `${this.targetCountry} ${this.remaining}`;
  }
}

const attackTargetCompare = (a, b) => a.compareTo(b);

class EliminationTarget {
  constructor() {
    this.attackTargets = [];
    this.ps = null;
    this.target = false;
    this.allOrNone = false;
    this.co = null;
  }
  compareTo(other) {
    if (this.target) return -1;
    if (other.target) return 1;
    const diff = other.ps.p.getCards().length - this.ps.p.getCards().length;
    if (diff !== 0) return diff;
    return this.ps.defenseValue - other.ps.defenseValue;
  }
  toString() { return 'Eliminate ' + (this.co != null ? this.co : this.ps.p); }
}

function newPlayerState() {
  return {
    p: null, attackValue: 0, defenseValue: 0, attackOrder: 0, playerValue: 0,
    owned: null, armies: 0, strategic: false,
  };
}

function newGameState() {
  return {
    me: null, owned: null, orderedPlayers: null, targetPlayers: [],
    capitals: null, commonThreat: null, breakOnlyTargets: false,
  };
}

function disjoint(a, b) {
  for (const x of a) if (b.has(x)) return false;
  return true;
}

export class AIDomination extends AISubmissive {
  constructor(type, random) {
    super(random);
    this.type = type;
    this.eliminating = false;
    this.breaking = null;
  }

  getPlaceArmies() {
    const game = this.game;
    if ((this.type === PLAYER_AI_EASY && game.NoEmptyCountries() && this.r.nextInt(6) !== 0) ||
        (game.getSetupDone() && this.type === PLAYER_AI_AVERAGE && this.r.nextBoolean())) {
      return this.simplePlacement();
    }
    if (game.NoEmptyCountries()) return this.plan(false);
    return this.findEmptyCountry();
  }

  simplePlacement() {
    const game = this.game;
    const player = this.player;
    if (!game.NoEmptyCountries()) return 'autoplace';
    const t = player.getTerritoriesOwned();
    const n = this.findAttackableTerritories(player, false);
    const copy = [...n];
    let c = null;
    if (n.length === 0 || t.length === 1) {
      c = t[0];
      return this.getPlaceCommand(c, player.getExtraArmies());
    }
    if (n.length === 1) {
      c = n[0];
      return this.getPlaceCommand(c, player.getExtraArmies());
    }
    const toTake = new Set();
    let fallback = null;
    let overload = null;
    let additional = 1;
    while (n.length > 0) {
      c = n.splice(this.r.nextInt(n.length), 1)[0];
      const cn = c.getNeighbours();
      for (let i = 0; i < cn.length; i++) {
        const other = cn[i];
        if (other.getOwner() === player || toTake.has(other)) continue;
        let diff = 0;
        if (game.getMaxDefendDice() === 2) {
          diff = c.getArmies() - 2 - (idiv(3 * other.getArmies(), 2) + other.getArmies() % 2);
        } else {
          diff = c.getArmies() - 2 - 2 * other.getArmies();
        }
        if (diff >= 0) {
          if (diff < other.getArmies() * 3) {
            overload = c;
            additional = other.getArmies() * 3 - diff;
          }
          toTake.add(other);
          continue;
        }
        if (-diff <= player.getExtraArmies()) return this.getPlaceCommand(c, -diff);
        if (fallback == null) {
          fallback = c;
          additional = Math.max(1, -diff);
        }
      }
    }
    if (fallback == null) {
      if (overload != null) return this.getPlaceCommand(overload, additional);
      return this.getPlaceCommand(this.randomCountry(copy), player.getExtraArmies());
    }
    return this.getPlaceCommand(fallback, additional);
  }

  /** looks at all the continents and tries to see which one it should place on */
  findEmptyCountry() {
    const game = this.game;
    const cont = game.getContinents();
    let check = -Number.MAX_VALUE;
    let toPlace = null;
    const players = new Map();
    for (let i = 0; i < game.getPlayers().length; i++) players.set(game.getPlayers()[i], i);

    const conts = [...cont];
    conts.sort((a0, a1) => Math.sign(this.getContinentValue(a1) - this.getContinentValue(a0)) | 0);

    outer: for (let i = 0; i < conts.length; i++) {
      const co = conts[i];
      const ct = co.getTerritoriesContained();
      let bestCountryScore = 0;
      let hasFree = false;
      let preferedCountry = null;
      const troops = new Array(game.getPlayers().length).fill(0);
      let hasPlacement = false;
      let otherOwner = null;
      for (let j = 0; j < ct.length; j++) {
        const country = ct[j];
        if (country.getOwner() == null) {
          hasFree = true;
          const countryScore = this.scoreCountry(country);
          if (preferedCountry == null || countryScore < bestCountryScore || (countryScore === bestCountryScore && this.r.nextBoolean())) {
            bestCountryScore = countryScore;
            preferedCountry = country;
          }
        } else {
          const index = players.get(country.getOwner());
          troops[index]++;
          if (country.getOwner() === this.player) hasPlacement = true;
          else if (otherOwner == null) otherOwner = country.getOwner();
          else if (otherOwner !== country.getOwner() && this.r.nextBoolean()) hasPlacement = true;
        }
      }
      if (!hasFree) continue;
      if (this.type === PLAYER_AI_HARD && !hasPlacement) return this.getPlaceCommand(preferedCountry, 1);

      const continentValue = this.getContinentValue(co);
      for (let j = 0; j < troops.length; j++) {
        let numberofEnemyUnits = 0;
        let territorynum = 1;
        let numberOfEnemies = 0;
        for (let k = 0; k < troops.length; k++) {
          if (j === k) territorynum += troops[k];
          else {
            numberofEnemyUnits += troops[k];
            if (troops[k] > 0) numberOfEnemies++;
          }
        }
        let score = territorynum / Math.max(1, numberofEnemyUnits * numberOfEnemies);
        score *= continentValue;
        score /= bestCountryScore;
        const p = game.getPlayers()[j];
        if (p !== this.player) {
          if (territorynum === ct.length) {
            toPlace = preferedCountry;
            break outer;
          }
        }
        if (check <= score) {
          check = score;
          toPlace = preferedCountry;
        } else if (toPlace == null) {
          toPlace = preferedCountry;
        }
      }
    }
    if (toPlace == null) return 'autoplace';
    return this.getPlaceCommand(toPlace, 1);
  }

  /** gives a score (lower is better) to a country */
  scoreCountry(country) {
    const game = this.game;
    const player = this.player;
    const n = country.getIncomingNeighbours().length;
    let countryScore = n + 6;
    if (country.getArmies() > 0) {
      countryScore += n;
      countryScore -= country.getArmies();
    }
    if (n < 3) countryScore -= 2;
    if (game.getSetupDone() && country.getCrossContinentNeighbours().length === 1) countryScore -= 3;
    let neighborBonus = 0;
    let neighbors = 0;
    for (let k = 0; k < n; k++) {
      const cn = country.getIncomingNeighbours()[k];
      if (cn.getOwner() === player) {
        neighborBonus -= cn.getArmies();
        neighbors++;
      } else if (cn.getOwner() != null) {
        countryScore += idiv(cn.getArmies(), 2) + cn.getArmies() % 2;
      }
    }
    const n1 = country.getNeighbours().length;
    for (let k = 0; k < n1; k++) {
      const cn = country.getNeighbours()[k];
      if (cn.getOwner() === player) {
        neighborBonus -= cn.getArmies();
        neighbors++;
      } else if (cn.getOwner() == null && cn.getContinent() !== country.getContinent()) {
        countryScore--;
      }
    }
    neighbors = idiv(neighbors, 2) + neighbors % 2;
    countryScore += idiv(neighborBonus, 4) + neighborBonus % 2;
    if (!game.getSetupDone() || neighbors > 1) {
      countryScore = toInt(countryScore - Math.pow(neighbors, 2));
      if (!game.getSetupDone()) countryScore = Math.max(1, countryScore);
    }
    return countryScore;
  }

  /** general planning method for both attack and placement */
  plan(attack) {
    const game = this.game;
    const player = this.player;
    const attackable = this.findAttackableTerritories(player, attack);
    if (attack && attackable.length === 0) return 'endattack';
    const gameState = this.getGameState(player, false);

    // kill switch
    if (attack && (game.getCurrentPlayer().getStatistics().length > MAX_AI_TURNS &&
        (gameState.me.playerValue < gameState.orderedPlayers[gameState.orderedPlayers.length - 1].playerValue || this.r.nextBoolean()))) {
      let keepPlaying = false;
      for (const p of game.getPlayers()) {
        if (p.getType() === PLAYER_HUMAN && p.getTerritoriesOwned().length > 0) {
          keepPlaying = true;
          break;
        }
      }
      if (!keepPlaying) {
        const attackFrom = attackable[this.r.nextInt(attackable.length)];
        for (const c of attackFrom.getNeighbours()) {
          if (c.getOwner() !== player) return 'attack ' + attackFrom.getColor() + ' ' + c.getColor();
        }
      }
    }

    const targets = this.searchAllTargets(attack, attackable, gameState);

    // easy seems to be too hard based upon player feedback, so this dumbs down the play with a greedy attack
    if (attack && player.getType() === PLAYER_AI_EASY && game.getMaxDefendDice() === 2 && game.isCapturedCountry() && this.r.nextBoolean()) {
      const targetList = [...targets.values()];
      targetList.sort(reverse(attackTargetCompare));
      for (const at of targetList) {
        if (at.remaining < 1) break;
        const route = this.findBestRoute(attackable, gameState, attack, null, at, gameState.targetPlayers[0], targets);
        const start = attackable[route];
        return this.getAttackCommand(targets, at, route, start);
      }
    }
    return this.planInner(attack, attackable, gameState, targets);
  }

  searchAllTargets(attack, attackable, gameState) {
    const targets = new Map();
    const extra = this.player.getExtraArmies();
    for (let i = 0; i < attackable.length; i++) {
      const c = attackable[i];
      const attackForce = c.getArmies();
      this.searchTargets(targets, c, attackForce, i, attackable.length,
        this.game.getSetupDone() ? extra : (idiv(extra, 2) + extra % 2), attack, gameState);
    }
    return targets;
  }

  planInner(attack, attackable, gameState, targets) {
    const game = this.game;
    const player = this.player;
    let shouldEndAttack = false;
    let pressAttack = false;
    const extra = player.getExtraArmies();
    const allCountriesTaken = new Set();
    const continents = this.findTargetContinents(gameState, targets, attack, true);
    const v = this.getBorder(gameState);
    let isTooWeak = false;

    if (game.getSetupDone()) {
      pressAttack = this.pressAttack(gameState);
      shouldEndAttack = this.shouldEndAttack(gameState);
      isTooWeak = this.isTooWeak(gameState);
      // eliminate
      const toEliminate = this.findEliminationTargets(targets, gameState, attack, extra);
      if (toEliminate.length > 0) {
        toEliminate.sort((a, b) => a.compareTo(b));
        for (let i = 0; i < toEliminate.length; i++) {
          const et = toEliminate[i];
          const totalCards = player.getCards().length + et.ps.p.getCards().length;
          if (this.type === PLAYER_AI_HARD &&
              gameState.orderedPlayers.length > 1 &&
              gameState.me.playerValue < gameState.orderedPlayers[0].playerValue &&
              shouldEndAttack &&
              et.ps.armies > gameState.me.armies * 0.4 &&
              et.ps.armies - this.getCardEstimate(et.ps.p.getCards().length) >
                (totalCards > DEFAULT_MAX_CARDS ? 1 : (gameState.me.playerValue / gameState.orderedPlayers[0].playerValue)) *
                this.getCardEstimate(player.getCards().length + et.ps.p.getCards().length)) {
            toEliminate.splice(i--, 1);
            continue;
          }
          if ((et.ps.p.getCards().length === 0 && gameState.orderedPlayers[0].playerValue > 0.7 * gameState.me.playerValue) ||
              (et.ps.p.getCards().length > 2 && player.getCards().length + et.ps.p.getCards().length <= DEFAULT_MAX_CARDS)) {
            // don't consider in a second pass
            toEliminate.splice(i--, 1);
          }
          const result = this.eliminate(attackable, targets, gameState, attack, extra, allCountriesTaken, et, shouldEndAttack, false);
          if (result != null) {
            this.eliminating = true;
            return result;
          }
        }
      }

      const objective = this.planObjective(attack, attackable, gameState, targets, allCountriesTaken, pressAttack, shouldEndAttack, true);
      if (objective != null) return objective;

      if (this.type === PLAYER_AI_HARD && gameState.orderedPlayers.length > 1 &&
          (this.isIncreasingSet() || gameState.me.playerValue > gameState.orderedPlayers[0].playerValue)) {
        // consider low probability eliminations
        if (toEliminate.length > 0) {
          if (!attack) {
            const newTargets = this.searchAllTargets(true, attackable, gameState);
            outer: for (let i = 0; i < toEliminate.length; i++) {
              const et = toEliminate[i];
              for (let j = 0; j < et.attackTargets.length; j++) {
                const newTarget = newTargets.get(et.attackTargets[j].targetCountry);
                if (newTarget == null) continue outer;
                et.attackTargets[j] = newTarget;
              }
              const result = this.eliminate(attackable, newTargets, gameState, attack, extra, allCountriesTaken, et, shouldEndAttack, true);
              if (result != null) {
                this.eliminating = true;
                return result;
              }
            }
          } else if (this.isIncreasingSet()) {
            const et = toEliminate[0];
            et.allOrNone = false;
            const result = this.eliminate(attackable, targets, gameState, attack, extra, allCountriesTaken, et, shouldEndAttack, true);
            if (result != null) return result;
          }
        }
        // just try to stay in the game
        if (this.isIncreasingSet() && gameState.me.defenseValue < gameState.orderedPlayers[0].attackValue) {
          shouldEndAttack = true;
        }
      }

      if (!attack && allCountriesTaken.size === 0 && shouldEndAttack && !pressAttack && game.getCards().length > 0) {
        const result = this.ensureRiskCard(attackable, gameState, targets, pressAttack, continents);
        if (result != null) return result;
      }

      // attack the common threat
      if ((gameState.commonThreat != null && gameState.commonThreat.owned.size > 0) || (gameState.breakOnlyTargets && !isTooWeak)) {
        const result = this.breakContinent(attackable, targets, gameState, attack, pressAttack, v);
        if (result != null) return result;
      }

      if (!attack && (gameState.orderedPlayers.length > 1 || player.getCapital() != null || player.getMission() != null || game.getMaxDefendDice() > 2)) {
        const result = this.fortify(gameState, attackable, true, v);
        if (result != null) {
          // prefer attack to fortification
          if (continents.length > 0 && pressAttack && player.getCapital() == null) {
            const toAttack = this.eliminate(attackable, targets, gameState, attack, extra, allCountriesTaken, continents[0], false, false);
            if (toAttack != null) return toAttack;
          }
          return result;
        }
      }

      // free a continent, but only plan to do so if in a good position
      if (pressAttack || (this.type !== PLAYER_AI_HARD && attack) || (this.type === PLAYER_AI_HARD && !isTooWeak &&
          (player.getMission() != null || gameState.me.owned.size > 0 || continents.length === 0 || attack))) {
        const result = this.breakContinent(attackable, targets, gameState, attack, pressAttack, v);
        if (result != null) return result;
      }
    } else if (!attack) {
      const result = this.fortify(gameState, attackable, game.getMaxDefendDice() === 2, v);
      if (result != null) return result;
    }

    const objective = this.planObjective(attack, attackable, gameState, targets, allCountriesTaken, pressAttack, shouldEndAttack, false);
    if (objective != null) return objective;

    // take over a continent
    if (continents.length > 0 && (!shouldEndAttack ||
        (!game.isCapturedCountry() && game.getCards().length > 0) ||
        !attack ||
        gameState.commonThreat != null ||
        (!isTooWeak && (gameState.breakOnlyTargets || gameState.me.defenseValue > gameState.orderedPlayers[0].attackValue)))) {
      let toConsider = continents.length;
      if (attack && isTooWeak) toConsider = 1;
      for (let i = 0; i < toConsider; i++) {
        const result = this.eliminate(attackable, targets, gameState, attack, extra, allCountriesTaken, continents[i], shouldEndAttack, false);
        if (result != null) {
          this.eliminating = true;
          for (const c of continents[i].co.getTerritoriesContained()) {
            if (c.getOwner() !== player && !allCountriesTaken.has(c)) {
              this.eliminating = false;
              break;
            }
          }
          if (this.shouldProactivelyFortify(continents[i].co, attack, attackable, gameState, targets, pressAttack, continents)) {
            const border = [];
            for (const c of continents[i].co.getBorderCountries()) {
              if (c.getOwner() === player) border.push(c);
            }
            const placement = this.fortify(gameState, attackable, false, border);
            if (placement != null) return placement;
          }
          return result;
        }
      }
      if (!attack) {
        let min = null;
        for (let i = 0; i < toConsider; i++) {
          const et = continents[i];
          for (let k = 0; k < et.attackTargets.length; k++) {
            const at = et.attackTargets[k];
            if (min == null || (!allCountriesTaken.has(at.targetCountry) && at.remaining < min.remaining)) min = at;
          }
        }
        if (min != null) {
          const route = this.findBestRoute(attackable, gameState, attack, min.targetCountry.getContinent(), min,
            game.getSetupDone() ? gameState.targetPlayers[0] : null, targets);
          if (route !== -1) {
            let toPlace = -min.routeRemaining[route] + 2;
            if (toPlace < 0) toPlace = idiv(player.getExtraArmies(), 3);
            return this.getPlaceCommand(attackable[route], toPlace);
          }
        }
      }
    }

    if (attack) return this.lastAttacks(attack, attackable, gameState, targets, shouldEndAttack, v);

    const result = this.fortify(gameState, attackable, false, v);
    if (result != null) return result;

    // fail-safe
    return AISubmissive.prototype.getPlaceArmies.call(this);
  }

  planObjective(/* attack, attackable, gameState, targets, allCountriesTaken, pressAttack, shouldEndAttack, highProbability */) {
    return null;
  }

  shouldProactivelyFortify(c, attack, attackable, gameState, targets, pressAttack, continents) {
    return this.type === PLAYER_AI_HARD && !this.isIncreasingSet() && this.eliminating &&
      gameState.commonThreat == null && !attack && this.ensureRiskCard(attackable, gameState, targets, pressAttack, continents) == null;
  }

  isIncreasingSet() {
    const game = this.game;
    return game.getCardMode() === CARD_INCREASING_SET && (this.type !== PLAYER_AI_HARD || game.getNewCardState() > 12) &&
      (game.getCards().length > 0 || game.isRecycleCards());
  }

  ensureRiskCard(attackable, gameState, targets, pressAttack, continents) {
    if (this.type === PLAYER_AI_EASY) return null;
    const game = this.game;
    const attacks = [...targets.values()];
    attacks.sort(attackTargetCompare);
    let target = null;
    let found = false;
    let bestRoute = 0;
    for (let i = attacks.length - 1; i >= 0; i--) {
      const at = attacks[i];
      if (target != null && at.remaining < target.remaining) break;
      if (found) continue;
      if (at.remaining > 0) {
        target = null;
        break;
      }
      if (continents.length > 0 && at.targetCountry.getContinent() === continents[0].co) {
        bestRoute = this.findBestRoute(attackable, gameState, pressAttack, null, at, game.getSetupDone() ? gameState.targetPlayers[0] : null, targets);
        target = at;
        found = true;
      } else {
        const route = this.findBestRoute(attackable, gameState, pressAttack, null, at, game.getSetupDone() ? gameState.targetPlayers[0] : null, targets);
        if (target == null || gameState.targetPlayers.includes(at.targetCountry.getOwner()) || this.r.nextBoolean()) {
          bestRoute = route;
          target = at;
        }
      }
    }
    if (target != null) return this.getPlaceCommand(attackable[bestRoute], -target.remaining + 1);
    return null;
  }

  /** one last pass looking to get a risk card or reduce forces */
  lastAttacks(attack, attackable, gameState, targets, shouldEndAttack, border) {
    const game = this.game;
    const player = this.player;
    const isTooWeak = this.isTooWeak(gameState) && gameState.me.defenseValue < 0.5 * gameState.orderedPlayers[0].defenseValue;
    const forceReduction = game.isCapturedCountry() || game.getCards().length === 0 || gameState.me.playerValue > 1.5 * gameState.orderedPlayers[0].playerValue;
    const sorted = [...targets.values()];
    sorted.sort(attackTargetCompare);
    for (let i = sorted.length - 1; i >= 0; i--) {
      const target = sorted[i];
      if (target.depth > 1) break;
      const bestRoute = this.findBestRoute(attackable, gameState, attack, null, target, gameState.targetPlayers[0], targets);
      if (bestRoute === -1) continue;
      const attackFrom = attackable[bestRoute];
      const initialAttack = this.getCountryToAttack(targets, target, bestRoute, attackFrom);
      const half = idiv(attackFrom.getArmies(), 2) + attackFrom.getArmies() % 2;
      if (forceReduction) {
        // peephole break continent
        if ((attackFrom.getCrossContinentNeighbours().length === 1 || !border.includes(attackFrom)) &&
            attackFrom.getCrossContinentNeighbours().includes(initialAttack) &&
            ((gameState.commonThreat != null && gameState.commonThreat.p === initialAttack.getOwner()) || gameState.targetPlayers.includes(initialAttack.getOwner()) || (gameState.commonThreat == null && !gameState.breakOnlyTargets)) &&
            initialAttack.getContinent().getOwner() != null &&
            (!border.includes(attackFrom) || initialAttack.getArmies() === 1 || attackFrom.getArmies() > 3) &&
            target.remaining >= -half) {
          return this.getAttackCommand(targets, target, bestRoute, attackFrom);
        }
        if (gameState.commonThreat != null && gameState.commonThreat.p === initialAttack.getContinent().getOwner() && target.remaining >= -half) {
          return this.getAttackCommand(targets, target, bestRoute, attackFrom);
        }
      } else if (target.remaining >= -half) {
        if (gameState.commonThreat != null && !this.isIncreasingSet() && gameState.commonThreat.p !== initialAttack.getOwner() &&
            !gameState.targetPlayers.includes(initialAttack.getOwner()) && initialAttack.getContinent().getOwner() != null) {
          continue;
        }
        if (this.type !== PLAYER_AI_EASY && attackFrom.getArmies() - target.remaining >
            this.getCardEstimate(player.getCards().length < 4 ? 4 : 5) + idiv(initialAttack.getArmies(), 2)) {
          continue;
        }
        if (isTooWeak && target.remaining < 0 && this.isIncreasingSet() && player.getCards().length === 0) continue;
        return this.getAttackCommand(targets, target, bestRoute, attackFrom);
      }
    }
    if (!isTooWeak && this.type !== PLAYER_AI_EASY) {
      for (let i = 0; i < sorted.length; i++) {
        const target = sorted[i];
        if (target.depth > 1) continue;
        const bestRoute = this.findBestRoute(attackable, gameState, attack, null, target, gameState.targetPlayers[0], targets);
        if (bestRoute === -1) continue;
        const attackFrom = attackable[bestRoute];
        const initialAttack = this.getCountryToAttack(targets, target, bestRoute, attackFrom);
        if (border.includes(attackFrom) && initialAttack.getArmies() < 5) continue;
        if (attackFrom.getArmies() < 3 ||
            (game.getMaxDefendDice() > 2 && initialAttack.getArmies() > 2 && (gameState.me.playerValue < 1.5 * gameState.orderedPlayers[0].playerValue || game.isCapturedCountry())) ||
            (attackFrom.getArmies() < 4 && attackFrom.getArmies() - 1 <= initialAttack.getArmies())) {
          continue;
        }
        if (gameState.commonThreat != null) {
          if (gameState.commonThreat.p === initialAttack.getOwner()) return this.getAttackCommand(targets, target, bestRoute, attackFrom);
        } else {
          const half = idiv(attackFrom.getArmies(), 2) + attackFrom.getArmies() % 2;
          if (this.ownsNeighbours(player, initialAttack) && target.remaining > -half) {
            if ((this.isIncreasingSet() || gameState.me.playerValue < 0.8 * gameState.orderedPlayers[0].playerValue) &&
                !this.isGoodIdea(gameState, targets, bestRoute, target, attackFrom, null, shouldEndAttack)) {
              continue;
            }
            return this.getAttackCommand(targets, target, bestRoute, attackFrom);
          }
          const neighbours = attackFrom.getIncomingNeighbours();
          let count = 0;
          for (let j = 0; j < neighbours.length; j++) {
            if (neighbours[j].getOwner() !== player) count++;
          }
          if (shouldEndAttack && (target.routeRemaining[bestRoute] > 0 && count > 1)) continue;
          if (gameState.orderedPlayers[0].playerValue > gameState.me.playerValue) {
            for (let j = 0; j < gameState.orderedPlayers.length; j++) {
              const ps = gameState.orderedPlayers[j];
              if (ps.p === initialAttack.getOwner() && (!gameState.breakOnlyTargets || gameState.targetPlayers.includes(ps.p)) &&
                  (ps.attackOrder === 1 || gameState.orderedPlayers.length === 1 || ps.defenseValue > gameState.me.defenseValue * 1.2 ||
                   (!shouldEndAttack && this.isGoodIdea(gameState, targets, bestRoute, target, attackFrom, null, shouldEndAttack)))) {
                return this.getAttackCommand(targets, target, bestRoute, attackFrom);
              }
            }
            continue;
          }
          if ((this.isIncreasingSet() || (game.getCardMode() === CARD_ITALIANLIKE_SET && game.getCards().length > 0) ||
               gameState.me.playerValue < 0.8 * gameState.orderedPlayers[0].playerValue) &&
              !this.isGoodIdea(gameState, targets, bestRoute, target, attackFrom, null, shouldEndAttack)) {
            continue;
          }
          return this.getAttackCommand(targets, target, bestRoute, attackFrom);
        }
      }
    }
    return 'endattack';
  }

  /** quick check to see if we're significantly weaker than the strongest player */
  isTooWeak(gameState) {
    const game = this.game;
    const player = this.player;
    const result = (gameState.orderedPlayers.length > 1 || player.getMission() != null || player.getCapital() != null) &&
      gameState.me.defenseValue < gameState.orderedPlayers[0].attackValue / Math.max(2, gameState.orderedPlayers.length - 1);
    if (!result && this.type === PLAYER_AI_HARD &&
        gameState.orderedPlayers.length > 2 &&
        (gameState.me.defenseValue < 1.2 * gameState.orderedPlayers[gameState.orderedPlayers.length - 1].defenseValue ||
          ((gameState.commonThreat != null || player.getStatistics().length < 4 || player.getCards().length < 2) &&
            gameState.me.defenseValue < (game.getMaxDefendDice() === 2 ? 1.2 : 1) * gameState.orderedPlayers[0].attackValue)) &&
        this.shouldEndAttack(gameState)) {
      return true;
    }
    return result;
  }

  /** stops non-priority attacks if there is too much pressure */
  shouldEndAttack(gameState) {
    if (gameState.orderedPlayers.length < 2 || this.type === PLAYER_AI_EASY) return false;
    const defense = gameState.me.defenseValue;
    let sum = 0;
    for (const ps of gameState.orderedPlayers) sum += ps.attackValue;
    if (defense > sum) return false;
    const ratio = defense / sum;
    if (ratio < 0.5) return true;
    return this.r.nextDouble() > (ratio - 0.5) * 2;
  }

  /** if the ai should be more aggressive */
  pressAttack(gameState) {
    if (this.type === PLAYER_AI_EASY) return this.r.nextBoolean();
    if (gameState.orderedPlayers.length < 2) return true;
    const defense = gameState.me.defenseValue;
    let sum = 0;
    for (const ps of gameState.orderedPlayers) sum += ps.attackValue;
    return defense > sum;
  }

  /** find the continents that we're interested in competing for */
  findTargetContinents(gameState, targets, attack, filterNoAttacks) {
    const game = this.game;
    const player = this.player;
    const c = game.getContinents();
    let targetContinents = Math.max(1, c.length - gameState.orderedPlayers.length);
    const vals = [];
    let result = [];
    const seen = new Set();
    for (let i = 0; i < c.length; i++) {
      const co = c[i];
      if (gameState.owned[i] != null && (gameState.owned[i] === player || (gameState.commonThreat != null && gameState.commonThreat.p !== gameState.owned[i]))) {
        continue;
      }
      const ct = co.getTerritoriesContained();
      const at = [];
      let territories = 0;
      let troops = 0;
      let enemyTerritories = 0;
      let enemyTroops = 0;
      seen.clear();
      for (let j = 0; j < ct.length; j++) {
        const country = ct[j];
        if (country.getOwner() === player) {
          territories++;
          troops += country.getArmies();
        } else {
          const t = targets.get(country);
          if (t != null) at.push(t);
          enemyTerritories++;
          let toAttack = 0;
          if (gameState.commonThreat == null || gameState.commonThreat.p !== country.getOwner()) {
            toAttack += country.getArmies();
          } else {
            toAttack += idiv(country.getArmies(), 2);
          }
          if (toAttack >= game.getMaxDefendDice() && (t == null || t.remaining <= 0)) {
            if (game.getMaxDefendDice() === 2) toAttack = idiv(3 * toAttack, 2);
            else toAttack *= 2;
          }
          enemyTroops += toAttack;
        }
        const ccns = country.getCrossContinentNeighbours();
        if (ccns.length > 0) {
          for (let k = 0; k < ccns.length; k++) {
            const ccn = ccns[k];
            if (!seen.has(ccn)) {
              seen.add(ccn);
              if (ccn.getOwner() === player) {
                if (country.getOwner() !== player) troops += ccn.getArmies() - 1;
              } else if (gameState.commonThreat == null) {
                enemyTroops = toInt(enemyTroops + ccn.getArmies() * 0.8);
              }
            }
          }
        }
      }
      if (at.length === 0 && filterNoAttacks) continue;
      const needed = enemyTroops + enemyTerritories + territories - troops + (attack ? game.getMaxDefendDice() * co.getBorderCountries().length : 0);
      if (attack && game.isCapturedCountry() && (needed * 0.8 > troops)) continue;
      let ratio = Math.max(1, territories + 2 * troops + idiv(player.getExtraArmies(), game.getSetupDone() ? 2 : 3)) / (enemyTerritories + 2 * enemyTroops);
      let pow = 2;
      if (!game.getSetupDone()) pow = 3;
      if (ratio < 0.5) {
        if (gameState.commonThreat != null) continue;
        ratio /= Math.pow(Math.max(1, enemyTroops - enemyTerritories), pow);
      } else {
        targetContinents++;
      }
      if (gameState.commonThreat == null) {
        ratio *= Math.pow(this.getContinentValue(co), 1 / (gameState.me.owned.size + 1));
      }
      const key = -ratio;
      let index = binarySearchDouble(vals, key);
      if (index < 0) index = -index - 1;
      vals.splice(index, 0, key);
      const et = new EliminationTarget();
      et.allOrNone = false;
      et.attackTargets = at;
      et.co = co;
      et.ps = gameState.orderedPlayers[0];
      result.splice(index, 0, et);
    }
    if (result.length > targetContinents) result = result.slice(0, targetContinents);
    return result;
  }

  /** find the best route (the index in attackable) for the given target selection */
  findBestRoute(attackable, gameState, attack, targetCo, selection, targetPlayer, targets) {
    let bestRoute = 0;
    let bestPath = null;
    for (let i = 1; i < selection.routeRemaining.length; i++) {
      if (selection.routeRemaining[i] === INT_MIN) continue;
      const diff = isub(selection.routeRemaining[bestRoute], selection.routeRemaining[i]);
      const start = attackable[i];
      if (selection.routeRemaining[bestRoute] === INT_MIN) {
        bestRoute = i;
        continue;
      }
      // short sighted check to see if we're cutting off an attack line
      if (attack && selection.routeRemaining[i] >= 0 && diff !== 0 && selection.routeRemaining[bestRoute] >= 0) {
        const path = this.getPath(selection, targets, i, start);
        if (bestPath == null) bestPath = this.getPath(selection, targets, bestRoute, attackable[bestRoute]);
        const path1 = new Set();
        for (const attacked of path) {
          if (bestPath.has(attacked) && attacked.getArmies() <= 4) path1.add(attacked);
        }
        if (diff < 0 && path1.size > 0) {
          const specificTargets = new Map();
          this.searchTargets(specificTargets, start, start.getArmies(), 0, 1, this.player.getExtraArmies(), attack, gameState, EMPTY_SET, path1);
          const forwardMin = this.getMinRemaining(specificTargets, start.getArmies(), false, gameState);
          if (forwardMin > -diff) {
            bestRoute = i;
            bestPath = path;
          }
        } else if (diff > 0 && path1.size === 0 && start.getArmies() >= 3) {
          bestRoute = i;
          bestPath = path;
        }
        continue;
      }
      if (diff === 0 && attack) {
        const start1 = attackable[bestRoute];
        const adjustedCost1 = start1.getArmies() - selection.routeRemaining[bestRoute];
        const adjustedCost2 = start.getArmies() - selection.routeRemaining[i];
        if (adjustedCost1 < adjustedCost2) continue;
        if (adjustedCost2 < adjustedCost1) {
          bestRoute = i;
          continue;
        }
      }
      if ((diff < 0 && (!attack || selection.routeRemaining[bestRoute] < 0)) ||
          (diff === 0 &&
            ((selection.attackPath[i] != null && selection.attackPath[i].getOwner() === targetPlayer) ||
             ((targetPlayer == null || selection.attackPath[bestRoute].getOwner() !== targetPlayer) && start.getContinent() === targetCo)))) {
        bestRoute = i;
      }
    }
    if (selection.routeRemaining[bestRoute] === INT_MIN) return -1;
    return bestRoute;
  }

  /** set of the path from start (exclusive) to the given target */
  getPath(at, targets, i, start) {
    const path = new Set();
    let toAttack = at.targetCountry;
    path.add(toAttack);
    while (!start.isNeighbours(toAttack)) {
      at = targets.get(at.attackPath[i]);
      toAttack = at.targetCountry;
      path.add(toAttack);
    }
    return path;
  }

  getAttackCommand(targets, selection, best, start) {
    const toAttack = this.getCountryToAttack(targets, selection, best, start);
    return 'attack ' + start.getColor() + ' ' + toAttack.getColor();
  }

  getCountryToAttack(targets, selection, best, start) {
    let toAttack = selection.targetCountry;
    while (!start.isNeighbours(toAttack)) {
      selection = targets.get(selection.attackPath[best]);
      toAttack = selection.targetCountry;
    }
    return toAttack;
  }

  /** simplistic fortification */
  fortify(gs, attackable, minimal, borders) {
    const game = this.game;
    const player = this.player;
    const min = Math.max(game.getMaxDefendDice(), this.getMinPlacement());
    for (let i = 0; i < borders.length; i++) {
      const c = borders[i];
      if (c.getArmies() < min) return this.getPlaceCommand(c, min - c.getArmies());
    }
    if (minimal && (!game.getSetupDone() || (this.isIncreasingSet() && player.getCards().length > 1))) return null;
    for (let i = 0; i < borders.length; i++) {
      const c = borders[i];
      const diff = this.additionalTroopsNeeded(c, gs);
      if (diff > 0) return this.getPlaceCommand(c, Math.min(player.getExtraArmies(), diff));
      if (!minimal && -diff < c.getArmies() + 2) {
        return this.getPlaceCommand(c, Math.min(player.getExtraArmies(), c.getArmies() + 2 + diff));
      }
    }
    return null;
  }

  /** simplistic (immediate) guess at the additional troops needed */
  additionalTroopsNeeded(c, gs) {
    const player = this.player;
    let needed = 0;
    const minimal = !gs.capitals.has(c);
    const v = c.getIncomingNeighbours();
    for (let j = 0; j < v.length; j++) {
      const n = v[j];
      if (n.getOwner() !== player) {
        if (minimal) needed = Math.max(needed, n.getArmies());
        else needed += (n.getArmies() - 1);
      }
    }
    if (!this.isIncreasingSet() && this.type !== PLAYER_AI_EASY && gs.commonThreat == null && gs.me.playerValue < gs.orderedPlayers[0].playerValue) {
      for (const cont of c.getCrossContinentNeighbours()) {
        if (!gs.me.owned.has(c.getContinent()) && cont.getArmies() < cont.getContinent().getArmyValue()) {
          if (gs.me.owned.has(cont.getContinent()) && needed > 0) {
            needed += cont.getContinent().getArmyValue();
          } else {
            needed = Math.max(needed, idiv(cont.getContinent().getArmyValue(), 2));
          }
          break;
        }
      }
    }
    return needed - c.getArmies();
  }

  getMinPlacement() { return 1; }

  /** the border of my continents, starting with actual borders then the front */
  getBorder(gs) {
    const player = this.player;
    const borders = [];
    if (gs.me.owned.size === 0) return borders;
    const front = new Set();
    const visited = new Set();
    for (const myCont of gs.me.owned) {
      const v = myCont.getBorderCountries();
      for (let j = 0; j < v.length; j++) {
        const border = v[j];
        if (!this.ownsNeighbours(player, border) || this.isAttackable(border)) {
          borders.push(border);
        } else {
          if (border.getCrossContinentNeighbours().length === 1) {
            const country = border.getCrossContinentNeighbours()[0];
            if (country.getOwner() !== player) {
              borders.push(country);
              continue;
            }
          }
          const n = border.getCrossContinentNeighbours();
          this.findFront(gs, front, myCont, visited, n);
        }
      }
    }
    for (const f of front) borders.push(f);
    return borders;
  }

  isAttackable(c) {
    for (const country of c.getIncomingNeighbours()) {
      if (country.getOwner() !== this.player) return true;
    }
    return false;
  }

  findFront(gs, front, myCont, visited, n) {
    const stack = [...n];
    while (stack.length > 0) {
      const b = stack.pop();
      if (visited.has(b)) continue;
      visited.add(b);
      if (b.getOwner() === this.player && b.getContinent() !== myCont) {
        if (gs.me.owned.has(b.getContinent())) continue;
        if (this.isAttackable(b)) front.add(b);
        else stack.push(...b.getNeighbours());
      }
    }
  }

  /** estimates a baseline value for a continent */
  getContinentValue(co) {
    const game = this.game;
    let players = 0;
    for (const p of game.getPlayers()) if (p.getTerritoriesOwned().length > 0) players++;
    const freeContinents = game.getContinents().length - players;
    let continentValue = co.getArmyValue() + idiv(co.getTerritoriesContained().length, 3);
    let neighbors = 0;
    for (const b of co.getBorderCountries()) neighbors += b.getCrossContinentNeighbours().length;
    continentValue /= Math.pow(2 * neighbors - co.getBorderCountries().length, 2);
    if (freeContinents > co.getBorderCountries().length) continentValue *= co.getBorderCountries().length;
    return continentValue;
  }

  /** break continents starting with the strongest player */
  breakContinent(attackable, targets, gameState, attack, press, borders) {
    const game = this.game;
    const player = this.player;
    const toBreak = this.getContinentsToBreak(gameState);
    if (!attack && this.type === PLAYER_AI_EASY) return null;
    outer: for (let i = 0; i < toBreak.length; i++) {
      const c = toBreak[i];
      const tp = c.getTerritoriesContained()[0].getOwner();
      let ps = null;
      for (let j = 0; j < gameState.orderedPlayers.length; j++) {
        ps = gameState.orderedPlayers[j];
        if (ps.p === tp) break;
      }
      if ((!press || !attack) && !gameState.targetPlayers.includes(tp)) {
        if (gameState.commonThreat != null || gameState.breakOnlyTargets) continue outer;
        if (ps.attackOrder !== 1 && ps.playerValue < gameState.me.playerValue) continue outer;
      }
      const t = c.getTerritoriesContained();
      let best = INT_MAX;
      let selection = null;
      let bestRoute = 0;
      for (let j = 0; j < t.length; j++) {
        const target = t[j];
        const attackTarget = targets.get(target);
        if (attackTarget == null || attackTarget.remaining === INT_MIN ||
            (attackTarget.remaining + player.getExtraArmies() < 1 && (game.getCards().length > 0 || !press))) {
          continue;
        }
        const route = this.findBestRoute(attackable, gameState, attack, null, attackTarget, gameState.orderedPlayers[0].p, targets);
        const attackFrom = attackable[route];
        if (gameState.commonThreat == null && !gameState.breakOnlyTargets && gameState.me.owned.size === 0 && attackTarget.routeRemaining[route] + player.getExtraArmies() < 1) {
          continue;
        }
        let cost = attackFrom.getArmies() - attackTarget.routeRemaining[route];
        if (borders.includes(attackFrom)) cost += game.getMaxDefendDice();
        if (cost < best || (cost === best && this.r.nextBoolean())) {
          best = cost;
          bestRoute = route;
          selection = attackTarget;
        }
      }
      if (selection != null) {
        const attackFrom = attackable[bestRoute];
        if (best > (3 * c.getArmyValue() + 2 * selection.targetCountry.getArmies()) && game.getMaxDefendDice() === 2) {
          let value = 3 * c.getArmyValue();
          let collateral = 0;
          const path = this.getPath(selection, targets, bestRoute, attackFrom);
          for (const attacked of path) {
            value++;
            if (attacked.getOwner() === selection.targetCountry.getOwner() || gameState.targetPlayers.includes(attacked.getOwner())) {
              if (game.getMaxDefendDice() === 2 || attacked.getArmies() < 3) value += idiv(3 * attacked.getArmies(), 2) + attacked.getArmies() % 2;
              else value += 2 * attacked.getArmies();
            } else if (game.getMaxDefendDice() === 2 || attacked.getArmies() < 3) {
              collateral += idiv(3 * attacked.getArmies(), 2) + attacked.getArmies() % 2;
            } else {
              collateral += 2 * attacked.getArmies();
            }
          }
          if (value < best && (!attack || this.r.nextInt(best - value) !== 0) &&
              (gameState.commonThreat == null || !gameState.breakOnlyTargets || idiv(value, ps.attackOrder) < collateral)) {
            continue outer;
          }
        }
        const result = this.getMove(targets, attack, selection, bestRoute, attackFrom);
        if (result == null) continue outer;
        this.breaking = c;
        return result;
      }
    }
    return null;
  }

  /** list of continents to break in priority order */
  getContinentsToBreak(gs) {
    const game = this.game;
    const result = [];
    const vals = [];
    for (let i = 0; i < gs.owned.length; i++) {
      if (gs.owned[i] != null && gs.owned[i] !== this.player) {
        const co = game.getContinents()[i];
        const val = -this.getContinentValue(co) * game.getContinents()[i].getArmyValue();
        let index = binarySearchDouble(vals, val);
        if (index < 0) index = -index - 1;
        vals.splice(index, 0, val);
        result.splice(index, 0, co);
      }
    }
    return result;
  }

  /** determine if elimination is possible, with simple heuristic passes */
  eliminate(attackable, targets, gameState, attack, remaining, allCountriesTaken, et, shouldEndAttack, lowProbability) {
    let selection = null;
    let bestRoute = 0;
    if (this.type === PLAYER_AI_EASY || (this.type === PLAYER_AI_AVERAGE && !et.allOrNone && this.r.nextInt(3) !== 0) ||
        (!et.allOrNone && !et.target && shouldEndAttack && attack)) {
      // just be greedy, take the best (least costly) attack first
      for (let i = 0; i < et.attackTargets.length; i++) {
        const at = et.attackTargets[i];
        if (at.depth !== 1 || allCountriesTaken.has(at.targetCountry)) continue;
        const route = this.findBestRoute(attackable, gameState, attack, null, at, et.ps.p, targets);
        const attackFrom = attackable[route];
        if (((at.routeRemaining[route] > 0 && (selection == null || at.routeRemaining[route] < selection.routeRemaining[bestRoute] || selection.routeRemaining[bestRoute] < 1)) ||
             (at.remaining > 1 && attackFrom.getArmies() > 3 && (selection != null && at.remaining < selection.remaining))) &&
            this.isGoodIdea(gameState, targets, route, at, attackFrom, et, attack)) {
          selection = at;
          bestRoute = route;
        }
      }
      return this.getMove(targets, attack, selection, bestRoute, attackable[bestRoute]);
    }
    // otherwise we use more logic to plan a more complete attack
    const countriesTaken = new Set(allCountriesTaken);
    const placements = new Set();
    let bestCost = INT_MAX;
    et.attackTargets.sort(reverse(attackTargetCompare));
    const toTake = new Set();
    for (const at of et.attackTargets) {
      if (!allCountriesTaken.has(at.targetCountry)) toTake.add(at.targetCountry);
    }
    outer: for (let i = 0; i < et.attackTargets.length && toTake.size > 0; i++) {
      let attackTarget = et.attackTargets[i];
      if (!toTake.has(attackTarget.targetCountry)) continue;
      let attackFrom = null;
      let route = 0;
      let clone = true;
      let path = null;
      let pathRemaining = 0;
      while (true) {
        route = this.findBestRoute(attackable, gameState, attack, null, attackTarget, et.ps.p, targets);
        if (route === -1) {
          if (!et.allOrNone) continue outer;
          return null;
        }
        attackFrom = attackable[route];
        if (!placements.has(attackFrom)) {
          pathRemaining = attackTarget.routeRemaining[route];
          if ((pathRemaining + remaining >= 1 ||
               (attackTarget.remaining + remaining >= 2 && attackFrom.getArmies() + remaining >= 4)) &&
              (et.allOrNone || this.isGoodIdea(gameState, targets, route, attackTarget, attackFrom, et, attack))) {
            path = this.getPath(attackTarget, targets, route, attackFrom);
            if (disjoint(path, countriesTaken)) {
              if (pathRemaining + remaining >= 3) {
                const exclusions = new Set(countriesTaken);
                for (const p of path) exclusions.add(p);
                const newTargets = new Map();
                this.searchTargets(newTargets, attackTarget.targetCountry, pathRemaining, 0, 1, remaining, lowProbability ? true : attack, gameState, toTake, exclusions);
                let newTarget = null;
                for (const next of newTargets.values()) {
                  if (toTake.has(next.targetCountry) && next.routeRemaining[0] < pathRemaining && next.routeRemaining[0] + remaining >= 1) {
                    pathRemaining = next.routeRemaining[0];
                    newTarget = next;
                  }
                }
                if (newTarget != null) {
                  for (const p of this.getPath(newTarget, newTargets, 0, attackTarget.targetCountry)) path.add(p);
                  attackTarget.routeRemaining[route] = pathRemaining;
                }
              }
              break;
            }
          } else if (et.allOrNone && et.attackTargets.length === 1 &&
              this.type === PLAYER_AI_HARD &&
              attackTarget.remaining + remaining > -attackTarget.targetCountry.getArmies() &&
              gameState.me.playerValue < gameState.orderedPlayers[0].playerValue) {
            path = this.getPath(attackTarget, targets, route, attackFrom);
            break;
          }
        }
        if (clone) {
          attackTarget = attackTarget.clone();
          attackTarget.routeRemaining = [...attackTarget.routeRemaining];
          clone = false;
        }
        attackTarget.routeRemaining[route] = INT_MIN;
      }
      for (const c of path) {
        countriesTaken.add(c);
        toTake.delete(c);
      }
      if (pathRemaining < 1) remaining += pathRemaining - 1;
      const cost = attackFrom.getArmies() - pathRemaining;
      if (selection == null || (attack && cost < bestCost && cost > 0)) {
        selection = attackTarget;
        bestCost = cost;
        bestRoute = route;
      }
      placements.add(attackFrom);
    }
    const attackFrom = attackable[bestRoute];
    const result = this.getMove(targets, attack, selection, bestRoute, attackFrom);
    if (result != null) for (const c of countriesTaken) allCountriesTaken.add(c);
    return result;
  }

  /** ensure that we're not doing something stupid */
  isGoodIdea(gameState, targets, route, attackTarget, attackFrom, et, attack) {
    const game = this.game;
    const player = this.player;
    const c = this.getCountryToAttack(targets, attackTarget, route, attackFrom);
    if (gameState.orderedPlayers.length > 1 && (et == null || et.ps == null || c.getOwner() !== et.ps.p) && !gameState.targetPlayers.includes(c.getOwner())) {
      if (gameState.commonThreat != null && c.getOwner() !== gameState.commonThreat.p && c.getContinent().getOwner() != null) return false;
      if (player.getMission() == null && game.getCardMode() === CARD_ITALIANLIKE_SET && c.getOwner().getCards().length < 4) return true;
      if (gameState.commonThreat != null && c.getOwner().getCards().length <= 2) return true;
      if (player.getMission() != null || ((attack || this.isIncreasingSet()) &&
          (c.getOwner().getCards().length > 1 || (c.getOwner().getCards().length === 1 && game.getCards().length === 0)))) {
        for (let i = gameState.orderedPlayers.length - 1; i >= 0; i--) {
          const ps = gameState.orderedPlayers[i];
          if (ps.playerValue >= gameState.me.playerValue) break;
          if (ps.p === c.getOwner()) {
            if (ps.attackOrder === 1 && c.getOwner().getCards().length > 3) return true;
            if (this.type === PLAYER_AI_HARD && this.isIncreasingSet() &&
                gameState.me.playerValue < gameState.orderedPlayers[0].playerValue &&
                game.getNewCardState() > gameState.me.defenseValue) {
              return true;
            }
            const top = gameState.orderedPlayers[0];
            if (ps.defenseValue - idiv(5 * c.getArmies(), 4) - c.getArmies() % 4 - 1 < 2 * (top.attackValue - idiv(top.armies, 3)) / 3) {
              return false;
            }
            break;
          }
        }
      }
    }
    return true;
  }

  /** gets the move (placement or attack) or returns null if it's not a good attack */
  getMove(targets, attack, selection, route, attackFrom) {
    if (selection == null) return null;
    if (attack) {
      if (attackFrom.getArmies() < 5 && selection.remaining < 1) {
        const toAttack = this.getCountryToAttack(targets, selection, route, attackFrom);
        if (toAttack.getArmies() >= attackFrom.getArmies()) return null;
      }
      return this.getAttackCommand(targets, selection, route, attackFrom);
    }
    if (selection.remaining < 1 || selection.routeRemaining[route] < 2) {
      return this.getPlaceCommand(attackFrom, -selection.routeRemaining[route] + 2);
    }
    return null;
  }

  /** the possible elimination targets in priority order */
  findEliminationTargets(targets, gameState, attack, remaining) {
    const game = this.game;
    const player = this.player;
    const toEliminate = [];
    players: for (let i = 0; i < gameState.orderedPlayers.length; i++) {
      const ps = gameState.orderedPlayers[i];
      const player2 = ps.p;
      if ((player2.getCards().length === 0 && player2.getTerritoriesOwned().length > 1) || ps.defenseValue > gameState.me.attackValue + player.getExtraArmies()) {
        continue;
      }
      const isTarget = gameState.targetPlayers.length > 1 && gameState.targetPlayers[0] === player2;
      let divisor = 1;
      const cardCount = player2.getCards().length;
      if ((!this.isIncreasingSet() || game.getNewCardState() < gameState.me.defenseValue / 8) &&
          (!attack || player2.getTerritoriesOwned().length > 1) && game.getCards().length > 0 && cardCount < 3 &&
          (cardCount + player.getCards().length < game.getMaxCardsPerPlayer())) {
        divisor += (0.5 * Math.max(0, this.isIncreasingSet() ? 2 : 4 - cardCount));
      }
      if (!isTarget && ps.defenseValue > gameState.me.armies / divisor + player.getExtraArmies()) continue;
      const targetCountries = player2.getTerritoriesOwned();
      const et = new EliminationTarget();
      et.ps = ps;
      for (let j = 0; j < targetCountries.length; j++) {
        const target = targetCountries[j];
        const attackTarget = targets.get(target);
        if (attackTarget == null || attackTarget.remaining === INT_MIN || (!attack && -attackTarget.remaining > remaining)) {
          continue players;
        }
        et.attackTargets.push(attackTarget);
      }
      et.target = isTarget;
      et.allOrNone = true;
      toEliminate.push(et);
    }
    return toEliminate;
  }

  /**
   * search using Dijkstra's algorithm.
   * attack true = neutral cost, false = slightly pessimistic
   */
  searchTargets(targets, startCountry, startArmies, start, totalStartingPoints, extra, attack, gameState, wayPoints = EMPTY_SET, exclusions = EMPTY_SET) {
    const game = this.game;
    const player = this.player;
    const type = this.type;
    const neighboursOpen = (c) => {
      let count = 0;
      for (const n of c.getNeighbours()) {
        if (n.getOwner() !== player && !exclusions.has(c)) count++;
      }
      return count;
    };
    const remaining = new JavaPriorityQueue((o1, o2) => {
      const diff = isub(o2.routeRemaining[start], o1.routeRemaining[start]);
      if (type === PLAYER_AI_HARD) {
        if (wayPoints.has(o1.targetCountry)) {
          if (wayPoints.has(o2.targetCountry)) {
            const outs1 = neighboursOpen(o1.targetCountry);
            const outs2 = neighboursOpen(o2.targetCountry);
            if (outs1 === 1) {
              if (outs2 === 1) return -diff;
              return 1;
            } else if (outs2 === 1) {
              return -1;
            }
            return diff + 2 * (outs1 - outs2);
          }
          return -1;
        }
        // the original checks wayPoints.contains(o2) (an AttackTarget), which is never true
        if (wayPoints.has(o2)) return 1;
      }
      return diff;
    });
    if (type === PLAYER_AI_HARD) {
      const ratio = gameState.me.playerValue / gameState.orderedPlayers[0].playerValue;
      if (ratio < 0.4) attack = false;
    } else if (type === PLAYER_AI_EASY) {
      attack = false;
    }
    const at0 = new AttackTarget(totalStartingPoints, startCountry);
    at0.routeRemaining[start] = startArmies;
    remaining.add(at0);
    while (!remaining.isEmpty()) {
      const current = remaining.poll();
      // the original checks wayPoints.contains(current) (an AttackTarget), which is never true

      let attackForce = current.routeRemaining[start];
      attackForce -= this.getMinPlacement();
      attackForce -= Math.min(idiv(current.targetCountry.getArmies(), attack ? 3 : 2), current.depth);
      if (attackForce + extra < 1) break;

      const v = current.targetCountry.getNeighbours();
      for (let i = 0; i < v.length; i++) {
        const c = v[i];
        if (c.getOwner() === player) continue;
        let cumulativeForces = targets.get(c);
        if (cumulativeForces == null) {
          if (exclusions.has(c)) continue;
          cumulativeForces = new AttackTarget(totalStartingPoints, c);
          targets.set(c, cumulativeForces);
        } else if (cumulativeForces.routeRemaining[start] !== INT_MIN) {
          continue;
        }
        cumulativeForces.depth = current.depth + 1;
        let available = attackForce;
        let toAttack = c.getArmies();
        if (game.getMaxDefendDice() === 2 || gameState.me.playerValue > gameState.orderedPlayers[0].playerValue || gameState.me.p.getType() === PLAYER_AI_EASY) {
          if (attack) {
            while (toAttack >= 10 || (available >= 10 && toAttack >= 5)) {
              toAttack -= 4;
              available -= 3;
            }
          }
          while (toAttack >= 5 || (available >= 5 && toAttack >= 2)) {
            toAttack -= 2;
            available -= 2;
          }
        } else if (attack) {
          const rounds = idiv(toAttack - 3, 3);
          if (rounds > 0) {
            toAttack -= 3 * rounds;
            available -= 3 * rounds;
          }
        }
        if (attack && available === toAttack + 1 && toAttack <= 2) {
          available = 1;
        } else if (game.getMaxDefendDice() === 2 || toAttack <= 2) {
          available = available - idiv(3 * toAttack, 2) - toAttack % 2;
        } else {
          available = available - 2 * toAttack;
        }
        cumulativeForces.attackPath[start] = current.targetCountry;
        cumulativeForces.routeRemaining[start] = available;
        if (cumulativeForces.remaining >= 0 && available >= 0) {
          cumulativeForces.remaining = (cumulativeForces.remaining + available) | 0;
        } else {
          cumulativeForces.remaining = Math.max(cumulativeForces.remaining, available);
        }
        remaining.add(cumulativeForces);
      }
    }
  }

  getBattleWon() {
    const gameState = this.getGameState(this.player, false);
    return this.getBattleWonFor(gameState);
  }

  /** compute the battle won move */
  getBattleWonFor(gameState) {
    const game = this.game;
    const player = this.player;
    if (this.ownsNeighbours(player, game.getDefender())) return 'move ' + game.getMustMove();
    let needed = -game.getAttacker().getArmies();
    const border = this.getBorder(gameState);
    let specialCase = false;
    if (!this.isIncreasingSet() && !this.eliminating && this.type !== PLAYER_AI_EASY) {
      let cont = null;
      if (!border.includes(game.getDefender()) || !gameState.me.owned.has(game.getDefender().getContinent())) {
        for (const c of game.getAttacker().getCrossContinentNeighbours()) {
          if (gameState.me.owned.has(c.getContinent())) {
            cont = c.getContinent();
            specialCase = true;
            break;
          }
        }
      }
      if (border.includes(game.getAttacker())) {
        specialCase = true;
        if (cont != null && game.getCardMode() === CARD_FIXED_SET && border.includes(game.getDefender()) && game.getDefender().getContinent() === game.getAttacker().getContinent()) {
          cont = null;
          specialCase = false;
        } else if (gameState.me.owned.has(game.getAttacker().getContinent())) {
          cont = game.getAttacker().getContinent();
        }
      }
      if (specialCase && game.getCardMode() !== CARD_FIXED_SET) needed = this.additionalTroopsNeeded(game.getAttacker(), gameState);
      if (cont != null) {
        if (cont.getBorderCountries().length > 2) {
          needed += cont.getArmyValue();
        } else {
          if (specialCase && game.getCardMode() === CARD_FIXED_SET) needed = this.additionalTroopsNeeded(game.getAttacker(), gameState);
          needed += idiv(4 * cont.getArmyValue(), Math.max(1, cont.getBorderCountries().length));
        }
      } else if (specialCase) {
        needed += game.getMaxDefendDice();
      }
    }
    if (specialCase && ((this.breaking != null && this.breaking.getOwner() != null) || gameState.commonThreat != null)) {
      needed = idiv(needed, 2);
    }
    if (!specialCase && this.ownsNeighbours(player, game.getAttacker())) {
      return 'move ' + Math.max(game.getMustMove(), game.getAttacker().getArmies() - this.getMinPlacement());
    }
    if (!specialCase && game.getMaxDefendDice() === 3 && !this.ownsNeighbours(player, game.getAttacker()) && gameState.me.playerValue > gameState.orderedPlayers[0].playerValue) {
      needed += game.getMaxDefendDice();
    }
    let forwardMin = 0;
    if (game.getAttacker().getArmies() - 1 > game.getMustMove()) {
      const defender = game.getDefender();
      const targets = new Map();
      this.searchTargets(targets, defender, game.getAttacker().getArmies() - 1, 0, 1, player.getExtraArmies(), true, gameState);
      forwardMin = this.getMinRemaining(targets, game.getAttacker().getArmies() - 1, border.includes(game.getAttacker()), gameState);
      if (forwardMin === INT_MAX) return 'move ' + game.getMustMove();
    }
    return 'move ' + Math.max(Math.min(-needed, game.getAttacker().getArmies() - Math.max(this.getMinPlacement(), forwardMin)), game.getMustMove());
  }

  /** an estimate of the remaining troops after taking all possible targets */
  getMinRemaining(targets, forwardMin, isBorder, gameState) {
    const game = this.game;
    let total = 0;
    for (const attackTarget of targets.values()) {
      if (attackTarget.remaining < 0 && !isBorder) return 0;
      total += 1;
      if (game.getMaxDefendDice() === 2 || attackTarget.targetCountry.getArmies() < 3) {
        total += attackTarget.targetCountry.getArmies();
        if (attackTarget.targetCountry.getArmies() < 2) total += attackTarget.targetCountry.getArmies();
      } else {
        total += 2 * attackTarget.targetCountry.getArmies();
      }
    }
    if (game.getMaxDefendDice() === 2) {
      total = toInt(total * 1.3);
      forwardMin -= total;
    } else {
      forwardMin -= total;
    }
    if (this.type === PLAYER_AI_HARD && !this.isIncreasingSet() && isBorder && this.isTooWeak(gameState)) return INT_MAX;
    return Math.max(isBorder ? game.getMaxDefendDice() : 0, forwardMin);
  }

  /** takes several passes over applicable territories to determine the tactical move */
  getTacMove() {
    const player = this.player;
    const t = player.getTerritoriesOwned();
    let sender = null;
    let receiver = null;
    let lowestScore = INT_MAX;
    const gs = this.getGameState(player, false);
    const v = this.getBorder(gs);
    const filtered = [];
    let targetContinents = null;

    for (let i = 0; i < t.length; i++) {
      const c = t[i];
      if (c.getArmies() <= this.getMinPlacement() || gs.capitals.has(c)) continue;
      // cooperation check to see if we should leave this continent
      if (c.getArmies() > 2 && gs.commonThreat != null && c.getCrossContinentNeighbours().length > 0 && !this.ownsNeighbours(player, c)) {
        coop: for (let j = 0; j < c.getNeighbours().length; j++) {
          const n = c.getNeighbours()[j];
          if (n.getOwner() === player && n.getContinent() !== c.getContinent()) {
            if (targetContinents == null) {
              const co = this.findTargetContinents(gs, new Map(), false, false);
              targetContinents = co.map((et) => et.co);
            }
            const index = targetContinents.indexOf(c.getContinent());
            if (index === -1 && c.getContinent().getOwner() === player) break coop;
            const indexOther = targetContinents.indexOf(n.getContinent());
            if ((indexOther > -1 && (index === -1 || index > indexOther)) || ((index === -1 || index > 0) && n.getContinent().getOwner() === player)) {
              const toSend = c.getArmies() - this.getMinPlacement();
              return this.getMoveCommand(c, n, toSend);
            }
          }
        }
      }
      if (v.includes(c) && idiv(this.additionalTroopsNeeded(c, gs), 2) + this.getMinPlacement() >= 0) continue;
      filtered.push(c);
      const score = this.scoreCountry(c);
      for (let j = 0; j < c.getNeighbours().length; j++) {
        const n = c.getNeighbours()[j];
        if (n.getOwner() !== player || !v.includes(n) || this.additionalTroopsNeeded(n, gs) < -1) continue;
        const total = -score + this.scoreCountry(n);
        if (total < lowestScore) {
          sender = c;
          receiver = n;
          lowestScore = total;
        }
      }
    }
    if (receiver != null) {
      let toSend = sender.getArmies() - this.getMinPlacement();
      if (v.includes(sender)) toSend = idiv(-this.additionalTroopsNeeded(sender, gs), 2) - this.getMinPlacement();
      return this.getMoveCommand(sender, receiver, toSend);
    }
    // move to the battle
    let max = null;
    for (let i = filtered.length - 1; i >= 0; i--) {
      const c = filtered[i];
      if (!this.ownsNeighbours(player, c)) {
        filtered.splice(i, 1);
        continue;
      }
      if (max == null || c.getArmies() > max.getArmies()) max = c;
      const score = this.scoreCountry(c);
      for (let j = 0; j < c.getNeighbours().length; j++) {
        const n = c.getNeighbours()[j];
        if (n.getOwner() !== player || this.ownsNeighbours(player, n)) continue;
        const total = -score + this.scoreCountry(n);
        if (total < lowestScore) {
          sender = c;
          receiver = n;
          lowestScore = total;
        }
      }
    }
    if (receiver != null) {
      let toSend = sender.getArmies() - this.getMinPlacement();
      if (v.includes(sender)) toSend = idiv(-this.additionalTroopsNeeded(sender, gs), 2) - this.getMinPlacement();
      return this.getMoveCommand(sender, receiver, toSend);
    }
    // move from the interior (not very smart)
    if (max != null && max.getArmies() > this.getMinPlacement() + 1) {
      let least = INT_MAX;
      for (let j = 0; j < max.getNeighbours().length; j++) {
        const n = max.getNeighbours()[j];
        if (max.getOwner() !== player) continue;
        if (n.getArmies() < least) {
          receiver = n;
          least = n.getArmies();
        }
      }
      if (receiver != null) return this.getMoveCommand(max, receiver, max.getArmies() - this.getMinPlacement() - 1);
    }
    return 'nomove';
  }

  getMoveCommand(sender, receiver, toSend) {
    return 'movearmies ' + sender.getColor() + ' ' + receiver.getColor() + ' ' + toSend;
  }

  getAttack() {
    this.eliminating = false;
    this.breaking = null;
    return this.plan(true);
  }

  /** will roll the maximum, but checks to see if the attack is still the best plan every 3rd roll */
  getRoll() {
    const game = this.game;
    const n = game.getAttacker().getArmies() - 1;
    const m = game.getDefender().getArmies();
    if (n < 3 && game.getBattleRounds() > 0 && (n < m || (n === m && game.getDefender().getOwner().getTerritoriesOwned().length !== 1))) {
      return 'retreat';
    }
    if (this.type !== PLAYER_AI_EASY && (game.getBattleRounds() % 3 === 2 || (game.getBattleRounds() > 0 && (n - Math.min(m, game.getMaxDefendDice()) <= 0)))) {
      const result = this.plan(true);
      if (result === 'endattack') return 'retreat';
      const st = result.split(' ');
      if (game.getAttacker().getColor() !== parseInt(st[1], 10) || game.getDefender().getColor() !== parseInt(st[2], 10)) {
        return 'retreat';
      }
    }
    return 'roll ' + Math.min(3, n);
  }

  /** a quick overview of the game state */
  getGameState(p, excludeCards) {
    const game = this.game;
    const player = this.player;
    const players = game.getPlayers();
    const g = newGameState();
    const c = game.getContinents();
    g.capitals = player.getCapital() == null ? EMPTY_SET : new Set();
    g.owned = new Array(c.length);
    for (let i = 0; i < c.length; i++) g.owned[i] = c[i].getOwner();
    let index = -1;
    let playerCount = 1;
    for (let i = 0; i < players.length; i++) {
      const player2 = players[i];
      if (player2.getCapital() != null) {
        if (g.capitals !== EMPTY_SET) g.capitals.add(player2.getCapital());
        else throw new Error('UnsupportedOperationException');
      }
      if (player2.getTerritoriesOwned().length === 0) continue;
      if (player2 === p) index = i;
      else playerCount++;
    }
    g.orderedPlayers = [];
    let attackOrder = 0;
    for (let i = 0; i < players.length; i++) {
      // note: index may be -1 in the original too, (index + i) % size can then be -1 for i=0
      const player2 = players[javaMod(index + i, players.length)];
      if (player2 === undefined || player2.getTerritoriesOwned().length === 0) continue;
      const cards = player2.getCards().length + 1;
      const cardEstimate = (i === 0 && excludeCards) ? 0 : this.getCardEstimate(cards);
      const ps = newPlayerState();
      const t = player2.getTerritoriesOwned();
      let noArmies = 0;
      let attackable = 0;
      const strategic = this.isStrategic(player2);
      for (let j = 0; j < t.length; j++) {
        const country = t[j];
        noArmies += country.getArmies();
        let available = country.getArmies() - 1;
        if (this.ownsNeighbours(player2, country)) available = idiv(country.getArmies(), 2);
        if (available > 4) {
          if (available > 8 && strategic) {
            if (available > 13) available = toInt(available * 1.3);
            available += 2;
          }
          available += 1;
        }
        attackable += available;
      }
      let reenforcements = Math.max(3, idiv(player2.getNoTerritoriesOwned(), 3)) + cardEstimate;
      if (reenforcements > 8 && strategic) reenforcements = toInt(reenforcements * 1.3);
      let attack = attackable + reenforcements;
      const owned = new Set();
      for (let j = 0; j < g.owned.length; j++) {
        if (g.owned[j] === player2) {
          attack += c[j].getArmyValue();
          if (strategic) ps.playerValue += 3 * c[j].getArmyValue();
          else ps.playerValue += 1.5 * c[j].getArmyValue() + 1;
          owned.add(c[j]);
        }
      }
      ps.strategic = strategic;
      ps.armies = noArmies;
      ps.owned = owned;
      ps.attackValue = attack;
      ps.attackOrder = attackOrder;
      ps.defenseValue = idiv(5 * noArmies, 4) + noArmies % 4 + player2.getNoTerritoriesOwned();
      ps.p = player2;
      if (i === 0) g.me = ps;
      else g.orderedPlayers.push(ps);
      ps.playerValue += ps.attackValue + ((game.getMaxDefendDice() === 2 && !this.isIncreasingSet()) ? 1 : game.getMaxDefendDice() > 2 ? 3 : 2) * ps.defenseValue;
      attackOrder++;
    }
    g.orderedPlayers.sort(reverse(playerStateCompare));
    if (game.getSetupDone() && g.orderedPlayers.length > 0) {
      let multiplier = game.getCards().length === 0 ? (game.isRecycleCards() ? 1.2 : 1.1) : (player.getMission() != null || player.getCapital() != null) ? 1.1 : 1.3;
      const topPlayer = g.orderedPlayers[0];
      if (this.type === PLAYER_AI_EASY) {
        multiplier *= 1.6;
      } else if (this.type === PLAYER_AI_HARD && player.getStatistics().length > 3) {
        if (!this.isIncreasingSet()) {
          multiplier = Math.max(1, multiplier - 0.4 + g.orderedPlayers.length * 0.1);
        } else if (game.getCardMode() !== CARD_ITALIANLIKE_SET) {
          multiplier *= 1.5;
        }
      } else if (this.type === PLAYER_AI_AVERAGE) {
        multiplier *= 1.2;
      }
      g.targetPlayers.push(topPlayer.p);
      if (g.orderedPlayers.length > 1 && topPlayer.playerValue > multiplier * g.me.playerValue) {
        g.breakOnlyTargets = game.getMaxDefendDice() === 2;
        const ps = g.orderedPlayers[1];
        if (topPlayer.playerValue > multiplier * ps.playerValue) {
          g.commonThreat = topPlayer;
        } else {
          g.targetPlayers.push(ps.p);
        }
      } else if (this.type === PLAYER_AI_HARD && this.isIncreasingSet() && g.orderedPlayers[g.orderedPlayers.length - 1].defenseValue / topPlayer.attackValue > 0.3) {
        g.targetPlayers.length = 0;
        g.targetPlayers.push(g.orderedPlayers[g.orderedPlayers.length - 1].p);
      }
    }
    return g;
  }

  getCardEstimate(cards) {
    const game = this.game;
    const tradeIn = game.getCardMode() !== CARD_INCREASING_SET ? 8 : game.getNewCardState();
    return cards < 3 ? 0 : toInt((cards - 2) / 3.0 * tradeIn);
  }

  /** a quick measure of how the player has performed over the last several turns */
  isStrategic(player2) {
    if (player2 === this.player) return false;
    const stats = player2.getStatistics();
    if (stats.length < 4) return false;
    const end = 4;
    let reenforcements = 0;
    let kills = 0;
    let casualities = 0;
    for (let i = stats.length - 1; i >= end; i--) {
      const s = stats[i];
      reenforcements += s.get(StatType.REINFORCEMENTS);
      kills += s.get(StatType.KILLS);
      casualities += s.get(StatType.CASUALTIES);
      if (s.get(StatType.CONTINENTS) === 0) return false;
    }
    return reenforcements + idiv(kills, player2.getCards().length > 2 ? 1 : 2) > 2 * casualities;
  }

  /** delay trading in cards when sensible */
  getTrade() {
    const game = this.game;
    if (!game.getTradeCap() && this.type !== PLAYER_AI_EASY) {
      if (this.player.getCards().length >= game.getMaxCardsPerPlayer()) return super.getTrade();
      const gs = this.getGameState(this.player, true);
      if (gs.commonThreat == null && gs.orderedPlayers.length > 1 && !this.pressAttack(gs) && !this.isTooWeak(gs)) return 'endtrade';
    }
    return super.getTrade();
  }

  /** all countries that can be attacked from */
  findAttackableTerritories(p, attack) {
    const result = [];
    for (const country of p.getTerritoriesOwned()) {
      if ((!attack || country.getArmies() > 1) && !this.ownsNeighbours(p, country)) result.push(country);
    }
    return result;
  }

  ownsNeighbours(p, c) {
    for (const n of c.getNeighbours()) if (n.getOwner() !== p) return false;
    return true;
  }

  getTradeCommand(result) {
    const player = this.player;
    if (this.type !== PLAYER_AI_EASY) {
      let ownsCount = 0;
      for (let i = 0; i < result.length; i++) {
        if (result[i].getCountry() != null && player.getTerritoriesOwned().includes(result[i].getCountry())) ownsCount++;
      }
      // swap for a single owned country
      if (ownsCount !== 1 && player.getCards().length > 3) {
        // Arrays.asList(result) is a live view of the array in the original
        for (const card of player.getCards()) {
          if (result.some((x) => x.equals(card))) continue;
          if (ownsCount > 1) {
            if (card.getCountry() == null || !player.getTerritoriesOwned().includes(card.getCountry())) {
              for (let i = 0; i < result.length; i++) {
                if (result[i].getName() === card.getName()) {
                  result[i] = card;
                  if (--ownsCount === 1) return super.getTradeCommand(result);
                  break;
                }
              }
            }
          } else if (card.getCountry() != null && player.getTerritoriesOwned().includes(card.getCountry())) {
            for (let i = 0; i < result.length; i++) {
              if (result[i].getName() === card.getName()) {
                result[i] = card;
                return super.getTradeCommand(result);
              }
            }
          }
        }
      }
    }
    return super.getTradeCommand(result);
  }
}

function javaMod(a, b) {
  return a % b; // java % keeps the sign of the dividend, like JS
}
