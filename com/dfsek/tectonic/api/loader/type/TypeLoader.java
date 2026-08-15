package com.dfsek.tectonic.api.loader.type;

import com.dfsek.tectonic.api.depth.DepthTracker;
import com.dfsek.tectonic.api.exception.LoadException;
import com.dfsek.tectonic.api.loader.ConfigLoader;
import com.dfsek.tectonic.util.ClassAnnotatedTypeImpl;
import java.lang.reflect.AnnotatedType;
import org.jetbrains.annotations.NotNull;

public interface TypeLoader<T> {
   T load(@NotNull AnnotatedType var1, @NotNull Object var2, @NotNull ConfigLoader var3, DepthTracker var4) throws LoadException;

   default T load(@NotNull Class<T> t, @NotNull Object c, @NotNull ConfigLoader loader, DepthTracker depthTracker) throws LoadException {
      return this.load(new ClassAnnotatedTypeImpl(t), c, loader, depthTracker);
   }
}
