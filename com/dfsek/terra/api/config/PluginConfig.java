package com.dfsek.terra.api.config;

import com.dfsek.terra.api.Platform;

public interface PluginConfig {
   void load(Platform var1);

   boolean dumpDefaultConfig();

   boolean isDebugCommands();

   boolean isDebugProfiler();

   boolean isDebugScript();

   boolean isDebugLog();

   int getBiomeSearchResolution();

   int getStructureCache();

   int getSamplerCache();

   int getMaxRecursion();

   int getProviderCache();
}
