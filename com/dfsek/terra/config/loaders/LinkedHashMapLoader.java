package com.dfsek.terra.config.loaders;

import com.dfsek.tectonic.api.depth.DepthTracker;
import com.dfsek.tectonic.api.exception.LoadException;
import com.dfsek.tectonic.api.loader.ConfigLoader;
import com.dfsek.tectonic.api.loader.type.TypeLoader;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.jetbrains.annotations.NotNull;

public class LinkedHashMapLoader implements TypeLoader<LinkedHashMap<Object, Object>> {
   public LinkedHashMap<Object, Object> load(@NotNull AnnotatedType t, @NotNull Object c, @NotNull ConfigLoader loader, DepthTracker depthTracker) throws LoadException {
      Map<String, Object> config = (Map<String, Object>)c;
      LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
      if (!(t instanceof AnnotatedParameterizedType pType)) {
         throw new LoadException("Unable to load config", depthTracker);
      } else {
         AnnotatedType key = pType.getAnnotatedActualTypeArguments()[0];
         AnnotatedType value = pType.getAnnotatedActualTypeArguments()[1];

         for (Entry<String, Object> entry : config.entrySet()) {
            map.put(
               loader.loadType(key, entry.getKey(), depthTracker.entry(entry.getKey())),
               loader.loadType(value, entry.getValue(), depthTracker.entry(entry.getKey()))
            );
         }

         return map;
      }
   }
}
