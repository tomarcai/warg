// warg - port of net.yura.domination.engine.ai.logic.AICapital (Steven Hawkins). GPLv3.

import { AIDomination } from './AIDomination.js';
import { INT_MAX, toInt, idiv } from '../util.js';

export class AICapital extends AIDomination {
  /** adds the best defended capital to the border when there is a threat */
  getBorder(gs) {
    const border = super.getBorder(gs);
    if (gs.commonThreat == null) return border;
    const attack = idiv(toInt(gs.commonThreat.attackValue), gs.orderedPlayers.length + 1);
    let minNeeded = INT_MAX;
    let priority = null;
    for (const c of gs.capitals) {
      if (c.getOwner() !== this.player) continue;
      const additional = Math.max(attack - c.getArmies(), this.additionalTroopsNeeded(c, gs));
      if (additional <= 0) return border;
      if (additional < minNeeded) {
        minNeeded = additional;
        priority = c;
      }
    }
    if (priority != null) border.unshift(priority);
    return border;
  }

  /** overrides the planning behavior to consider taking capital logic */
  planObjective(attack, attackable, gameState, targets, allCountriesTaken, pressAttack, shouldEndAttack, highProbability) {
    const game = this.game;
    const player = this.player;
    if (game.getSetupDone()) {
      const owned = new Map();
      for (const c of gameState.capitals) {
        const count = owned.get(c.getOwner());
        owned.set(c.getOwner(), count == null ? 1 : count + 1);
      }
      const num = gameState.capitals.size;
      let myowned = owned.get(player);
      if (myowned == null) myowned = 0;
      const percentOwned = myowned / num;
      const ratio = gameState.me.playerValue / gameState.orderedPlayers[0].playerValue;

      // offensive planning
      if (highProbability || (this.isIncreasingSet() && (ratio / 3 > percentOwned)) ||
          (percentOwned >= 0.5 && (this.isIncreasingSet() || ratio > 1))) {
        const result = this.planCapitalMove(attack, attackable, gameState, targets, null, highProbability, allCountriesTaken, !highProbability, shouldEndAttack);
        if (result != null) return result;
      }
      // defensive planning
      for (const [other, numOwned] of owned) {
        if (other === player || numOwned / num < 0.5) continue;
        let primaryDefense = 0;
        for (const c of gameState.capitals) {
          if (c.getOwner() !== other) primaryDefense += c.getArmies();
        }
        for (let j = 0; j < gameState.orderedPlayers.length; j++) {
          const ps = gameState.orderedPlayers[j];
          if (ps.p === other) {
            if (gameState.commonThreat == null && gameState.orderedPlayers.length > 1 && ps.attackValue > (ps.strategic ? 3 : 4) * primaryDefense) {
              gameState.commonThreat = ps;
              if (!gameState.targetPlayers.includes(ps.p)) gameState.targetPlayers.push(ps.p);
            }
            if (ps.attackValue > 2 * primaryDefense) {
              if (myowned < 2) {
                const result = this.planCapitalMove(attack, attackable, gameState, targets, other, false, allCountriesTaken, !highProbability, shouldEndAttack);
                if (result != null) return result;
              }
            }
            break;
          }
        }
        break;
      }
    } else if (this.r.nextInt(game.getPlayers().length) === 0) {
      // defend the capital more
      const c = this.findCapital();
      return this.getPlaceCommand(c, 1);
    }
    return null;
  }

  /** plans to take one (owned by the target player) or all of the remaining capitals */
  planCapitalMove(attack, attackable, gameState, targets, target, allOrNone, allCountriesTaken, lowProbability, shouldEndAttack) {
    let remaining = this.player.getExtraArmies();
    const toAttack = [];
    for (const c of gameState.capitals) {
      if (c.getOwner() === this.player || (target != null && target !== c.getOwner())) continue;
      const at = targets.get(c);
      if (at == null) {
        if (allOrNone) return null;
        continue;
      }
      if (at.remaining < 1) {
        remaining += at.remaining;
        if (remaining < 1 && allOrNone) return null;
        if (!attack && !allOrNone) toAttack.push(at);
      } else if (attack) {
        toAttack.push(at);
      } else {
        return null;
      }
    }
    if (toAttack.length > 0) {
      if (allOrNone) {
        const et = {
          attackTargets: toAttack,
          ps: gameState.orderedPlayers[0],
          target: false,
          allOrNone,
          co: null,
          compareTo() { return 0; },
        };
        return this.eliminate(attackable, targets, gameState, attack, remaining, allCountriesTaken, et, shouldEndAttack, lowProbability);
      }
      toAttack.sort((a, b) => b.compareTo(a));
      const at = toAttack[0];
      const route = this.findBestRoute(attackable, gameState, attack, null, at, gameState.targetPlayers[0], targets);
      const start = attackable[route];
      if (attack) return this.getAttackCommand(targets, at, route, start);
      return this.getPlaceCommand(start, -at.remaining + 1);
    }
    return null;
  }

  /** defend capitals more after winning a battle */
  getBattleWonFor(gameState) {
    const game = this.game;
    if (gameState.commonThreat == null) return super.getBattleWonFor(gameState);
    if (gameState.capitals.has(game.getAttacker())) {
      const needed = this.additionalTroopsNeeded(game.getAttacker(), gameState);
      if (needed > 0) return 'move ' + game.getMustMove();
      return 'move ' + Math.max(game.getMustMove(), idiv(-needed, 2) - this.getMinPlacement());
    }
    if (gameState.capitals.has(game.getDefender()) &&
        (this.ownsNeighbours(this.player, game.getAttacker()) || !this.ownsNeighbours(this.player, game.getDefender()))) {
      return 'move all';
    }
    return super.getBattleWonFor(gameState);
  }

  getCapital() {
    return 'capital ' + this.findCapital().getColor();
  }

  /** the country with the lowest (best) score as the capital */
  findCapital() {
    let score = INT_MAX;
    let result = null;
    for (const c of this.player.getTerritoriesOwned()) {
      let val = this.scoreCountry(c);
      val -= idiv(c.getArmies(), this.game.getPlayers().length);
      if (val < score || (val === score && this.r.nextBoolean())) {
        score = val;
        result = c;
      }
    }
    return result;
  }

  getContinentValue(co) {
    const value = super.getContinentValue(co);
    if (!this.game.getSetupDone()) return Math.sqrt(value);
    return super.getContinentValue(co);
  }
}
