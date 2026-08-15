package com.dfsek.terra.config.loaders;

import ca.solostudios.strata.Versions;
import ca.solostudios.strata.parser.tokenizer.ParseException;
import ca.solostudios.strata.version.VersionRange;
import com.dfsek.tectonic.api.depth.DepthTracker;
import com.dfsek.tectonic.api.exception.LoadException;
import com.dfsek.tectonic.api.loader.ConfigLoader;
import com.dfsek.tectonic.api.loader.type.TypeLoader;
import java.lang.reflect.AnnotatedType;
import org.jetbrains.annotations.NotNull;

public class VersionRangeLoader implements TypeLoader<VersionRange> {
   public VersionRange load(@NotNull AnnotatedType t, @NotNull Object c, @NotNull ConfigLoader loader, DepthTracker depthTracker) throws LoadException {
      try {
         return Versions.parseVersionRange((String)c);
      } catch (ParseException e) {
         throw new LoadException("Failed to parse version range: ", e, depthTracker);
      }
   }
}
