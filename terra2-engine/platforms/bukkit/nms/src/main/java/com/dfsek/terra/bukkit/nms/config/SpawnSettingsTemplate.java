package com.dfsek.terra.bukkit.nms.config;

import com.dfsek.tectonic.api.config.template.annotations.Default;
import com.dfsek.tectonic.api.config.template.annotations.Value;
import com.dfsek.tectonic.api.config.template.object.ObjectTemplate;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class SpawnSettingsTemplate implements ObjectTemplate<MobSpawnSettings> {

    private static final Logger logger = LoggerFactory.getLogger(SpawnTypeConfig.class);
    private static boolean used = false;

    @Value("spawns")
    @Default
    private List<SpawnTypeConfig> spawns = null;

    @Value("costs")
    @Default
    private List<SpawnCostConfig> costs = null;

    @Value("probability")
    @Default
    private Float probability = null;

    @Override
    public MobSpawnSettings get() {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
        for(var category:MobCategory.values())builder.noSpawns(category);
        for(SpawnTypeConfig spawn : spawns == null ? List.<SpawnTypeConfig>of() : spawns) {
            MobCategory group = spawn.getGroup();
            for(SpawnEntryConfig entry : spawn.getEntries()) {
                builder.addSpawn(entry.getSpawnEntry().type(), group, entry.getWeight(), entry.getSpawnEntry().count());
            }
        }
        for(SpawnCostConfig cost : costs == null ? List.<SpawnCostConfig>of() : costs) {
            builder.addMobSpawnCost(cost.getType(), cost.getMass(), cost.getGravity());
        }
        // probability moved to the biome environment attribute in 26.3; the parent template applies it.

        return builder.build();
    }
}
