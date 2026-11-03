# WARG — Cards and elimination (incremental status)

Reference: Domination r2844 `RiskGame.java`, `Player.java`, and `Card.java`.

## Implemented in independent WARG core

- `CardTradeRules` returns the *base* trade value for increasing, fixed and Italian card sets, with explicit suit types and configurable prior increasing-set value.
- `PlayerRoster` registers player IDs, keeps suit-only hands in insertion order, credits eliminations, transfers the eliminated player's entire hand in order, and signals forced trades when the configured maximum is exceeded.
- `TurnController` can optionally receive a `PlayerRoster`. When the defender loses their final territory, the attacker is credited with the elimination and inherits the hand. After compulsory conquest movement, excess cards transition into `TRADE_CARDS`.
- The original engine uses 5 as the default card cap; Italian-style configuration can disable the cap, represented in the WARG roster by `Integer.MAX_VALUE`.
- A fixed-set wildcard with two distinct non-wildcard suits is worth 10, per the reference implementation, rather than being rejected.

## Compatibility limitations

**This is NOT yet a complete card system.** Current WARG hands store only a card *suit*, not territory associations. The original cards also identify territories and grant contextual bonuses when traded. WARG does not yet execute a trade, remove cards from the hand, modify the global increasing-set score, recycle used cards, issue cards on conquest, or resume play after a mandatory trade. The current turn controller intentionally leaves the player in `TRADE_CARDS` when a forced trade is required.

Complete end-turn rotation, capital/mission victories, game-over transitions, and elimination statistics remain pending. The existing elimination ledger is connected only to the optional roster, which new full-game integrations must supply. Behavior for malformed commands may intentionally differ until parity tests are complete.

## Validation

Run `sh scripts/test-warg-core.sh`, or inspect the `WARG core smoke tests` GitHub Actions workflow on `gpt`. Tests cover card-set examples and elimination transfer, including a forced-trade transition. These unit tests do not establish whole-engine equivalence.

## Attribution and licensing

Domination was created by Yura Mamyrin and contributors; reference imported from SourceForge SVN r2844. WARG is independent work in progress. Existing GPLv3 and third-party licensing notices remain applicable wherever copyrighted material is reused.
