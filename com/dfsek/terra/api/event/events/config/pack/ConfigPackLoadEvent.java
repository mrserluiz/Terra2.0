package com.dfsek.terra.api.event.events.config.pack;

import com.dfsek.tectonic.api.config.template.ConfigTemplate;
import com.dfsek.tectonic.api.exception.ConfigException;
import com.dfsek.terra.api.config.ConfigPack;
import com.dfsek.terra.api.event.events.FailThroughEvent;
import com.dfsek.terra.api.event.events.PackEvent;

public abstract class ConfigPackLoadEvent implements PackEvent, FailThroughEvent {
   private final ConfigPack pack;
   private final ConfigPackLoadEvent.ExceptionalConsumer<ConfigTemplate> configLoader;

   public ConfigPackLoadEvent(ConfigPack pack, ConfigPackLoadEvent.ExceptionalConsumer<ConfigTemplate> configLoader) {
      this.pack = pack;
      this.configLoader = configLoader;
   }

   public <T extends ConfigTemplate> T loadTemplate(T template) throws ConfigException {
      this.configLoader.accept(template);
      return template;
   }

   @Override
   public ConfigPack getPack() {
      return this.pack;
   }

   public interface ExceptionalConsumer<T extends ConfigTemplate> {
      void accept(T var1) throws ConfigException;
   }
}
