package com.dfsek.terra.api.event.events.config;

import com.dfsek.tectonic.api.config.Configuration;
import com.dfsek.terra.api.config.ConfigPack;
import com.dfsek.terra.api.config.Loader;
import com.dfsek.terra.api.event.events.FailThroughEvent;
import com.dfsek.terra.api.event.events.PackEvent;
import java.util.function.BiConsumer;

public class ConfigurationDiscoveryEvent implements PackEvent, FailThroughEvent {
   private final ConfigPack pack;
   private final Loader loader;
   private final BiConsumer<String, Configuration> consumer;

   public ConfigurationDiscoveryEvent(ConfigPack pack, Loader loader, BiConsumer<String, Configuration> consumer) {
      this.pack = pack;
      this.loader = loader;
      this.consumer = consumer;
   }

   public void register(String identifier, Configuration config) {
      this.consumer.accept(identifier, config);
   }

   @Override
   public ConfigPack getPack() {
      return this.pack;
   }

   public Loader getLoader() {
      return this.loader;
   }
}
