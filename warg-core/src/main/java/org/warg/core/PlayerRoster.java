package org.warg.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Player order, hands and credited eliminations.
 * An elimination is committed exactly once by the battle controller.
 */
public final class PlayerRoster {
    public static final int DEFAULT_MAX_CARDS = 5;
    private final Map<String, Player> players = new LinkedHashMap<>();
    private final int maximumCards;

    public PlayerRoster(int maximumCards) {
        if (maximumCards < 1) throw new IllegalArgumentException("invalid maximum cards");
        this.maximumCards = maximumCards;
    }

    public static final class Player {
        public final String id;
        private final List<CardTradeRules.Suit> cards = new ArrayList<>();
        private final Set<String> eliminated = new LinkedHashSet<>();

        private Player(String id) { this.id = id; }
        public List<CardTradeRules.Suit> cards() { return Collections.unmodifiableList(cards); }
        public Set<String> eliminatedPlayers() { return Collections.unmodifiableSet(eliminated); }
    }

    public void add(String id) {
        if (id == null || id.trim().isEmpty() || players.containsKey(id))
            throw new IllegalArgumentException("invalid or duplicate player");
        players.put(id, new Player(id));
    }

    public Player get(String id) {
        Player p = players.get(id);
        if (p == null) throw new IllegalArgumentException("unknown player: " + id);
        return p;
    }

    public void giveCard(String id, CardTradeRules.Suit card) {
        if (card == null) throw new IllegalArgumentException("null card");
        get(id).cards.add(card);
    }

    /**
     * Mirrors the legacy elimination transfer order (victim's cards appended
     * to attacker's hand). Can exceed the card cap after elimination.
     */
    public void eliminate(String winner, String loser) {
        if (winner == null || winner.equals(loser))
            throw new IllegalArgumentException("invalid elimination");
        Player conqueror = get(winner);
        Player victim = get(loser);
        if (!conqueror.eliminated.add(loser))
            throw new IllegalStateException("player already eliminated");
        conqueror.cards.addAll(victim.cards);
        victim.cards.clear();
    }

    public boolean requiresForcedTrade(String id) {
        return get(id).cards.size() > maximumCards;
    }

    public List<String> orderedPlayers() {
        return Collections.unmodifiableList(new ArrayList<>(players.keySet()));
    }
}
