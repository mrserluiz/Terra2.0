package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import com.dfsek.terra.lib.google.common.base.Objects;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.primitives.Ints;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.util.Arrays;
import java.util.Collection;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true, serializable = true)
class RegularImmutableMultiset<E> extends ImmutableMultiset<E> {
   private static final Multisets.ImmutableEntry<?>[] EMPTY_ARRAY = new Multisets.ImmutableEntry[0];
   static final ImmutableMultiset<Object> EMPTY = create(ImmutableList.of());
   @VisibleForTesting
   static final double MAX_LOAD_FACTOR = 1.0;
   @VisibleForTesting
   static final double HASH_FLOODING_FPP = 0.001;
   @VisibleForTesting
   static final int MAX_HASH_BUCKET_LENGTH = 9;
   private final transient Multisets.ImmutableEntry<E>[] entries;
   private final transient Multisets.@Nullable ImmutableEntry<?>[] hashTable;
   private final transient int size;
   private final transient int hashCode;
   @LazyInit
   private transient @Nullable ImmutableSet<E> elementSet;

   static <E> ImmutableMultiset<E> create(Collection<? extends Multiset.Entry<? extends E>> entries) {
      int distinct = entries.size();
      Multisets.ImmutableEntry<E>[] entryArray = new Multisets.ImmutableEntry[distinct];
      if (distinct == 0) {
         return new RegularImmutableMultiset<>(entryArray, EMPTY_ARRAY, 0, 0, ImmutableSet.of());
      }

      int tableSize = Hashing.closedTableSize(distinct, 1.0);
      int mask = tableSize - 1;
      Multisets.ImmutableEntry<E>[] hashTable = new Multisets.ImmutableEntry[tableSize];
      int index = 0;
      int hashCode = 0;
      long size = 0L;

      for (Multiset.Entry<? extends E> entryWithWildcard : entries) {
         Multiset.Entry<E> entry = (Multiset.Entry<E>)entryWithWildcard;
         E element = Preconditions.checkNotNull(entry.getElement());
         int count = entry.getCount();
         int hash = element.hashCode();
         int bucket = Hashing.smear(hash) & mask;
         Multisets.ImmutableEntry<E> bucketHead = hashTable[bucket];
         Multisets.ImmutableEntry<E> newEntry;
         if (bucketHead == null) {
            boolean canReuseEntry = entry instanceof Multisets.ImmutableEntry && !(entry instanceof RegularImmutableMultiset.NonTerminalEntry);
            newEntry = canReuseEntry ? (Multisets.ImmutableEntry)entry : new Multisets.ImmutableEntry<>(element, count);
         } else {
            newEntry = new RegularImmutableMultiset.NonTerminalEntry<>(element, count, bucketHead);
         }

         hashCode += hash ^ count;
         entryArray[index++] = newEntry;
         hashTable[bucket] = newEntry;
         size += count;
      }

      return hashFloodingDetected(hashTable)
         ? JdkBackedImmutableMultiset.create(ImmutableList.asImmutableList(entryArray))
         : new RegularImmutableMultiset<>(entryArray, hashTable, Ints.saturatedCast(size), hashCode, null);
   }

   private static boolean hashFloodingDetected(Multisets.@Nullable ImmutableEntry<?>[] hashTable) {
      for (int i = 0; i < hashTable.length; i++) {
         int bucketLength = 0;

         for (Multisets.ImmutableEntry<?> entry = hashTable[i]; entry != null; entry = entry.nextInBucket()) {
            if (++bucketLength > 9) {
               return true;
            }
         }
      }

      return false;
   }

   private RegularImmutableMultiset(
      Multisets.ImmutableEntry<E>[] entries, Multisets.@Nullable ImmutableEntry<?>[] hashTable, int size, int hashCode, @Nullable ImmutableSet<E> elementSet
   ) {
      this.entries = entries;
      this.hashTable = hashTable;
      this.size = size;
      this.hashCode = hashCode;
      this.elementSet = elementSet;
   }

   @Override
   boolean isPartialView() {
      return false;
   }

   @Override
   public int count(Object element) {
      Multisets.ImmutableEntry<?>[] hashTable = this.hashTable;
      if (element != null && hashTable.length != 0) {
         int hash = Hashing.smearedHash(element);
         int mask = hashTable.length - 1;

         for (Multisets.ImmutableEntry<?> entry = hashTable[hash & mask]; entry != null; entry = entry.nextInBucket()) {
            if (Objects.equal(element, entry.getElement())) {
               return entry.getCount();
            }
         }

         return 0;
      } else {
         return 0;
      }
   }

   @Override
   public int size() {
      return this.size;
   }

   @Override
   public ImmutableSet<E> elementSet() {
      ImmutableSet<E> result = this.elementSet;
      return result == null ? (this.elementSet = new ImmutableMultiset.ElementSet<>(Arrays.asList(this.entries), this)) : result;
   }

   @Override
   Multiset.Entry<E> getEntry(int index) {
      return this.entries[index];
   }

   @Override
   public int hashCode() {
      return this.hashCode;
   }

   @J2ktIncompatible
   @GwtIncompatible
   @Override
   Object writeReplace() {
      return super.writeReplace();
   }

   private static final class NonTerminalEntry<E> extends Multisets.ImmutableEntry<E> {
      private final Multisets.ImmutableEntry<E> nextInBucket;

      NonTerminalEntry(E element, int count, Multisets.ImmutableEntry<E> nextInBucket) {
         super(element, count);
         this.nextInBucket = nextInBucket;
      }

      @Override
      public Multisets.ImmutableEntry<E> nextInBucket() {
         return this.nextInBucket;
      }
   }
}
