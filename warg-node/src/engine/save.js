// warg - save / load of a RiskGame as JSON (replaces Java serialization). GPLv3.

import { RiskGame, ANY_CONTINENT, SAVE_VERSION } from './RiskGame.js';
import { Card, Mission, Player, Statistic } from './model.js';
import { Random } from './util.js';

function cardToJson(c) {
  return { name: c.name, country: c.country ? c.country.color : null };
}

function continentRef(game, c) {
  if (c == null) return null;
  if (c === ANY_CONTINENT || ANY_CONTINENT.equals(c)) return '*';
  return game.Continents.indexOf(c) + 1;
}

export function serializeGame(game) {
  const players = game.Players;
  const pIndex = (p) => (p == null ? null : players.indexOf(p));
  const json = {
    format: SAVE_VERSION,
    mapfile: game.mapfile,
    cardsfile: game.cardsfile,
    ImagePic: game.ImagePic,
    ImageMap: game.ImageMap,
    previewPic: game.previewPic,
    properties: [...game.properties.entries()],
    gameMode: game.gameMode,
    cardMode: game.cardMode,
    recycleCards: game.recycleCards,
    maxDefendDice: game.maxDefendDice,
    minimumNewArmies: game.minimumNewArmies,
    maxCardsPerPlayer: game.maxCardsPerPlayer,
    runmaptest: game.runmaptest,
    replayCommands: [...game.replayCommands],
    setup: game.setup,
    gameState: game.gameState,
    cardState: game.cardState,
    mustmove: game.mustmove,
    capturedCountry: game.capturedCountry,
    tradeCap: game.tradeCap,
    attacker: game.attacker ? game.attacker.color : null,
    defender: game.defender ? game.defender.color : null,
    attackerDice: game.attackerDice,
    defenderDice: game.defenderDice,
    currentPlayer: pIndex(game.currentPlayer),
    random: game.r.getState(),
    loaded: game.Countries != null,
    players: players.map((p) => ({
      type: p.type,
      name: p.name,
      color: p.color,
      address: p.address,
      extraArmies: p.extraArmies,
      cards: p.cardsOwned.map(cardToJson),
      territories: p.territoriesOwned.map((c) => c.color),
      playersEliminated: p.playersEliminated.map(pIndex),
      capital: p.capital ? p.capital.color : null,
      mission: p.mission ? game.Missions.indexOf(p.mission) : null,
      autoendgo: p.autoendgo,
      autodefend: p.autodefend,
      statistics: p.statistics.map((s) => ({ s: [...s.statistics], dice: s.dice, diceCount: s.diceCount })),
    })),
  };
  if (game.Countries != null) {
    json.countries = game.Countries.map((c) => ({ owner: pIndex(c.owner), armies: c.armies }));
    json.cards = game.Cards.map(cardToJson);
    json.usedCards = game.usedCards.map(cardToJson);
    json.missions = game.Missions.map((m) => ({
      player: pIndex(m.player),
      noc: m.noofcountries,
      noa: m.noofarmies,
      c1: continentRef(game, m.con1),
      c2: continentRef(game, m.con2),
      c3: continentRef(game, m.con3),
      description: m.discription,
    }));
  }
  return json;
}

/** rebuilds a game from json, the map file is re-read to get the countries and borders */
export async function deserializeGame(json, io, options = {}) {
  if (!json || json.format !== SAVE_VERSION) throw new Error('not a warg save file');
  const game = new RiskGame(io, options);
  game.r = new Random(json.random);
  game.mapfile = json.mapfile;
  game.cardsfile = json.cardsfile;
  game.ImagePic = json.ImagePic;
  game.ImageMap = json.ImageMap;
  game.previewPic = json.previewPic;
  game.properties = new Map(json.properties);
  game.gameMode = json.gameMode;
  game.cardMode = json.cardMode;
  game.recycleCards = json.recycleCards;
  game.maxDefendDice = json.maxDefendDice;
  game.minimumNewArmies = json.minimumNewArmies;
  game.maxCardsPerPlayer = json.maxCardsPerPlayer;
  game.runmaptest = json.runmaptest;
  game.replayCommands = [...json.replayCommands];
  game.setup = json.setup;
  game.gameState = json.gameState;
  game.cardState = json.cardState;
  game.mustmove = json.mustmove;
  game.capturedCountry = json.capturedCountry;
  game.tradeCap = json.tradeCap;
  game.attackerDice = json.attackerDice;
  game.defenderDice = json.defenderDice;

  const players = json.players.map((pj) => {
    const p = new Player(pj.type, pj.name, pj.color, pj.address);
    p.extraArmies = pj.extraArmies;
    p.autoendgo = pj.autoendgo;
    p.autodefend = pj.autodefend;
    p.statistics = pj.statistics.map((sj) => {
      const s = new Statistic();
      s.statistics = [...sj.s];
      s.dice = sj.dice;
      s.diceCount = sj.diceCount;
      return s;
    });
    p.currentStatistic = p.statistics[p.statistics.length - 1];
    return p;
  });
  game.Players = players;

  if (json.loaded) {
    // the map itself (names, positions, borders) comes from the map file
    await game.loadMap(true);
    const country = (id) => (id == null ? null : game.Countries[id - 1]);
    const card = (cj) => new Card(cj.name, country(cj.country));
    json.countries.forEach((cj, i) => {
      game.Countries[i].owner = cj.owner == null ? null : players[cj.owner];
      game.Countries[i].armies = cj.armies;
    });
    game.Cards = json.cards.map(card);
    game.usedCards = json.usedCards.map(card);
    const cont = (ref) => (ref == null ? null : ref === '*' ? ANY_CONTINENT : game.Continents[ref - 1]);
    game.Missions = json.missions.map((mj) => new Mission(mj.player == null ? null : players[mj.player], mj.noc, mj.noa, cont(mj.c1), cont(mj.c2), cont(mj.c3), mj.description));
    json.players.forEach((pj, i) => {
      const p = players[i];
      p.cardsOwned = pj.cards.map(card);
      p.territoriesOwned = pj.territories.map(country);
      p.playersEliminated = pj.playersEliminated.map((x) => players[x]);
      p.capital = country(pj.capital);
      p.mission = pj.mission == null ? null : game.Missions[pj.mission];
    });
    game.attacker = country(json.attacker);
    game.defender = country(json.defender);
  }
  game.currentPlayer = json.currentPlayer == null ? null : players[json.currentPlayer];
  return game;
}
