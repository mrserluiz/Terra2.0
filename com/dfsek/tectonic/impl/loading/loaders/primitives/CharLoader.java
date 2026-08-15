package com.dfsek.tectonic.impl.loading.loaders.primitives;

import com.dfsek.tectonic.api.depth.DepthTracker;
import com.dfsek.tectonic.api.exception.LoadException;
import com.dfsek.tectonic.api.loader.ConfigLoader;
import com.dfsek.tectonic.api.loader.type.TypeLoader;
import java.lang.reflect.AnnotatedType;
import org.jetbrains.annotations.NotNull;

public class CharLoader implements TypeLoader<Character> {
   public Character load(@NotNull AnnotatedType t, @NotNull Object c, @NotNull ConfigLoader loader, DepthTracker depthTracker) {
      try {
         return (Character)c;
      } catch (ClassCastException e) {
         throw new LoadException("Data provided is not a character. Data is type: " + c.getClass().getSimpleName(), e, depthTracker);
      }
   }
}
