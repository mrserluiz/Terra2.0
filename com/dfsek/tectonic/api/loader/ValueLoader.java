package com.dfsek.tectonic.api.loader;

import com.dfsek.tectonic.api.config.Configuration;
import com.dfsek.tectonic.api.depth.DepthTracker;
import java.lang.reflect.AnnotatedType;

@FunctionalInterface
public interface ValueLoader {
   default Object load(String key, AnnotatedType type, Configuration configuration, DepthTracker depthTracker) {
      return this.load(key, type, configuration, depthTracker, false);
   }

   Object load(String var1, AnnotatedType var2, Configuration var3, DepthTracker var4, boolean var5);
}
