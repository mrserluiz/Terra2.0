package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Objects;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotCall;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Map.Entry;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
public final class ArrayTable<R, C, V> extends AbstractTable<R, C, V> implements Serializable {
   private final ImmutableList<R> rowList;
   private final ImmutableList<C> columnList;
   private final ImmutableMap<R, Integer> rowKeyToIndex;
   private final ImmutableMap<C, Integer> columnKeyToIndex;
   private final @Nullable V[][] array;
   @LazyInit
   private transient ArrayTable.@Nullable ColumnMap columnMap;
   @LazyInit
   private transient ArrayTable.@Nullable RowMap rowMap;
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 0L;

   public static <R, C, V> ArrayTable<R, C, V> create(Iterable<? extends R> rowKeys, Iterable<? extends C> columnKeys) {
      return new ArrayTable<>(rowKeys, columnKeys);
   }

   public static <R, C, V> ArrayTable<R, C, V> create(Table<R, C, ? extends @Nullable V> table) {
      return table instanceof ArrayTable ? new ArrayTable<>((ArrayTable<R, C, V>)table) : new ArrayTable<>(table);
   }

   private ArrayTable(Iterable<? extends R> rowKeys, Iterable<? extends C> columnKeys) {
      this.rowList = ImmutableList.copyOf(rowKeys);
      this.columnList = ImmutableList.copyOf(columnKeys);
      Preconditions.checkArgument(this.rowList.isEmpty() == this.columnList.isEmpty());
      this.rowKeyToIndex = Maps.indexMap(this.rowList);
      this.columnKeyToIndex = Maps.indexMap(this.columnList);
      V[][] tmpArray = (V[][])(new Object[this.rowList.size()][this.columnList.size()]);
      this.array = tmpArray;
      this.eraseAll();
   }

   private ArrayTable(Table<R, C, ? extends @Nullable V> table) {
      this(table.rowKeySet(), table.columnKeySet());
      this.putAll(table);
   }

   private ArrayTable(ArrayTable<R, C, V> table) {
      this.rowList = table.rowList;
      this.columnList = table.columnList;
      this.rowKeyToIndex = table.rowKeyToIndex;
      this.columnKeyToIndex = table.columnKeyToIndex;
      V[][] copy = (V[][])(new Object[this.rowList.size()][this.columnList.size()]);
      this.array = copy;

      for (int i = 0; i < this.rowList.size(); i++) {
         System.arraycopy(table.array[i], 0, copy[i], 0, table.array[i].length);
      }
   }

   public ImmutableList<R> rowKeyList() {
      return this.rowList;
   }

   public ImmutableList<C> columnKeyList() {
      return this.columnList;
   }

   public @Nullable V at(int rowIndex, int columnIndex) {
      Preconditions.checkElementIndex(rowIndex, this.rowList.size());
      Preconditions.checkElementIndex(columnIndex, this.columnList.size());
      return this.array[rowIndex][columnIndex];
   }

   @CanIgnoreReturnValue
   public @Nullable V set(int rowIndex, int columnIndex, @Nullable V value) {
      Preconditions.checkElementIndex(rowIndex, this.rowList.size());
      Preconditions.checkElementIndex(columnIndex, this.columnList.size());
      V oldValue = this.array[rowIndex][columnIndex];
      this.array[rowIndex][columnIndex] = value;
      return oldValue;
   }

   @GwtIncompatible
   public V[][] toArray(Class<V> valueClass) {
      V[][] copy = (V[][])((Object[][])Array.newInstance(valueClass, this.rowList.size(), this.columnList.size()));

      for (int i = 0; i < this.rowList.size(); i++) {
         System.arraycopy(this.array[i], 0, copy[i], 0, this.array[i].length);
      }

      return copy;
   }

   @Deprecated
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public void clear() {
      throw new UnsupportedOperationException();
   }

   public void eraseAll() {
      for (V[] row : this.array) {
         Arrays.fill(row, null);
      }
   }

   @Override
   public boolean contains(@Nullable Object rowKey, @Nullable Object columnKey) {
      return this.containsRow(rowKey) && this.containsColumn(columnKey);
   }

   @Override
   public boolean containsColumn(@Nullable Object columnKey) {
      return this.columnKeyToIndex.containsKey(columnKey);
   }

   @Override
   public boolean containsRow(@Nullable Object rowKey) {
      return this.rowKeyToIndex.containsKey(rowKey);
   }

   @Override
   public boolean containsValue(Object value) {
      for (V[] row : this.array) {
         for (V element : row) {
            if (Objects.equal(value, element)) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public @Nullable V get(@Nullable Object rowKey, @Nullable Object columnKey) {
      Integer rowIndex = this.rowKeyToIndex.get(rowKey);
      Integer columnIndex = this.columnKeyToIndex.get(columnKey);
      return rowIndex != null && columnIndex != null ? this.at(rowIndex, columnIndex) : null;
   }

   @Override
   public boolean isEmpty() {
      return this.rowList.isEmpty() || this.columnList.isEmpty();
   }

   @CanIgnoreReturnValue
   @Override
   public @Nullable V put(R rowKey, C columnKey, @Nullable V value) {
      Preconditions.checkNotNull(rowKey);
      Preconditions.checkNotNull(columnKey);
      Integer rowIndex = this.rowKeyToIndex.get(rowKey);
      Preconditions.checkArgument(rowIndex != null, "Row %s not in %s", rowKey, this.rowList);
      Integer columnIndex = this.columnKeyToIndex.get(columnKey);
      Preconditions.checkArgument(columnIndex != null, "Column %s not in %s", columnKey, this.columnList);
      return this.set(rowIndex, columnIndex, value);
   }

   @Override
   public void putAll(Table<? extends R, ? extends C, ? extends @Nullable V> table) {
      super.putAll(table);
   }

   @Deprecated
   @DoNotCall("Always throws UnsupportedOperationException")
   @CanIgnoreReturnValue
   @Override
   public @Nullable V remove(@Nullable Object rowKey, @Nullable Object columnKey) {
      throw new UnsupportedOperationException();
   }

   @CanIgnoreReturnValue
   public @Nullable V erase(@Nullable Object rowKey, @Nullable Object columnKey) {
      Integer rowIndex = this.rowKeyToIndex.get(rowKey);
      Integer columnIndex = this.columnKeyToIndex.get(columnKey);
      return rowIndex != null && columnIndex != null ? this.set(rowIndex, columnIndex, null) : null;
   }

   @Override
   public int size() {
      return this.rowList.size() * this.columnList.size();
   }

   @Override
   public Set<Table.Cell<R, C, @Nullable V>> cellSet() {
      return super.cellSet();
   }

   @Override
   Iterator<Table.Cell<R, C, @Nullable V>> cellIterator() {
      return new AbstractIndexedListIterator<Table.Cell<R, C, V>>(this.size()) {
         protected Table.Cell<R, C, @Nullable V> get(int index) {
            return ArrayTable.this.getCell(index);
         }
      };
   }

   @Override
   Spliterator<Table.Cell<R, C, V>> cellSpliterator() {
      return CollectSpliterators.indexed(this.size(), 273, this::getCell);
   }

   private Table.Cell<R, C, @Nullable V> getCell(int index) {
      return new Tables.AbstractCell<R, C, V>() {
         final int rowIndex = index / ArrayTable.this.columnList.size();
         final int columnIndex = index % ArrayTable.this.columnList.size();

         @Override
         public R getRowKey() {
            return ArrayTable.this.rowList.get(this.rowIndex);
         }

         @Override
         public C getColumnKey() {
            return ArrayTable.this.columnList.get(this.columnIndex);
         }

         @Override
         public @Nullable V getValue() {
            return (V)ArrayTable.this.at(this.rowIndex, this.columnIndex);
         }
      };
   }

   private @Nullable V getValue(int index) {
      int rowIndex = index / this.columnList.size();
      int columnIndex = index % this.columnList.size();
      return this.at(rowIndex, columnIndex);
   }

   @Override
   public Map<R, @Nullable V> column(C columnKey) {
      Preconditions.checkNotNull(columnKey);
      Integer columnIndex = this.columnKeyToIndex.get(columnKey);
      return columnIndex == null ? Collections.emptyMap() : new ArrayTable.Column(columnIndex);
   }

   public ImmutableSet<C> columnKeySet() {
      return this.columnKeyToIndex.keySet();
   }

   @Override
   public Map<C, Map<R, @Nullable V>> columnMap() {
      ArrayTable<R, C, V>.ColumnMap map = this.columnMap;
      return map == null ? (this.columnMap = new ArrayTable.ColumnMap()) : map;
   }

   @Override
   public Map<C, @Nullable V> row(R rowKey) {
      Preconditions.checkNotNull(rowKey);
      Integer rowIndex = this.rowKeyToIndex.get(rowKey);
      return rowIndex == null ? Collections.emptyMap() : new ArrayTable.Row(rowIndex);
   }

   public ImmutableSet<R> rowKeySet() {
      return this.rowKeyToIndex.keySet();
   }

   @Override
   public Map<R, Map<C, @Nullable V>> rowMap() {
      ArrayTable<R, C, V>.RowMap map = this.rowMap;
      return map == null ? (this.rowMap = new ArrayTable.RowMap()) : map;
   }

   @Override
   public Collection<@Nullable V> values() {
      return super.values();
   }

   @Override
   Iterator<@Nullable V> valuesIterator() {
      return new AbstractIndexedListIterator<V>(this.size()) {
         @Override
         protected @Nullable V get(int index) {
            return (V)ArrayTable.this.getValue(index);
         }
      };
   }

   @Override
   Spliterator<V> valuesSpliterator() {
      return CollectSpliterators.indexed(this.size(), 16, this::getValue);
   }

   private abstract static class ArrayMap<K, V> extends Maps.IteratorBasedAbstractMap<K, V> {
      private final ImmutableMap<K, Integer> keyIndex;

      private ArrayMap(ImmutableMap<K, Integer> keyIndex) {
         this.keyIndex = keyIndex;
      }

      @Override
      public Set<K> keySet() {
         return this.keyIndex.keySet();
      }

      K getKey(int index) {
         return this.keyIndex.keySet().asList().get(index);
      }

      abstract String getKeyRole();

      @ParametricNullness
      abstract V getValue(int index);

      @ParametricNullness
      abstract V setValue(int index, @ParametricNullness V newValue);

      @Override
      public int size() {
         return this.keyIndex.size();
      }

      @Override
      public boolean isEmpty() {
         return this.keyIndex.isEmpty();
      }

      Entry<K, V> getEntry(int index) {
         Preconditions.checkElementIndex(index, this.size());
         return new AbstractMapEntry<K, V>() {
            @Override
            public K getKey() {
               return (K)ArrayMap.this.getKey(index);
            }

            @ParametricNullness
            @Override
            public V getValue() {
               return (V)ArrayMap.this.getValue(index);
            }

            @ParametricNullness
            @Override
            public V setValue(@ParametricNullness V value) {
               return (V)ArrayMap.this.setValue(index, value);
            }
         };
      }

      @Override
      Iterator<Entry<K, V>> entryIterator() {
         return new AbstractIndexedListIterator<Entry<K, V>>(this.size()) {
            protected Entry<K, V> get(int index) {
               return ArrayMap.this.getEntry(index);
            }
         };
      }

      @Override
      Spliterator<Entry<K, V>> entrySpliterator() {
         return CollectSpliterators.indexed(this.size(), 16, this::getEntry);
      }

      @Override
      public boolean containsKey(@Nullable Object key) {
         return this.keyIndex.containsKey(key);
      }

      @Override
      public @Nullable V get(@Nullable Object key) {
         Integer index = this.keyIndex.get(key);
         return index == null ? null : this.getValue(index);
      }

      @Override
      public @Nullable V put(K key, @ParametricNullness V value) {
         Integer index = this.keyIndex.get(key);
         if (index == null) {
            throw new IllegalArgumentException(this.getKeyRole() + " " + key + " not in " + this.keyIndex.keySet());
         } else {
            return this.setValue(index, value);
         }
      }

      @Override
      public @Nullable V remove(@Nullable Object key) {
         throw new UnsupportedOperationException();
      }

      @Override
      public void clear() {
         throw new UnsupportedOperationException();
      }
   }

   private class Column extends ArrayTable.ArrayMap<R, V> {
      final int columnIndex;

      Column(int columnIndex) {
         super(ArrayTable.this.rowKeyToIndex);
         this.columnIndex = columnIndex;
      }

      @Override
      String getKeyRole() {
         return "Row";
      }

      @Override
      @Nullable V getValue(int index) {
         return ArrayTable.this.at(index, this.columnIndex);
      }

      @Override
      @Nullable V setValue(int index, @Nullable V newValue) {
         return ArrayTable.this.set(index, this.columnIndex, newValue);
      }
   }

   private class ColumnMap extends ArrayTable.ArrayMap<C, Map<R, V>> {
      private ColumnMap() {
         super(ArrayTable.this.columnKeyToIndex);
      }

      @Override
      String getKeyRole() {
         return "Column";
      }

      Map<R, @Nullable V> getValue(int index) {
         return ArrayTable.this.new Column(index);
      }

      Map<R, @Nullable V> setValue(int index, Map<R, @Nullable V> newValue) {
         throw new UnsupportedOperationException();
      }

      public @Nullable Map<R, @Nullable V> put(C key, Map<R, @Nullable V> value) {
         throw new UnsupportedOperationException();
      }
   }

   private class Row extends ArrayTable.ArrayMap<C, V> {
      final int rowIndex;

      Row(int rowIndex) {
         super(ArrayTable.this.columnKeyToIndex);
         this.rowIndex = rowIndex;
      }

      @Override
      String getKeyRole() {
         return "Column";
      }

      @Override
      @Nullable V getValue(int index) {
         return ArrayTable.this.at(this.rowIndex, index);
      }

      @Override
      @Nullable V setValue(int index, @Nullable V newValue) {
         return ArrayTable.this.set(this.rowIndex, index, newValue);
      }
   }

   private class RowMap extends ArrayTable.ArrayMap<R, Map<C, V>> {
      private RowMap() {
         super(ArrayTable.this.rowKeyToIndex);
      }

      @Override
      String getKeyRole() {
         return "Row";
      }

      Map<C, @Nullable V> getValue(int index) {
         return ArrayTable.this.new Row(index);
      }

      Map<C, @Nullable V> setValue(int index, Map<C, @Nullable V> newValue) {
         throw new UnsupportedOperationException();
      }

      public @Nullable Map<C, @Nullable V> put(R key, Map<C, @Nullable V> value) {
         throw new UnsupportedOperationException();
      }
   }
}
