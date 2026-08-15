package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.Serializable;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import org.jspecify.annotations.Nullable;

@GwtCompatible(serializable = true, emulated = true)
final class RegularImmutableMap<K, V> extends ImmutableMap<K, V> {
   static final ImmutableMap<Object, Object> EMPTY = new RegularImmutableMap<>((Entry<Object, Object>[])ImmutableMap.EMPTY_ENTRY_ARRAY, null, 0);
   @VisibleForTesting
   static final double MAX_LOAD_FACTOR = 1.2;
   @VisibleForTesting
   static final double HASH_FLOODING_FPP = 0.001;
   static final int MAX_HASH_BUCKET_LENGTH = 8;
   @VisibleForTesting
   final transient Entry<K, V>[] entries;
   private final transient @Nullable ImmutableMapEntry<K, V> @Nullable [] table;
   private final transient int mask;
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 0L;

   static <K, V> ImmutableMap<K, V> fromEntries(Entry<K, V>... entries) {
      return fromEntryArray(entries.length, entries, true);
   }

   static <K, V> ImmutableMap<K, V> fromEntryArray(int n, @Nullable Entry<K, V>[] entryArray, boolean throwIfDuplicateKeys) {
      Preconditions.checkPositionIndex(n, entryArray.length);
      if (n == 0) {
         return (ImmutableMap<K, V>)EMPTY;
      }

      try {
         return fromEntryArrayCheckingBucketOverflow(n, entryArray, throwIfDuplicateKeys);
      } catch (RegularImmutableMap.BucketOverflowException e) {
         return JdkBackedImmutableMap.create(n, entryArray, throwIfDuplicateKeys);
      }
   }

   private static <K, V> ImmutableMap<K, V> fromEntryArrayCheckingBucketOverflow(int n, Entry<K, V>[] entryArray, boolean throwIfDuplicateKeys) throws RegularImmutableMap.BucketOverflowException {
      Entry<K, V>[] entries = n == entryArray.length ? entryArray : ImmutableMapEntry.createEntryArray(n);
      int tableSize = Hashing.closedTableSize(n, 1.2);
      ImmutableMapEntry<K, V>[] table = ImmutableMapEntry.createEntryArray(tableSize);
      int mask = tableSize - 1;
      IdentityHashMap<Entry<K, V>, Boolean> duplicates = null;
      int dupCount = 0;

      for (int entryIndex = n - 1; entryIndex >= 0; entryIndex--) {
         Entry<K, V> entry = Objects.requireNonNull(entryArray[entryIndex]);
         K key = entry.getKey();
         V value = entry.getValue();
         CollectPreconditions.checkEntryNotNull(key, value);
         int tableIndex = Hashing.smear(key.hashCode()) & mask;
         ImmutableMapEntry<K, V> keyBucketHead = table[tableIndex];
         ImmutableMapEntry<K, V> effectiveEntry = checkNoConflictInKeyBucket(key, value, keyBucketHead, throwIfDuplicateKeys);
         if (effectiveEntry == null) {
            effectiveEntry = keyBucketHead == null
               ? makeImmutable(entry, key, value)
               : new ImmutableMapEntry.NonTerminalImmutableMapEntry<>(key, value, keyBucketHead);
            table[tableIndex] = effectiveEntry;
         } else {
            if (duplicates == null) {
               duplicates = new IdentityHashMap<>();
            }

            duplicates.put(effectiveEntry, true);
            dupCount++;
            if (entries == entryArray) {
               Entry<K, V>[] originalEntries = entries;
               entries = (Entry<K, V>[])originalEntries.clone();
            }
         }

         entries[entryIndex] = effectiveEntry;
      }

      if (duplicates != null) {
         entries = removeDuplicates(entries, n, n - dupCount, duplicates);
         int newTableSize = Hashing.closedTableSize(entries.length, 1.2);
         if (newTableSize != tableSize) {
            return fromEntryArrayCheckingBucketOverflow(entries.length, entries, true);
         }
      }

      return new RegularImmutableMap<>(entries, table, mask);
   }

   static <K, V> Entry<K, V>[] removeDuplicates(Entry<K, V>[] entries, int n, int newN, IdentityHashMap<Entry<K, V>, Boolean> duplicates) {
      Entry<K, V>[] newEntries = ImmutableMapEntry.createEntryArray(newN);
      int in = 0;
      int out = 0;

      while (in < n) {
         label18: {
            Entry<K, V> entry = entries[in];
            Boolean status = duplicates.get(entry);
            if (status != null) {
               if (!status) {
                  break label18;
               }

               duplicates.put(entry, false);
            }

            newEntries[out++] = entry;
         }

         in++;
      }

      return newEntries;
   }

   static <K, V> ImmutableMapEntry<K, V> makeImmutable(Entry<K, V> entry, K key, V value) {
      boolean reusable = entry instanceof ImmutableMapEntry && ((ImmutableMapEntry)entry).isReusable();
      return reusable ? (ImmutableMapEntry)entry : new ImmutableMapEntry<>(key, value);
   }

   static <K, V> ImmutableMapEntry<K, V> makeImmutable(Entry<K, V> entry) {
      return makeImmutable(entry, entry.getKey(), entry.getValue());
   }

   private RegularImmutableMap(Entry<K, V>[] entries, @Nullable ImmutableMapEntry<K, V> @Nullable [] table, int mask) {
      this.entries = entries;
      this.table = table;
      this.mask = mask;
   }

   @CanIgnoreReturnValue
   static <K, V> @Nullable ImmutableMapEntry<K, V> checkNoConflictInKeyBucket(
      Object key, Object newValue, @Nullable ImmutableMapEntry<K, V> keyBucketHead, boolean throwIfDuplicateKeys
   ) throws RegularImmutableMap.BucketOverflowException {
      int bucketSize = 0;

      while (keyBucketHead != null) {
         if (keyBucketHead.getKey().equals(key)) {
            if (!throwIfDuplicateKeys) {
               return keyBucketHead;
            }

            checkNoConflict(false, "key", keyBucketHead, key + "=" + newValue);
         }

         if (++bucketSize > 8) {
            throw new RegularImmutableMap.BucketOverflowException();
         }

         keyBucketHead = keyBucketHead.getNextInKeyBucket();
      }

      return null;
   }

   @Override
   public @Nullable V get(@Nullable Object key) {
      return get(key, this.table, this.mask);
   }

   static <V> @Nullable V get(@Nullable Object key, @Nullable ImmutableMapEntry<?, V> @Nullable [] keyTable, int mask) {
      if (key != null && keyTable != null) {
         int index = Hashing.smear(key.hashCode()) & mask;

         for (ImmutableMapEntry<?, V> entry = keyTable[index]; entry != null; entry = entry.getNextInKeyBucket()) {
            Object candidateKey = entry.getKey();
            if (key.equals(candidateKey)) {
               return entry.getValue();
            }
         }

         return null;
      } else {
         return null;
      }
   }

   @Override
   public void forEach(BiConsumer<? super K, ? super V> action) {
      Preconditions.checkNotNull(action);

      for (Entry<K, V> entry : this.entries) {
         action.accept(entry.getKey(), entry.getValue());
      }
   }

   @Override
   public int size() {
      return this.entries.length;
   }

   @Override
   boolean isPartialView() {
      return false;
   }

   @Override
   ImmutableSet<Entry<K, V>> createEntrySet() {
      return new ImmutableMapEntrySet.RegularEntrySet<>(this, this.entries);
   }

   @Override
   ImmutableSet<K> createKeySet() {
      return new RegularImmutableMap.KeySet<>(this);
   }

   @Override
   ImmutableCollection<V> createValues() {
      return new RegularImmutableMap.Values<>(this);
   }

   @J2ktIncompatible
   @GwtIncompatible
   @Override
   Object writeReplace() {
      return super.writeReplace();
   }

   static class BucketOverflowException extends Exception {
   }

   @GwtCompatible(emulated = true)
   private static final class KeySet<K> extends IndexedImmutableSet<K> {
      private final RegularImmutableMap<K, ?> map;

      KeySet(RegularImmutableMap<K, ?> map) {
         this.map = map;
      }

      @Override
      K get(int index) {
         return this.map.entries[index].getKey();
      }

      @Override
      public boolean contains(@Nullable Object object) {
         return this.map.containsKey(object);
      }

      @Override
      boolean isPartialView() {
         return true;
      }

      @Override
      public int size() {
         return this.map.size();
      }

      @J2ktIncompatible
      @GwtIncompatible
      @Override
      Object writeReplace() {
         return super.writeReplace();
      }

      @GwtIncompatible
      @J2ktIncompatible
      private static class SerializedForm<K> implements Serializable {
         final ImmutableMap<K, ?> map;
         @GwtIncompatible
         @J2ktIncompatible
         private static final long serialVersionUID = 0L;

         SerializedForm(ImmutableMap<K, ?> map) {
            this.map = map;
         }

         Object readResolve() {
            return this.map.keySet();
         }
      }
   }

   @GwtCompatible(emulated = true)
   private static final class Values<K, V> extends ImmutableList<V> {
      final RegularImmutableMap<K, V> map;

      Values(RegularImmutableMap<K, V> map) {
         this.map = map;
      }

      @Override
      public V get(int index) {
         return this.map.entries[index].getValue();
      }

      @Override
      public int size() {
         return this.map.size();
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

      @GwtIncompatible
      @J2ktIncompatible
      private static class SerializedForm<V> implements Serializable {
         final ImmutableMap<?, V> map;
         @GwtIncompatible
         @J2ktIncompatible
         private static final long serialVersionUID = 0L;

         SerializedForm(ImmutableMap<?, V> map) {
            this.map = map;
         }

         Object readResolve() {
            return this.map.values();
         }
      }
   }
}
