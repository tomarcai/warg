// warg - file access for the engine when running in Node.js. GPLv3.

import fs from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const here = path.dirname(fileURLToPath(import.meta.url));
export const ROOT = path.resolve(here, '..', '..');
export const REPO = path.resolve(ROOT, '..');

export const PATHS = {
  maps: process.env.WARG_MAPS || path.join(REPO, 'Domination', 'swingUI', 'game', 'Domination', 'maps'),
  customMaps: process.env.WARG_CUSTOM_MAPS || path.join(ROOT, 'maps-custom'),
  vector: path.join(ROOT, 'vector'),
  translations: process.env.WARG_TRANSLATIONS || path.join(REPO, 'Domination', 'src', 'net', 'yura', 'domination', 'engine', 'translation'),
  extraTranslations: path.join(ROOT, 'resources', 'translation'),
  sounds: path.join(REPO, 'Domination', 'swingUI', 'game', 'Domination', 'sound'),
  help: path.join(REPO, 'Domination', 'swingUI', 'game', 'Domination', 'help'),
  uiArt: path.join(REPO, 'Domination', 'swingUI', 'game', 'Domination', 'jar', 'net', 'yura', 'domination', 'ui', 'flashgui'),
  saves: process.env.WARG_SAVES || path.join(ROOT, 'saves'),
  scripts: path.join(REPO, 'Domination', 'res', 'test_scripts'),
};

/** same as RiskUtil.readMap: UTF-8 when there is a BOM, ISO-8859-1 otherwise */
export function decodeMapBuffer(buf) {
  if (buf.length >= 3 && buf[0] === 0xef && buf[1] === 0xbb && buf[2] === 0xbf) {
    return buf.subarray(3).toString('utf8');
  }
  return buf.toString('latin1');
}

/** rejects names that try to leave the folder */
export function safeJoin(dir, name) {
  const p = path.resolve(dir, name);
  if (p !== dir && !p.startsWith(dir + path.sep)) throw new Error('bad path: ' + name);
  return p;
}

async function readOrNull(file, decode) {
  try {
    return decode(await fs.readFile(file));
  } catch (e) {
    if (e.code === 'ENOENT') return null;
    throw e;
  }
}

export function createNodeIo(paths = PATHS) {
  return {
    async readMap(name) {
      // maps made with the editor first, then the original Domination maps
      const custom = await readOrNull(safeJoin(paths.customMaps, name), decodeMapBuffer);
      if (custom != null) return custom;
      return readOrNull(safeJoin(paths.maps, name), decodeMapBuffer);
    },
    async readResource(name) {
      // our own translations (e.g. Spanish missions) are layered on top of the originals
      const extra = await readOrNull(safeJoin(paths.extraTranslations, name), (b) => b.toString('latin1'));
      if (extra != null) return extra;
      return readOrNull(safeJoin(paths.translations, name), (b) => b.toString('latin1'));
    },
    async saveGame(name, json) {
      await fs.mkdir(paths.saves, { recursive: true });
      const file = safeJoin(paths.saves, name.endsWith('.json') ? name : name + '.json');
      await fs.writeFile(file, JSON.stringify(json));
    },
    async loadGame(name) {
      const file = safeJoin(paths.saves, name.endsWith('.json') ? name : name + '.json');
      return JSON.parse(await fs.readFile(file, 'utf8'));
    },
    async readScript(name) {
      const file = path.isAbsolute(name) ? name : path.resolve(process.cwd(), name);
      return fs.readFile(file, 'latin1');
    },
  };
}
