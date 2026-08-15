package com.dfsek.terra.config.loaders;

import com.dfsek.tectonic.api.config.template.object.ObjectTemplate;
import com.dfsek.tectonic.api.depth.DepthTracker;
import com.dfsek.tectonic.api.exception.LoadException;
import com.dfsek.tectonic.api.loader.ConfigLoader;
import com.dfsek.tectonic.api.loader.type.TypeLoader;
import com.dfsek.tectonic.impl.MapConfiguration;
import com.dfsek.terra.api.registry.Registry;
import java.lang.reflect.AnnotatedType;
import java.util.Map;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public class GenericTemplateSupplierLoader<T> implements TypeLoader<T> {
   private final Registry<Supplier<ObjectTemplate<T>>> registry;

   public GenericTemplateSupplierLoader(Registry<Supplier<ObjectTemplate<T>>> registry) {
      this.registry = registry;
   }

   @Override
   public T load(@NotNull AnnotatedType t, @NotNull Object c, ConfigLoader loader, DepthTracker depthTracker) throws LoadException {
      Map<String, Object> map = (Map<String, Object>)c;
      String type = (String)map.get("type");
      return loader.load(
            this.registry.getByID(type).orElseThrow(() -> new LoadException("No such entry: " + map.get("type"), depthTracker)).get(),
            new MapConfiguration(map),
            depthTracker.intrinsic("With type \"" + type + "\"")
         )
         .get();
   }
}
