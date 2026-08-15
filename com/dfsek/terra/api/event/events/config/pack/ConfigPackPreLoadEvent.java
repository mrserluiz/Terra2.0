package com.dfsek.terra.api.event.events.config.pack;

import com.dfsek.tectonic.api.config.template.ConfigTemplate;
import com.dfsek.terra.api.config.ConfigPack;

public class ConfigPackPreLoadEvent extends ConfigPackLoadEvent {
   public ConfigPackPreLoadEvent(ConfigPack pack, ConfigPackLoadEvent.ExceptionalConsumer<ConfigTemplate> configLoader) {
      super(pack, configLoader);
   }
}
