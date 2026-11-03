// warg - port of Domination (Yura Mamyrin) to JavaScript. GPLv3.
// Small helpers that reproduce Java semantics the original engine relies on.

/** Java int division (truncates toward zero). */
export function idiv(a, b) {
  return Math.trunc(a / b);
}

/** Java (int) cast of a double. */
export function toInt(x) {
  if (Number.isNaN(x)) return 0;
  if (x >= 2147483647) return 2147483647;
  if (x <= -2147483648) return -2147483648;
  return Math.trunc(x);
}

export const INT_MIN = -2147483648;
export const INT_MAX = 2147483647;

/** int subtraction with 32 bit overflow, as used inside Java compareTo methods. */
export function isub(a, b) {
  return (a - b) | 0;
}

/**
 * Random source equivalent to java.util.Random usage in the original.
 * By default it uses Math.random. A seed makes it deterministic (tests).
 */
export class Random {
  constructor(seed) {
    if (seed === undefined || seed === null) {
      seed = Math.floor(Math.random() * 4294967296);
    }
    // mulberry32, its whole state is one uint32 so it can be saved with the game
    // (the Java game serializes its java.util.Random too, so undo replays the same dice)
    this.state = seed >>> 0;
  }
  _next() {
    this.state = (this.state + 0x6d2b79f5) >>> 0;
    let t = this.state;
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  }
  getState() {
    return this.state;
  }
  setState(s) {
    this.state = s >>> 0;
  }
  nextDouble() {
    return this._next();
  }
  nextInt(n) {
    if (n === undefined) return Math.floor(this._next() * 4294967296) | 0;
    if (!(n > 0)) throw new Error('bound must be positive: ' + n);
    return Math.floor(this._next() * n);
  }
  nextBoolean() {
    return this._next() < 0.5;
  }
  /** Collections.shuffle */
  shuffle(list) {
    for (let i = list.length - 1; i > 0; i--) {
      const j = this.nextInt(i + 1);
      const t = list[i];
      list[i] = list[j];
      list[j] = t;
    }
    return list;
  }
}

/** Double.compare */
export function doubleCompare(a, b) {
  if (a < b) return -1;
  if (a > b) return 1;
  if (Object.is(a, b)) return 0;
  if (Number.isNaN(a)) return Number.isNaN(b) ? 0 : 1;
  if (Number.isNaN(b)) return -1;
  // -0.0 vs 0.0
  return Object.is(a, -0) ? -1 : 1;
}

/** Collections.binarySearch(List<Double>, key) exactly as java.util.Collections does it */
export function binarySearchDouble(list, key) {
  let low = 0;
  let high = list.length - 1;
  while (low <= high) {
    const mid = (low + high) >>> 1;
    const cmp = doubleCompare(list[mid], key);
    if (cmp < 0) low = mid + 1;
    else if (cmp > 0) high = mid - 1;
    else return mid;
  }
  return -(low + 1);
}

/** Java String.hashCode */
export function javaStringHash(s) {
  let h = 0;
  for (let i = 0; i < s.length; i++) {
    h = (Math.imul(31, h) + s.charCodeAt(i)) | 0;
  }
  return h;
}

/**
 * Order of keys as java.util.HashMap (default capacity 16, no resize for small maps)
 * would iterate them. Only valid for small maps of String keys (< 13 entries).
 */
export function javaHashMapKeyOrder(keysInInsertionOrder) {
  const withIndex = keysInInsertionOrder.map((k, i) => {
    const h = javaStringHash(k);
    const spread = h ^ (h >>> 16);
    return { k, bucket: spread & 15, i };
  });
  withIndex.sort((a, b) => a.bucket - b.bucket || a.i - b.i);
  return withIndex.map((x) => x.k);
}

/**
 * Port of java.util.PriorityQueue (binary heap) so that poll order with
 * ties matches the original AI exactly.
 */
export class JavaPriorityQueue {
  constructor(comparator) {
    this.queue = [];
    this.cmp = comparator;
  }
  get size() {
    return this.queue.length;
  }
  isEmpty() {
    return this.queue.length === 0;
  }
  clear() {
    this.queue.length = 0;
  }
  add(e) {
    const k = this.queue.length;
    this.queue.push(e);
    this._siftUp(k, e);
    return true;
  }
  poll() {
    const q = this.queue;
    if (q.length === 0) return null;
    const result = q[0];
    const x = q.pop();
    if (q.length > 0) this._siftDown(0, x);
    return result;
  }
  _siftUp(k, x) {
    const q = this.queue;
    while (k > 0) {
      const parent = (k - 1) >>> 1;
      const e = q[parent];
      if (this.cmp(x, e) >= 0) break;
      q[k] = e;
      k = parent;
    }
    q[k] = x;
  }
  _siftDown(k, x) {
    const q = this.queue;
    const n = q.length;
    const half = n >>> 1;
    while (k < half) {
      let child = (k << 1) + 1;
      let c = q[child];
      const right = child + 1;
      if (right < n && this.cmp(c, q[right]) > 0) c = q[(child = right)];
      if (this.cmp(x, c) <= 0) break;
      q[k] = c;
      k = child;
    }
    q[k] = x;
  }
}

/** Replace all occurrences (JavaCompatUtil.replaceAll) */
export function replaceAll(str, find, rep) {
  return String(str).split(find).join(rep);
}

/** Simple StringTokenizer on whitespace */
export function tokenize(str) {
  return String(str).split(/[ \t\n\r\f]+/).filter((t) => t.length > 0);
}

export function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}
