package org.terra2.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Only a bound loot-table execution may issue origin. Item drop/pickup is never a mint trigger. */
public final class WorldLootPolicy {
    public enum Trigger { LOOT_TABLE, PLAYER_DROP, ITEM_SPAWN, PICKUP }
    private record Binding(WorldTarget target, String pack, Set<String> tables) {}
    private static final Set<String> VANILLA = Set.of("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end");
    private final Set<String> protectedWorlds;
    private final LootOrigin authority;
    private final Map<String, Binding> bindings = new ConcurrentHashMap<>();
    public WorldLootPolicy(Set<String> protectedWorlds, LootOrigin authority) {
        this.protectedWorlds = Set.copyOf(protectedWorlds); this.authority = Objects.requireNonNull(authority);
    }
    public void authorize(WorldTarget target, String pack, Set<String> tables) {
        if(target.dimensionKey() == null || VANILLA.contains(target.dimensionKey()) || protectedWorlds.contains(target.worldName()))
            throw new IllegalArgumentException("Unbound/protected loot target: " + target);
        var next = new Binding(target, Objects.requireNonNull(pack), Set.copyOf(tables));
        bindings.compute(target.worldName(), (name, previous) -> {
            if(previous != null && !previous.equals(next)) throw new IllegalStateException("Cannot replace active loot binding: " + name);
            return next;
        });
    }
    public boolean allows(WorldTarget target, String pack, String table) {
        var binding = bindings.get(target.worldName());
        return binding != null && binding.target().equals(target) && binding.pack().equals(pack) && binding.tables().contains(table);
    }
    public Optional<LootOrigin.Receipt> mint(Trigger trigger, WorldTarget target, String pack, String table, boolean existingOriginMarker) {
        if(trigger != Trigger.LOOT_TABLE || existingOriginMarker || !allows(target, pack, table)) return Optional.empty();
        return Optional.of(authority.issue(target, pack, table));
    }
}
