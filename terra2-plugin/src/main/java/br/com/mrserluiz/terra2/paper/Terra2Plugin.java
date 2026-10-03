package br.com.mrserluiz.terra2.paper;

import br.com.mrserluiz.terra2.core.MultiWorldProvisioner;
import br.com.mrserluiz.terra2.core.WorldSafetyGuard;
import br.com.mrserluiz.terra2.integration.CommunityPackCatalog;
import br.com.mrserluiz.terra2.integration.PackValidator;
import br.com.mrserluiz.terra2.integration.ProvisioningResult;
import br.com.mrserluiz.terra2.integration.WorldManifestStore;
import br.com.mrserluiz.terra2.integration.WorldProvisioningService;
import java.nio.file.Path;
import java.time.Clock;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public final class Terra2Plugin extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();

        PaperConfiguration configuration;
        try {
            configuration = new PaperConfigurationLoader().load(getConfig());
        } catch (RuntimeException error) {
            getLogger().severe("Invalid configuration; no world was touched: " + error.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!configuration.safety().enabled()) {
            getLogger().info("Safe mode: generation is globally disabled. No world will be created or modified.");
            return;
        }
        if (configuration.worlds().isEmpty()) {
            getLogger().warning("Generation is enabled, but no worlds are authorized. No action was taken.");
            return;
        }

        Path packsDirectory = getDataFolder().toPath().resolve("packs");
        PaperWorldStorage storage = new PaperWorldStorage();
        WorldProvisioningService service = new WorldProvisioningService(
                new CommunityPackCatalog(packsDirectory),
                new PackValidator(),
                new WorldManifestStore(storage::resolve),
                new StandaloneGenerationRuntime(),
                new WorldSafetyGuard(),
                Clock.systemUTC()
        );

        Map<String, ProvisioningResult> results = new MultiWorldProvisioner(service).provisionAll(
                configuration.safety(), configuration.worlds(), primaryWorldNames());
        results.forEach(this::logResult);
    }

    private Set<String> primaryWorldNames() {
        Set<String> names = new LinkedHashSet<>();
        if (!getServer().getWorlds().isEmpty()) {
            World primary = getServer().getWorlds().getFirst();
            names.add(primary.getName());
            names.add(primary.getName() + "_nether");
            names.add(primary.getName() + "_the_end");
            // Modern vanilla dimensions can have names unrelated to the old suffixes.
            for (World world : getServer().getWorlds()) {
                if (world.getKey().getNamespace().equals("minecraft")) names.add(world.getName());
            }
        }
        return Set.copyOf(names);
    }

    private void logResult(String worldName, ProvisioningResult result) {
        String message = "[" + worldName + "] " + result.status() + ": " + result.message();
        if (result.status() == ProvisioningResult.Status.CREATED
                || result.status() == ProvisioningResult.Status.ALREADY_MANAGED) {
            getLogger().info(message);
        } else {
            getLogger().warning(message);
        }
    }
}
