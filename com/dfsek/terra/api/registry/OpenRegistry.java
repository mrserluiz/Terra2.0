package com.dfsek.terra.api.registry;

import com.dfsek.terra.api.registry.exception.DuplicateEntryException;
import com.dfsek.terra.api.registry.key.Keyed;
import com.dfsek.terra.api.registry.key.RegistryKey;
import org.jetbrains.annotations.NotNull;

public interface OpenRegistry<T> extends Registry<T> {
   boolean register(@NotNull RegistryKey var1, @NotNull T var2);

   default boolean register(@NotNull Keyed<? extends T> value) {
      return this.register(value.getRegistryKey(), (T)value);
   }

   void registerChecked(@NotNull RegistryKey var1, @NotNull T var2) throws DuplicateEntryException;

   default void registerChecked(@NotNull Keyed<? extends T> value) {
      this.registerChecked(value.getRegistryKey(), (T)value);
   }

   void clear();
}
