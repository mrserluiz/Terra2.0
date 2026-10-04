package com.dfsek.terra.bukkit.nms;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.loot.*;
import org.terra2.core.WorldTarget;

/** Native operations for a single immutable world binding. No implicit world selection.
 * Installation/persistence must be supplied by the platform before invoking these operations. */
public final class NativeWorldExecutor {
    private final WorldTarget target;
    private final NativeResourceGraph graph;
    private final Predicate<WorldTarget> authorization;
    private static final Constructor<LootContext> LOOT_CONTEXT = lootConstructor();
    public NativeWorldExecutor(WorldTarget target, NativeResourceGraph graph, Predicate<WorldTarget> authorization) {
        if(target.dimensionKey() == null || Set.of("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end").contains(target.dimensionKey()))
            throw new IllegalArgumentException("Native execution requires an authorized custom world binding");
        if(!graph.report().valid()) throw new IllegalArgumentException("Cannot execute a partial native resource graph");
        this.target = target; this.graph = graph; this.authorization = Objects.requireNonNull(authorization);
    }
    private void assertWorld(ServerLevel level) {
        WorldTarget actual = new WorldTarget(level.getWorld().getName(), level.dimension().identifier().toString());
        if(!actual.equals(target) || !authorization.test(actual)) throw new IllegalStateException("Native world execution refused: " + actual);
    }
    public ChunkGeneratorStructureState placementState(ServerLevel level, ChunkGenerator generator, RandomState random) {
        assertWorld(level);
        return ChunkGeneratorStructureState.createForNormal(random, level.getSeed(), generator.getBiomeSource(),
            graph.lookup().lookupOrThrow(Registries.STRUCTURE_SET), level.spigotConfig);
    }
    /** Structure.generate delegates jigsaw expansion, rotations, collision tests and start height to Minecraft. */
    public StructureStart assemble(ServerLevel level, String structureId, ChunkGenerator generator, RandomState random,
        RegistryAccess registries, StructureTemplateManager templates, ChunkPos chunk, int references) {
        assertWorld(level);
        var holder = graph.resource(Registries.STRUCTURE, structureId);
        requirePersistentAlias(registries, holder);
        return holder.value().generate(holder, level.dimension(), registries, generator, generator.getBiomeSource(), random,
            templates, level.getSeed(), chunk, references, level, holder.value().biomes()::contains);
    }
    /** Native placement runs the pool elements' full processor lists, block/entity NBT and afterPlace handlers. */
    public void place(ServerLevel level, WorldGenLevel region, StructureStart start, StructureManager structures,
        ChunkGenerator generator, RandomSource random, BoundingBox clip, ChunkPos chunk) {
        assertWorld(level);
        if(region.getLevel() != level) throw new IllegalStateException("Structure region belongs to another world");
        if(!start.isValid()) return;
        if(level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getKey(start.getStructure()) == null)
            throw new IllegalStateException("Structure is not registered for safe persistence");
        start.placeInChunk(region, structures, generator, random, clip, chunk);
    }
    /** Resolver is private to this invocation; Minecraft evaluates all loot functions and conditions. */
    public void loot(ServerLevel level, String tableId, LootParams params, RandomSource random, Consumer<ItemStack> output) {
        assertWorld(level);
        if(params.getLevel() != level) throw new IllegalStateException("Loot parameters belong to another world");
        var table = graph.resource(Registries.LOOT_TABLE, tableId).value();
        // Custom enchantments/trial-spawner dependencies must have persistent registry identities.
        for(var key : List.of(Registries.ENCHANTMENT, Registries.TRIAL_SPAWNER_CONFIG)) {
            var lookup = graph.lookup().lookupOrThrow(key);
            lookup.listElements().filter(holder -> holder.key().identifier().getNamespace().equals("terra2"))
                .forEach(holder -> requirePersistentAliasUnchecked(level.registryAccess(), key, holder));
        }
        try {
            LootContext context = LOOT_CONTEXT.newInstance(params, random, graph.lookup());
            table.getRandomItems(context, output);
        } catch(ReflectiveOperationException error) { throw new IllegalStateException("Cannot construct isolated native loot context", error); }
    }
    private static void requirePersistentAlias(RegistryAccess access, Holder.Reference<Structure> holder) {
        var actual = access.lookupOrThrow(Registries.STRUCTURE).get(holder.key());
        if(actual.isEmpty() || actual.get().value() != holder.value())
            throw new IllegalStateException("Native structure alias has not been installed for persistence: " + holder.key());
    }
    @SuppressWarnings({"unchecked", "rawtypes"}) private static void requirePersistentAliasUnchecked(RegistryAccess access,
        net.minecraft.resources.ResourceKey key, Holder.Reference<?> holder) {
        var registry = access.lookupOrThrow(key);
        var actual = registry.get(holder.key());
        if(actual.isEmpty() || ((Holder.Reference<?>) actual.get()).value() != holder.value())
            throw new IllegalStateException("Native loot dependency has no persistent identity: " + holder.key());
    }
    private static Constructor<LootContext> lootConstructor() {
        try {
            var constructor = LootContext.class.getDeclaredConstructor(LootParams.class, RandomSource.class, HolderGetter.Provider.class);
            constructor.setAccessible(true); return constructor;
        } catch(ReflectiveOperationException error) { throw new ExceptionInInitializerError(error); }
    }
}
