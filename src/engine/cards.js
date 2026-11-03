/** Card trade values ported from Domination r2844 RiskGame#getTradeAbsValue. */
export const CARD_MODE = Object.freeze({ INCREASING: 0, FIXED: 1, ITALIAN: 2 });
export const CARD = Object.freeze({ INFANTRY: 'Infantry', CAVALRY: 'Cavalry', CANNON: 'Cannon', WILDCARD: 'wildcard' });
export function nextTradeValue(previous = 0) {
  if (previous < 4) return previous + 4;
  if (previous < 12) return previous + 2;
  if (previous < 15) return previous + 3;
  return previous + 5;
}
export function tradeValue(cards, mode = CARD_MODE.INCREASING, previous = 0) {
  if (!Array.isArray(cards) || cards.length !== 3) return 0;
  const names = cards.map(c => typeof c === 'string' ? c : c.type);
  if (!names.every(c => Object.values(CARD).includes(c))) return 0;
  const wilds = names.filter(c => c === CARD.WILDCARD).length;
  const ordinary = names.filter(c => c !== CARD.WILDCARD);
  const same = new Set(ordinary).size <= 1;
  const distinct = new Set(ordinary).size === ordinary.length;
  if (mode === CARD_MODE.INCREASING) return wilds > 0 || same || distinct ? nextTradeValue(previous) : 0;
  if (mode === CARD_MODE.FIXED) {
    if (same) {
      const type = ordinary[0];
      return type === CARD.INFANTRY ? 4 : type === CARD.CAVALRY ? 6 : type === CARD.CANNON ? 8 : 12;
    }
    if (distinct) return 10;
    return 0;
  }
  if (mode === CARD_MODE.ITALIAN) {
    if (wilds === 0 && same) return ordinary[0] === CARD.CAVALRY ? 8 : ordinary[0] === CARD.INFANTRY ? 6 : 4;
    if (wilds === 0 && distinct) return 10;
    if (wilds === 1 && same) return 12;
    return 0;
  }
  throw new RangeError('Unknown card mode');
}
export function bestTrade(cards, mode = CARD_MODE.INCREASING, previous = 0) {
  let best = null;
  for (let i = 0; i < cards.length; i++) for (let j = i + 1; j < cards.length; j++) for (let k = j + 1; k < cards.length; k++) {
    const selection = [cards[i], cards[j], cards[k]];
    const value = tradeValue(selection, mode, previous);
    if (value > (best?.value ?? 0)) best = { cards: selection, indices: [i, j, k], value };
  }
  return best;
}
export function parseCards(source, countryIds = null) {
  let section = '';
  const cards = [], missions = [];
  for (const raw of source.split(/\r?\n/)) {
    const line = raw.trim();
    if (!line || line.startsWith(';')) continue;
    if (/^\[[^\]]+\]$/.test(line)) { section = line.slice(1, -1).toLowerCase(); continue; }
    const parts = line.split(/\s+/);
    if (section === 'cards') {
      const [type, id] = parts;
      if (!Object.values(CARD).includes(type)) throw new Error('Invalid card: ' + line);
      if (type !== CARD.WILDCARD && (!/^\d+$/.test(id ?? '') || (countryIds && !countryIds.has(Number(id))))) throw new Error('Invalid card country: ' + line);
      cards.push({ type, countryId: type === CARD.WILDCARD ? null : Number(id) });
    } else if (section === 'missions') {
      if (parts.length < 6) throw new Error('Invalid mission: ' + line);
      missions.push({ player: Number(parts[0]), countries: Number(parts[1]), armies: Number(parts[2]), continents: parts.slice(3, 6), description: parts.slice(6).join(' ') });
    }
  }
  return { cards, missions };
}
