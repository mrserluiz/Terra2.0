package com.dfsek.terra.api.inject;

import com.dfsek.terra.api.inject.exception.InjectionException;
import com.dfsek.terra.api.inject.impl.InjectorImpl;

public interface Injector<T> {
   static <T1> Injector<T1> get(T1 value) {
      return new InjectorImpl<>(value);
   }

   void addExplicitTarget(Class<? extends T> var1);

   void inject(Object var1) throws InjectionException;
}
