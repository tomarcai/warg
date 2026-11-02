package org.warg.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Independent, in-memory map state. Neighbor edges are directed, as in
 * Domination r2844's Country.isNeighbours(). No legacy code dependencies.
 */
public final class Board {
    private final Map<String, Territory> territories = new LinkedHashMap<>();
    private final Map<String, Continent> continents = new LinkedHashMap<>();

    public static final class Territory {
        public final String id;
        public final String continentId;
        private String owner;
        private int armies;
        private final Set<String> neighbors = new LinkedHashSet<>();

        private Territory(String id, String continentId) {
            this.id = id;
            this.continentId = continentId;
        }

        public String owner() { return owner; }
        public int armies() { return armies; }
        public Set<String> neighbors() { return Collections.unmodifiableSet(neighbors); }
    }

    public static final class Continent {
        public final String id;
        public final int bonus;
        private final List<String> territoryIds = new ArrayList<>();

        private Continent(String id, int bonus) {
            this.id = id;
            this.bonus = bonus;
        }

        public List<String> territoryIds() { return Collections.unmodifiableList(territoryIds); }
    }

    private static String required(String value) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("blank identifier");
        return value;
    }

    public void addContinent(String id, int bonus) {
        required(id);
        if (continents.containsKey(id)) throw new IllegalArgumentException("duplicate continent");
        continents.put(id, new Continent(id, bonus));
    }

    public void addTerritory(String id, String continentId) {
        required(id);
        Continent continent = continents.get(required(continentId));
        if (continent == null || territories.containsKey(id)) throw new IllegalArgumentException("missing continent or duplicate territory");
        Territory territory = new Territory(id, continentId);
        territories.put(id, territory);
        continent.territoryIds.add(id);
    }

    public void connect(String source, String destination) {
        Territory from = territory(source);
        territory(destination);
        from.neighbors.add(destination);
    }

    public Territory territory(String id) {
        Territory territory = territories.get(id);
        if (territory == null) throw new IllegalArgumentException("unknown territory: " + id);
        return territory;
    }

    public void occupy(String id, String owner, int armies) {
        if (armies < 0 || (owner == null && armies != 0))
            throw new IllegalArgumentException("invalid occupation");
        Territory territory = territory(id);
        territory.owner = owner;
        territory.armies = armies;
    }

    public boolean adjacent(String from, String to) {
        return territory(from).neighbors.contains(territory(to).id);
    }

    public boolean mayAttack(String currentPlayer, String source, String target) {
        Territory from = territory(source);
        Territory to = territory(target);
        return currentPlayer != null && currentPlayer.equals(from.owner)
                && to.owner != null && !currentPlayer.equals(to.owner)
                && from.armies > 1 && adjacent(source, target);
    }

    public boolean mayFortify(String currentPlayer, String source, String target, int count) {
        Territory from = territory(source);
        Territory to = territory(target);
        return currentPlayer != null && currentPlayer.equals(from.owner)
                && currentPlayer.equals(to.owner) && adjacent(source, target)
                && count > 0 && from.armies > count;
    }

    public void fortify(String currentPlayer, String source, String target, int count) {
        if (!mayFortify(currentPlayer, source, target, count))
            throw new IllegalArgumentException("illegal fortification");
        territory(source).armies -= count;
        territory(target).armies += count;
    }

    public int ownedTerritories(String player) {
        Objects.requireNonNull(player, "player");
        int result = 0;
        for (Territory territory : territories.values()) {
            if (player.equals(territory.owner)) result++;
        }
        return result;
    }

    /**
     * Intentionally retains original vacuous ownership for an empty continent:
     * the legacy isOwned() loop returns true without inspecting any territories.
     */
    public boolean ownsContinent(String player, String continentId) {
        Objects.requireNonNull(player, "player");
        Continent continent = continents.get(continentId);
        if (continent == null) throw new IllegalArgumentException("unknown continent");
        for (String id : continent.territoryIds) {
            if (!player.equals(territory(id).owner)) return false;
        }
        return true;
    }

    public int reinforcements(String player, Rules rules) {
        int bonus = rules.territoryReinforcements(ownedTerritories(player));
        for (Continent continent : continents.values()) {
            if (ownsContinent(player, continent.id)) bonus += continent.bonus;
        }
        return bonus;
    }
}
