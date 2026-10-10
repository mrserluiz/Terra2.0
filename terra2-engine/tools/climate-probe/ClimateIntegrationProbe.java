import java.nio.file.*;
import java.util.*;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.plugin.java.JavaPlugin;
import com.dfsek.terra.bukkit.TerraBukkitPlugin;
import org.terra2.api.climate.WorldClimateService;
import Kinkin.aeternum.AeternumSeasonsPlugin;
import Kinkin.aeternum.calendar.Season;

/** Disposable real-Paper probe, never shipped in a production plugin. */
public final class ClimateIntegrationProbe extends JavaPlugin {
    public void onEnable() { Bukkit.getScheduler().runTaskLater(this, this::check, 20L); }
    private void require(boolean value, String why) { if(!value) throw new IllegalStateException(why); }
    private void check() {
        try {
            var terra = (TerraBukkitPlugin) Bukkit.getPluginManager().getPlugin("Terra2");
            var aeternum = (AeternumSeasonsPlugin) Bukkit.getPluginManager().getPlugin("AeternumSeasons");
            require(terra != null && terra.isEnabled() && aeternum != null && aeternum.isEnabled(), "Independent plugins enabled");
            var api = Bukkit.getServicesManager().load(WorldClimateService.class);
            require(api != null && api.apiVersion() == 1, "Terra2 provides climate API 1");
            var seasons = aeternum.getSeasons();
            seasons.setSeason(Season.SUMMER);
            var main = Bukkit.getWorld("world");
            require(api.profile(main).isEmpty(), "Primary world must not receive metadata");
            require(api.profile(null).isEmpty(), "Null world must not receive metadata");
            var world = WorldCreator.name("terra2_climate_smoke").seed(42).environment(World.Environment.NORMAL)
                .generator("Terra2:HYDRAXIA").createWorld();
            require(world != null, "HYDRAXIA world created");
            world.getChunkAt(0, 0);
            String actual = world.getBiome(0, 64, 0).getKey().toString();
            require(actual.startsWith("terra:"), "A real custom biome is present: " + actual);
            Path state = getDataFolder().toPath().resolve("biome.txt"); Files.createDirectories(state.getParent());
            if(Files.exists(state)) require(Files.readString(state).equals(actual), "Custom biome survives restart");
            else Files.writeString(state, actual);
            require(api.describe(world).get("world-uuid").equals(world.getUID().toString()), "Metadata belongs to this world UUID");
            boolean bridgeExpected = !Boolean.getBoolean("terra2.climate.no-bridge");
            var bridge = Bukkit.getPluginManager().getPlugin("Terra2AeternumBridge");
            require(bridgeExpected == (bridge != null && bridge.isEnabled()), "Optional bridge presence");
            if(!bridgeExpected) {
                require(seasons.climateProfile(world) == null, "Aeternum independent without the bridge");
                require(seasons.getStateCopy(world).season == Season.SUMMER, "Aeternum uses its own calendar without the bridge");
                getLogger().info("TERRA2_CLIMATE_NO_BRIDGE_OK");
                Bukkit.shutdown(); return;
            }
            require(seasons.getStateCopy(world).season == Season.WINTER, "Aeternum consumes Terra2 winter");
            require(seasons.climateBiome(world, world.getBiome(0, 64, 0)) == Biome.SNOWY_PLAINS, "Virtual snowy reference");
            require(seasons.preservesWorldBiomes(world), "Real biome painting/restoration blocked");
            require(seasons.getStateCopy(main).season == Season.SUMMER && seasons.climateProfile(main) == null, "Main world unchanged");
            require(!aeternum.cfg.climate.contains("world_climate.profiles.terra2_climate_smoke"), "No profile copied into Aeternum YAML");
            Path settings = terra.getDataFolder().toPath().resolve("terra2-settings.yml"); String original = Files.readString(settings);
            try {
                Files.writeString(settings, original.replace("season: WINTER", "season: SUMMER").replace("minecraft:snowy_plains", "minecraft:desert"));
                terra.reloadGenerationSettings();
                require(seasons.getStateCopy(world).season == Season.SUMMER && seasons.climateBiome(world, Biome.PLAINS) == Biome.DESERT,
                    "Terra2 reload updates consumer without restarting Aeternum");
                Files.writeString(settings, original.replace("season: WINTER", "season: INVALID"));
                boolean refused = false; try { terra.reloadGenerationSettings(); } catch(Exception expected) { refused = true; }
                require(refused && seasons.climateBiome(world, Biome.PLAINS) == Biome.DESERT, "Invalid reload preserves previous snapshot");
                Files.writeString(settings, original + "  world:\n    pack: HYDRAXIA\n    climate:\n      season: WINTER\n      reference-biome: minecraft:snowy_plains\n");
                refused = false; try { terra.reloadGenerationSettings(); } catch(Exception expected) { refused = true; }
                require(refused && api.profile(main).isEmpty(), "Protected world climate refused");
            } finally { Files.writeString(settings, original); terra.reloadGenerationSettings(); }
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "terra2climatebridge disconnect");
            require(seasons.climateProfile(world) == null && seasons.getStateCopy(world).season == Season.SUMMER, "Bridge removal releases external profile");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "terra2climatebridge connect");
            require(seasons.getStateCopy(world).season == Season.WINTER, "Bridge reconnects independently");
            Bukkit.getScheduler().runTaskLater(this, () -> {
                try {
                    require(actual.equals(world.getBiome(0, 64, 0).getKey().toString()), "Custom biome unchanged after effects/reload");
                    world.save(); seasons.persistNow();
                    getLogger().info("TERRA2_CLIMATE_BRIDGE_OK biome=" + actual);
                } catch(Throwable failed) { failed.printStackTrace(); getLogger().severe("TERRA2_CLIMATE_BRIDGE_FAILED"); }
                finally { Bukkit.shutdown(); }
            }, 60L);
        } catch(Throwable failed) { failed.printStackTrace(); getLogger().severe("TERRA2_CLIMATE_BRIDGE_FAILED"); Bukkit.shutdown(); }
    }
}
