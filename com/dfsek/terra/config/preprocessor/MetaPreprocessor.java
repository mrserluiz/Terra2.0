package com.dfsek.terra.config.preprocessor;

import com.dfsek.tectonic.api.config.Configuration;
import com.dfsek.tectonic.api.depth.DepthTracker;
import com.dfsek.tectonic.api.exception.LoadException;
import com.dfsek.tectonic.api.preprocessor.ValuePreprocessor;
import com.dfsek.terra.api.util.generic.pair.Pair;
import java.lang.annotation.Annotation;
import java.util.Map;

public abstract class MetaPreprocessor<A extends Annotation> implements ValuePreprocessor<A> {
   private final Map<String, Configuration> configs;

   public MetaPreprocessor(Map<String, Configuration> configs) {
      this.configs = configs;
   }

   protected Pair<Configuration, Object> getMetaValue(String meta, DepthTracker depthTracker) {
      int sep = meta.indexOf(58);
      String file = meta.substring(0, sep);
      String key = meta.substring(sep + 1);
      if (!this.configs.containsKey(file)) {
         throw new LoadException("Cannot fetch metavalue: No such config: " + file, depthTracker);
      } else {
         Configuration config = this.configs.get(file);
         if (!config.contains(key)) {
            throw new LoadException("Cannot fetch metavalue: No such key " + key + " in configuration " + config.getName(), depthTracker);
         } else {
            return Pair.of(config, config.get(key));
         }
      }
   }
}
