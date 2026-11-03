// warg - .properties reading and ResourceBundle like lookups (TranslationBundle / MapTranslator). GPLv3.

/** Parse a java .properties file (text already decoded as ISO-8859-1) */
export function parseProperties(text) {
  const result = new Map();
  const lines = text.split(/\r\n|\r|\n/);
  let i = 0;
  while (i < lines.length) {
    let line = lines[i++];
    let trimmed = line.replace(/^[ \t\f]+/, '');
    if (trimmed.length === 0 || trimmed[0] === '#' || trimmed[0] === '!') continue;
    // line continuation: odd number of trailing backslashes
    while (endsWithContinuation(trimmed) && i < lines.length) {
      trimmed = trimmed.substring(0, trimmed.length - 1) + lines[i++].replace(/^[ \t\f]+/, '');
    }
    // find key end
    let k = 0;
    let key = '';
    while (k < trimmed.length) {
      const ch = trimmed[k];
      if (ch === '\\') { key += trimmed.substring(k, k + 2); k += 2; continue; }
      if (ch === '=' || ch === ':' || ch === ' ' || ch === '\t' || ch === '\f') break;
      key += ch; k++;
    }
    // skip separator
    while (k < trimmed.length && (trimmed[k] === ' ' || trimmed[k] === '\t' || trimmed[k] === '\f')) k++;
    if (k < trimmed.length && (trimmed[k] === '=' || trimmed[k] === ':')) k++;
    while (k < trimmed.length && (trimmed[k] === ' ' || trimmed[k] === '\t' || trimmed[k] === '\f')) k++;
    const value = trimmed.substring(k);
    result.set(unescape(key), unescape(value));
  }
  return result;
}

function endsWithContinuation(s) {
  let n = 0;
  for (let j = s.length - 1; j >= 0 && s[j] === '\\'; j--) n++;
  return n % 2 === 1;
}

function unescape(s) {
  let out = '';
  for (let i = 0; i < s.length; i++) {
    const ch = s[i];
    if (ch !== '\\') { out += ch; continue; }
    const n = s[++i];
    if (n === undefined) break;
    if (n === 'u') {
      const hex = s.substring(i + 1, i + 5);
      out += String.fromCharCode(parseInt(hex, 16));
      i += 4;
    } else if (n === 't') out += '\t';
    else if (n === 'n') out += '\n';
    else if (n === 'r') out += '\r';
    else if (n === 'f') out += '\f';
    else out += n;
  }
  return out;
}

/** A chain of bundles, most specific first (like java.util.ResourceBundle with parents) */
export class Bundle {
  constructor(maps, language) {
    this.maps = maps.filter(Boolean);
    this.language = language;
  }
  has(key) {
    return this.maps.some((m) => m.has(key));
  }
  /** returns the string or throws (MissingResourceException semantics) */
  getString(key) {
    for (const m of this.maps) {
      if (m.has(key)) return m.get(key);
    }
    const err = new Error("Can't find resource for key " + key);
    err.missing = true;
    throw err;
  }
  /** returns the string or the key itself */
  t(key, ...args) {
    let s;
    try { s = this.getString(key); } catch (e) { s = key; }
    args.forEach((a, i) => { s = s.split('{' + i + '}').join(String(a)); });
    return s;
  }
  getLanguage() { return this.language; }
}

/**
 * Loads a bundle named base (e.g. "Risk") for a language, using a reader
 * that returns the file text or null if missing.
 */
export async function loadBundle(readText, base, language) {
  const names = [];
  if (language) {
    const parts = language.split(/[_-]/);
    if (parts.length > 1) names.push(`${base}_${parts[0]}_${parts[1].toUpperCase()}`);
    names.push(`${base}_${parts[0]}`);
  }
  names.push(base);
  const maps = [];
  for (const n of names) {
    const text = await readText(n + '.properties');
    if (text != null) maps.push(parseProperties(text));
  }
  if (maps.length === 0) return null;
  return new Bundle(maps, language);
}
