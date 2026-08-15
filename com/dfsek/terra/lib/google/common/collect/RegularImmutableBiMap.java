package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.google.j2objc.annotations.RetainedWith;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

@GwtCompatible(serializable = true, emulated = true)
class RegularImmutableBiMap<K, V> extends ImmutableBiMap<K, V> {
   static final RegularImmutableBiMap<Object, Object> EMPTY = new RegularImmutableBiMap<>(
      null, null, (Entry<Object, Object>[])ImmutableMap.EMPTY_ENTRY_ARRAY, 0, 0
   );
   static final double MAX_LOAD_FACTOR = 1.2;
   private final transient @Nullable ImmutableMapEntry<K, V> @Nullable [] keyTable;
   private final transient @Nullable ImmutableMapEntry<K, V> @Nullable [] valueTable;
   @VisibleForTesting
   final transient Entry<K, V>[] entries;
   private final transient int mask;
   private final transient int hashCode;
   @LazyInit
   @RetainedWith
   private transient @Nullable ImmutableBiMap<V, K> inverse;

   static <K, V> ImmutableBiMap<K, V> fromEntries(Entry<K, V>... entries) {
      return fromEntryArray(entries.length, entries);
   }

   static <K, V> ImmutableBiMap<K, V> fromEntryArray(int n, Entry<K, V>[] entryArray) {
      Preconditions.checkPositionIndex(n, entryArray.length);
      int tableSize = Hashing.closedTableSize(n, 1.2);
      int mask = tableSize - 1;
      ImmutableMapEntry<K, V>[] keyTable = ImmutableMapEntry.createEntryArray(tableSize);
      ImmutableMapEntry<K, V>[] valueTable = ImmutableMapEntry.createEntryArray(tableSize);
      Entry<K, V>[] entries = n == entryArray.length ? entryArray : ImmutableMapEntry.createEntryArray(n);
      int hashCode = 0;

      for (int i = 0; i < n; i++) {
         Entry<K, V> entry = Objects.requireNonNull(entryArray[i]);
         K key = entry.getKey();
         V value = entry.getValue();
         CollectPreconditions.checkEntryNotNull(key, value);
         int keyHash = key.hashCode();
         int valueHash = value.hashCode();
         int keyBucket = Hashing.smear(keyHash) & mask;
         int valueBucket = Hashing.smear(valueHash) & mask;
         ImmutableMapEntry<K, V> nextInKeyBucket = keyTable[keyBucket];
         ImmutableMapEntry<K, V> nextInValueBucket = valueTable[valueBucket];

         try {
            RegularImmutableMap.checkNoConflictInKeyBucket(key, value, nextInKeyBucket, true);
            checkNoConflictInValueBucket(value, entry, nextInValueBucket);
         } catch (RegularImmutableMap.BucketOverflowException e) {
            return JdkBackedImmutableBiMap.create(n, entryArray);
         }

         ImmutableMapEntry<K, V> newEntry = nextInValueBucket == null && nextInKeyBucket == null
            ? RegularImmutableMap.makeImmutable(entry, key, value)
            : new ImmutableMapEntry.NonTerminalImmutableBiMapEntry<>(key, value, nextInKeyBucket, nextInValueBucket);
         keyTable[keyBucket] = newEntry;
         valueTable[valueBucket] = newEntry;
         entries[i] = newEntry;
         hashCode += keyHash ^ valueHash;
      }

      return new RegularImmutableBiMap<>(keyTable, valueTable, entries, mask, hashCode);
   }

   private RegularImmutableBiMap(
      @Nullable ImmutableMapEntry<K, V> @Nullable [] keyTable,
      @Nullable ImmutableMapEntry<K, V> @Nullable [] valueTable,
      Entry<K, V>[] entries,
      int mask,
      int hashCode
   ) {
      this.keyTable = keyTable;
      this.valueTable = valueTable;
      this.entries = entries;
      this.mask = mask;
      this.hashCode = hashCode;
   }

   private static void checkNoConflictInValueBucket(Object value, Entry<?, ?> entry, @Nullable ImmutableMapEntry<?, ?> valueBucketHead) throws RegularImmutableMap.BucketOverflowException {
      int bucketSize = 0;

      while (valueBucketHead != null) {
         checkNoConflict(!value.equals(valueBucketHead.getValue()), "value", entry, valueBucketHead);
         if (++bucketSize > 8) {
            throw new RegularImmutableMap.BucketOverflowException();
         }

         valueBucketHead = valueBucketHead.getNextInValueBucket();
      }
   }

   @Override
   public @Nullable V get(@Nullable Object key) {
      return RegularImmutableMap.get(key, this.keyTable, this.mask);
   }

   @Override
   ImmutableSet<Entry<K, V>> createEntrySet() {
      return this.isEmpty() ? ImmutableSet.of() : new ImmutableMapEntrySet.RegularEntrySet<>(this, this.entries);
   }

   @Override
   ImmutableSet<K> createKeySet() {
      return new ImmutableMapKeySet<>(this);
   }

   @Override
   public void forEach(BiConsumer<? super K, ? super V> action) {
      Preconditions.checkNotNull(action);

      for (Entry<K, V> entry : this.entries) {
         action.accept(entry.getKey(), entry.getValue());
      }
   }

   @Override
   boolean isHashCodeFast() {
      return true;
   }

   @Override
   public int hashCode() {
      return this.hashCode;
   }

   @Override
   boolean isPartialView() {
      return false;
   }

   @Override
   public int size() {
      return this.entries.length;
   }

   @Override
   public ImmutableBiMap<V, K> inverse() {
      if (this.isEmpty()) {
         return ImmutableBiMap.of();
      }

      ImmutableBiMap<V, K> result = this.inverse;
      return result == null ? (this.inverse = new RegularImmutableBiMap.Inverse()) : result;
   }

   @J2ktIncompatible
   @GwtIncompatible
   @Override
   Object writeReplace() {
      return super.writeReplace();
   }

   private final class Inverse extends ImmutableBiMap<V, K> {
      private Inverse() {
      }

      @Override
      public int size() {
         return this.inverse().size();
      }

      @Override
      public ImmutableBiMap<K, V> inverse() {
         return RegularImmutableBiMap.this;
      }

      @Override
      public void forEach(BiConsumer<? super V, ? super K> action) {
         Preconditions.checkNotNull(action);
         RegularImmutableBiMap.this.forEach((k, v) -> action.accept(v, k));
      }

      @Override
      public @Nullable K get(@Nullable Object value) {
         if (value != null && RegularImmutableBiMap.this.valueTable != null) {
            int bucket = Hashing.smear(value.hashCode()) & RegularImmutableBiMap.this.mask;

            for (ImmutableMapEntry<K, V> entry = RegularImmutableBiMap.this.valueTable[bucket]; entry != null; entry = entry.getNextInValueBucket()) {
               if (value.equals(entry.getValue())) {
                  return entry.getKey();
               }
            }

            return null;
         } else {
            return null;
         }
      }

      @Override
      ImmutableSet<V> createKeySet() {
         return new ImmutableMapKeySet<>(this);
      }

      @Override
      ImmutableSet<Entry<V, K>> createEntrySet() {
         return new RegularImmutableBiMap.Inverse.InverseEntrySet();
      }

      @Override
      boolean isPartialView() {
         return false;
      }

      @J2ktIncompatible
      @GwtIncompatible
      @Override
      Object writeReplace() {
         return new RegularImmutableBiMap.InverseSerializedForm<>(RegularImmutableBiMap.this);
      }

      @J2ktIncompatible
      private void readObject(ObjectInputStream stream) throws InvalidObjectException {
         throw new InvalidObjectException("Use InverseSerializedForm");
      }

      final class InverseEntrySet extends ImmutableMapEntrySet<V, K> {
         @Override
         ImmutableMap<V, K> map() {
            return Inverse.this;
         }

         @Override
         boolean isHashCodeFast() {
            return true;
         }

         @Override
         public int hashCode() {
            return RegularImmutableBiMap.this.hashCode;
         }

         @Override
         public UnmodifiableIterator<Entry<V, K>> iterator() {
            return (UnmodifiableIterator<Entry<V, K>>)this.asList().iterator();
         }

         @Override
         public void forEach(Consumer<? super Entry<V, K>> action) {
            this.asList().forEach((Consumer<? super Entry<K, V>>)action);
         }

         @Override
         ImmutableList<Entry<V, K>> createAsList() {
            return new ImmutableAsList<Entry<V, K>>() {
               public Entry<V, K> get(int index) {
                  Entry<K, V> entry = RegularImmutableBiMap.this.entries[index];
                  return Maps.immutableEntry(entry.getValue(), entry.getKey());
               }

               @Override
               ImmutableCollection<Entry<V, K>> delegateCollection() {
                  return InverseEntrySet.this;
               }

               @J2ktIncompatible
               @GwtIncompatible
               @Override
               Object writeReplace() {
                  return super.writeReplace();
               }
            };
         }

         @J2ktIncompatible
         @GwtIncompatible
         @Override
         Object writeReplace() {
            return super.writeReplace();
         }
      }
   }

   @J2ktIncompatible
   private static class InverseSerializedForm<K, V> implements Serializable {
      private final ImmutableBiMap<K, V> forward;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 1L;

      InverseSerializedForm(ImmutableBiMap<K, V> forward) {
         this.forward = forward;
      }

      Object readResolve() {
         return this.forward.inverse();
      }
   }
}
