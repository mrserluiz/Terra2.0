package com.dfsek.terra.api.event.events.config.pack;

import com.dfsek.tectonic.api.config.template.ConfigTemplate;
import com.dfsek.terra.api.config.ConfigPack;

public class ConfigPackPostLoadEvent extends ConfigPackLoadEvent {
   public ConfigPackPostLoadEvent(ConfigPack pack, ConfigPackLoadEvent.ExceptionalConsumer<ConfigTemplate> loader) {
      super(pack, loader);
   }
}
