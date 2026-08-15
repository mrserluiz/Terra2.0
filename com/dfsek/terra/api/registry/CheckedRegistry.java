package com.dfsek.terra.api.registry;

import com.dfsek.terra.api.registry.exception.DuplicateEntryException;
import com.dfsek.terra.api.registry.key.Keyed;
import com.dfsek.terra.api.registry.key.RegistryKey;
import org.jetbrains.annotations.NotNull;

public interface CheckedRegistry<T> extends Registry<T> {
   void register(@NotNull RegistryKey var1, @NotNull T var2) throws DuplicateEntryException;

   default void register(@NotNull Keyed<? extends T> value) throws DuplicateEntryException {
      this.register(value.getRegistryKey(), (T)value);
   }
}
