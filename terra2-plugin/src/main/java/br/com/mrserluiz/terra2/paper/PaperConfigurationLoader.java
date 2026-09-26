package br.com.mrserluiz.terra2.paper;

import br.com.mrserluiz.terra2.core.GenerationSafetySettings;
import br.com.mrserluiz.terra2.core.WorldDefinition;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

final class PaperConfigurationLoader {
    PaperConfiguration load(FileConfiguration config) {
        GenerationSafetySettings safety = new GenerationSafetySettings(
                config.getBoolean("generation.enabled", false),
                config.getBoolean("generation.affect-existing-worlds", false),
                config.getBoolean("generation.generate-new-chunks-only", true),
                config.getBoolean("generation.require-explicit-world-selection", true)
        );

        ConfigurationSection worldsSection = config.getConfigurationSection("worlds");
        if (worldsSection == null) return new PaperConfiguration(safety, List.of());

        List<WorldDefinition> worlds = new ArrayList<>();
        for (String worldName : worldsSection.getKeys(false)) {
            ConfigurationSection world = worldsSection.getConfigurationSection(worldName);
            if (world == null) continue;
            String packId = world.getString("pack", "").trim();
            if (packId.isEmpty()) {
                throw new IllegalArgumentException("World '" + worldName + "' has no Terra pack ID.");
            }
            worlds.add(new WorldDefinition(
                    worldName,
                    world.getBoolean("enabled", false),
                    world.getBoolean("create-if-missing", false),
                    packId,
                    world.getString("environment", "NORMAL"),
                    world.contains("seed") ? world.getLong("seed") : null
            ));
        }
        return new PaperConfiguration(safety, worlds);
    }
}
