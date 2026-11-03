// warg - port of net.yura.domination.engine.ai.logic.AIMission (Steven Hawkins). GPLv3.

import { AIDomination } from './AIDomination.js';

export class AIMission extends AIDomination {
  fortify(gs, attackable, minimal, borders) {
    const player = this.player;
    if (player.getMission().getNoofarmies() > 1 &&
        (player.getTerritoriesOwned().length - 3 >= player.getMission().getNoofcountries())) {
      const result = super.fortify(gs, attackable, true, player.getTerritoriesOwned());
      if (result != null) return result;
    }
    return super.fortify(gs, attackable, minimal, borders);
  }

  shouldEndAttack(gameState) {
    const result = super.shouldEndAttack(gameState);
    if (result && this.isCloseToTerritoryTarget()) return false;
    return result;
  }

  scoreCountry(country) {
    let result = super.scoreCountry(country);
    const m = this.player.getMission();
    if (m.getPlayer() != null && !this.isTargetMoot()) {
      for (const nc of country.getNeighbours()) {
        if (nc.getOwner() === m.getPlayer()) result--;
      }
    }
    return result;
  }

  isCloseToTerritoryTarget() {
    const m = this.player.getMission();
    return m.getNoofcountries() > 0 &&
      (m.getPlayer() == null || this.isTargetMoot()) &&
      this.player.getTerritoriesOwned().length - 3 >= m.getNoofcountries();
  }

  pressAttack(gameState) {
    const result = super.pressAttack(gameState);
    if (!result && this.isCloseToTerritoryTarget()) return true;
    return result;
  }

  getMinPlacement() {
    return Math.max(1, this.player.getMission().getNoofarmies());
  }

  getGameState(p, excludeCards) {
    const g = super.getGameState(p, excludeCards);
    const m = this.player.getMission();
    if (m.getPlayer() != null && !this.isTargetMoot() && m.getPlayer() !== g.orderedPlayers[0].p) {
      g.targetPlayers.unshift(m.getPlayer());
    }
    if (g.commonThreat == null) {
      for (const ps of g.orderedPlayers) {
        if (ps.owned.size > 1) {
          g.breakOnlyTargets = true;
          g.targetPlayers.unshift(ps.p);
          break;
        }
      }
    }
    return g;
  }

  isTargetMoot() {
    const m = this.player.getMission();
    return m.getPlayer() != null && (m.getPlayer() === this.player || m.getPlayer().getTerritoriesOwned().length === 0);
  }

  getContinentValue(co) {
    let result = super.getContinentValue(co);
    if (this.isTargetContinent(co)) result *= 4;
    return result;
  }

  isTargetContinent(co) {
    const m = this.player.getMission();
    return m.getContinent1() === co || m.getContinent2() === co || m.getContinent3() === co;
  }

  shouldProactivelyFortify(co, attack, attackable, gameState, targets, pressAttack, continents) {
    const result = super.shouldProactivelyFortify(co, attack, attackable, gameState, targets, pressAttack, continents);
    if (result && this.isTargetContinent(co) && gameState.me.owned.size > 0) return false;
    return result;
  }
}
