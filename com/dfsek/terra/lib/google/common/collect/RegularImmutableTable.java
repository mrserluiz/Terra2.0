package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;

@GwtCompatible
abstract class RegularImmutableTable<R, C, V> extends ImmutableTable<R, C, V> {
   abstract Table.Cell<R, C, V> getCell(int iterationIndex);

   @Override
   final ImmutableSet<Table.Cell<R, C, V>> createCellSet() {
      return this.isEmpty() ? ImmutableSet.of() : new RegularImmutableTable.CellSet();
   }

   abstract V getValue(int iterationIndex);

   @Override
   final ImmutableCollection<V> createValues() {
      return this.isEmpty() ? ImmutableList.of() : new RegularImmutableTable.Values();
   }

   static <R, C, V> RegularImmutableTable<R, C, V> forCells(
      List<Table.Cell<R, C, V>> cells, @Nullable Comparator<? super R> rowComparator, @Nullable Comparator<? super C> columnComparator
   ) {
      Preconditions.checkNotNull(cells);
      if (rowComparator != null || columnComparator != null) {
         Comparator<Table.Cell<R, C, V>> comparator = (cell1, cell2) -> {
            int rowCompare = rowComparator == null ? 0 : rowComparator.compare(cell1.getRowKey(), cell2.getRowKey());
            if (rowCompare != 0) {
               return rowCompare;
            } else {
               return columnComparator == null ? 0 : columnComparator.compare(cell1.getColumnKey(), cell2.getColumnKey());
            }
         };
         Collections.sort(cells, comparator);
      }

      return forCellsInternal(cells, rowComparator, columnComparator);
   }

   static <R, C, V> RegularImmutableTable<R, C, V> forCells(Iterable<Table.Cell<R, C, V>> cells) {
      return forCellsInternal(cells, null, null);
   }

   private static <R, C, V> RegularImmutableTable<R, C, V> forCellsInternal(
      Iterable<Table.Cell<R, C, V>> cells, @Nullable Comparator<? super R> rowComparator, @Nullable Comparator<? super C> columnComparator
   ) {
      Set<R> rowSpaceBuilder = new LinkedHashSet<>();
      Set<C> columnSpaceBuilder = new LinkedHashSet<>();
      ImmutableList<Table.Cell<R, C, V>> cellList = ImmutableList.copyOf(cells);

      for (Table.Cell<R, C, V> cell : cells) {
         rowSpaceBuilder.add(cell.getRowKey());
         columnSpaceBuilder.add(cell.getColumnKey());
      }

      ImmutableSet<R> rowSpace = rowComparator == null
         ? ImmutableSet.copyOf(rowSpaceBuilder)
         : ImmutableSet.copyOf(ImmutableList.sortedCopyOf(rowComparator, rowSpaceBuilder));
      ImmutableSet<C> columnSpace = columnComparator == null
         ? ImmutableSet.copyOf(columnSpaceBuilder)
         : ImmutableSet.copyOf(ImmutableList.sortedCopyOf(columnComparator, columnSpaceBuilder));
      return forOrderedComponents(cellList, rowSpace, columnSpace);
   }

   static <R, C, V> RegularImmutableTable<R, C, V> forOrderedComponents(
      ImmutableList<Table.Cell<R, C, V>> cellList, ImmutableSet<R> rowSpace, ImmutableSet<C> columnSpace
   ) {
      return cellList.size() > (long)rowSpace.size() * columnSpace.size() / 2L
         ? new DenseImmutableTable<>(cellList, rowSpace, columnSpace)
         : new SparseImmutableTable<>(cellList, rowSpace, columnSpace);
   }

   final void checkNoDuplicate(R rowKey, C columnKey, @Nullable V existingValue, V newValue) {
      Preconditions.checkArgument(existingValue == null, "Duplicate key: (row=%s, column=%s), values: [%s, %s].", rowKey, columnKey, newValue, existingValue);
   }

   @J2ktIncompatible
   @GwtIncompatible
   @Override
   abstract Object writeReplace();

   private final class CellSet extends IndexedImmutableSet<Table.Cell<R, C, V>> {
      private CellSet() {
      }

      @Override
      public int size() {
         return RegularImmutableTable.this.size();
      }

      Table.Cell<R, C, V> get(int index) {
         return RegularImmutableTable.this.getCell(index);
      }

      @Override
      public boolean contains(@Nullable Object object) {
         if (!(object instanceof Table.Cell)) {
            return false;
         }

         Table.Cell<?, ?, ?> cell = (Table.Cell<?, ?, ?>)object;
         Object value = RegularImmutableTable.this.get(cell.getRowKey(), cell.getColumnKey());
         return value != null && value.equals(cell.getValue());
      }

      @Override
      boolean isPartialView() {
         return false;
      }

      @J2ktIncompatible
      @GwtIncompatible
      @Override
      Object writeReplace() {
         return super.writeReplace();
      }
   }

   private final class Values extends ImmutableList<V> {
      private Values() {
      }

      @Override
      public int size() {
         return RegularImmutableTable.this.size();
      }

      @Override
      public V get(int index) {
         return RegularImmutableTable.this.getValue(index);
      }

      @Override
      boolean isPartialView() {
         return true;
      }

      @J2ktIncompatible
      @GwtIncompatible
      @Override
      Object writeReplace() {
         return super.writeReplace();
      }
   }
}
