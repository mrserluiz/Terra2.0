package org.incendo.cloud.help.result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
@Generated(from = "IndexCommandResult", generator = "Immutables")
@Immutable
final class IndexCommandResultImpl<C> implements IndexCommandResult<C> {
   private final @NonNull HelpQuery<C> query;
   private final @NonNull List<CommandEntry<C>> entries;

   private IndexCommandResultImpl(@NonNull HelpQuery<C> query, Iterable<? extends CommandEntry<C>> entries) {
      this.query = Objects.requireNonNull(query, "query");
      this.entries = createUnmodifiableList(false, createSafeList(entries, true, false));
   }

   private IndexCommandResultImpl(IndexCommandResultImpl<C> original, @NonNull HelpQuery<C> query, @NonNull List<CommandEntry<C>> entries) {
      this.query = query;
      this.entries = entries;
   }

   @Override
   public @NonNull HelpQuery<C> query() {
      return this.query;
   }

   @Override
   public @NonNull List<CommandEntry<C>> entries() {
      return this.entries;
   }

   public final IndexCommandResultImpl<C> withQuery(HelpQuery<C> value) {
      if (this.query == value) {
         return this;
      }

      HelpQuery<C> newValue = Objects.requireNonNull(value, "query");
      return new IndexCommandResultImpl<>(this, newValue, this.entries);
   }

   @SafeVarargs
   public final IndexCommandResultImpl<C> withEntries(CommandEntry<C>... elements) {
      List<CommandEntry<C>> newValue = createUnmodifiableList(false, createSafeList(Arrays.asList(elements), true, false));
      return new IndexCommandResultImpl<>(this, this.query, newValue);
   }

   public final IndexCommandResultImpl<C> withEntries(Iterable<? extends CommandEntry<C>> elements) {
      if (this.entries == elements) {
         return this;
      }

      List<CommandEntry<C>> newValue = createUnmodifiableList(false, createSafeList(elements, true, false));
      return new IndexCommandResultImpl<>(this, this.query, newValue);
   }

   @Override
   public boolean equals(@Nullable Object another) {
      return this == another ? true : another instanceof IndexCommandResultImpl && this.equalTo(0, (IndexCommandResultImpl<?>)another);
   }

   private boolean equalTo(int synthetic, IndexCommandResultImpl<?> another) {
      return this.query.equals(another.query) && this.entries.equals(another.entries);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + this.query.hashCode();
      return h + (h << 5) + this.entries.hashCode();
   }

   @Override
   public String toString() {
      return "IndexCommandResult{query=" + this.query + ", entries=" + this.entries + "}";
   }

   public static <C> IndexCommandResultImpl<C> of(@NonNull HelpQuery<C> query, @NonNull List<CommandEntry<C>> entries) {
      return of(query, (Iterable<? extends CommandEntry<C>>)entries);
   }

   public static <C> IndexCommandResultImpl<C> of(@NonNull HelpQuery<C> query, Iterable<? extends CommandEntry<C>> entries) {
      return new IndexCommandResultImpl<>(query, entries);
   }

   public static <C> IndexCommandResultImpl<C> copyOf(IndexCommandResult<C> instance) {
      return instance instanceof IndexCommandResultImpl ? (IndexCommandResultImpl)instance : of(instance.query(), instance.entries());
   }

   private static <T> List<T> createSafeList(Iterable<? extends T> iterable, boolean checkNulls, boolean skipNulls) {
      ArrayList<T> list;
      if (iterable instanceof Collection) {
         int size = ((Collection)iterable).size();
         if (size == 0) {
            return Collections.emptyList();
         }

         list = new ArrayList<>(size);
      } else {
         list = new ArrayList<>();
      }

      for (T element : iterable) {
         if (!skipNulls || element != null) {
            if (checkNulls) {
               Objects.requireNonNull(element, "element");
            }

            list.add(element);
         }
      }

      return list;
   }

   private static <T> List<T> createUnmodifiableList(boolean clone, List<T> list) {
      switch (list.size()) {
         case 0:
            return Collections.emptyList();
         case 1:
            return Collections.singletonList(list.get(0));
         default:
            if (clone) {
               return Collections.unmodifiableList(new ArrayList<>(list));
            } else {
               if (list instanceof ArrayList) {
                  ((ArrayList)list).trimToSize();
               }

               return Collections.unmodifiableList(list);
            }
      }
   }
}
