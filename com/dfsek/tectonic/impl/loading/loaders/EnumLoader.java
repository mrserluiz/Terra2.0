package com.dfsek.tectonic.impl.loading.loaders;

import com.dfsek.tectonic.api.depth.DepthTracker;
import com.dfsek.tectonic.api.exception.LoadException;
import com.dfsek.tectonic.api.loader.ConfigLoader;
import com.dfsek.tectonic.api.loader.type.TypeLoader;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.jetbrains.annotations.NotNull;

public class EnumLoader implements TypeLoader<Enum<?>> {
   public Enum<?> load(@NotNull AnnotatedType t, @NotNull Object c, @NotNull ConfigLoader loader, DepthTracker depthTracker) throws LoadException {
      Type base = t.getType();
      Class<Enum> enumClass;
      if (base instanceof ParameterizedType) {
         enumClass = (Class<Enum>)((ParameterizedType)base).getRawType();
      } else {
         enumClass = (Class<Enum>)base;
      }

      return Enum.valueOf(enumClass, (String)c);
   }
}
