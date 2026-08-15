package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public final class MoreCollectors {
   private static final Collector<Object, ?, Optional<Object>> TO_OPTIONAL = Collector.of(
      MoreCollectors.ToOptionalState::new,
      MoreCollectors.ToOptionalState::add,
      MoreCollectors.ToOptionalState::combine,
      MoreCollectors.ToOptionalState::getOptional,
      Characteristics.UNORDERED
   );
   private static final Object NULL_PLACEHOLDER = new Object();
   private static final Collector<@Nullable Object, ?, @Nullable Object> ONLY_ELEMENT = Collector.of(
      MoreCollectors.ToOptionalState::new, (state, o) -> state.add(o == null ? NULL_PLACEHOLDER : o), MoreCollectors.ToOptionalState::combine, state -> {
         Object result = state.getElement();
         return result == NULL_PLACEHOLDER ? null : result;
      }, Characteristics.UNORDERED
   );

   public static <T> Collector<T, ?, Optional<T>> toOptional() {
      return (Collector<T, ?, Optional<T>>)TO_OPTIONAL;
   }

   public static <T> Collector<T, ?, T> onlyElement() {
      return (Collector<T, ?, T>)ONLY_ELEMENT;
   }

   private MoreCollectors() {
   }

   private static final class ToOptionalState {
      static final int MAX_EXTRAS = 4;
      @Nullable Object element = null;
      List<Object> extras = Collections.emptyList();

      ToOptionalState() {
      }

      IllegalArgumentException multiples(boolean overflow) {
         StringBuilder sb = new StringBuilder().append("expected one element but was: <").append(this.element);

         for (Object o : this.extras) {
            sb.append(", ").append(o);
         }

         if (overflow) {
            sb.append(", ...");
         }

         sb.append('>');
         throw new IllegalArgumentException(sb.toString());
      }

      void add(Object o) {
         Preconditions.checkNotNull(o);
         if (this.element == null) {
            this.element = o;
         } else if (this.extras.isEmpty()) {
            this.extras = new ArrayList<>(4);
            this.extras.add(o);
         } else {
            if (this.extras.size() >= 4) {
               throw this.multiples(true);
            }

            this.extras.add(o);
         }
      }

      MoreCollectors.ToOptionalState combine(MoreCollectors.ToOptionalState other) {
         if (this.element == null) {
            return other;
         }

         if (other.element == null) {
            return this;
         }

         if (this.extras.isEmpty()) {
            this.extras = new ArrayList<>();
         }

         this.extras.add(other.element);
         this.extras.addAll(other.extras);
         if (this.extras.size() > 4) {
            this.extras.subList(4, this.extras.size()).clear();
            throw this.multiples(true);
         } else {
            return this;
         }
      }

      Optional<Object> getOptional() {
         if (this.extras.isEmpty()) {
            return Optional.ofNullable(this.element);
         } else {
            throw this.multiples(false);
         }
      }

      Object getElement() {
         if (this.element == null) {
            throw new NoSuchElementException();
         } else if (this.extras.isEmpty()) {
            return this.element;
         } else {
            throw this.multiples(false);
         }
      }
   }
}
