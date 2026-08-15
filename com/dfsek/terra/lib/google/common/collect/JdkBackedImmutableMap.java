package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
final class JdkBackedImmutableMap<K, V> extends ImmutableMap<K, V> {
   private final transient Map<K, V> delegateMap;
   private final transient ImmutableList<Entry<K, V>> entries;

   static <K, V> ImmutableMap<K, V> create(int n, Entry<K, V>[] entryArray, boolean throwIfDuplicateKeys) {
      Map<K, V> delegateMap = Maps.newHashMapWithExpectedSize(n);
      Map<K, V> duplicates = null;
      int dupCount = 0;

      for (int i = 0; i < n; i++) {
         entryArray[i] = RegularImmutableMap.makeImmutable(Objects.requireNonNull(entryArray[i]));
         K key = entryArray[i].getKey();
         V value = entryArray[i].getValue();
         V oldValue = delegateMap.put(key, value);
         if (oldValue != null) {
            if (throwIfDuplicateKeys) {
               throw conflictException("key", entryArray[i], entryArray[i].getKey() + "=" + oldValue);
            }

            if (duplicates == null) {
               duplicates = new HashMap<>();
            }

            duplicates.put(key, value);
            dupCount++;
         }
      }

      if (duplicates != null) {
         Entry<K, V>[] newEntryArray = new Entry[n - dupCount];
         int inI = 0;
         int outI = 0;

         while (inI < n) {
            label35: {
               Entry<K, V> entry = Objects.requireNonNull(entryArray[inI]);
               K key = entry.getKey();
               if (duplicates.containsKey(key)) {
                  V value = duplicates.get(key);
                  if (value == null) {
                     break label35;
                  }

                  entry = new ImmutableMapEntry<>(key, value);
                  duplicates.put(key, null);
               }

               newEntryArray[outI++] = entry;
            }

            inI++;
         }

         entryArray = newEntryArray;
      }

      return new JdkBackedImmutableMap<>(delegateMap, ImmutableList.asImmutableList(entryArray, n));
   }

   JdkBackedImmutableMap(Map<K, V> delegateMap, ImmutableList<Entry<K, V>> entries) {
      this.delegateMap = delegateMap;
      this.entries = entries;
   }

   @Override
   public int size() {
      return this.entries.size();
   }

   @Override
   public @Nullable V get(@Nullable Object key) {
      return this.delegateMap.get(key);
   }

   @Override
   ImmutableSet<Entry<K, V>> createEntrySet() {
      return new ImmutableMapEntrySet.RegularEntrySet<>(this, this.entries);
   }

   @Override
   public void forEach(BiConsumer<? super K, ? super V> action) {
      Preconditions.checkNotNull(action);
      this.entries.forEach(e -> action.accept(e.getKey(), e.getValue()));
   }

   @Override
   ImmutableSet<K> createKeySet() {
      return new ImmutableMapKeySet<>(this);
   }

   @Override
   ImmutableCollection<V> createValues() {
      return new ImmutableMapValues<>(this);
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
