package com.dfsek.terra.bukkit.nms;

import com.mojang.serialization.Lifecycle;
import java.nio.file.*;
import java.util.*;
import net.minecraft.SharedConstants;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
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
        RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY).listRegistries().forEach(lookup -> lookups.put(lookup.key(), lookup));
        VanillaRegistries.createLookup().listRegistries().forEach(lookup -> lookups.put(lookup.key(), lookup));
        LootDataType.values().forEach(type -> { var lookup = emptyLoot(type); lookups.put(lookup.key(), lookup); });
        var provider = HolderLookup.Provider.create(lookups.values().stream());
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(provider).forEach(net.minecraft.core.component.DataComponentInitializers.PendingComponents::apply);
        return provider;
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
        write("data/test/tags/worldgen/biome/forest.json", "{\"replace\":true,\"values\":[\"minecraft:plains\"]}");
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
        assertEquals(1, graph.sourceLookup(Registries.STRUCTURE_SET).listElements().count());
        assertTrue(graph.sourceLookup(Registries.STRUCTURE_SET).listElements().allMatch(holder ->
            holder.key().identifier().toString().startsWith("terra2:" + graph.scope().fingerprint() + "/")));
        base.lookupOrThrow(Registries.STRUCTURE_SET).listElements().forEach(holder ->
            assertTrue(graph.sourceLookup(Registries.STRUCTURE_SET).get(holder.key()).isEmpty()));
    }
    @Test void reportsMissingCustomDependenciesAndRefusesToExposeThePartialGraph() throws Exception {
        fixtures(); write("data/test/worldgen/template_pool/rooms.json", "{\"fallback\":\"test:missing\",\"elements\":[]}");
        var graph = new NativeResourceGraph(ResourceBundle.read(directory), vanilla());
        assertFalse(graph.report().valid());
        assertThrows(IllegalStateException.class, () -> graph.resource(Registries.STRUCTURE, "test:dungeon"));
    }
    private java.util.List<net.minecraft.world.item.ItemStack> roll(String table, NativeResourceGraph graph) throws Exception {
        var configuration = new io.papermc.paper.configuration.GlobalConfiguration();
        configuration.misc = configuration.new Misc();
        var configure = io.papermc.paper.configuration.GlobalConfiguration.class.getDeclaredMethod("set", io.papermc.paper.configuration.GlobalConfiguration.class);
        configure.setAccessible(true); configure.invoke(null, configuration);
        var parameters = new net.minecraft.world.level.storage.loot.LootParams(null,
            new net.minecraft.util.context.ContextMap.Builder().withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,
                new net.minecraft.world.phys.Vec3(0, 64, 0)).create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CHEST), Map.of(), 0);
        var constructor = net.minecraft.world.level.storage.loot.LootContext.class.getDeclaredConstructor(
            net.minecraft.world.level.storage.loot.LootParams.class, net.minecraft.util.RandomSource.class, HolderGetter.Provider.class);
        constructor.setAccessible(true);
        var context = constructor.newInstance(parameters, net.minecraft.util.RandomSource.create(42), graph.lookup());
        var items = new ArrayList<net.minecraft.world.item.ItemStack>();
        graph.resource(Registries.LOOT_TABLE, table).value().getRandomItemsRaw(context, items::add);
        return items;
    }
    @Test void executesNativeLootUsingTheIsolatedResolver() throws Exception {
        fixtures();
        var graph = new NativeResourceGraph(ResourceBundle.read(directory), vanilla());
        assertTrue(graph.report().valid(), () -> String.join("\n", graph.report().errors()));
        var items = roll("test:chest", graph);
        assertEquals(1, items.size()); assertEquals(net.minecraft.world.item.Items.STONE, items.getFirst().getItem());
    }
    @Test void resolvesNestedLootAndNativeItemFunctionsWithoutRenamingClientAssets() throws Exception {
        fixtures();
        write("data/test/loot_table/relic.json", """
            {"type":"minecraft:chest","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"minecraft:stone","functions":[
              {"function":"minecraft:set_count","count":3},
              {"function":"minecraft:set_name","name":{"text":"Relíquia da dimensão"},"target":"item_name"},
              {"function":"minecraft:set_lore","lore":[{"text":"Encontrada nesta dimensão"}],"mode":"replace_all"},
              {"function":"minecraft:set_components","components":{"minecraft:item_model":"test:relic"}}
            ]}]}]}
            """);
        write("data/test/loot_table/nested.json", """
            {"type":"minecraft:chest","pools":[{"rolls":1,"entries":[{"type":"minecraft:loot_table","value":"test:relic"}]}]}
            """);
        var graph = new NativeResourceGraph(ResourceBundle.read(directory), vanilla());
        assertTrue(graph.report().valid(), () -> String.join("\n", graph.report().errors()));
        var items = roll("test:nested", graph);
        assertEquals(1, items.size()); var item = items.getFirst();
        assertEquals(3, item.getCount());
        assertEquals("Relíquia da dimensão", item.get(net.minecraft.core.component.DataComponents.ITEM_NAME).getString());
        assertEquals("Encontrada nesta dimensão", item.get(net.minecraft.core.component.DataComponents.LORE).lines().getFirst().getString());
        assertEquals("test:relic", item.get(net.minecraft.core.component.DataComponents.ITEM_MODEL).toString());
    }
    @Test void decodesInstrumentOptionsAsAnInstrumentTag() throws Exception {
        fixtures();
        write("data/test/tags/instrument/horns.json", "{\"replace\":true,\"values\":[\"minecraft:ponder_goat_horn\"]}");
        write("data/test/loot_table/horn.json", """
            {"type":"minecraft:chest","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"minecraft:goat_horn",
              "functions":[{"function":"minecraft:set_instrument","options":"#test:horns"}]}]}]}
            """);
        var graph = new NativeResourceGraph(ResourceBundle.read(directory), vanilla());
        assertTrue(graph.report().valid(), () -> String.join("\n", graph.report().errors()));
        var items = roll("test:horn", graph);
        assertEquals(1, items.size());
        assertEquals(net.minecraft.world.item.Items.GOAT_HORN, items.getFirst().getItem());
        assertNotNull(items.getFirst().get(net.minecraft.core.component.DataComponents.INSTRUMENT));
    }
}
