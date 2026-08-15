package org.incendo.cloud.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.STABLE)
public final class ParserParameters {
   private final Map<ParserParameter<?>, Object> internalMap = new HashMap<>();

   public static @NonNull ParserParameters empty() {
      return new ParserParameters();
   }

   public static <T> @NonNull ParserParameters single(final @NonNull ParserParameter<T> parameter, final @NonNull T value) {
      ParserParameters parameters = new ParserParameters();
      parameters.store(parameter, value);
      return parameters;
   }

   public <T> boolean has(final @NonNull ParserParameter<T> parameter) {
      return this.internalMap.containsKey(parameter);
   }

   public <T> void store(final @NonNull ParserParameter<T> parameter, final @NonNull T value) {
      this.internalMap.put(parameter, value);
   }

   public <T> @NonNull T get(final @NonNull ParserParameter<T> parameter, final @NonNull T defaultValue) {
      return (T)this.internalMap.getOrDefault(parameter, defaultValue);
   }

   public void merge(final @NonNull ParserParameters other) {
      this.internalMap.putAll(other.internalMap);
   }

   public @NonNull Map<@NonNull ParserParameter<?>, @NonNull Object> parameters() {
      return Collections.unmodifiableMap(this.internalMap);
   }
}
