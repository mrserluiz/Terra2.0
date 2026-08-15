package org.incendo.cloud.help.result;

import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Generated;
import org.incendo.cloud.help.HelpQuery;

@ParametersAreNonnullByDefault
@CheckReturnValue
@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
@Generated(from = "VerboseCommandResult", generator = "Immutables")
@Immutable
final class VerboseCommandResultImpl<C> implements VerboseCommandResult<C> {
   private final @NonNull HelpQuery<C> query;
   private final @NonNull CommandEntry<C> entry;

   private VerboseCommandResultImpl(@NonNull HelpQuery<C> query, @NonNull CommandEntry<C> entry) {
      this.query = Objects.requireNonNull(query, "query");
      this.entry = Objects.requireNonNull(entry, "entry");
   }

   private VerboseCommandResultImpl(VerboseCommandResultImpl<C> original, @NonNull HelpQuery<C> query, @NonNull CommandEntry<C> entry) {
      this.query = query;
      this.entry = entry;
   }

   @Override
   public @NonNull HelpQuery<C> query() {
      return this.query;
   }

   @Override
   public @NonNull CommandEntry<C> entry() {
      return this.entry;
   }

   public final VerboseCommandResultImpl<C> withQuery(HelpQuery<C> value) {
      if (this.query == value) {
         return this;
      }

      HelpQuery<C> newValue = Objects.requireNonNull(value, "query");
      return new VerboseCommandResultImpl<>(this, newValue, this.entry);
   }

   public final VerboseCommandResultImpl<C> withEntry(CommandEntry<C> value) {
      if (this.entry == value) {
         return this;
      }

      CommandEntry<C> newValue = Objects.requireNonNull(value, "entry");
      return new VerboseCommandResultImpl<>(this, this.query, newValue);
   }

   @Override
   public boolean equals(@Nullable Object another) {
      return this == another ? true : another instanceof VerboseCommandResultImpl && this.equalTo(0, (VerboseCommandResultImpl<?>)another);
   }

   private boolean equalTo(int synthetic, VerboseCommandResultImpl<?> another) {
      return this.query.equals(another.query) && this.entry.equals(another.entry);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + this.query.hashCode();
      return h + (h << 5) + this.entry.hashCode();
   }

   @Override
   public String toString() {
      return "VerboseCommandResult{query=" + this.query + ", entry=" + this.entry + "}";
   }

   public static <C> VerboseCommandResultImpl<C> of(@NonNull HelpQuery<C> query, @NonNull CommandEntry<C> entry) {
      return new VerboseCommandResultImpl<>(query, entry);
   }

   public static <C> VerboseCommandResultImpl<C> copyOf(VerboseCommandResult<C> instance) {
      return instance instanceof VerboseCommandResultImpl ? (VerboseCommandResultImpl)instance : of(instance.query(), instance.entry());
   }
}
