package com.dfsek.terra.config.pack;

import ca.solostudios.strata.version.VersionRange;
import com.dfsek.tectonic.api.config.template.ConfigTemplate;
import com.dfsek.tectonic.api.config.template.annotations.Default;
import com.dfsek.tectonic.api.config.template.annotations.Value;
import com.dfsek.terra.api.addon.BaseAddon;
import java.util.HashMap;
import java.util.Map;

public class ConfigPackAddonsTemplate implements ConfigTemplate {
   @Value("addons")
   @Default
   private Map<BaseAddon, VersionRange> addons = new HashMap<>();

   public Map<BaseAddon, VersionRange> getAddons() {
      return this.addons;
   }
}
