package com.dfsek.terra.registry;

import com.dfsek.tectonic.api.depth.DepthTracker;
import com.dfsek.tectonic.api.exception.LoadException;
import com.dfsek.tectonic.api.loader.ConfigLoader;
import com.dfsek.terra.api.registry.Registry;
import com.dfsek.terra.api.registry.key.RegistryKey;
import com.dfsek.terra.api.util.reflection.TypeKey;
import java.lang.reflect.AnnotatedType;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

public class LockedRegistryImpl<T> implements Registry<T> {
   private final Registry<T> registry;

   public LockedRegistryImpl(Registry<T> registry) {
      this.registry = registry;
   }

   @Override
   public Optional<T> get(@NotNull RegistryKey key) {
      return this.registry.get(key);
   }

   @Override
   public boolean contains(@NotNull RegistryKey key) {
      return this.registry.contains(key);
   }

   @Override
   public void forEach(@NotNull Consumer<T> consumer) {
      this.registry.forEach(consumer);
   }

   @Override
   public void forEach(@NotNull BiConsumer<RegistryKey, T> consumer) {
      this.registry.forEach(consumer);
   }

   @NotNull
   @Override
   public Collection<T> entries() {
      return this.registry.entries();
   }

   @NotNull
   @Override
   public Set<RegistryKey> keys() {
      return this.registry.keys();
   }

   @Override
   public TypeKey<T> getType() {
      return this.registry.getType();
   }

   @Override
   public Map<RegistryKey, T> getMatches(String id) {
      return this.registry.getMatches(id);
   }

   @Override
   public T load(@NotNull AnnotatedType t, @NotNull Object c, @NotNull ConfigLoader loader, DepthTracker depthTracker) throws LoadException {
      return this.registry.load(t, c, loader, depthTracker);
   }
}
