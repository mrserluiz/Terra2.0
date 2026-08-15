package com.dfsek.terra.config.loaders;

import com.dfsek.tectonic.api.config.template.annotations.Value;
import com.dfsek.tectonic.api.config.template.object.ObjectTemplate;
import com.dfsek.tectonic.api.depth.DepthTracker;
import com.dfsek.tectonic.api.exception.LoadException;
import com.dfsek.tectonic.api.loader.ConfigLoader;
import com.dfsek.tectonic.api.loader.type.TypeLoader;
import com.dfsek.tectonic.impl.MapConfiguration;
import com.dfsek.terra.api.config.meta.Meta;
import com.dfsek.terra.api.util.ConstantRange;
import com.dfsek.terra.api.util.Range;
import java.lang.reflect.AnnotatedType;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class RangeLoader implements TypeLoader<Range> {
   public Range load(@NotNull AnnotatedType type, @NotNull Object o, @NotNull ConfigLoader configLoader, DepthTracker depthTracker) throws LoadException {
      if (o instanceof Map) {
         return configLoader.load(new RangeLoader.RangeMapTemplate(), new MapConfiguration((Map<String, Object>)o), depthTracker).get();
      }

      int h = configLoader.loadType(Integer.class, o, depthTracker);
      return new ConstantRange(h, h + 1);
   }

   private static class RangeMapTemplate implements ObjectTemplate<Range> {
      @Value("min")
      private @Meta int min;
      @Value("max")
      private @Meta int max;

      public Range get() {
         return new ConstantRange(this.min, this.max);
      }
   }
}
