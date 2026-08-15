package com.dfsek.terra.config.preprocessor;

import com.dfsek.tectonic.api.config.Configuration;
import com.dfsek.tectonic.api.depth.DepthTracker;
import com.dfsek.tectonic.api.depth.IndexLevel;
import com.dfsek.tectonic.api.exception.LoadException;
import com.dfsek.tectonic.api.loader.ConfigLoader;
import com.dfsek.tectonic.api.preprocessor.Result;
import com.dfsek.terra.api.config.meta.Meta;
import com.dfsek.terra.api.util.generic.pair.Pair;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class MetaListLikePreprocessor extends MetaPreprocessor<Meta> {
   public MetaListLikePreprocessor(Map<String, Configuration> configs) {
      super(configs);
   }

   @NotNull
   public <T> Result<T> process(AnnotatedType t, T c, ConfigLoader loader, Meta annotation, DepthTracker depthTracker) {
      if (t.getType() instanceof ParameterizedType parameterizedType
         && parameterizedType.getRawType() instanceof Class<?> baseClass
         && (List.class.isAssignableFrom(baseClass) || Set.class.isAssignableFrom(baseClass))
         && c instanceof List<Object> list) {
         int offset = 0;
         List<Object> newList = new ArrayList<>((List)c);

         for (int i = 0; i < list.size(); i++) {
            Object o = list.get(i);
            if (o instanceof String) {
               String s = ((String)o).trim();
               if (s.startsWith("<< ")) {
                  String meta = s.substring(3);
                  Pair<Configuration, Object> pair = this.getMetaValue(meta, depthTracker);
                  Object metaValue = pair.getRight();
                  if (!(metaValue instanceof List<Object> metaList)) {
                     throw new LoadException(
                        "Meta list / set injection (via <<) must point to a list. '" + meta + "' points to type " + metaValue.getClass().getCanonicalName(),
                        depthTracker
                     );
                  }

                  newList.remove(i + offset);
                  newList.addAll(i + offset, metaList);
                  int begin = i + offset;
                  offset += metaList.size() - 1;
                  int end = i + offset;
                  depthTracker.addIntrinsicLevel(level -> {
                     if (level instanceof IndexLevel indexLevel && indexLevel.getIndex() >= begin && indexLevel.getIndex() <= end) {
                        String configName;
                        if (pair.getLeft().getName() == null) {
                           configName = "Anonymous Configuration";
                        } else {
                           configName = pair.getLeft().getName();
                        }

                        return Optional.of("From configuration \"" + configName + "\"");
                     } else {
                        return Optional.empty();
                     }
                  });
               }
            }
         }

         return Result.overwrite((T)newList, depthTracker);
      } else {
         return Result.noOp();
      }
   }
}
