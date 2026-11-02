# WARG — Design and compatibility contract

## Purpose

WARG aims to be a newly written implementation of the game mechanics of [Domination](https://domination.sourceforge.net/), with a modern, restrained military-themed interface. The reference is Domination by Yura Mamyrin and contributors, SourceForge SVN revision r2844 (Risk Game 1.3.6).

**Current status:** specification only. The existing repository currently contains the original Domination sources; no claim of a completed rewrite or 100% proven compatibility is made.

## Development policy

- Make WARG development changes **only on the `gpt` branch**. Never commit to `main` or any other branch.
- Keep the legacy Domination r2844 tree intact as a reference and behavioral oracle; develop the new engine in separate code/packages.
- Preserve the gameplay semantics of the reference rather than simplifying or changing rules to suit the redesigned interface.
- Verify equivalence with repeatable tests, including edge cases. Report any differences honestly.
- Respect the GPLv3, original author credits, and licenses for included third-party libraries and resources. A rewrite does not automatically remove obligations for reused material.

## Functional compatibility checklist

| Area | Required checks |
| --- | --- |
| Setup | Players, maps, modes, missions, initial placements |
| Turn progression | Legal actions and exact phase transitions |
| Reinforcements | Territory minimums, continent bonuses, placement |
| Combat | Adjacency, dice counts, ordering, ties, losses, conquest |
| Movement | Fortification permissions and limits |
| Cards | Earning, trading, forced exchanges, bonuses |
| Victory | Elimination and mode-specific winning conditions |
| Maps | File parsing, territory IDs, continent rules, connections |
| AI | Existing difficulties and observable decisions; deterministic comparisons where possible |
| Randomness | Seeded reproducibility for tests; no unsupported determinism claims |
| Saves | Round-trip state and explicitly scoped cross-version compatibility |
| Multiplayer | Commands, synchronization, supported protocol behavior |
| UI | Access to every supported action without introducing new rules |

## Development sequence

1. **Baseline:** inventory Domination r2844 modules, tests, maps, saves, supported modes and build requirements.
2. **Oracle:** capture deterministic command/state traces and edge-case fixtures from the original engine.
3. **New headless core:** implement model, rules, command validation and state transitions independently, with parity tests.
4. **Integrations:** implement map formats, cards, AI, persistence and multiplayer incrementally.
5. **UI:** map-first modern design with charcoal, subdued olive/khaki and muted amber, legible ownership colors, compact tactical controls and accessible interactions.
6. **Validation:** document coverage, regressions, performance, remaining differences and licensing checks before declaring compatibility.

## Attribution

WARG is an independent project inspired by Domination, created by Yura Mamyrin and contributors. Domination is its technical and behavioral reference. WARG is not an official Domination release. Original notices and licensing terms remain applicable wherever original code, art or other resources are retained or redistributed.
