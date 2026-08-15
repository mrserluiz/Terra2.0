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
@Generated(from = "MultipleCommandResult", generator = "Immutables")
@Immutable
final class MultipleCommandResultImpl<C> implements MultipleCommandResult<C> {
   private final @NonNull HelpQuery<C> query;
   private final @NonNull String longestPath;
   private final @NonNull List<String> childSuggestions;

   private MultipleCommandResultImpl(@NonNull HelpQuery<C> query, @NonNull String longestPath, Iterable<String> childSuggestions) {
      this.query = Objects.requireNonNull(query, "query");
      this.longestPath = Objects.requireNonNull(longestPath, "longestPath");
      this.childSuggestions = createUnmodifiableList(false, createSafeList(childSuggestions, true, false));
   }

   private MultipleCommandResultImpl(
      MultipleCommandResultImpl<C> original, @NonNull HelpQuery<C> query, @NonNull String longestPath, @NonNull List<String> childSuggestions
   ) {
      this.query = query;
      this.longestPath = longestPath;
      this.childSuggestions = childSuggestions;
   }

   @Override
   public @NonNull HelpQuery<C> query() {
      return this.query;
   }

   @Override
   public @NonNull String longestPath() {
      return this.longestPath;
   }

   @Override
   public @NonNull List<String> childSuggestions() {
      return this.childSuggestions;
   }

   public final MultipleCommandResultImpl<C> withQuery(HelpQuery<C> value) {
      if (this.query == value) {
         return this;
      }

      HelpQuery<C> newValue = Objects.requireNonNull(value, "query");
      return new MultipleCommandResultImpl<>(this, newValue, this.longestPath, this.childSuggestions);
   }

   public final MultipleCommandResultImpl<C> withLongestPath(String value) {
      String newValue = Objects.requireNonNull(value, "longestPath");
      return this.longestPath.equals(newValue) ? this : new MultipleCommandResultImpl<>(this, this.query, newValue, this.childSuggestions);
   }

   public final MultipleCommandResultImpl<C> withChildSuggestions(String... elements) {
      List<String> newValue = createUnmodifiableList(false, createSafeList(Arrays.asList(elements), true, false));
      return new MultipleCommandResultImpl<>(this, this.query, this.longestPath, newValue);
   }

   public final MultipleCommandResultImpl<C> withChildSuggestions(Iterable<String> elements) {
      if (this.childSuggestions == elements) {
         return this;
      }

      List<String> newValue = createUnmodifiableList(false, createSafeList(elements, true, false));
      return new MultipleCommandResultImpl<>(this, this.query, this.longestPath, newValue);
   }

   @Override
   public boolean equals(@Nullable Object another) {
      return this == another ? true : another instanceof MultipleCommandResultImpl && this.equalTo(0, (MultipleCommandResultImpl<?>)another);
   }

   private boolean equalTo(int synthetic, MultipleCommandResultImpl<?> another) {
      return this.query.equals(another.query) && this.longestPath.equals(another.longestPath) && this.childSuggestions.equals(another.childSuggestions);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + this.query.hashCode();
      h += (h << 5) + this.longestPath.hashCode();
      return h + (h << 5) + this.childSuggestions.hashCode();
   }

   @Override
   public String toString() {
      return "MultipleCommandResult{query=" + this.query + ", longestPath=" + this.longestPath + ", childSuggestions=" + this.childSuggestions + "}";
   }

   public static <C> MultipleCommandResultImpl<C> of(@NonNull HelpQuery<C> query, @NonNull String longestPath, @NonNull List<String> childSuggestions) {
      return of(query, longestPath, (Iterable<String>)childSuggestions);
   }

   public static <C> MultipleCommandResultImpl<C> of(@NonNull HelpQuery<C> query, @NonNull String longestPath, Iterable<String> childSuggestions) {
      return new MultipleCommandResultImpl<>(query, longestPath, childSuggestions);
   }

   public static <C> MultipleCommandResultImpl<C> copyOf(MultipleCommandResult<C> instance) {
      return instance instanceof MultipleCommandResultImpl
         ? (MultipleCommandResultImpl)instance
         : of(instance.query(), instance.longestPath(), instance.childSuggestions());
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
