package org.incendo.cloud.parser.flag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(status = Status.STABLE)
public final class FlagContext {
   public static final Object FLAG_PRESENCE_VALUE = new Object();
   private final Map<String, List> flagValues = new HashMap<>();

   private FlagContext() {
   }

   public static @NonNull FlagContext create() {
      return new FlagContext();
   }

   public void addPresenceFlag(final @NonNull CommandFlag<?> flag) {
      ((List)this.flagValues.computeIfAbsent(flag.name(), $ -> new ArrayList())).add(FLAG_PRESENCE_VALUE);
   }

   public <T> void addValueFlag(final @NonNull CommandFlag<T> flag, final @NonNull T value) {
      ((List)this.flagValues.computeIfAbsent(flag.name(), $ -> new ArrayList())).add(value);
   }

   @API(status = Status.STABLE)
   public <T> int count(final @NonNull CommandFlag<T> flag) {
      return this.getAll(flag).size();
   }

   @API(status = Status.STABLE)
   public int count(final @NonNull String flag) {
      return this.getAll(flag).size();
   }

   public boolean isPresent(final @NonNull String flag) {
      List value = this.flagValues.get(flag);
      return value != null && !value.isEmpty();
   }

   @API(status = Status.STABLE)
   public boolean isPresent(final @NonNull CommandFlag<Void> flag) {
      return this.isPresent(flag.name());
   }

   @API(status = Status.STABLE)
   public <T> @NonNull Optional<T> getValue(final @NonNull String name) {
      List value = this.flagValues.get(name);
      return value != null && !value.isEmpty() ? Optional.of((T)value.get(0)) : Optional.empty();
   }

   @API(status = Status.STABLE)
   public <T> @NonNull Optional<T> getValue(final @NonNull CommandFlag<T> flag) {
      return this.getValue(flag.name());
   }

   public <T> @Nullable T getValue(final @NonNull String name, final @Nullable T defaultValue) {
      return this.<T>getValue(name).orElse(defaultValue);
   }

   @API(status = Status.STABLE)
   public <T> @Nullable T getValue(final @NonNull CommandFlag<T> name, final @Nullable T defaultValue) {
      return this.getValue(name).orElse(defaultValue);
   }

   @API(status = Status.STABLE)
   public boolean hasFlag(final @NonNull String name) {
      return this.getValue(name).isPresent();
   }

   @API(status = Status.STABLE)
   public boolean hasFlag(final @NonNull CommandFlag<?> flag) {
      return this.getValue(flag).isPresent();
   }

   @API(status = Status.STABLE)
   public boolean contains(final @NonNull String name) {
      return this.hasFlag(name);
   }

   @API(status = Status.STABLE)
   public boolean contains(final @NonNull CommandFlag<?> flag) {
      return this.hasFlag(flag);
   }

   @API(status = Status.STABLE)
   public <T> @Nullable T get(final @NonNull String name) {
      return this.<T>getValue(name).orElse(null);
   }

   @API(status = Status.STABLE)
   public <T> @Nullable T get(final @NonNull CommandFlag<T> flag) {
      return this.getValue(flag).orElse(null);
   }

   @API(status = Status.STABLE)
   public <T> @NonNull Collection<T> getAll(final @NonNull CommandFlag<T> flag) {
      List values = this.flagValues.get(flag.name());
      return values != null ? Collections.unmodifiableList(values) : Collections.emptyList();
   }

   @API(status = Status.STABLE)
   public <T> @NonNull Collection<T> getAll(final @NonNull String flag) {
      List values = this.flagValues.get(flag);
      return values != null ? Collections.unmodifiableList(values) : Collections.emptyList();
   }
}
