package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class ForwardingTable<R, C, V> extends ForwardingObject implements Table<R, C, V> {
   protected ForwardingTable() {
   }

   protected abstract Table<R, C, V> delegate();

   @Override
   public Set<Table.Cell<R, C, V>> cellSet() {
      return this.delegate().cellSet();
   }

   @Override
   public void clear() {
      this.delegate().clear();
   }

   @Override
   public Map<R, V> column(@ParametricNullness C columnKey) {
      return this.delegate().column(columnKey);
   }

   @Override
   public Set<C> columnKeySet() {
      return this.delegate().columnKeySet();
   }

   @Override
   public Map<C, Map<R, V>> columnMap() {
      return this.delegate().columnMap();
   }

   @Override
   public boolean contains(@Nullable Object rowKey, @Nullable Object columnKey) {
      return this.delegate().contains(rowKey, columnKey);
   }

   @Override
   public boolean containsColumn(@Nullable Object columnKey) {
      return this.delegate().containsColumn(columnKey);
   }

   @Override
   public boolean containsRow(@Nullable Object rowKey) {
      return this.delegate().containsRow(rowKey);
   }

   @Override
   public boolean containsValue(@Nullable Object value) {
      return this.delegate().containsValue(value);
   }

   @Override
   public @Nullable V get(@Nullable Object rowKey, @Nullable Object columnKey) {
      return this.delegate().get(rowKey, columnKey);
   }

   @Override
   public boolean isEmpty() {
      return this.delegate().isEmpty();
   }

   @CanIgnoreReturnValue
   @Override
   public @Nullable V put(@ParametricNullness R rowKey, @ParametricNullness C columnKey, @ParametricNullness V value) {
      return this.delegate().put(rowKey, columnKey, value);
   }

   @Override
   public void putAll(Table<? extends R, ? extends C, ? extends V> table) {
      this.delegate().putAll(table);
   }

   @CanIgnoreReturnValue
   @Override
   public @Nullable V remove(@Nullable Object rowKey, @Nullable Object columnKey) {
      return this.delegate().remove(rowKey, columnKey);
   }

   @Override
   public Map<C, V> row(@ParametricNullness R rowKey) {
      return this.delegate().row(rowKey);
   }

   @Override
   public Set<R> rowKeySet() {
      return this.delegate().rowKeySet();
   }

   @Override
   public Map<R, Map<C, V>> rowMap() {
      return this.delegate().rowMap();
   }

   @Override
   public int size() {
      return this.delegate().size();
   }

   @Override
   public Collection<V> values() {
      return this.delegate().values();
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      return obj == this || this.delegate().equals(obj);
   }

   @Override
   public int hashCode() {
      return this.delegate().hashCode();
   }
}
