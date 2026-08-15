package com.dfsek.terra.api.registry.meta;

import com.dfsek.terra.api.registry.Registry;
import com.dfsek.terra.api.util.reflection.TypeKey;
import java.lang.reflect.Type;

public interface RegistryHolder {
   default <T> Registry<T> getRegistry(Class<T> clazz) {
      return this.getRegistry((Type)clazz);
   }

   default <T> Registry<T> getRegistry(TypeKey<T> type) {
      return this.getRegistry(type.getType());
   }

   <T> Registry<T> getRegistry(Type var1);
}
