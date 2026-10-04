package org.terra2.core;

import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WorldLootPolicyTest {
    private final WorldTarget heat = new WorldTarget("aeternum_heat", "minecraft:aeternum_heat");
    private final LootOrigin authority = new LootOrigin(new byte[32]);
    private WorldLootPolicy policy() {
        var policy = new WorldLootPolicy(Set.of("world"), authority);
        policy.authorize(heat, "Dungeon", Set.of("terra2:heat/chest")); return policy;
    }
    @Test void onlyFreshAuthorizedLootCanReceiveOrigin() {
        var policy = policy();
        var minted = policy.mint(WorldLootPolicy.Trigger.LOOT_TABLE, heat, "Dungeon", "terra2:heat/chest", false).orElseThrow();
        assertTrue(authority.authentic(minted));
        assertTrue(policy.mint(WorldLootPolicy.Trigger.LOOT_TABLE, heat, "Dungeon", "terra2:heat/chest", true).isEmpty());
        for(var trigger : List.of(WorldLootPolicy.Trigger.PLAYER_DROP, WorldLootPolicy.Trigger.ITEM_SPAWN, WorldLootPolicy.Trigger.PICKUP))
            assertTrue(policy.mint(trigger, heat, "Dungeon", "terra2:heat/chest", false).isEmpty());
        assertFalse(policy.allows(new WorldTarget("aeternum_heat", "minecraft:other"), "Dungeon", "terra2:heat/chest"));
        assertFalse(policy.allows(new WorldTarget("other", "minecraft:other"), "Dungeon", "terra2:heat/chest"));
        assertFalse(policy.allows(heat, "Other", "terra2:heat/chest"));
        assertFalse(policy.allows(heat, "Dungeon", "terra2:other/chest"));
    }
    @Test void provenanceCannotBeReassignedToAnotherWorldOrServer() {
        var r = policy().mint(WorldLootPolicy.Trigger.LOOT_TABLE, heat, "Dungeon", "terra2:heat/chest", false).orElseThrow();
        assertFalse(authority.authentic(new LootOrigin.Receipt(r.version(), "other", r.dimension(), r.pack(), r.table(), r.nonce(), r.signature())));
        assertFalse(authority.authentic(new LootOrigin.Receipt(r.version(), r.world(), "minecraft:other", r.pack(), r.table(), r.nonce(), r.signature())));
        assertFalse(authority.authentic(new LootOrigin.Receipt(r.version(), r.world(), r.dimension(), "Other", r.table(), r.nonce(), r.signature())));
        assertFalse(authority.authentic(new LootOrigin.Receipt(r.version(), r.world(), r.dimension(), r.pack(), "other:table", r.nonce(), r.signature())));
        byte[] anotherKey = new byte[32]; anotherKey[0] = 1;
        assertFalse(new LootOrigin(anotherKey).authentic(r));
    }
    @Test void primaryAndUnboundWorldsAreNeverAuthorized() {
        var policy = policy();
        assertThrows(IllegalArgumentException.class, () -> policy.authorize(new WorldTarget("world", "minecraft:custom"), "Pack", Set.of()));
        assertThrows(IllegalArgumentException.class, () -> policy.authorize(new WorldTarget("other", "minecraft:overworld"), "Pack", Set.of()));
        assertThrows(IllegalArgumentException.class, () -> policy.authorize(new WorldTarget("other", null), "Pack", Set.of()));
    }
}
