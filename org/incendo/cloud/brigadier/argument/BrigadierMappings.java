package org.incendo.cloud.brigadier.argument;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.parser.ArgumentParser;

@API(status = Status.INTERNAL, since = "2.0.0")
public interface BrigadierMappings<C, S> {
   static <C, S> @NonNull BrigadierMappings<C, S> create() {
      return new BrigadierMappingsImpl<>();
   }

   <T, K extends ArgumentParser<C, T>> @Nullable BrigadierMapping<C, K, S> mapping(@NonNull Class<K> parserType);

   default <T, K extends ArgumentParser<C, T>> void registerMapping(@NonNull Class<K> parserType, @NonNull BrigadierMapping<?, K, S> mapping) {
      this.registerMappingUnsafe(parserType, mapping);
   }

   <K extends ArgumentParser<C, ?>> void registerMappingUnsafe(@NonNull Class<K> parserType, @NonNull BrigadierMapping<?, ?, S> mapping);
}
