package com.dfsek.terra.config.preprocessor;

import com.dfsek.tectonic.api.config.Configuration;
import com.dfsek.tectonic.api.depth.DepthTracker;
import com.dfsek.tectonic.api.exception.LoadException;
import com.dfsek.tectonic.api.loader.ConfigLoader;
import com.dfsek.tectonic.api.preprocessor.Result;
import com.dfsek.terra.api.config.meta.Meta;
import com.dfsek.terra.lib.commons.text.StringSubstitutor;
import java.lang.reflect.AnnotatedType;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class MetaStringPreprocessor extends MetaPreprocessor<Meta> {
   public MetaStringPreprocessor(Map<String, Configuration> configs) {
      super(configs);
   }

   @NotNull
   public <T> Result<T> process(AnnotatedType t, T c, ConfigLoader loader, Meta annotation, DepthTracker depthTracker) {
      if (String.class.equals(t.getType()) && c instanceof String candidate) {
         StringSubstitutor substitutor = new StringSubstitutor(
            key -> {
               Object meta = this.getMetaValue(key, depthTracker).getRight();
               if (!(meta instanceof String) && !(meta instanceof Number) && !(meta instanceof Character) && !(meta instanceof Boolean)) {
                  throw new LoadException(
                     "MetaString template injection candidate must be string or primitive, is type " + meta.getClass().getCanonicalName(), depthTracker
                  );
               } else {
                  return meta.toString();
               }
            }
         );
         return Result.overwrite((T)substitutor.replace(candidate), depthTracker);
      } else {
         return Result.noOp();
      }
   }
}
