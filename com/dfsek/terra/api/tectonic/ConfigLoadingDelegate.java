package com.dfsek.terra.api.tectonic;

import com.dfsek.tectonic.api.config.template.object.ObjectTemplate;
import com.dfsek.tectonic.api.loader.type.TypeLoader;
import java.lang.reflect.Type;
import java.util.function.Supplier;

public interface ConfigLoadingDelegate {
   <T> ConfigLoadingDelegate applyLoader(Type var1, TypeLoader<T> var2);

   default <T> ConfigLoadingDelegate applyLoader(Class<? extends T> type, TypeLoader<T> loader) {
      return this.applyLoader((Type)type, loader);
   }

   <T> ConfigLoadingDelegate applyLoader(Type var1, Supplier<ObjectTemplate<T>> var2);

   default <T> ConfigLoadingDelegate applyLoader(Class<? extends T> type, Supplier<ObjectTemplate<T>> loader) {
      return this.applyLoader((Type)type, loader);
   }
}
