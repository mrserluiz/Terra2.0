package com.dfsek.terra.config.preprocessor;

import com.dfsek.tectonic.api.config.Configuration;
import com.dfsek.tectonic.api.depth.DepthTracker;
import com.dfsek.tectonic.api.loader.ConfigLoader;
import com.dfsek.tectonic.api.preprocessor.Result;
import com.dfsek.terra.api.config.meta.Meta;
import com.dfsek.terra.api.util.generic.pair.Pair;
import java.lang.reflect.AnnotatedType;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class MetaValuePreprocessor extends MetaPreprocessor<Meta> {
   public MetaValuePreprocessor(Map<String, Configuration> configs) {
      super(configs);
   }

   @NotNull
   public <T> Result<T> process(AnnotatedType t, T c, ConfigLoader configLoader, Meta annotation, DepthTracker depthTracker) {
      if (c instanceof String) {
         String value = ((String)c).trim();
         if (value.startsWith("$") && !value.startsWith("${")) {
            Pair<Configuration, Object> pair = this.getMetaValue(value.substring(1), depthTracker);
            String configName;
            if (pair.getLeft().getName() == null) {
               configName = "Anonymous Configuration";
            } else {
               configName = pair.getLeft().getName();
            }

            return Result.overwrite((T)pair.getRight(), depthTracker.intrinsic("From configuration \"" + configName + "\""));
         }
      }

      return Result.noOp();
   }
}
