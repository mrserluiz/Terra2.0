package com.dfsek.terra.bukkit.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import org.bukkit.configuration.file.YamlConfiguration;

/** Command-only policy: commands cannot remove any world protection. */
public final class WorldSettingsEditor {
    private static final Set<String> BASE_NAMES = Set.of("world", "nether", "end", "world_nether", "world_the_end", "the_nether", "the_end");
    private static final Set<String> BASE_KEYS = Set.of("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end");
    private WorldSettingsEditor() {}

    public static boolean blocked(YamlConfiguration settings, String primary, String world, String dimensionKey) {
        if(primary == null || world == null) return true;
        String name = world.toLowerCase(Locale.ROOT);
        String root = primary.toLowerCase(Locale.ROOT);
        return BASE_NAMES.contains(name) || name.equals(root) || name.equals(root + "_nether") || name.equals(root + "_the_end")
            || settings.getStringList("generation.protected-worlds").stream().anyMatch(world::equalsIgnoreCase)
            || BASE_KEYS.contains(dimensionKey == null ? "" : dimensionKey);
    }

    public static YamlConfiguration select(YamlConfiguration current, String primary, String world,
                                           String dimensionKey, boolean loaded, String activeSelection, String selection) throws Exception {
        if(!world.matches("[A-Za-z0-9_-]+")) throw new IllegalArgumentException("world-name");
        if(blocked(current, primary, world, dimensionKey)) throw new IllegalArgumentException("protected");
        if(loaded && !selection.equals(activeSelection)) throw new IllegalArgumentException("loaded");
        YamlConfiguration next = new YamlConfiguration();
        next.options().parseComments(true);
        next.loadFromString(current.saveToString());
        var worlds = next.getConfigurationSection("worlds");
        if(worlds == null) throw new IllegalArgumentException("worlds-section");
        if(worlds.getKeys(false).stream().anyMatch(name -> !name.equals(world) && name.equalsIgnoreCase(world)))
            throw new IllegalArgumentException("world-name");
        String path = "worlds." + world;
        // Preserve loot rules and unrelated world settings; only replace source selection.
        next.set(path + ".packs", null);
        next.set(path + ".datapack", null);
        next.set(path + ".dimension", null);
        next.set(path + ".pack", selection);
        GenerationSettings.packIds(next, world);
        next.set("generation.enabled", true);
        return next;
    }

    public static void persist(Path target, YamlConfiguration next) throws Exception {
        Path staged = Files.createTempFile(target.toAbsolutePath().getParent(), "terra2-settings-", ".tmp");
        try {
            Files.writeString(staged, next.saveToString(), StandardCharsets.UTF_8);
            // Validate the exact bytes before replacing the file. Memory is updated only after this succeeds.
            GenerationSettings.load(staged.toFile());
            Files.move(staged, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally { Files.deleteIfExists(staged); }
    }
}
