package br.com.mrserluiz.terra2.paper;

import br.com.mrserluiz.terra2.core.WorldDefinition;
import br.com.mrserluiz.terra2.integration.PackDescriptor;
import br.com.mrserluiz.terra2.integration.TerraRuntimeAdapter;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/** Paper bridge that talks to Terra through its public Bukkit entry point and registry surface. */
final class PaperTerraRuntimeAdapter implements TerraRuntimeAdapter {
    private final Plugin terra;

    PaperTerraRuntimeAdapter(Plugin terra) {
        this.terra = terra;
    }

    @Override
    public boolean terraAvailable() {
        return terra instanceof JavaPlugin && terra.isEnabled();
    }

    @Override
    public Set<String> installedAddonIds() {
        try {
            Object platform = invoke(terra, "getPlatform");
            Object registry = invoke(platform, "getAddons");
            Object keys = invoke(registry, "keys");
            if (!(keys instanceof Collection<?> collection)) return Set.of();

            Set<String> ids = new LinkedHashSet<>();
            for (Object key : collection) {
                ids.add(String.valueOf(invoke(key, "getID")));
                ids.add(key.toString());
            }
            return Set.copyOf(ids);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Could not read Terra addon registry.", error);
        }
    }

    @Override
    public boolean packLoaded(String packId) {
        try {
            Object platform = invoke(terra, "getPlatform");
            Object registry = invoke(platform, "getConfigRegistry");
            Method getById = registry.getClass().getMethod("getByID", String.class);
            Object result = getById.invoke(registry, packId);
            return result instanceof Optional<?> optional && optional.isPresent();
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Could not read Terra pack registry.", error);
        }
    }

    @Override
    public boolean createWorld(WorldDefinition definition, PackDescriptor pack) {
        JavaPlugin terraPlugin = (JavaPlugin) terra;
        ChunkGenerator generator = terraPlugin.getDefaultWorldGenerator(definition.worldName(), pack.id());
        if (generator == null) throw new IllegalStateException("Terra returned no generator for " + pack.id());

        WorldCreator creator = new WorldCreator(definition.worldName())
                .environment(World.Environment.valueOf(definition.environment()))
                .generator(generator);
        if (definition.seed() != null) creator.seed(definition.seed());
        return Bukkit.createWorld(creator) != null;
    }

    private static Object invoke(Object target, String method) throws ReflectiveOperationException {
        return target.getClass().getMethod(method).invoke(target);
    }
}
