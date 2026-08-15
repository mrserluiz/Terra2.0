package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.base.Supplier;
import com.google.errorprone.annotations.InlineMe;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import org.jspecify.annotations.Nullable;

@GwtCompatible(serializable = true)
public class TreeBasedTable<R, C, V> extends StandardRowSortedTable<R, C, V> {
   private final Comparator<? super C> columnComparator;
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 0L;

   public static <R extends Comparable, C extends Comparable, V> TreeBasedTable<R, C, V> create() {
      return new TreeBasedTable<>(Ordering.natural(), Ordering.natural());
   }

   public static <R, C, V> TreeBasedTable<R, C, V> create(Comparator<? super R> rowComparator, Comparator<? super C> columnComparator) {
      Preconditions.checkNotNull(rowComparator);
      Preconditions.checkNotNull(columnComparator);
      return new TreeBasedTable<>(rowComparator, columnComparator);
   }

   public static <R, C, V> TreeBasedTable<R, C, V> create(TreeBasedTable<R, C, ? extends V> table) {
      TreeBasedTable<R, C, V> result = new TreeBasedTable<>(Objects.requireNonNull(table.rowKeySet().comparator()), table.columnComparator());
      result.putAll(table);
      return result;
   }

   TreeBasedTable(Comparator<? super R> rowComparator, Comparator<? super C> columnComparator) {
      super(new TreeMap<>(rowComparator), new TreeBasedTable.Factory<>(columnComparator));
      this.columnComparator = columnComparator;
   }

   @Deprecated
   @InlineMe(replacement = "requireNonNull(this.rowKeySet().comparator())", staticImports = "java.util.Objects.requireNonNull")
   public final Comparator<? super R> rowComparator() {
      return Objects.requireNonNull(this.rowKeySet().comparator());
   }

   @Deprecated
   public Comparator<? super C> columnComparator() {
      return this.columnComparator;
   }

   public SortedMap<C, V> row(R rowKey) {
      return new TreeBasedTable.TreeRow(rowKey);
   }

   @Override
   Iterator<C> createColumnKeyIterator() {
      final Comparator<? super C> comparator = this.columnComparator();
      final Iterator<C> merged = Iterators.mergeSorted(Iterables.transform(this.backingMap.values(), input -> input.keySet().iterator()), comparator);
      return new AbstractIterator<C>() {
         @Nullable Object lastValue;

         @Override
         protected @Nullable C computeNext() {
            while (merged.hasNext()) {
               C next = merged.next();
               boolean duplicate = this.lastValue != null && comparator.compare(next, (C)this.lastValue) == 0;
               if (!duplicate) {
                  this.lastValue = next;
                  return (C)this.lastValue;
               }
            }

            this.lastValue = null;
            return (C)this.endOfData();
         }
      };
   }

   private static class Factory<C, V> implements Supplier<Map<C, V>>, Serializable {
      final Comparator<? super C> comparator;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      Factory(Comparator<? super C> comparator) {
         this.comparator = comparator;
      }

      public Map<C, V> get() {
         return new TreeMap<>(this.comparator);
      }
   }

   private class TreeRow extends StandardTable<R, C, V>.Row implements SortedMap<C, V> {
      final @Nullable Object lowerBound;
      final @Nullable Object upperBound;
      transient @Nullable SortedMap<C, V> wholeRow;

      TreeRow(R rowKey) {
         this(rowKey, null, null);
      }

      TreeRow(R rowKey, @Nullable C lowerBound, @Nullable C upperBound) {
         super(rowKey);
         this.lowerBound = lowerBound;
         this.upperBound = upperBound;
         Preconditions.checkArgument(lowerBound == null || upperBound == null || this.compare(lowerBound, upperBound) <= 0);
      }

      public SortedSet<C> keySet() {
         return new Maps.SortedKeySet<>(this);
      }

      @Override
      public Comparator<? super C> comparator() {
         return TreeBasedTable.this.columnComparator();
      }

      int compare(Object a, Object b) {
         Comparator<Object> cmp = this.comparator();
         return cmp.compare(a, b);
      }

      boolean rangeContains(@Nullable Object o) {
         return o != null
            && (this.lowerBound == null || this.compare(this.lowerBound, o) <= 0)
            && (this.upperBound == null || this.compare(this.upperBound, o) > 0);
      }

      @Override
      public SortedMap<C, V> subMap(C fromKey, C toKey) {
         Preconditions.checkArgument(this.rangeContains(Preconditions.checkNotNull(fromKey)) && this.rangeContains(Preconditions.checkNotNull(toKey)));
         return TreeBasedTable.this.new TreeRow(this.rowKey, fromKey, toKey);
      }

      @Override
      public SortedMap<C, V> headMap(C toKey) {
         Preconditions.checkArgument(this.rangeContains(Preconditions.checkNotNull(toKey)));
         return TreeBasedTable.this.new TreeRow(this.rowKey, this.lowerBound, toKey);
      }

      @Override
      public SortedMap<C, V> tailMap(C fromKey) {
         Preconditions.checkArgument(this.rangeContains(Preconditions.checkNotNull(fromKey)));
         return TreeBasedTable.this.new TreeRow(this.rowKey, fromKey, this.upperBound);
      }

      @Override
      public C firstKey() {
         this.updateBackingRowMapField();
         if (this.backingRowMap == null) {
            throw new NoSuchElementException();
         } else {
            return ((SortedMap)this.backingRowMap).firstKey();
         }
      }

      @Override
      public C lastKey() {
         this.updateBackingRowMapField();
         if (this.backingRowMap == null) {
            throw new NoSuchElementException();
         } else {
            return ((SortedMap)this.backingRowMap).lastKey();
         }
      }

      void updateWholeRowField() {
         if (this.wholeRow == null || this.wholeRow.isEmpty() && TreeBasedTable.this.backingMap.containsKey(this.rowKey)) {
            this.wholeRow = (SortedMap<C, V>)TreeBasedTable.this.backingMap.get(this.rowKey);
         }
      }

      @Nullable SortedMap<C, V> computeBackingRowMap() {
         this.updateWholeRowField();
         SortedMap<C, V> map = this.wholeRow;
         if (map != null) {
            if (this.lowerBound != null) {
               map = map.tailMap((C)this.lowerBound);
            }

            if (this.upperBound != null) {
               map = map.headMap((C)this.upperBound);
            }

            return map;
         } else {
            return null;
         }
      }

      @Override
      void maintainEmptyInvariant() {
         this.updateWholeRowField();
         if (this.wholeRow != null && this.wholeRow.isEmpty()) {
            TreeBasedTable.this.backingMap.remove(this.rowKey);
            this.wholeRow = null;
            this.backingRowMap = null;
         }
      }

      @Override
      public boolean containsKey(@Nullable Object key) {
         return this.rangeContains(key) && super.containsKey(key);
      }

      @Override
      public @Nullable V put(C key, V value) {
         Preconditions.checkArgument(this.rangeContains(Preconditions.checkNotNull(key)));
         return (V)super.put((V)key, value);
      }
   }
}
