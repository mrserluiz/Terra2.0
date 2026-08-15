package com.dfsek.terra.api.registry.key;

public interface Keyed<T extends Keyed<T>> extends Namespaced, StringIdentifiable {
   RegistryKey getRegistryKey();

   @Override
   default String getNamespace() {
      return this.getRegistryKey().getNamespace();
   }

   @Override
   default String getID() {
      return this.getRegistryKey().getID();
   }
}
