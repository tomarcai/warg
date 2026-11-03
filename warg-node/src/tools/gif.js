// warg - minimal GIF decoder (first frame), enough for the Domination "_map.gif" files. GPLv3.
// Returns { width, height, indices: Uint8Array, palette: Uint8Array (r,g,b triplets) }.

export function decodeGif(buf) {
  const b = buf instanceof Uint8Array ? buf : new Uint8Array(buf);
  const sig = String.fromCharCode(...b.subarray(0, 6));
  if (sig !== 'GIF87a' && sig !== 'GIF89a') throw new Error('not a GIF');
  let p = 6;
  const u16 = () => { const v = b[p] | (b[p + 1] << 8); p += 2; return v; };
  const width = u16();
  const height = u16();
  const flags = b[p++];
  p += 2; // background colour, aspect
  let palette = null;
  if (flags & 0x80) {
    const size = 3 * (1 << ((flags & 7) + 1));
    palette = b.slice(p, p + size);
    p += size;
  }
  while (p < b.length) {
    const block = b[p++];
    if (block === 0x21) { // extension
      p++; // label
      let len;
      while ((len = b[p++]) !== 0) p += len;
    } else if (block === 0x2c) { // image
      const ix = u16(); const iy = u16(); const iw = u16(); const ih = u16();
      const iflags = b[p++];
      if (iflags & 0x80) {
        const size = 3 * (1 << ((iflags & 7) + 1));
        palette = b.slice(p, p + size);
        p += size;
      }
      const interlaced = (iflags & 0x40) !== 0;
      const minCode = b[p++];
      // gather sub blocks
      const chunks = [];
      let total = 0;
      let len;
      while ((len = b[p++]) !== 0) { chunks.push(b.subarray(p, p + len)); total += len; p += len; }
      const data = new Uint8Array(total);
      let o = 0;
      for (const c of chunks) { data.set(c, o); o += c.length; }
      const pixels = lzw(data, minCode, iw * ih);
      const indices = new Uint8Array(width * height);
      const rowOrder = interlaced ? interlaceRows(ih) : null;
      for (let r = 0; r < ih; r++) {
        const y = iy + (rowOrder ? rowOrder[r] : r);
        if (y >= height) continue;
        indices.set(pixels.subarray(r * iw, r * iw + Math.min(iw, width - ix)), y * width + ix);
      }
      return { width, height, indices, palette };
    } else if (block === 0x3b) {
      break;
    } else {
      throw new Error('bad GIF block ' + block);
    }
  }
  throw new Error('GIF has no image');
}

function interlaceRows(h) {
  const rows = [];
  for (const [start, step] of [[0, 8], [4, 8], [2, 4], [1, 2]]) for (let y = start; y < h; y += step) rows.push(y);
  return rows;
}

function lzw(data, minCode, count) {
  const out = new Uint8Array(count);
  const clear = 1 << minCode;
  const eoi = clear + 1;
  const prefix = new Int32Array(4096);
  const suffix = new Uint8Array(4096);
  const first = new Uint8Array(4096);
  const stack = new Uint8Array(4097);
  let codeSize = minCode + 1;
  let next = clear + 2;
  let old = -1;
  let bits = 0, acc = 0, ip = 0, op = 0;
  for (let i = 0; i < clear; i++) { suffix[i] = i; first[i] = i; }
  while (op < count) {
    while (bits < codeSize) {
      if (ip >= data.length) return out;
      acc |= data[ip++] << bits;
      bits += 8;
    }
    const code = acc & ((1 << codeSize) - 1);
    acc >>>= codeSize;
    bits -= codeSize;
    if (code === clear) { codeSize = minCode + 1; next = clear + 2; old = -1; continue; }
    if (code === eoi) break;
    let sp = 0;
    let c = code;
    let kwk = false;
    if (code >= next) {
      if (old < 0) break;
      kwk = true;
      c = old;
    }
    while (c > eoi) { stack[sp++] = suffix[c]; c = prefix[c]; }
    stack[sp++] = c;
    const firstChar = c;
    while (sp > 0 && op < count) out[op++] = stack[--sp];
    if (kwk && op < count) out[op++] = firstChar;
    if (old !== -1 && next < 4096) {
      prefix[next] = old;
      suffix[next] = firstChar;
      first[next] = first[old];
      next++;
      if (next === (1 << codeSize) && codeSize < 12) codeSize++;
    }
    old = code;
  }
  return out;
}
