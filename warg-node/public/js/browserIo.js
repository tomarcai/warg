// warg - file access for the engine in the browser (talks to the local Node server). GPLv3.

function decode(buf) {
  const b = new Uint8Array(buf);
  if (b.length >= 3 && b[0] === 0xef && b[1] === 0xbb && b[2] === 0xbf) {
    return new TextDecoder('utf-8').decode(b.subarray(3));
  }
  // ISO-8859-1: every byte is one code point (TextDecoder would use windows-1252)
  let s = '';
  for (let i = 0; i < b.length; i += 8192) s += String.fromCharCode.apply(null, b.subarray(i, i + 8192));
  return s;
}

async function fetchText(url) {
  const r = await fetch(url);
  if (r.status === 404) return null;
  if (!r.ok) throw new Error(url + ': ' + r.status);
  return decode(await r.arrayBuffer());
}

export const browserIo = {
  readMap: (name) => fetchText('/maps/' + name.split('/').map(encodeURIComponent).join('/')),
  readResource: (name) => fetchText('/translation/' + encodeURIComponent(name)),
  async saveGame(name, json) {
    const r = await fetch('/api/saves/' + encodeURIComponent(name), {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(json),
    });
    if (!r.ok) throw new Error('save failed: ' + r.status);
  },
  async loadGame(name) {
    const r = await fetch('/api/saves/' + encodeURIComponent(name));
    if (!r.ok) throw new Error('not found: ' + name);
    return r.json();
  },
  async readScript(name) {
    const t = await fetchText('/scripts/' + encodeURIComponent(name));
    if (t == null) throw new Error('not found: ' + name);
    return t;
  },
};

export async function listMaps() {
  return (await fetch('/api/maps')).json();
}
export async function listSaves() {
  return (await fetch('/api/saves')).json();
}
export async function deleteSave(name) {
  await fetch('/api/saves/' + encodeURIComponent(name), { method: 'DELETE' });
}
