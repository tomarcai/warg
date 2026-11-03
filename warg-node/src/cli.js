#!/usr/bin/env node
// warg - text console, like the "commandline" UI of the original Domination. GPLv3.
// Usage: node src/cli.js [--lang es|en]
// Then type commands: newgame, autosetup, startgame domination increasing, placearmies 1 3 ...

import readline from 'node:readline';
import { Risk } from './engine/Risk.js';
import { createNodeIo } from './server/nodeIo.js';

const langArg = process.argv.indexOf('--lang');
const language = langArg > 0 ? process.argv[langArg + 1] : 'es';

const risk = new Risk(createNodeIo(), { language, showDice: false });
await risk.ready();

const rl = readline.createInterface({ input: process.stdin, output: process.stdout, prompt: '> ' });

risk.addRiskListener({
  sendMessage(output) { console.log(output); },
  setGameStatus(help) { if (help) console.log('[' + help + ']'); },
  needInput() { rl.prompt(); },
  gameOver(won) { console.log(won ? '*** You won! ***' : '*** Game over ***'); },
  showMessageDialog(text) { console.log('!! ' + text); },
});

console.log('warg - Domination in Node.js. Type "newgame" then "autosetup" and "startgame domination increasing". Ctrl+D to quit.');
rl.prompt();
rl.on('line', (line) => {
  const cmd = line.trim();
  if (cmd === 'quit' || cmd === 'exit') { rl.close(); return; }
  risk.parser(cmd);
});
rl.on('close', async () => { await risk.idle(); process.exit(0); });
