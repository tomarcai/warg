package org.warg.core;

/**
 * Incremental, independent turn controller modeled on Domination r2844's
 * phase transitions. Does not yet implement setup, cards or turn rotation.
 *
 * This is an explicit subset, not a complete RiskGame replacement.
 */
public final class TurnController {
    public enum Phase {
        TRADE_CARDS, PLACE_ARMIES, ATTACKING, ROLLING,
        DEFEND_YOURSELF, BATTLE_WON, FORTIFYING, END_TURN
    }

    private final Board board;
    private final Rules rules;
    private final PlayerRoster roster;
    private final String activePlayer;
    private Phase phase;
    private int unplaced;
    private String attackingFrom;
    private String defendingTo;
    private int attackingDice;
    private int defendingDice;
    private String defenderPlayer;
    private int mustMove;
    private int battleRounds;

    /**
     * Begin after initial setup, with the player and earned reinforcements
     * supplied by a future game-level turn scheduler.
     */
    public TurnController(Board board, Rules rules, String activePlayer, int reinforcements) {
        this(board, rules, activePlayer, reinforcements, null);
    }

    /**
     * When supplied, the player roster receives elimination credits and cards.
     * An absent roster supports standalone rule tests but cannot track cards.
     */
    public TurnController(Board board, Rules rules, String activePlayer, int reinforcements, PlayerRoster roster) {
        if (board == null || rules == null || activePlayer == null || activePlayer.trim().isEmpty()
                || reinforcements < 0) throw new IllegalArgumentException("invalid turn setup");
        if (roster != null) roster.get(activePlayer);
        this.roster = roster;
        this.board = board;
        this.rules = rules;
        this.activePlayer = activePlayer;
        this.unplaced = reinforcements;
        this.phase = reinforcements == 0 ? Phase.ATTACKING : Phase.PLACE_ARMIES;
    }

    public Phase phase() { return phase; }
    public int unplacedArmies() { return unplaced; }
    public String activePlayer() { return activePlayer; }
    public String attackingFrom() { return attackingFrom; }
    public String defendingTo() { return defendingTo; }
    public String defendingPlayer() { return defenderPlayer; }
    public int attackingDice() { return attackingDice; }
    public int defendingDice() { return defendingDice; }
    public int mustMove() { return mustMove; }
    public int battleRounds() { return battleRounds; }

    /**
     * Domination r2844 placeArmy after initial setup accepts a quantity that
     * does not exceed the remaining reinforcement pool.
     * Note that the legacy code does NOT reject zero/negative quantities at
     * this point. WARG rejects those unsafe inputs deliberately; exact
     * malformed-command parity remains an open compatibility issue.
     */
    public boolean placeArmies(String territory, int count) {
        if (phase != Phase.PLACE_ARMIES || count <= 0 || count > unplaced) return false;
        Board.Territory destination;
        try { destination = board.territory(territory); }
        catch (IllegalArgumentException invalid) { return false; }
        if (!activePlayer.equals(destination.owner())) return false;
        if (count > Integer.MAX_VALUE - destination.armies()) return false;
        board.occupy(territory, activePlayer, destination.armies() + count);
        unplaced -= count;
        if (unplaced == 0) phase = Phase.ATTACKING;
        return true;
    }

    public boolean attack(String source, String target) {
        if (phase != Phase.ATTACKING) return false;
        try {
            if (!board.mayAttack(activePlayer, source, target)) return false;
            attackingFrom = source;
            defendingTo = target;
            defenderPlayer = board.territory(target).owner();
            if (roster != null) roster.get(defenderPlayer);
            attackingDice = defendingDice = 0;
            battleRounds = mustMove = 0;
            phase = Phase.ROLLING;
            return true;
        }
        catch (IllegalArgumentException invalid) { return false; }
    }

    public boolean rollAttacker(int count) {
        if (phase != Phase.ROLLING || count < 1
                || count > rules.maximumAttackerDice(board.territory(attackingFrom).armies())) return false;
        attackingDice = count;
        phase = Phase.DEFEND_YOURSELF;
        return true;
    }

    public boolean rollDefender(int count, String player) {
        if (phase != Phase.DEFEND_YOURSELF || !defenderPlayer.equals(player)
                || count < 1 || count > rules.maximumDefenderDice(board.territory(defendingTo).armies())) return false;
        defendingDice = count;
        // Legacy rollD remains in DEFEND_YOURSELF until battle() resolves.
        return true;
    }


    /**
     * Completes one round using already rolled, descending, zero-based dice.
     * The combat RNG remains outside this class; no synthetic dice are made.
     * Ownership transfers immediately on conquest, before compulsory movement,
     * matching RiskGame.battle(). Player cards and statistics are not modeled.
     */
    public CombatResult battle(int[] attackRoll, int[] defenseRoll) {
        if (phase != Phase.DEFEND_YOURSELF || attackingDice == 0 || defendingDice == 0)
            throw new IllegalStateException("battle is not ready");
        if (attackRoll == null || defenseRoll == null
                || attackRoll.length != attackingDice || defenseRoll.length != defendingDice)
            throw new IllegalArgumentException("dice lengths differ from selected counts");

        BattleRound.Losses losses = BattleRound.resolve(attackRoll, defenseRoll);
        Board.Territory from = board.territory(attackingFrom);
        Board.Territory to = board.territory(defendingTo);
        if (from.armies() <= losses.attacker || to.armies() < losses.defender)
            throw new IllegalStateException("invalid army counts for battle");
        board.occupy(attackingFrom, activePlayer, from.armies() - losses.attacker);
        board.occupy(defendingTo, defenderPlayer, to.armies() - losses.defender);
        battleRounds++;
        attackingDice = defendingDice = 0;

        if (to.armies() == 0) {
            int maxMove = from.armies() - 1;
            mustMove = attackRoll.length;
            // Due to selected dice limits, maxMove cannot be below mustMove.
            board.occupy(defendingTo, activePlayer, 0);
            boolean eliminated = board.ownedTerritories(defenderPlayer) == 0;
            if (eliminated && roster != null) roster.eliminate(activePlayer, defenderPlayer);
            phase = Phase.BATTLE_WON;
            return new CombatResult(losses.attacker, losses.defender, true,
                    eliminated, mustMove, maxMove);
        }
        if (from.armies() == 1) {
            clearBattle();
            phase = Phase.ATTACKING;
        }
        else {
            phase = Phase.ROLLING;
        }
        return new CombatResult(losses.attacker, losses.defender, false, false, 0, 0);
    }

    /**
     * Complete the compulsory post-conquest movement.
     * Returns false on illegal quantities without mutating the board.
     */
    public boolean moveAfterConquest(int amount) {
        if (phase != Phase.BATTLE_WON || mustMove < 1) return false;
        Board.Territory from = board.territory(attackingFrom);
        Board.Territory to = board.territory(defendingTo);
        if (amount < mustMove || amount >= from.armies()
                || amount > Integer.MAX_VALUE - to.armies()) return false;
        board.occupy(attackingFrom, activePlayer, from.armies() - amount);
        board.occupy(defendingTo, activePlayer, to.armies() + amount);
        clearBattle();
        phase = roster != null && roster.requiresForcedTrade(activePlayer)
                ? Phase.TRADE_CARDS : Phase.ATTACKING; // actual trading is pending
        return true;
    }

    public int moveAllAfterConquest() {
        return phase == Phase.BATTLE_WON && mustMove > 0
                ? board.territory(attackingFrom).armies() - 1 : -1;
    }


    public boolean retreat() {
        if (phase != Phase.ROLLING) return false;
        clearBattle();
        phase = Phase.ATTACKING;
        return true;
    }

    public boolean endAttack() {
        if (phase != Phase.ATTACKING) return false;
        phase = Phase.FORTIFYING;
        return true;
    }

    public boolean fortify(String from, String to, int count) {
        if (phase != Phase.FORTIFYING) return false;
        try {
            if (!board.mayFortify(activePlayer, from, to, count)) return false;
            board.fortify(activePlayer, from, to, count);
            phase = Phase.END_TURN;
            return true;
        }
        catch (IllegalArgumentException invalid) { return false; }
    }

    public boolean skipFortification() {
        if (phase != Phase.FORTIFYING) return false;
        phase = Phase.END_TURN;
        return true;
    }

    private void clearBattle() {
        attackingFrom = defendingTo = defenderPlayer = null;
        attackingDice = defendingDice = 0;
        mustMove = 0;
    }
}
