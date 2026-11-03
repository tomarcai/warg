/**
 * Domination r2844 .map format reader.
 * Reference: Domination/src/net/yura/domination/engine/core/RiskGame.java#loadMap
 * A parse error is preferable to silently changing the map topology.
 */
export function parseMap(source) {
  if (typeof source !== 'string') throw new TypeError('Map source must be text');
  let section = '';
  const files = {}, metadata = {}, continents = [], countries = [], borders = new Map();
  const valid = new Set(['files', 'continents', 'countries', 'borders', 'missions', 'properties']);
  for (const raw of source.split(/\r?\n/)) {
    const line = raw.trim();
    if (!line || line.startsWith(';') || line.startsWith('#')) continue;
    const heading = /^\[([^\]]+)\]$/.exec(line);
    if (heading) { section = heading[1].toLowerCase(); continue; }
    const parts = line.split(/\s+/);
    if (section === 'files') {
      if (parts.length < 2) throw new Error('Malformed files entry: ' + line);
      files[parts[0]] = parts.slice(1).join(' ');
    } else if (section === 'continents') {
      if (parts.length < 3 || !Number.isInteger(Number(parts[1]))) throw new Error('Malformed continent: ' + line);
      continents.push({ id: continents.length + 1, name: parts[0], bonus: Number(parts[1]), color: parts[2] });
    } else if (section === 'countries') {
      if (parts.length < 5) throw new Error('Malformed country: ' + line);
      const [id, name, continent, x, y] = parts;
      if (![id,continent,x,y].every(n => Number.isInteger(Number(n)))) throw new Error('Invalid country: ' + line);
      countries.push({ id: Number(id), name, continent: Number(continent), x: Number(x), y: Number(y), neighbours: [] });
    } else if (section === 'borders') {
      if (!parts.every(p => /^\d+$/.test(p))) throw new Error('Invalid borders: ' + line);
      borders.set(Number(parts[0]), parts.slice(1).map(Number));
    } else if (!section && parts[0] === 'name') {
      metadata.name = line.slice(5).trim();
    } else if (!valid.has(section) || section === 'properties') {
      metadata[line.split(' ')[0]] = line.substring(line.indexOf(' ') + 1);
    }
  }
  if (!countries.length || !continents.length) throw new Error('Map must include countries and continents');
  const ids = new Set(countries.map(c => c.id));
  if (ids.size !== countries.length) throw new Error('Duplicate country id');
  for (const c of countries) {
    if (!continents.some(x => x.id === c.continent)) throw new Error('Unknown continent: ' + c.continent);
    const linked = borders.get(c.id);
    if (!linked) throw new Error('Missing borders: ' + c.id);
    for (const n of linked) if (!ids.has(n) || n === c.id) throw new Error('Invalid neighbour: ' + c.id + ' -> ' + n);
    c.neighbours = [...new Set(linked)];
  }
  // Domination's Country.isNeighbours uses directed entries as authored.
  return { name: metadata.name ?? null, files, continents, countries, metadata };
}
