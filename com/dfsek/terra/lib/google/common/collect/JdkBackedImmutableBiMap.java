package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.google.j2objc.annotations.RetainedWith;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
final class JdkBackedImmutableBiMap<K, V> extends ImmutableBiMap<K, V> {
   private final transient ImmutableList<Entry<K, V>> entries;
   private final Map<K, V> forwardDelegate;
   private final Map<V, K> backwardDelegate;
   @LazyInit
   @RetainedWith
   private transient @Nullable JdkBackedImmutableBiMap<V, K> inverse;

   static <K, V> ImmutableBiMap<K, V> create(int n, @Nullable Entry<K, V>[] entryArray) {
      Map<K, V> forwardDelegate = Maps.newHashMapWithExpectedSize(n);
      Map<V, K> backwardDelegate = Maps.newHashMapWithExpectedSize(n);

      for (int i = 0; i < n; i++) {
         Entry<K, V> e = RegularImmutableMap.makeImmutable(Objects.requireNonNull(entryArray[i]));
         entryArray[i] = e;
         V oldValue = forwardDelegate.putIfAbsent(e.getKey(), e.getValue());
         if (oldValue != null) {
            throw conflictException("key", e.getKey() + "=" + oldValue, entryArray[i]);
         }

         K oldKey = backwardDelegate.putIfAbsent(e.getValue(), e.getKey());
         if (oldKey != null) {
            throw conflictException("value", oldKey + "=" + e.getValue(), entryArray[i]);
         }
      }

      ImmutableList<Entry<K, V>> entryList = ImmutableList.asImmutableList(entryArray, n);
      return new JdkBackedImmutableBiMap<>(entryList, forwardDelegate, backwardDelegate);
   }

   private JdkBackedImmutableBiMap(ImmutableList<Entry<K, V>> entries, Map<K, V> forwardDelegate, Map<V, K> backwardDelegate) {
      this.entries = entries;
      this.forwardDelegate = forwardDelegate;
      this.backwardDelegate = backwardDelegate;
   }

   @Override
   public int size() {
      return this.entries.size();
   }

   @Override
   public ImmutableBiMap<V, K> inverse() {
      JdkBackedImmutableBiMap<V, K> result = this.inverse;
      if (result == null) {
         this.inverse = result = new JdkBackedImmutableBiMap<>(new JdkBackedImmutableBiMap.InverseEntries(), this.backwardDelegate, this.forwardDelegate);
         result.inverse = this;
      }

      return result;
   }

   @Override
   public @Nullable V get(@Nullable Object key) {
      return this.forwardDelegate.get(key);
   }

   @Override
   ImmutableSet<Entry<K, V>> createEntrySet() {
      return new ImmutableMapEntrySet.RegularEntrySet<>(this, this.entries);
   }

   @Override
   ImmutableSet<K> createKeySet() {
      return new ImmutableMapKeySet<>(this);
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

   private final class InverseEntries extends ImmutableList<Entry<V, K>> {
      private InverseEntries() {
      }

      public Entry<V, K> get(int index) {
         Entry<K, V> entry = JdkBackedImmutableBiMap.this.entries.get(index);
         return Maps.immutableEntry(entry.getValue(), entry.getKey());
      }

      @Override
      boolean isPartialView() {
         return false;
      }

      @Override
      public int size() {
         return JdkBackedImmutableBiMap.this.entries.size();
      }

      @J2ktIncompatible
      @GwtIncompatible
      @Override
      Object writeReplace() {
         return super.writeReplace();
      }
   }
}
