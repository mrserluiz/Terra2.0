import java.nio.file.*;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import com.dfsek.terra.bukkit.TerraBukkitPlugin;
import com.dfsek.terra.bukkit.bedrock.*;
import com.google.gson.JsonParser;

/** Isolated startup/registration test, not a substitute for a real Bedrock client. */
public final class BedrockIntegrationProbe extends JavaPlugin {
    public void onEnable() { Bukkit.getScheduler().runTaskLater(this, this::check, 80L); }
    void check() {
        try {
            var terra = (TerraBukkitPlugin) Bukkit.getPluginManager().getPlugin("Terra2");
            var geyser = Bukkit.getPluginManager().getPlugin("Geyser-Spigot");
            require(terra != null && terra.isEnabled(), "Terra2 enabled with Geyser");
            require(BedrockCommands.supportsBiomes(geyser), "Actual loaded Geyser custom biome capability");
            var expected = terra.platform().bedrockBiomeAppearances();
            Class<?> registries = Class.forName("org.geysermc.geyser.registry.Registries", false, geyser.getClass().getClassLoader());
            Object registry = registries.getField("CUSTOM_BIOMES").get(null);
            Map<?, ?> registered = (Map<?, ?>) registry.getClass().getMethod("get").invoke(registry);
            Set<String> ids = new HashSet<>(); registered.keySet().forEach(id -> ids.add(id.toString()));
            require(ids.equals(expected.keySet()), "All and only exported custom biomes registered");
            Path installed = geyser.getDataFolder().toPath().resolve("custom_mappings/terra2-biomes.json");
            byte[] before = Files.readAllBytes(installed);
            BedrockCommands.handle(terra, Bukkit.getConsoleSender(), new String[]{"bedrock", "status"});
            BedrockCommands.handle(terra, Bukkit.getConsoleSender(), new String[]{"bedrock", "install"});
            require(Arrays.equals(before, Files.readAllBytes(installed)), "Install is idempotent and preserves exact mappings");
            Path pack = geyser.getDataFolder().toPath().resolve("cache/custom_biomes.mcpack");
            require(Files.isRegularFile(pack) && Files.size(pack) > 0, "Geyser generated the real runtime resource pack");
            try(var zip = new java.util.zip.ZipFile(pack.toFile())) {
                long assets = zip.stream().filter(entry -> entry.getName().contains("biomes/") && entry.getName().endsWith(".json")).count();
                require(assets == expected.size(), "Every custom biome has an asset in runtime pack");
            }
            require(!Bukkit.getWorld("world").getBiome(0, 64, 0).getKey().getNamespace().equals("terra"), "Primary world remains vanilla");
            Path state = getDataFolder().toPath().resolve("mapping.sha256"); Files.createDirectories(state.getParent());
            String checksum = HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(before));
            if(Files.exists(state)) require(Files.readString(state).equals(checksum), "Mapping and registration survive restart");
            else Files.writeString(state, checksum);
            getLogger().info("TERRA2_GEYSER_STARTUP_OK biomes=" + expected.size());
        } catch(Throwable failure) { getLogger().log(java.util.logging.Level.SEVERE, "TERRA2_GEYSER_STARTUP_FAILED", failure); }
        finally { Bukkit.shutdown(); }
    }
    void require(boolean valid, String reason) { if(!valid) throw new IllegalStateException(reason); }
}
