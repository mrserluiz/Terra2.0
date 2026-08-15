package com.dfsek.terra.api.addon;

import ca.solostudios.strata.version.Version;
import ca.solostudios.strata.version.VersionRange;
import com.dfsek.terra.api.registry.key.Namespaced;
import com.dfsek.terra.api.registry.key.StringIdentifiable;
import java.util.Collections;
import java.util.Map;

public interface BaseAddon extends StringIdentifiable, Namespaced {
   default void initialize() {
   }

   default Map<String, VersionRange> getDependencies() {
      return Collections.emptyMap();
   }

   Version getVersion();

   @Override
   default String getNamespace() {
      return this.getID();
   }
}
