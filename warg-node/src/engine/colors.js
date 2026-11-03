// warg - port of Domination ColorUtil. GPLv3.
// Colors are stored as Java style signed 32 bit ARGB ints.

export const BLACK = 0xff000000 | 0;
export const WHITE = 0xffffffff | 0;
export const LIGHT_GRAY = 0xffc0c0c0 | 0;
export const GRAY = 0xff808080 | 0;
export const DARK_GRAY = 0xff404040 | 0;
export const RED = 0xffff0000 | 0;
export const PINK = 0xffffafaf | 0;
export const ORANGE = 0xffffc800 | 0;
export const YELLOW = 0xffffff00 | 0;
export const GREEN = 0xff00ff00 | 0;
export const MAGENTA = 0xffff00ff | 0;
export const CYAN = 0xff00ffff | 0;
export const BLUE = 0xff0000ff | 0;

const intToString = new Map();
const stringToInt = new Map();
function add(color, name) {
  intToString.set(color, name);
  stringToInt.set(name, color);
}
add(BLACK, 'black');
add(BLUE, 'blue');
add(CYAN, 'cyan');
add(DARK_GRAY, 'darkgray');
add(GRAY, 'gray');
add(GREEN, 'green');
add(LIGHT_GRAY, 'lightgray');
add(MAGENTA, 'magenta');
add(ORANGE, 'orange');
add(PINK, 'pink');
add(RED, 'red');
add(WHITE, 'white');
add(YELLOW, 'yellow');

/** The 12 colours offered in the new game screen (NewGameFrame order) */
export const PLAYER_COLORS = [
  'pink', 'red', 'orange', 'yellow',
  'green', 'cyan', 'blue', 'magenta',
  'white', 'lightgray', 'darkgray', 'black',
];

export function getStringForColor(c) {
  const r = intToString.get(c | 0);
  if (r) return r;
  return getHexForColor(c);
}

/** Integer.decode compatible parse, returns 0 if it can not be parsed */
function javaDecode(nm) {
  let s = nm;
  let neg = false;
  if (s.startsWith('-')) { neg = true; s = s.substring(1); } else if (s.startsWith('+')) s = s.substring(1);
  let radix = 10;
  if (s.startsWith('0x') || s.startsWith('0X')) { radix = 16; s = s.substring(2); } else if (s.startsWith('#')) { radix = 16; s = s.substring(1); } else if (s.startsWith('0') && s.length > 1) { radix = 8; s = s.substring(1); }
  if (s.length === 0) return null;
  const valid = radix === 16 ? /^[0-9a-fA-F]+$/ : radix === 8 ? /^[0-7]+$/ : /^[0-9]+$/;
  if (!valid.test(s)) return null;
  let v = parseInt(s, radix);
  if (neg) v = -v;
  if (v > 2147483647 || v < -2147483648) return null;
  return v;
}

/** get a color int from a name or number, 0 if not possible */
export function getColor(nm) {
  const c = stringToInt.get(nm);
  if (c !== undefined) return c;
  const v = javaDecode(String(nm));
  if (v === null) return 0;
  return (0xff000000 | v) | 0;
}

export function getRed(rgb) { return (rgb >> 16) & 0xff; }
export function getGreen(rgb) { return (rgb >> 8) & 0xff; }
export function getBlue(rgb) { return rgb & 0xff; }

export function isColorLight(c) {
  const r = getRed(c);
  const g = getGreen(c);
  return (r > 240 || g > 240) || (r > 150 && g > 150);
}

export function getTextColorFor(c) {
  return isColorLight(c) ? BLACK : WHITE;
}

export function getHexForColor(c) {
  return '#' + ((c & 0xffffff) | 0x1000000).toString(16).substring(1);
}

export function toCss(c, alpha) {
  const r = getRed(c), g = getGreen(c), b = getBlue(c);
  if (alpha === undefined) return `rgb(${r},${g},${b})`;
  return `rgba(${r},${g},${b},${alpha})`;
}

/** java.awt.Color.HSBtoRGB */
export function HSBtoRGB(hue, saturation, brightness) {
  let r = 0, g = 0, b = 0;
  if (saturation === 0) {
    r = g = b = Math.trunc(brightness * 255.0 + 0.5);
  } else {
    const h = (hue - Math.floor(hue)) * 6.0;
    const f = h - Math.floor(h);
    const p = brightness * (1.0 - saturation);
    const q = brightness * (1.0 - saturation * f);
    const t = brightness * (1.0 - (saturation * (1.0 - f)));
    switch (Math.trunc(h)) {
      case 0: r = brightness * 255 + 0.5; g = t * 255 + 0.5; b = p * 255 + 0.5; break;
      case 1: r = q * 255 + 0.5; g = brightness * 255 + 0.5; b = p * 255 + 0.5; break;
      case 2: r = p * 255 + 0.5; g = brightness * 255 + 0.5; b = t * 255 + 0.5; break;
      case 3: r = p * 255 + 0.5; g = q * 255 + 0.5; b = brightness * 255 + 0.5; break;
      case 4: r = t * 255 + 0.5; g = p * 255 + 0.5; b = brightness * 255 + 0.5; break;
      case 5: r = brightness * 255 + 0.5; g = p * 255 + 0.5; b = q * 255 + 0.5; break;
      default: break;
    }
    r = Math.trunc(r); g = Math.trunc(g); b = Math.trunc(b);
  }
  return (0xff000000 | (r << 16) | (g << 8) | b) | 0;
}
