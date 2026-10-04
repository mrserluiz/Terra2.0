package com.dfsek.terra.bukkit.nms;

import com.mojang.serialization.Lifecycle;
import java.nio.file.*;
import java.util.*;
import net.minecraft.SharedConstants;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.Validatable;
import org.terra2.pack.ResourceBundle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class NativeResourceGraphTest {
    @TempDir Path directory;
    private <T extends Validatable> HolderLookup.RegistryLookup<T> emptyLoot(LootDataType<T> type) {
        return new MappedRegistry<T>(type.registryKey(), Lifecycle.stable());
    }
    private HolderLookup.Provider vanilla() {
        SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
        var lookups = new LinkedHashMap<net.minecraft.resources.ResourceKey<? extends Registry<?>>, HolderLookup.RegistryLookup<?>>();
        VanillaRegistries.createLookup().listRegistries().forEach(lookup -> lookups.put(lookup.key(), lookup));
        LootDataType.values().forEach(type -> { var lookup = emptyLoot(type); lookups.put(lookup.key(), lookup); });
        return HolderLookup.Provider.create(lookups.values().stream());
    }
    private void write(String path, String text) throws Exception {
        var file = directory.resolve(path); Files.createDirectories(file.getParent()); Files.writeString(file, text);
    }
    private void fixtures() throws Exception {
        write("data/test/worldgen/template_pool/rooms.json", """
            {"fallback":"minecraft:empty","elements":[{"weight":1,"element":{"element_type":"minecraft:single_pool_element",
            "location":"test:room","processors":"minecraft:empty","projection":"rigid"}}]}
            """);
        write("data/test/structure/room.nbt", "Source identifier fixture; NBT is separately validated by TemplateMigration");
        write("data/test/tags/worldgen/biome/forest.json", "{\"values\":[\"minecraft:plains\"]}");
        write("data/test/worldgen/structure/dungeon.json", """
            {"type":"minecraft:jigsaw","biomes":"#test:forest","step":"surface_structures","spawn_overrides":{},
             "terrain_adaptation":"none","start_pool":"test:rooms","size":1,"start_height":{"absolute":64},
             "max_distance_from_center":80,"use_expansion_hack":false}
            """);
        write("data/test/worldgen/structure_set/dungeons.json", """
            {"structures":[{"structure":"test:dungeon","weight":1}],
             "placement":{"type":"minecraft:random_spread","spacing":16,"separation":8,"salt":123}}
            """);
        write("data/test/loot_table/chest.json", """
            {"type":"minecraft:chest","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"minecraft:stone"}]}]}
            """);
    }
    @Test void decodesNativeStructurePoolPlacementAndLootWithoutChangingVanillaRegistries() throws Exception {
        fixtures(); var base = vanilla();
        long before = base.lookupOrThrow(Registries.TEMPLATE_POOL).listElements().count();
        var graph = new NativeResourceGraph(ResourceBundle.read(directory), base);
        assertTrue(graph.report().valid(), () -> String.join("\n", graph.report().errors()));
        assertEquals(before, base.lookupOrThrow(Registries.TEMPLATE_POOL).listElements().count());
        assertTrue(base.lookupOrThrow(Registries.STRUCTURE).get(net.minecraft.resources.ResourceKey.create(Registries.STRUCTURE,
            net.minecraft.resources.Identifier.parse(graph.scope().privateId("test:dungeon")))).isEmpty());
        var structure = graph.resource(Registries.STRUCTURE, "test:dungeon").value();
        var plains = base.lookupOrThrow(Registries.BIOME).getOrThrow(net.minecraft.resources.ResourceKey.create(Registries.BIOME,
            net.minecraft.resources.Identifier.parse("minecraft:plains")));
        assertTrue(structure.biomes().contains(plains));
        var placement = (RandomSpreadStructurePlacement) graph.resource(Registries.STRUCTURE_SET, "test:dungeons").value().placement();
        assertEquals(placement.getPotentialStructureChunk(42L, -100, 100), placement.getPotentialStructureChunk(42L, -100, 100));
        assertNotNull(graph.resource(Registries.LOOT_TABLE, "test:chest").value());
    }
    @Test void reportsMissingCustomDependenciesAndRefusesToExposeThePartialGraph() throws Exception {
        fixtures(); write("data/test/worldgen/template_pool/rooms.json", "{\"fallback\":\"test:missing\",\"elements\":[]}");
        var graph = new NativeResourceGraph(ResourceBundle.read(directory), vanilla());
        assertFalse(graph.report().valid());
        assertThrows(IllegalStateException.class, () -> graph.resource(Registries.STRUCTURE, "test:dungeon"));
    }
}
