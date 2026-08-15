package com.dfsek.terra.config.preprocessor;

import com.dfsek.tectonic.api.config.Configuration;
import com.dfsek.tectonic.api.depth.DepthTracker;
import com.dfsek.tectonic.api.depth.EntryLevel;
import com.dfsek.tectonic.api.exception.LoadException;
import com.dfsek.tectonic.api.loader.ConfigLoader;
import com.dfsek.tectonic.api.preprocessor.Result;
import com.dfsek.terra.api.config.meta.Meta;
import com.dfsek.terra.api.util.generic.pair.Pair;
import com.dfsek.terra.api.util.reflection.TypeKey;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class MetaMapPreprocessor extends MetaPreprocessor<Meta> {
   private static final TypeKey<List<String>> STRING_LIST = new TypeKey<List<String>>() {};

   public MetaMapPreprocessor(Map<String, Configuration> configs) {
      super(configs);
   }

   @NotNull
   public <T> Result<T> process(AnnotatedType t, T c, ConfigLoader loader, Meta annotation, DepthTracker depthTracker) {
      if (t.getType() instanceof ParameterizedType parameterizedType
         && parameterizedType.getRawType() instanceof Class<?> baseClass
         && Map.class.isAssignableFrom(baseClass)
         && c instanceof Map<Object, Object> map
         && map.containsKey("<<")) {
         Map<Object, Object> newMap = new HashMap<>(map);
         List<String> keys = (List<String>)loader.loadType(STRING_LIST.getAnnotatedType(), map.get("<<"), depthTracker);
         keys.forEach(
            key -> {
               Pair<Configuration, Object> pair = this.getMetaValue(key, depthTracker);
               Object meta = pair.getRight();
               if (!(meta instanceof Map)) {
                  throw new LoadException("MetaMap injection candidate must be list, is type " + meta.getClass().getCanonicalName(), depthTracker);
               }

               newMap.putAll((Map<? extends Object, ? extends Object>)meta);
               String configName;
               if (pair.getLeft().getName() == null) {
                  configName = "Anonymous Configuration";
               } else {
                  configName = pair.getLeft().getName();
               }

               depthTracker.addIntrinsicLevel(
                  level -> level instanceof EntryLevel entryLevel && ((Map)meta).containsKey(entryLevel.getName())
                     ? Optional.of("From configuration \"" + configName + "\"")
                     : Optional.empty()
               );
            }
         );
         newMap.putAll(map);
         newMap.remove("<<");
         return Result.overwrite((T)newMap, depthTracker);
      } else {
         return Result.noOp();
      }
   }
}
