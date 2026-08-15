package com.dfsek.terra.config.loaders;

import com.dfsek.tectonic.api.depth.DepthTracker;
import com.dfsek.tectonic.api.exception.LoadException;
import com.dfsek.tectonic.api.loader.ConfigLoader;
import com.dfsek.tectonic.api.loader.type.TypeLoader;
import com.dfsek.terra.api.util.collection.ProbabilityCollection;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.jetbrains.annotations.NotNull;

public class ProbabilityCollectionLoader implements TypeLoader<ProbabilityCollection<Object>> {
   public ProbabilityCollection<Object> load(@NotNull AnnotatedType type, @NotNull Object o, @NotNull ConfigLoader configLoader, DepthTracker depthTracker) throws LoadException {
      ProbabilityCollection<Object> collection = new ProbabilityCollection<>();
      if (!(type instanceof AnnotatedParameterizedType pType)) {
         throw new LoadException("Unable to load config! Could not retrieve parameterized type: " + type, depthTracker);
      } else {
         AnnotatedType generic = pType.getAnnotatedActualTypeArguments()[0];
         if (o instanceof Map<Object, Object> map) {
            if (map.size() == 1) {
               Object onlyKey = map.keySet().iterator().next();
               return new ProbabilityCollection.Singleton<>(configLoader.loadType(generic, onlyKey, depthTracker));
            }

            for (Entry<Object, Object> entry : map.entrySet()) {
               collection.add(
                  configLoader.loadType(generic, entry.getKey(), depthTracker.entry((String)entry.getKey())),
                  configLoader.loadType(Integer.class, entry.getValue(), depthTracker.entry((String)entry.getKey()))
               );
            }
         } else {
            if (!(o instanceof List<Map<Object, Object>> list)) {
               if (o instanceof String) {
                  return new ProbabilityCollection.Singleton<>(configLoader.loadType(generic, o, depthTracker));
               }

               throw new LoadException("Malformed Probability Collection: " + o, depthTracker);
            }

            if (list.size() == 1) {
               Map<Object, Object> map = list.getFirst();
               if (map.size() == 1) {
                  Iterator var18 = map.keySet().iterator();
                  if (var18.hasNext()) {
                     Object value = var18.next();
                     return new ProbabilityCollection.Singleton<>(configLoader.loadType(generic, value, depthTracker));
                  }
               }
            }

            for (int i = 0; i < list.size(); i++) {
               Map<Object, Object> map = list.get(i);

               for (Entry<Object, Object> entry : map.entrySet()) {
                  if (entry.getValue() == null) {
                     throw new LoadException("No probability defined for entry \"" + entry.getKey() + "\"", depthTracker);
                  }

                  Object val = configLoader.loadType(generic, entry.getKey(), depthTracker.index(i).entry((String)entry.getKey()));
                  collection.add(val, configLoader.loadType(Integer.class, entry.getValue(), depthTracker.entry((String)entry.getKey())));
               }
            }
         }

         return collection;
      }
   }
}
