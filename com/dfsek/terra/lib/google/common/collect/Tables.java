package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Objects;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public final class Tables {
   private Tables() {
   }

   public static <T, R, C, V, I extends Table<R, C, V>> Collector<T, ?, I> toTable(
      Function<? super T, ? extends R> rowFunction,
      Function<? super T, ? extends C> columnFunction,
      Function<? super T, ? extends V> valueFunction,
      Supplier<I> tableSupplier
   ) {
      return TableCollectors.toTable(rowFunction, columnFunction, valueFunction, tableSupplier);
   }

   public static <T, R, C, V, I extends Table<R, C, V>> Collector<T, ?, I> toTable(
      Function<? super T, ? extends R> rowFunction,
      Function<? super T, ? extends C> columnFunction,
      Function<? super T, ? extends V> valueFunction,
      BinaryOperator<V> mergeFunction,
      Supplier<I> tableSupplier
   ) {
      return TableCollectors.toTable(rowFunction, columnFunction, valueFunction, mergeFunction, tableSupplier);
   }

   public static <R, C, V> Table.Cell<R, C, V> immutableCell(@ParametricNullness R rowKey, @ParametricNullness C columnKey, @ParametricNullness V value) {
      return new Tables.ImmutableCell<>(rowKey, columnKey, value);
   }

   public static <R, C, V> Table<C, R, V> transpose(Table<R, C, V> table) {
      return table instanceof Tables.TransposeTable ? ((Tables.TransposeTable)table).original : new Tables.TransposeTable<>(table);
   }

   private static <R, C, V> Table.Cell<C, R, V> transposeCell(Table.Cell<R, C, V> cell) {
      return immutableCell(cell.getColumnKey(), cell.getRowKey(), cell.getValue());
   }

   public static <R, C, V> Table<R, C, V> newCustomTable(
      Map<R, Map<C, V>> backingMap, com.dfsek.terra.lib.google.common.base.Supplier<? extends Map<C, V>> factory
   ) {
      Preconditions.checkArgument(backingMap.isEmpty());
      Preconditions.checkNotNull(factory);
      return new StandardTable<>(backingMap, factory);
   }

   public static <R, C, V1, V2> Table<R, C, V2> transformValues(
      Table<R, C, V1> fromTable, com.dfsek.terra.lib.google.common.base.Function<? super V1, V2> function
   ) {
      return new Tables.TransformedTable<>(fromTable, function);
   }

   public static <R, C, V> Table<R, C, V> unmodifiableTable(Table<? extends R, ? extends C, ? extends V> table) {
      return new Tables.UnmodifiableTable<>(table);
   }

   public static <R, C, V> RowSortedTable<R, C, V> unmodifiableRowSortedTable(RowSortedTable<R, ? extends C, ? extends V> table) {
      return new Tables.UnmodifiableRowSortedMap<>(table);
   }

   @J2ktIncompatible
   public static <R, C, V> Table<R, C, V> synchronizedTable(Table<R, C, V> table) {
      return Synchronized.table(table, null);
   }

   static boolean equalsImpl(Table<?, ?, ?> table, @Nullable Object obj) {
      if (obj == table) {
         return true;
      } else if (obj instanceof Table) {
         Table<?, ?, ?> that = (Table<?, ?, ?>)obj;
         return table.cellSet().equals(that.cellSet());
      } else {
         return false;
      }
   }

   abstract static class AbstractCell<R, C, V> implements Table.Cell<R, C, V> {
      @Override
      public boolean equals(@Nullable Object obj) {
         if (obj == this) {
            return true;
         }

         if (!(obj instanceof Table.Cell)) {
            return false;
         }

         Table.Cell<?, ?, ?> other = (Table.Cell<?, ?, ?>)obj;
         return Objects.equal(this.getRowKey(), other.getRowKey())
            && Objects.equal(this.getColumnKey(), other.getColumnKey())
            && Objects.equal(this.getValue(), other.getValue());
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.getRowKey(), this.getColumnKey(), this.getValue());
      }

      @Override
      public String toString() {
         return "(" + this.getRowKey() + "," + this.getColumnKey() + ")=" + this.getValue();
      }
   }

   static final class ImmutableCell<R, C, V> extends Tables.AbstractCell<R, C, V> implements Serializable {
      @ParametricNullness
      private final R rowKey;
      @ParametricNullness
      private final C columnKey;
      @ParametricNullness
      private final V value;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      ImmutableCell(@ParametricNullness R rowKey, @ParametricNullness C columnKey, @ParametricNullness V value) {
         this.rowKey = rowKey;
         this.columnKey = columnKey;
         this.value = value;
      }

      @ParametricNullness
      @Override
      public R getRowKey() {
         return this.rowKey;
      }

      @ParametricNullness
      @Override
      public C getColumnKey() {
         return this.columnKey;
      }

      @ParametricNullness
      @Override
      public V getValue() {
         return this.value;
      }
   }

   private static class TransformedTable<R, C, V1, V2> extends AbstractTable<R, C, V2> {
      final Table<R, C, V1> fromTable;
      final com.dfsek.terra.lib.google.common.base.Function<? super V1, V2> function;

      TransformedTable(Table<R, C, V1> fromTable, com.dfsek.terra.lib.google.common.base.Function<? super V1, V2> function) {
         this.fromTable = Preconditions.checkNotNull(fromTable);
         this.function = Preconditions.checkNotNull(function);
      }

      @Override
      public boolean contains(@Nullable Object rowKey, @Nullable Object columnKey) {
         return this.fromTable.contains(rowKey, columnKey);
      }

      @Override
      public @Nullable V2 get(@Nullable Object rowKey, @Nullable Object columnKey) {
         return this.contains(rowKey, columnKey) ? this.function.apply(NullnessCasts.uncheckedCastNullableTToT(this.fromTable.get(rowKey, columnKey))) : null;
      }

      @Override
      public int size() {
         return this.fromTable.size();
      }

      @Override
      public void clear() {
         this.fromTable.clear();
      }

      @Override
      public @Nullable V2 put(@ParametricNullness R rowKey, @ParametricNullness C columnKey, @ParametricNullness V2 value) {
         throw new UnsupportedOperationException();
      }

      @Override
      public void putAll(Table<? extends R, ? extends C, ? extends V2> table) {
         throw new UnsupportedOperationException();
      }

      @Override
      public @Nullable V2 remove(@Nullable Object rowKey, @Nullable Object columnKey) {
         return this.contains(rowKey, columnKey)
            ? this.function.apply(NullnessCasts.uncheckedCastNullableTToT(this.fromTable.remove(rowKey, columnKey)))
            : null;
      }

      @Override
      public Map<C, V2> row(@ParametricNullness R rowKey) {
         return Maps.transformValues(this.fromTable.row(rowKey), this.function);
      }

      @Override
      public Map<R, V2> column(@ParametricNullness C columnKey) {
         return Maps.transformValues(this.fromTable.column(columnKey), this.function);
      }

      Table.Cell<R, C, V2> applyToValue(Table.Cell<R, C, V1> cell) {
         return Tables.immutableCell(cell.getRowKey(), cell.getColumnKey(), this.function.apply(cell.getValue()));
      }

      @Override
      Iterator<Table.Cell<R, C, V2>> cellIterator() {
         return Iterators.transform(this.fromTable.cellSet().iterator(), this::applyToValue);
      }

      @Override
      Spliterator<Table.Cell<R, C, V2>> cellSpliterator() {
         return CollectSpliterators.map(this.fromTable.cellSet().spliterator(), this::applyToValue);
      }

      @Override
      public Set<R> rowKeySet() {
         return this.fromTable.rowKeySet();
      }

      @Override
      public Set<C> columnKeySet() {
         return this.fromTable.columnKeySet();
      }

      @Override
      Collection<V2> createValues() {
         return Collections2.transform(this.fromTable.values(), this.function);
      }

      @Override
      public Map<R, Map<C, V2>> rowMap() {
         return Maps.transformValues(this.fromTable.rowMap(), row -> Maps.transformValues((Map<C, V1>)row, this.function));
      }

      @Override
      public Map<C, Map<R, V2>> columnMap() {
         return Maps.transformValues(this.fromTable.columnMap(), column -> Maps.transformValues((Map<R, V1>)column, this.function));
      }
   }

   private static class TransposeTable<C, R, V> extends AbstractTable<C, R, V> {
      final Table<R, C, V> original;

      TransposeTable(Table<R, C, V> original) {
         this.original = Preconditions.checkNotNull(original);
      }

      @Override
      public void clear() {
         this.original.clear();
      }

      @Override
      public Map<C, V> column(@ParametricNullness R columnKey) {
         return this.original.row(columnKey);
      }

      @Override
      public Set<R> columnKeySet() {
         return this.original.rowKeySet();
      }

      @Override
      public Map<R, Map<C, V>> columnMap() {
         return this.original.rowMap();
      }

      @Override
      public boolean contains(@Nullable Object rowKey, @Nullable Object columnKey) {
         return this.original.contains(columnKey, rowKey);
      }

      @Override
      public boolean containsColumn(@Nullable Object columnKey) {
         return this.original.containsRow(columnKey);
      }

      @Override
      public boolean containsRow(@Nullable Object rowKey) {
         return this.original.containsColumn(rowKey);
      }

      @Override
      public boolean containsValue(@Nullable Object value) {
         return this.original.containsValue(value);
      }

      @Override
      public @Nullable V get(@Nullable Object rowKey, @Nullable Object columnKey) {
         return this.original.get(columnKey, rowKey);
      }

      @Override
      public @Nullable V put(@ParametricNullness C rowKey, @ParametricNullness R columnKey, @ParametricNullness V value) {
         return this.original.put(columnKey, rowKey, value);
      }

      @Override
      public void putAll(Table<? extends C, ? extends R, ? extends V> table) {
         this.original.putAll(Tables.transpose(table));
      }

      @Override
      public @Nullable V remove(@Nullable Object rowKey, @Nullable Object columnKey) {
         return this.original.remove(columnKey, rowKey);
      }

      @Override
      public Map<R, V> row(@ParametricNullness C rowKey) {
         return this.original.column(rowKey);
      }

      @Override
      public Set<C> rowKeySet() {
         return this.original.columnKeySet();
      }

      @Override
      public Map<C, Map<R, V>> rowMap() {
         return this.original.columnMap();
      }

      @Override
      public int size() {
         return this.original.size();
      }

      @Override
      public Collection<V> values() {
         return this.original.values();
      }

      @Override
      Iterator<Table.Cell<C, R, V>> cellIterator() {
         return Iterators.transform(this.original.cellSet().iterator(), x$0 -> Tables.transposeCell((Table.Cell<R, C, V>)x$0));
      }

      @Override
      Spliterator<Table.Cell<C, R, V>> cellSpliterator() {
         return CollectSpliterators.map(this.original.cellSet().spliterator(), x$0 -> Tables.transposeCell((Table.Cell<R, C, V>)x$0));
      }
   }

   private static final class UnmodifiableRowSortedMap<R, C, V> extends Tables.UnmodifiableTable<R, C, V> implements RowSortedTable<R, C, V> {
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      public UnmodifiableRowSortedMap(RowSortedTable<R, ? extends C, ? extends V> delegate) {
         super(delegate);
      }

      protected RowSortedTable<R, C, V> delegate() {
         return (RowSortedTable<R, C, V>)super.delegate();
      }

      @Override
      public SortedMap<R, Map<C, V>> rowMap() {
         return Collections.unmodifiableSortedMap(Maps.transformValues(this.delegate().rowMap(), Collections::unmodifiableMap));
      }

      @Override
      public SortedSet<R> rowKeySet() {
         return Collections.unmodifiableSortedSet(this.delegate().rowKeySet());
      }
   }

   private static class UnmodifiableTable<R, C, V> extends ForwardingTable<R, C, V> implements Serializable {
      final Table<? extends R, ? extends C, ? extends V> delegate;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      UnmodifiableTable(Table<? extends R, ? extends C, ? extends V> delegate) {
         this.delegate = Preconditions.checkNotNull(delegate);
      }

      @Override
      protected Table<R, C, V> delegate() {
         return (Table<R, C, V>)this.delegate;
      }

      @Override
      public Set<Table.Cell<R, C, V>> cellSet() {
         return Collections.unmodifiableSet(super.cellSet());
      }

      @Override
      public void clear() {
         throw new UnsupportedOperationException();
      }

      @Override
      public Map<R, V> column(@ParametricNullness C columnKey) {
         return Collections.unmodifiableMap(super.column(columnKey));
      }

      @Override
      public Set<C> columnKeySet() {
         return Collections.unmodifiableSet(super.columnKeySet());
      }

      @Override
      public Map<C, Map<R, V>> columnMap() {
         return Collections.unmodifiableMap(Maps.transformValues(super.columnMap(), Collections::unmodifiableMap));
      }

      @Override
      public @Nullable V put(@ParametricNullness R rowKey, @ParametricNullness C columnKey, @ParametricNullness V value) {
         throw new UnsupportedOperationException();
      }

      @Override
      public void putAll(Table<? extends R, ? extends C, ? extends V> table) {
         throw new UnsupportedOperationException();
      }

      @Override
      public @Nullable V remove(@Nullable Object rowKey, @Nullable Object columnKey) {
         throw new UnsupportedOperationException();
      }

      @Override
      public Map<C, V> row(@ParametricNullness R rowKey) {
         return Collections.unmodifiableMap(super.row(rowKey));
      }

      @Override
      public Set<R> rowKeySet() {
         return Collections.unmodifiableSet(super.rowKeySet());
      }

      @Override
      public Map<R, Map<C, V>> rowMap() {
         return Collections.unmodifiableMap(Maps.transformValues(super.rowMap(), Collections::unmodifiableMap));
      }

      @Override
      public Collection<V> values() {
         return Collections.unmodifiableCollection(super.values());
      }
   }
}
