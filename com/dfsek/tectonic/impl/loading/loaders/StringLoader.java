package com.dfsek.tectonic.impl.loading.loaders;

import com.dfsek.tectonic.api.depth.DepthTracker;
import com.dfsek.tectonic.api.loader.ConfigLoader;
import com.dfsek.tectonic.api.loader.type.TypeLoader;
import java.lang.reflect.AnnotatedType;
import org.jetbrains.annotations.NotNull;

public class StringLoader implements TypeLoader<String> {
   public String load(@NotNull AnnotatedType t, @NotNull Object c, @NotNull ConfigLoader loader, DepthTracker depthTracker) {
      return (String)c;
   }
}
