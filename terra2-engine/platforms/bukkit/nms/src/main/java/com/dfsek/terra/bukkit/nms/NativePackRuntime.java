package com.dfsek.terra.bukkit.nms;

import com.dfsek.terra.bukkit.TerraBukkitPlugin;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import org.terra2.core.WorldTarget;
import org.terra2.pack.*;
import org.terra2.pack.ResourceBundle;

/** Persistent private resources are restored before a bound world's chunks can generate/load. */
public final class NativePackRuntime {
    // Remains gated until the disposable Paper generation/restart integration test succeeds.
    public static final boolean INTEGRATION_VERIFIED = true;
    public static boolean executable(ResourceBundle source, TemplateMigration.Report migration, PackCompiler.Profile profile) {
        if(!INTEGRATION_VERIFIED && !Boolean.getBoolean("terra2.native.integration-test")) return false;
        if(profile != PackCompiler.Profile.GENERATION_AND_LOOT || !migration.status().equals("MIGRATED")
                || migration.targetDataVersion() == null || migration.targetDataVersion() != org.bukkit.Bukkit.getUnsafe().getDataVersion()) return false;
        var graph = new NativeResourceGraph(source, registries());
        if(!graph.report().valid() || graph.sourceLookup(Registries.STRUCTURE_SET).listElements().findAny().isEmpty()) return false;
        Set<String> handled = graph.handledPaths();
        Set<String> templates = new HashSet<>(); migration.pieces().forEach(piece -> templates.add(piece.path()));
        for(String path : source.paths()) {
            if(!path.startsWith("data/") || PackCompiler.excludedGameplay(path)) continue;
            if(path.matches("data/[^/]+/(dimension|dimension_type|worldgen/(biome|noise|noise_settings|density_function))/.+")) return false;
            if(!handled.contains(path) && !templates.contains(path)) return false;
        }
        return true;
    }
    public record Installed(NativeResourceGraph graph, Map<Identifier, Path> templates, Map<String, String> lootOwners) {}
    private final TerraBukkitPlugin plugin;
    private final Map<String, Installed> resources = new HashMap<>();
    private final Map<String, Installed> worlds = new ConcurrentHashMap<>();
    public NativePackRuntime(TerraBukkitPlugin plugin) { this.plugin = plugin; }
    public static HolderLookup.Provider registries() {
        var server = MinecraftServer.getServer();
        var map = new LinkedHashMap<ResourceKey<? extends Registry<?>>, HolderLookup.RegistryLookup<?>>();
        server.registryAccess().listRegistries().forEach(lookup -> map.put(lookup.key(), lookup));
        server.reloadableRegistries().lookup().listRegistries().forEach(lookup -> map.put(lookup.key(), lookup));
        return HolderLookup.Provider.create(map.values().stream());
    }
    public synchronized void prepare(String world, List<TerraPack> packs) throws IOException {
        var nativePacks = packs.stream().filter(pack -> pack.nativeResources() != null).toList();
        if(nativePacks.isEmpty()) return;
        plugin.assertGenerationAuthorized(world, "PACKS");
        var source = ResourceBundle.merge(nativePacks.stream().map(pack -> pack.nativeResources().source()).toList());
        var installed = resources.get(source.fingerprint());
        if(installed == null) {
            if(!org.bukkit.Bukkit.getOnlinePlayers().isEmpty()) throw new IllegalStateException("New native registry identities require startup before players join");
            var live = registries(); var graph = new NativeResourceGraph(source, live);
            if(!graph.report().valid()) throw new IllegalStateException(String.join("\n", graph.report().errors()));
            Map<Identifier, Path> templates = new HashMap<>();
            Map<String, String> owners = new HashMap<>();
            for(var pack : nativePacks) {
                var nativeResources = pack.nativeResources();
                if(nativeResources.dataVersion() != org.bukkit.Bukkit.getUnsafe().getDataVersion()) throw new IllegalArgumentException("Reconvert pack for this server DataVersion: " + pack.id());
                for(var entry : nativeResources.templates().entrySet()) {
                    String[] parts = entry.getKey().split("/", 4);
                    String id = parts[1] + ":" + parts[3].substring(0, parts[3].length() - 4);
                    Identifier alias = Identifier.parse(graph.scope().privateId(id));
                    Path file = plugin.getDataFolder().toPath().resolve("native-runtime").resolve(source.fingerprint()).resolve("templates")
                        .resolve(parts[1]).resolve(parts[3]);
                    if(templates.putIfAbsent(alias, file) != null) throw new IllegalArgumentException("Conflicting native template: " + id);
                    persistTemplate(file, entry.getValue());
                }
                nativeResources.source().paths().stream().filter(path -> path.matches("data/[^/]+/loot_table/.+\\.json")).forEach(path -> {
                    String[] parts = path.split("/", 4);
                    String id = parts[1] + ":" + parts[3].substring(0, parts[3].length() - 5);
                    String alias = graph.scope().privateId(id);
                    if(owners.putIfAbsent(alias, pack.id()) != null) throw new IllegalArgumentException("Ambiguous native loot ownership: " + id);
                });
            }
            NativeRegistryInstaller.install(graph, live);
            graph.sourceLookup(Registries.LOOT_TABLE).listElements().forEach(holder ->
                NativeEntityLootBridge.wrap(holder.value(), holder.key().identifier().toString(), plugin.lootManager()));
            installed = new Installed(graph, Map.copyOf(templates), Map.copyOf(owners)); resources.put(source.fingerprint(), installed);
            plugin.lootManager().declareNativeTables(installed.lootOwners.keySet());
        }
        var previous = worlds.putIfAbsent(world, installed);
        if(previous != null && previous != installed) throw new IllegalStateException("Cannot replace active native world resources: " + world);
    }
    private static void persistTemplate(Path file, byte[] data) throws IOException {
        Files.createDirectories(file.getParent());
        if(Files.isSymbolicLink(file)) throw new IOException("Scoped template cannot be a symlink");
        if(Files.exists(file)) {
            if(!Arrays.equals(Files.readAllBytes(file), data)) throw new IOException("Stored native template differs: " + file);
            return;
        }
        Path temporary = Files.createTempFile(file.getParent(), ".template-", ".tmp");
        try { Files.write(temporary, data); Files.move(temporary, file); }
        finally { Files.deleteIfExists(temporary); }
    }
    public NativeWorldExecutor bind(ServerLevel level, StructureTemplateManager manager) {
        var installed = worlds.get(level.getWorld().getName());
        if(installed == null) return null;
        plugin.assertGenerationAuthorized(level.getWorld().getName(), "PACKS");
        NativeTemplateRepository.install(manager, installed.templates, installed.graph.scope());
        var target = new WorldTarget(level.getWorld().getName(), level.dimension().identifier().toString());
        return new NativeWorldExecutor(target, installed.graph, actual -> {
            if(!actual.equals(target)) return false;
            try { plugin.assertGenerationAuthorized(actual.worldName(), "PACKS"); return true; }
            catch(RuntimeException denied) { return false; }
        });
    }
    public Map<String, String> lootTables(String world) { var installed = worlds.get(world); return installed == null ? Map.of() : installed.lootOwners; }
}
