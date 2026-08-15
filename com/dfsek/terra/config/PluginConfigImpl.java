package com.dfsek.terra.config;

import com.dfsek.tectonic.api.config.template.ConfigTemplate;
import com.dfsek.tectonic.api.config.template.annotations.Default;
import com.dfsek.tectonic.api.config.template.annotations.Value;
import com.dfsek.tectonic.api.exception.ConfigException;
import com.dfsek.tectonic.api.loader.ConfigLoader;
import com.dfsek.tectonic.yaml.YamlConfiguration;
import com.dfsek.terra.api.Platform;
import com.dfsek.terra.api.config.PluginConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginConfigImpl implements ConfigTemplate, PluginConfig {
   private static final Logger logger = LoggerFactory.getLogger(PluginConfigImpl.class);
   @Value("debug.commands")
   @Default
   private boolean debugCommands = false;
   @Value("debug.profiler")
   @Default
   private boolean debugProfiler = false;
   @Value("debug.script")
   @Default
   private boolean debugScript = false;
   @Value("debug.log")
   @Default
   private boolean debugLog = false;
   @Value("biome-search-resolution")
   @Default
   private int biomeSearch = 4;
   @Value("cache.structure")
   @Default
   private int structureCache = 32;
   @Value("cache.sampler")
   @Default
   private int samplerCache = 1024;
   @Value("cache.biome-provider")
   @Default
   private int providerCache = 32;
   @Value("dump-default")
   @Default
   private boolean dumpDefaultData = true;
   @Value("script.max-recursion")
   @Default
   private int maxRecursion = 1000;

   @Override
   public void load(Platform platform) {
      logger.info("Loading config values from config.yml");

      try (FileInputStream file = new FileInputStream(new File(platform.getDataFolder(), "config.yml"))) {
         ConfigLoader loader = new ConfigLoader();
         loader.load(this, new YamlConfiguration(file, "config.yml"));
      } catch (ConfigException | IOException | UncheckedIOException e) {
         logger.error("Failed to load config", e);
      }

      if (this.debugCommands) {
         logger.info("Debug commands enabled.");
      }

      if (this.debugProfiler) {
         logger.info("Debug profiler enabled.");
      }

      if (this.debugScript) {
         logger.info("Script debug blocks enabled.");
      }

      if (this.debugLog) {
         logger.info("Debug logging enabled.");
      }
   }

   @Override
   public boolean dumpDefaultConfig() {
      return this.dumpDefaultData;
   }

   @Override
   public boolean isDebugCommands() {
      return this.debugCommands;
   }

   @Override
   public boolean isDebugProfiler() {
      return this.debugProfiler;
   }

   @Override
   public boolean isDebugScript() {
      return this.debugScript;
   }

   @Override
   public boolean isDebugLog() {
      return this.debugLog;
   }

   @Override
   public int getBiomeSearchResolution() {
      return this.biomeSearch;
   }

   @Override
   public int getStructureCache() {
      return this.structureCache;
   }

   @Override
   public int getSamplerCache() {
      return this.samplerCache;
   }

   @Override
   public int getMaxRecursion() {
      return this.maxRecursion;
   }

   @Override
   public int getProviderCache() {
      return this.providerCache;
   }
}
