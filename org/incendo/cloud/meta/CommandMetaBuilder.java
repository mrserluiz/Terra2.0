package org.incendo.cloud.meta;

import java.util.HashMap;
import java.util.Map;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.incendo.cloud.key.CloudKey;

@API(status = Status.STABLE)
public class CommandMetaBuilder {
   private final Map<CloudKey<?>, Object> map = new HashMap<>();

   CommandMetaBuilder() {
   }

   public @This @NonNull CommandMetaBuilder with(final @NonNull CommandMeta commandMeta) {
      this.map.putAll(commandMeta.all());
      return this;
   }

   public <V> @This @NonNull CommandMetaBuilder with(final @NonNull CloudKey<V> key, final @NonNull V value) {
      this.map.put(key, value);
      return this;
   }

   public @NonNull CommandMeta build() {
      return new SimpleCommandMeta(this.map);
   }
}
