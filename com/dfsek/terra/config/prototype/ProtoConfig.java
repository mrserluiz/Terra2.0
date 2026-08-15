package com.dfsek.terra.config.prototype;

import com.dfsek.tectonic.api.config.template.ConfigTemplate;
import com.dfsek.tectonic.api.config.template.annotations.Value;
import com.dfsek.terra.api.config.ConfigType;

public class ProtoConfig implements ConfigTemplate {
   @Value("id")
   private String id;
   @Value("type")
   private ConfigType<?, ?> type;

   public String getId() {
      return this.id;
   }

   public ConfigType<?, ?> getType() {
      return this.type;
   }
}
