// warg - port of Domination core model (Country, Continent, Card, Mission, Player, Statistic). GPLv3.

export const StatType = Object.freeze({
  COUNTRIES: 0,
  ARMIES: 1,
  KILLS: 2,
  CASUALTIES: 3,
  REINFORCEMENTS: 4,
  CONTINENTS: 5,
  CONNECTED_EMPIRE: 6,
  ATTACKS: 7,
  RETREATS: 8,
  COUNTRIES_WON: 9,
  COUNTRIES_LOST: 10,
  ATTACKED: 11,
  CARDS: 12,
  DICE: 13,
});

/** name + summable, same order as the Java enum */
export const STAT_TYPES = [
  { id: 0, key: 'countries', summable: false },
  { id: 1, key: 'armies', summable: false },
  { id: 2, key: 'kills', summable: true },
  { id: 3, key: 'casualties', summable: true },
  { id: 4, key: 'reinforcements', summable: true },
  { id: 5, key: 'continents', summable: false },
  { id: 6, key: 'empire', summable: false },
  { id: 7, key: 'attacks', summable: true },
  { id: 8, key: 'retreats', summable: true },
  { id: 9, key: 'victories', summable: true },
  { id: 10, key: 'defeats', summable: true },
  { id: 11, key: 'attacks', summable: true }, // ATTACKED uses the same name in the original
  { id: 12, key: 'cards', summable: false },
  { id: 13, key: 'dice', summable: false },
];

export class Statistic {
  constructor() {
    this.statistics = new Array(13).fill(0);
    this.dice = 0;
    this.diceCount = 0;
  }
  endGoStatistics(countries, armies, continents, conectedEmpire, cards) {
    this.statistics[StatType.COUNTRIES] = countries;
    this.statistics[StatType.ARMIES] = armies;
    this.statistics[StatType.CONTINENTS] = continents;
    this.statistics[StatType.CONNECTED_EMPIRE] = conectedEmpire;
    this.statistics[StatType.CARDS] = cards;
  }
  addReinforcements(a) { this.statistics[StatType.REINFORCEMENTS] += a; }
  addKill() { this.statistics[StatType.KILLS]++; }
  addCasualty() { this.statistics[StatType.CASUALTIES]++; }
  addAttack() { this.statistics[StatType.ATTACKS]++; }
  addAttacked() { this.statistics[StatType.ATTACKED]++; }
  addRetreat() { this.statistics[StatType.RETREATS]++; }
  addCountriesWon() { this.statistics[StatType.COUNTRIES_WON]++; }
  addCountriesLost() { this.statistics[StatType.COUNTRIES_LOST]++; }
  addDice(diceValue) {
    this.dice = (this.dice * this.diceCount + diceValue + 1) / (this.diceCount + 1);
    this.diceCount++;
  }
  get(statType) {
    if (statType === StatType.DICE) {
      if (this.diceCount === 0) return NaN;
      return this.dice;
    }
    return this.statistics[statType];
  }
}

export class Continent {
  constructor(id, name, numberOfArmies, color) {
    this.idString = id;
    this.name = name;
    this.color = color;
    this.armyValue = numberOfArmies;
    this.territoriesContained = [];
    this._borderCountries = null;
  }
  getIdString() { return this.idString; }
  getName() { return this.name; }
  getColor() { return this.color; }
  getArmyValue() { return this.armyValue; }
  isOwned(p) {
    for (const c of this.territoriesContained) {
      if (c.owner !== p) return false;
    }
    return true;
  }
  getNumberOwned(p) {
    let n = 0;
    for (const c of this.territoriesContained) if (c.owner === p) n++;
    return n;
  }
  getOwner() {
    let owner = this.territoriesContained[0].owner;
    for (let c = 1; c < this.territoriesContained.length && owner !== null; c++) {
      if (this.territoriesContained[c].owner !== owner) owner = null;
    }
    return owner;
  }
  getBorderCountries() {
    if (this._borderCountries === null) {
      const b = [];
      for (const country of this.territoriesContained) {
        for (const w of country.neighbours) {
          if (w.continent !== this) {
            b.push(country);
            break;
          }
        }
      }
      this._borderCountries = b;
    }
    return this._borderCountries;
  }
  getTerritoriesContained() { return this.territoriesContained; }
  addTerritoriesContained(t) {
    this._borderCountries = null;
    this.territoriesContained.push(t);
  }
  /** Continent.equals in Java compares name, id and army value */
  equals(o) {
    return o != null && o instanceof Continent && o.name === this.name && o.idString === this.idString && o.armyValue === this.armyValue;
  }
  toString() { return this.armyValue !== 0 ? `${this.idString} [${this.armyValue}]` : this.idString; }
}

export class Country {
  constructor(color, id, name, continent, x, y) {
    this.idString = id;
    this.color = color;
    this.name = name;
    this.continent = continent;
    this.owner = null;
    this.armies = 0;
    this.x = x;
    this.y = y;
    this.neighbours = [];
    this.incomingNeighbours = [];
    this._crossContinentNeighbours = null;
  }
  getIdString() { return this.idString; }
  getColor() { return this.color; }
  getName() { return this.name; }
  getArmies() { return this.armies; }
  getContinent() { return this.continent; }
  getOwner() { return this.owner; }
  setOwner(p) { this.owner = p; }
  getX() { return this.x; }
  getY() { return this.y; }
  getNeighbours() { return this.neighbours; }
  getIncomingNeighbours() { return this.incomingNeighbours; }
  isNeighbours(t) { return this.neighbours.includes(t); }
  getCrossContinentNeighbours() {
    if (this._crossContinentNeighbours === null) {
      this._crossContinentNeighbours = this.neighbours.filter((o) => o.continent !== this.continent);
    }
    return this._crossContinentNeighbours;
  }
  addNeighbour(t) {
    this._crossContinentNeighbours = null;
    this.neighbours.push(t);
    t.incomingNeighbours.push(this);
  }
  addArmy() { this.armies++; }
  addArmies(n) { this.armies += n; }
  removeArmies(n) { this.armies -= n; }
  looseArmy() { this.armies--; }
  getDistanceTo(px, py) {
    px -= this.x;
    py -= this.y;
    return Math.trunc(Math.sqrt(px * px + py * py));
  }
  toString() { return `${this.idString} (${this.color})`; }
}

export const CAVALRY = 'Cavalry';
export const INFANTRY = 'Infantry';
export const CANNON = 'Cannon';
export const WILDCARD = 'wildcard';

export class Card {
  constructor(name, country) {
    if (name !== CAVALRY && name !== INFANTRY && name !== CANNON && name !== WILDCARD) {
      throw new Error('trying to make a card with an unknown type: ' + name);
    }
    this.name = name;
    this.country = country;
  }
  getName() { return this.name; }
  getCountry() { return this.country; }
  equals(o) {
    return o instanceof Card && this.name === o.name && (
      (this.country == null && o.country == null) ||
      (this.country != null && o.country != null && this.country.color === o.country.color));
  }
  toString() { return this.country ? `${this.name} - ${this.country}` : this.name; }
}

export class Mission {
  constructor(p, noc, noa, c1, c2, c3, d) {
    this.player = p;
    this.noofcountries = noc;
    this.noofarmies = noa;
    this.con1 = c1;
    this.con2 = c2;
    this.con3 = c3;
    this.discription = d;
  }
  getPlayer() { return this.player; }
  getNoofcountries() { return this.noofcountries; }
  getNoofarmies() { return this.noofarmies; }
  getContinent1() { return this.con1; }
  getContinent2() { return this.con2; }
  getContinent3() { return this.con3; }
  getDiscription() { return this.discription; }
  toString() { return this.discription === '' ? '(Description Missing)' : this.discription; }
}

export const PLAYER_NEUTRAL = -1;
export const PLAYER_HUMAN = 0;
export const PLAYER_AI_EASY = 1;
export const PLAYER_AI_HARD = 2;
export const PLAYER_AI_CRAP = 3;
export const PLAYER_AI_AVERAGE = 4;
export const PLAYER_AI_TEST = 5;

export class Player {
  constructor(type, name, color, address) {
    this.type = type;
    this.name = name;
    this.color = color;
    this.address = address;
    this.extraArmies = 0;
    this.cardsOwned = [];
    this.territoriesOwned = [];
    this.playersEliminated = [];
    this.capital = null;
    this.mission = null;
    this.autoendgo = true;
    this.autodefend = true;
    this.statistics = [];
    this.currentStatistic = new Statistic();
    this.statistics.push(this.currentStatistic);
  }
  rename(n) { this.name = n; }
  toString() { return this.name; }
  nextTurn() {
    this.currentStatistic = new Statistic();
    this.statistics.push(this.currentStatistic);
  }
  getStatisticsFor(type) {
    return this.statistics.map((s) => s.get(type));
  }
  getStatistics() { return this.statistics; }
  getNoArmies() {
    let n = 0;
    for (let c = this.territoriesOwned.length - 1; c >= 0; c--) n += this.territoriesOwned[c].armies;
    return n;
  }
  getAddress() { return this.address; }
  getName() { return this.name; }
  getColor() { return this.color; }
  getExtraArmies() { return this.extraArmies; }
  addArmies(n) { this.extraArmies += n; }
  loseExtraArmy(n) { this.extraArmies -= n; }
  giveCard(card) { this.cardsOwned.push(card); }
  getCards() { return this.cardsOwned; }
  getCapital() { return this.capital; }
  setCapital(c) { this.capital = c; }
  getMission() { return this.mission; }
  setMission(m) { this.mission = m; }
  takeCard() { return this.cardsOwned.shift(); }
  /**
   * trading in the cards, adds armies to the first owned country on the cards
   * and removes the cards from the player. returns number of card countries owned
   */
  tradeInCards(card1, card2, card3, numberOfArmiesForFirstCountry) {
    const owned = (c) => c.country != null && this.territoriesOwned.includes(c.country);
    const country1Owned = owned(card1);
    const country2Owned = owned(card2);
    const country3Owned = owned(card3);
    if (numberOfArmiesForFirstCountry !== 0) {
      if (country1Owned) {
        card1.country.addArmies(numberOfArmiesForFirstCountry);
        this.currentStatistic.addReinforcements(numberOfArmiesForFirstCountry);
      } else if (country2Owned) {
        card2.country.addArmies(numberOfArmiesForFirstCountry);
        this.currentStatistic.addReinforcements(numberOfArmiesForFirstCountry);
      } else if (country3Owned) {
        card3.country.addArmies(numberOfArmiesForFirstCountry);
        this.currentStatistic.addReinforcements(numberOfArmiesForFirstCountry);
      }
    }
    const r1 = this._removeCard(card1);
    const r2 = this._removeCard(card2);
    const r3 = this._removeCard(card3);
    if (!r1 || !r2 || !r3) throw new Error('player does not have card');
    return (country1Owned ? 1 : 0) + (country2Owned ? 1 : 0) + (country3Owned ? 1 : 0);
  }
  /** Vector.remove(Object) uses equals(), so we do the same */
  _removeCard(card) {
    const i = this.cardsOwned.findIndex((c) => c.equals(card));
    if (i < 0) return false;
    this.cardsOwned.splice(i, 1);
    return true;
  }
  getTerritoriesOwned() { return this.territoriesOwned; }
  getNoTerritoriesOwned() { return this.territoriesOwned.length; }
  newCountry(c) { this.territoriesOwned.push(c); }
  lostCountry(c) {
    const i = this.territoriesOwned.indexOf(c);
    if (i >= 0) this.territoriesOwned.splice(i, 1);
  }
  addPlayersEliminated(p) { this.playersEliminated.push(p); }
  getPlayersEliminated() { return this.playersEliminated; }
  getType() { return this.type; }
  setType(t) { this.type = t; }
  setAddress(a) { this.address = a; }
  setAutoEndGo(b) { this.autoendgo = b; }
  getAutoEndGo() { return this.autoendgo; }
  setAutoDefend(b) { this.autodefend = b; }
  getAutoDefend() { return this.autodefend; }
  isAlive() { return this.extraArmies > 0 || this.territoriesOwned.length > 0; }
}
