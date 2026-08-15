package com.dfsek.terra.api.config;

import ca.solostudios.strata.version.Version;
import ca.solostudios.strata.version.VersionRange;
import com.dfsek.paralithic.eval.parser.Parser;
import com.dfsek.terra.api.addon.BaseAddon;
import com.dfsek.terra.api.properties.PropertyHolder;
import com.dfsek.terra.api.registry.key.Keyed;
import com.dfsek.terra.api.registry.key.RegistryKey;
import com.dfsek.terra.api.registry.meta.CheckedRegistryHolder;
import com.dfsek.terra.api.registry.meta.RegistryProvider;
import com.dfsek.terra.api.tectonic.ConfigLoadingDelegate;
import com.dfsek.terra.api.tectonic.LoaderRegistrar;
import com.dfsek.terra.api.tectonic.ShortcutLoader;
import com.dfsek.terra.api.util.reflection.TypeKey;
import com.dfsek.terra.api.world.biome.generation.BiomeProvider;
import com.dfsek.terra.api.world.chunk.generation.stage.GenerationStage;
import com.dfsek.terra.api.world.chunk.generation.util.provider.ChunkGeneratorProvider;
import java.util.List;
import java.util.Map;

public interface ConfigPack extends LoaderRegistrar, ConfigLoadingDelegate, CheckedRegistryHolder, RegistryProvider, Keyed<ConfigPack>, PropertyHolder {
   ConfigPack registerConfigType(ConfigType<?, ?> var1, RegistryKey var2, int var3);

   Map<BaseAddon, VersionRange> addons();

   BiomeProvider getBiomeProvider();

   List<GenerationStage> getStages();

   Loader getLoader();

   String getAuthor();

   Version getVersion();

   Parser.ParseOptions getExpressionParseOptions();

   <T> ConfigPack registerShortcut(TypeKey<T> var1, String var2, ShortcutLoader<T> var3);

   default <T> ConfigPack registerShortcut(Class<T> clazz, String shortcut, ShortcutLoader<T> loader) {
      return this.registerShortcut(TypeKey.of(clazz), shortcut, loader);
   }

   ChunkGeneratorProvider getGeneratorProvider();
}
