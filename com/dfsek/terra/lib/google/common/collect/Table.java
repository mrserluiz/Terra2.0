package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CompatibleWith;
import com.google.errorprone.annotations.DoNotMock;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;

@DoNotMock("Use ImmutableTable, HashBasedTable, or another implementation")
@GwtCompatible
public interface Table<R, C, V> {
   boolean contains(@CompatibleWith("R") @Nullable Object rowKey, @CompatibleWith("C") @Nullable Object columnKey);

   boolean containsRow(@CompatibleWith("R") @Nullable Object rowKey);

   boolean containsColumn(@CompatibleWith("C") @Nullable Object columnKey);

   boolean containsValue(@CompatibleWith("V") @Nullable Object value);

   @Nullable V get(@CompatibleWith("R") @Nullable Object rowKey, @CompatibleWith("C") @Nullable Object columnKey);

   boolean isEmpty();

   int size();

   @Override
   boolean equals(@Nullable Object obj);

   @Override
   int hashCode();

   void clear();

   @CanIgnoreReturnValue
   @Nullable V put(@ParametricNullness R rowKey, @ParametricNullness C columnKey, @ParametricNullness V value);

   void putAll(Table<? extends R, ? extends C, ? extends V> table);

   @CanIgnoreReturnValue
   @Nullable V remove(@CompatibleWith("R") @Nullable Object rowKey, @CompatibleWith("C") @Nullable Object columnKey);

   Map<C, V> row(@ParametricNullness R rowKey);

   Map<R, V> column(@ParametricNullness C columnKey);

   Set<Table.Cell<R, C, V>> cellSet();

   Set<R> rowKeySet();

   Set<C> columnKeySet();

   Collection<V> values();

   Map<R, Map<C, V>> rowMap();

   Map<C, Map<R, V>> columnMap();

   interface Cell<R, C, V> {
      @ParametricNullness
      R getRowKey();

      @ParametricNullness
      C getColumnKey();

      @ParametricNullness
      V getValue();

      @Override
      boolean equals(@Nullable Object obj);

      @Override
      int hashCode();
   }
}
