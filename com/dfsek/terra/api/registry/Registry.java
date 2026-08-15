package com.dfsek.terra.api.registry;

import com.dfsek.tectonic.api.loader.type.TypeLoader;
import com.dfsek.terra.api.registry.key.RegistryKey;
import com.dfsek.terra.api.util.reflection.TypeKey;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface Registry<T> extends TypeLoader<T> {
   @Contract(pure = true)
   Optional<T> get(@NotNull RegistryKey var1);

   @Contract(pure = true)
   boolean contains(@NotNull RegistryKey var1);

   void forEach(@NotNull Consumer<T> var1);

   void forEach(@NotNull BiConsumer<RegistryKey, T> var1);

   @NotNull
   @Contract(pure = true)
   Collection<T> entries();

   @NotNull
   @Contract(pure = true)
   Set<RegistryKey> keys();

   TypeKey<T> getType();

   default Class<? super T> getRawType() {
      return this.getType().getRawType();
   }

   default Optional<T> getByID(String id) {
      return this.getByID(
         id,
         map -> {
            if (map.isEmpty()) {
               return Optional.empty();
            } else if (map.size() == 1) {
               return map.values().stream().findFirst();
            } else {
               throw new IllegalArgumentException(
                  "ID \"" + id + "\" is ambiguous; matches: " + map.keySet().stream().map(RegistryKey::toString).reduce("", (a, b) -> a + "\n - " + b)
               );
            }
         }
      );
   }

   default Collection<T> getAllWithID(String id) {
      return this.getMatches(id).values();
   }

   Map<RegistryKey, T> getMatches(String var1);

   default Optional<T> getByID(String attempt, Function<Map<RegistryKey, T>, Optional<T>> reduction) {
      return attempt.contains(":") ? this.get(RegistryKey.parse(attempt)) : reduction.apply(this.getMatches(attempt));
   }
}
