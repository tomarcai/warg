// warg - port of net.yura.domination.engine.ai.AIManager and AbstractAI. GPLv3.

import { AISubmissive, AITest } from './AISubmissive.js';
import { AIDomination, PLAYER_AI_EASY, PLAYER_AI_AVERAGE, PLAYER_AI_HARD } from './AIDomination.js';
import { AIMission } from './AIMission.js';
import { AICapital } from './AICapital.js';
import * as RG from '../RiskGame.js';

/** picks the right AI logic for the game mode (AbstractAI) */
class AbstractAI {
  constructor(type, command, random) {
    this.type = type;
    this.command = command;
    this.domination = new AIDomination(type, random);
    this.mission = new AIMission(type, random);
    this.capital = new AICapital(type, random);
    this.current = null;
  }
  getType() { return this.type; }
  getCommand() { return this.command; }
  setGame(game) {
    if (game == null) {
      if (this.current != null) this.current.setGame(null);
    } else {
      const mode = game.getGameMode();
      if (mode === RG.MODE_CAPITAL) this.current = this.capital;
      else if (mode === RG.MODE_SECRET_MISSION) this.current = this.mission;
      else this.current = this.domination;
      this.current.setGame(game);
    }
  }
  getBattleWon() { return this.current.getBattleWon(); }
  getTacMove() { return this.current.getTacMove(); }
  getTrade() { return this.current.getTrade(); }
  getPlaceArmies() { return this.current.getPlaceArmies(); }
  getAttack() { return this.current.getAttack(); }
  getRoll() { return this.current.getRoll(); }
  getCapital() { return this.current.getCapital(); }
  getAutoDefendString() { return this.current.getAutoDefendString(); }
}

export class AIManager {
  constructor(random) {
    this.ais = new Map();
    this.wait = 500;
    // same order as META-INF/services
    this.registerAI(new AISubmissive(random));
    this.registerAI(new AbstractAI(PLAYER_AI_EASY, 'easy', random));
    this.registerAI(new AbstractAI(PLAYER_AI_AVERAGE, 'average', random));
    this.registerAI(new AbstractAI(PLAYER_AI_HARD, 'hard', random));
    this.registerAI(new AITest(random));
  }
  registerAI(ai) {
    const type = ai.getType();
    if (this.ais.has(type)) throw new Error('more then 1 ai with same type: ' + type);
    this.ais.set(type, ai);
  }
  getTypeFromCommand(command) {
    for (const ai of this.ais.values()) if (ai.getCommand() === command) return ai.getType();
    throw new Error("unknown command: '" + command + "'");
  }
  getCommandFromType(type) {
    for (const ai of this.ais.values()) if (ai.getType() === type) return ai.getCommand();
    throw new Error('unknown type ' + type);
  }
  getAICommands() {
    return [...this.ais.values()].map((a) => a.getCommand());
  }
  getOutput(game, type) {
    const ai = this.ais.get(type);
    if (ai == null) throw new Error('can not find ai for type ' + type);
    ai.setGame(game);
    let output = null;
    switch (game.getState()) {
      case RG.STATE_TRADE_CARDS: output = ai.getTrade(); break;
      case RG.STATE_PLACE_ARMIES: output = ai.getPlaceArmies(); break;
      case RG.STATE_ATTACKING: output = ai.getAttack(); break;
      case RG.STATE_ROLLING: output = ai.getRoll(); break;
      case RG.STATE_BATTLE_WON: output = ai.getBattleWon(); break;
      case RG.STATE_FORTIFYING: output = ai.getTacMove(); break;
      case RG.STATE_SELECT_CAPITAL: output = ai.getCapital(); break;
      case RG.STATE_DEFEND_YOURSELF: output = ai.getAutoDefendString(); break;
      case RG.STATE_END_TURN: output = 'endgo'; break;
      case RG.STATE_GAME_OVER: throw new Error('AI error: game is over');
      default: throw new Error('AI error: unknown state ' + game.getState());
    }
    if (output == null) throw new Error('AI ERROR! output is null');
    return output;
  }
  closeGame() {
    for (const ai of this.ais.values()) ai.setGame(null);
  }
}
