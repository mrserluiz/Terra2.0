package org.incendo.cloud.brigadier.argument;

import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.parser.ArgumentParser;

final class BrigadierMappingsImpl<C, S> implements BrigadierMappings<C, S> {
   private final Map<Class<?>, BrigadierMapping<?, ?, S>> mappers = new HashMap<>();

   @Override
   public <T, K extends ArgumentParser<C, T>> @Nullable BrigadierMapping<C, K, S> mapping(final @NonNull Class<K> parserType) {
      BrigadierMapping<?, ?, S> mapper = this.mappers.get(parserType);
      return (BrigadierMapping<C, K, S>)(mapper == null ? null : mapper);
   }

   @Override
   public <K extends ArgumentParser<C, ?>> void registerMappingUnsafe(final @NonNull Class<K> parserType, final @NonNull BrigadierMapping<?, ?, S> mapping) {
      this.mappers.put(parserType, mapping);
   }
}
