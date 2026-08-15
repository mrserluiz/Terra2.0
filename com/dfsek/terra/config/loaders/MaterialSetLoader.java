package com.dfsek.terra.config.loaders;

import com.dfsek.tectonic.api.depth.DepthTracker;
import com.dfsek.tectonic.api.exception.LoadException;
import com.dfsek.tectonic.api.loader.ConfigLoader;
import com.dfsek.tectonic.api.loader.type.TypeLoader;
import com.dfsek.terra.api.block.BlockType;
import com.dfsek.terra.api.util.collection.MaterialSet;
import java.lang.reflect.AnnotatedType;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class MaterialSetLoader implements TypeLoader<MaterialSet> {
   public MaterialSet load(@NotNull AnnotatedType type, @NotNull Object o, @NotNull ConfigLoader configLoader, DepthTracker depthTracker) throws LoadException {
      List<String> stringData = (List<String>)o;
      if (stringData.size() == 1) {
         return MaterialSet.singleton(configLoader.loadType(BlockType.class, stringData.get(0), depthTracker));
      }

      MaterialSet set = new MaterialSet();

      for (String string : stringData) {
         try {
            set.add(configLoader.loadType(BlockType.class, string, depthTracker));
         } catch (NullPointerException e) {
            throw new LoadException("Invalid data identifier \"" + string + "\"", e, depthTracker);
         }
      }

      return set;
   }
}
