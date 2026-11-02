package org.warg.core;

/** Immutable outcome of one WARG combat round. */
public final class CombatResult {
    public final int attackerLosses;
    public final int defenderLosses;
    public final boolean conquered;
    public final boolean defenderEliminated;
    public final int minimumMove;
    public final int maximumMove;

    public CombatResult(int attackerLosses, int defenderLosses, boolean conquered,
            boolean defenderEliminated, int minimumMove, int maximumMove) {
        this.attackerLosses = attackerLosses;
        this.defenderLosses = defenderLosses;
        this.conquered = conquered;
        this.defenderEliminated = defenderEliminated;
        this.minimumMove = minimumMove;
        this.maximumMove = maximumMove;
    }
}
