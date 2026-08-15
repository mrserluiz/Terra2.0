package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Map.Entry;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

@GwtCompatible(serializable = true, emulated = true)
public final class LinkedHashMultimap<K, V> extends LinkedHashMultimapGwtSerializationDependencies<K, V> {
   private static final int DEFAULT_KEY_CAPACITY = 16;
   private static final int DEFAULT_VALUE_SET_CAPACITY = 2;
   @VisibleForTesting
   static final double VALUE_SET_LOAD_FACTOR = 1.0;
   @VisibleForTesting
   transient int valueSetCapacity = 2;
   private transient LinkedHashMultimap.ValueEntry<K, V> multimapHeaderEntry;
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 1L;

   public static <K, V> LinkedHashMultimap<K, V> create() {
      return new LinkedHashMultimap<>(16, 2);
   }

   public static <K, V> LinkedHashMultimap<K, V> create(int expectedKeys, int expectedValuesPerKey) {
      return new LinkedHashMultimap<>(Maps.capacity(expectedKeys), Maps.capacity(expectedValuesPerKey));
   }

   public static <K, V> LinkedHashMultimap<K, V> create(Multimap<? extends K, ? extends V> multimap) {
      LinkedHashMultimap<K, V> result = create(multimap.keySet().size(), 2);
      result.putAll(multimap);
      return result;
   }

   private static <K, V> void succeedsInValueSet(LinkedHashMultimap.ValueSetLink<K, V> pred, LinkedHashMultimap.ValueSetLink<K, V> succ) {
      pred.setSuccessorInValueSet(succ);
      succ.setPredecessorInValueSet(pred);
   }

   private static <K, V> void succeedsInMultimap(LinkedHashMultimap.ValueEntry<K, V> pred, LinkedHashMultimap.ValueEntry<K, V> succ) {
      pred.setSuccessorInMultimap(succ);
      succ.setPredecessorInMultimap(pred);
   }

   private static <K, V> void deleteFromValueSet(LinkedHashMultimap.ValueSetLink<K, V> entry) {
      succeedsInValueSet(entry.getPredecessorInValueSet(), entry.getSuccessorInValueSet());
   }

   private static <K, V> void deleteFromMultimap(LinkedHashMultimap.ValueEntry<K, V> entry) {
      succeedsInMultimap(entry.getPredecessorInMultimap(), entry.getSuccessorInMultimap());
   }

   private LinkedHashMultimap(int keyCapacity, int valueSetCapacity) {
      super(Platform.newLinkedHashMapWithExpectedSize(keyCapacity));
      CollectPreconditions.checkNonnegative(valueSetCapacity, "expectedValuesPerKey");
      this.valueSetCapacity = valueSetCapacity;
      this.multimapHeaderEntry = LinkedHashMultimap.ValueEntry.newHeader();
      succeedsInMultimap(this.multimapHeaderEntry, this.multimapHeaderEntry);
   }

   @Override
   Set<V> createCollection() {
      return Platform.newLinkedHashSetWithExpectedSize(this.valueSetCapacity);
   }

   @Override
   Collection<V> createCollection(@ParametricNullness K key) {
      return new LinkedHashMultimap.ValueSet(key, this.valueSetCapacity);
   }

   @CanIgnoreReturnValue
   @Override
   public Set<V> replaceValues(@ParametricNullness K key, Iterable<? extends V> values) {
      return super.replaceValues(key, values);
   }

   @Override
   public Set<Entry<K, V>> entries() {
      return super.entries();
   }

   @Override
   public Set<K> keySet() {
      return super.keySet();
   }

   @Override
   public Collection<V> values() {
      return super.values();
   }

   @Override
   Iterator<Entry<K, V>> entryIterator() {
      return new Iterator<Entry<K, V>>() {
         LinkedHashMultimap.ValueEntry<K, V> nextEntry = LinkedHashMultimap.this.multimapHeaderEntry.getSuccessorInMultimap();
         LinkedHashMultimap.@Nullable ValueEntry<K, V> toRemove;

         @Override
         public boolean hasNext() {
            return this.nextEntry != LinkedHashMultimap.this.multimapHeaderEntry;
         }

         public Entry<K, V> next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            }

            LinkedHashMultimap.ValueEntry<K, V> result = this.nextEntry;
            this.toRemove = result;
            this.nextEntry = this.nextEntry.getSuccessorInMultimap();
            return result;
         }

         @Override
         public void remove() {
            Preconditions.checkState(this.toRemove != null, "no calls to next() since the last call to remove()");
            LinkedHashMultimap.this.remove(this.toRemove.getKey(), this.toRemove.getValue());
            this.toRemove = null;
         }
      };
   }

   @Override
   Spliterator<Entry<K, V>> entrySpliterator() {
      return Spliterators.spliterator(this.entries(), 17);
   }

   @Override
   Iterator<V> valueIterator() {
      return Maps.valueIterator(this.entryIterator());
   }

   @Override
   Spliterator<V> valueSpliterator() {
      return CollectSpliterators.map(this.entrySpliterator(), Entry::getValue);
   }

   @Override
   public void clear() {
      super.clear();
      succeedsInMultimap(this.multimapHeaderEntry, this.multimapHeaderEntry);
   }

   @GwtIncompatible
   @J2ktIncompatible
   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      stream.writeInt(this.keySet().size());

      for (K key : this.keySet()) {
         stream.writeObject(key);
      }

      stream.writeInt(this.size());

      for (Entry<K, V> entry : this.entries()) {
         stream.writeObject(entry.getKey());
         stream.writeObject(entry.getValue());
      }
   }

   @GwtIncompatible
   @J2ktIncompatible
   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.multimapHeaderEntry = LinkedHashMultimap.ValueEntry.newHeader();
      succeedsInMultimap(this.multimapHeaderEntry, this.multimapHeaderEntry);
      this.valueSetCapacity = 2;
      int distinctKeys = stream.readInt();
      Map<K, Collection<V>> map = Platform.newLinkedHashMapWithExpectedSize(12);

      for (int i = 0; i < distinctKeys; i++) {
         K key = (K)stream.readObject();
         map.put(key, this.createCollection(key));
      }

      int entries = stream.readInt();

      for (int i = 0; i < entries; i++) {
         K key = (K)stream.readObject();
         V value = (V)stream.readObject();
         Objects.requireNonNull(map.get(key)).add(value);
      }

      this.setMap(map);
   }

   @VisibleForTesting
   static final class ValueEntry<K, V> extends ImmutableEntry<K, V> implements LinkedHashMultimap.ValueSetLink<K, V> {
      final int smearedValueHash;
      LinkedHashMultimap.@Nullable ValueEntry<K, V> nextInValueBucket;
      private LinkedHashMultimap.@Nullable ValueSetLink<K, V> predecessorInValueSet;
      private LinkedHashMultimap.@Nullable ValueSetLink<K, V> successorInValueSet;
      private LinkedHashMultimap.@Nullable ValueEntry<K, V> predecessorInMultimap;
      private LinkedHashMultimap.@Nullable ValueEntry<K, V> successorInMultimap;

      ValueEntry(@ParametricNullness K key, @ParametricNullness V value, int smearedValueHash, LinkedHashMultimap.@Nullable ValueEntry<K, V> nextInValueBucket) {
         super(key, value);
         this.smearedValueHash = smearedValueHash;
         this.nextInValueBucket = nextInValueBucket;
      }

      static <K, V> LinkedHashMultimap.ValueEntry<K, V> newHeader() {
         return new LinkedHashMultimap.ValueEntry<>(null, null, 0, null);
      }

      boolean matchesValue(@Nullable Object v, int smearedVHash) {
         return this.smearedValueHash == smearedVHash && com.dfsek.terra.lib.google.common.base.Objects.equal(this.getValue(), v);
      }

      @Override
      public LinkedHashMultimap.ValueSetLink<K, V> getPredecessorInValueSet() {
         return Objects.requireNonNull(this.predecessorInValueSet);
      }

      @Override
      public LinkedHashMultimap.ValueSetLink<K, V> getSuccessorInValueSet() {
         return Objects.requireNonNull(this.successorInValueSet);
      }

      @Override
      public void setPredecessorInValueSet(LinkedHashMultimap.ValueSetLink<K, V> entry) {
         this.predecessorInValueSet = entry;
      }

      @Override
      public void setSuccessorInValueSet(LinkedHashMultimap.ValueSetLink<K, V> entry) {
         this.successorInValueSet = entry;
      }

      public LinkedHashMultimap.ValueEntry<K, V> getPredecessorInMultimap() {
         return Objects.requireNonNull(this.predecessorInMultimap);
      }

      public LinkedHashMultimap.ValueEntry<K, V> getSuccessorInMultimap() {
         return Objects.requireNonNull(this.successorInMultimap);
      }

      public void setSuccessorInMultimap(LinkedHashMultimap.ValueEntry<K, V> multimapSuccessor) {
         this.successorInMultimap = multimapSuccessor;
      }

      public void setPredecessorInMultimap(LinkedHashMultimap.ValueEntry<K, V> multimapPredecessor) {
         this.predecessorInMultimap = multimapPredecessor;
      }
   }

   @VisibleForTesting
   final class ValueSet extends Sets.ImprovedAbstractSet<V> implements LinkedHashMultimap.ValueSetLink<K, V> {
      @ParametricNullness
      private final Object key;
      @VisibleForTesting
      LinkedHashMultimap.@Nullable ValueEntry<K, V>[] hashTable;
      private int size = 0;
      private int modCount = 0;
      private LinkedHashMultimap.ValueSetLink<K, V> firstEntry;
      private LinkedHashMultimap.ValueSetLink<K, V> lastEntry;

      ValueSet(@ParametricNullness K key, int expectedValues) {
         this.key = key;
         this.firstEntry = this;
         this.lastEntry = this;
         int tableSize = Hashing.closedTableSize(expectedValues, 1.0);
         LinkedHashMultimap.ValueEntry<K, V>[] hashTable = new LinkedHashMultimap.ValueEntry[tableSize];
         this.hashTable = hashTable;
      }

      private int mask() {
         return this.hashTable.length - 1;
      }

      @Override
      public LinkedHashMultimap.ValueSetLink<K, V> getPredecessorInValueSet() {
         return this.lastEntry;
      }

      @Override
      public LinkedHashMultimap.ValueSetLink<K, V> getSuccessorInValueSet() {
         return this.firstEntry;
      }

      @Override
      public void setPredecessorInValueSet(LinkedHashMultimap.ValueSetLink<K, V> entry) {
         this.lastEntry = entry;
      }

      @Override
      public void setSuccessorInValueSet(LinkedHashMultimap.ValueSetLink<K, V> entry) {
         this.firstEntry = entry;
      }

      @Override
      public Iterator<V> iterator() {
         return new Iterator<V>() {
            LinkedHashMultimap.ValueSetLink<K, V> nextEntry = ValueSet.this.firstEntry;
            LinkedHashMultimap.@Nullable ValueEntry<K, V> toRemove;
            int expectedModCount = ValueSet.this.modCount;

            private void checkForComodification() {
               if (ValueSet.this.modCount != this.expectedModCount) {
                  throw new ConcurrentModificationException();
               }
            }

            @Override
            public boolean hasNext() {
               this.checkForComodification();
               return this.nextEntry != ValueSet.this;
            }

            @ParametricNullness
            @Override
            public V next() {
               if (!this.hasNext()) {
                  throw new NoSuchElementException();
               }

               LinkedHashMultimap.ValueEntry<K, V> entry = (LinkedHashMultimap.ValueEntry<K, V>)this.nextEntry;
               V result = entry.getValue();
               this.toRemove = entry;
               this.nextEntry = entry.getSuccessorInValueSet();
               return result;
            }

            @Override
            public void remove() {
               this.checkForComodification();
               Preconditions.checkState(this.toRemove != null, "no calls to next() since the last call to remove()");
               ValueSet.this.remove(this.toRemove.getValue());
               this.expectedModCount = ValueSet.this.modCount;
               this.toRemove = null;
            }
         };
      }

      @Override
      public void forEach(Consumer<? super V> action) {
         Preconditions.checkNotNull(action);

         for (LinkedHashMultimap.ValueSetLink<K, V> entry = this.firstEntry; entry != this; entry = entry.getSuccessorInValueSet()) {
            action.accept(((LinkedHashMultimap.ValueEntry)entry).getValue());
         }
      }

      @Override
      public int size() {
         return this.size;
      }

      @Override
      public boolean contains(@Nullable Object o) {
         int smearedHash = Hashing.smearedHash(o);

         for (LinkedHashMultimap.ValueEntry<K, V> entry = this.hashTable[smearedHash & this.mask()]; entry != null; entry = entry.nextInValueBucket) {
            if (entry.matchesValue(o, smearedHash)) {
               return true;
            }
         }

         return false;
      }

      @Override
      public boolean add(@ParametricNullness V value) {
         int smearedHash = Hashing.smearedHash(value);
         int bucket = smearedHash & this.mask();
         LinkedHashMultimap.ValueEntry<K, V> rowHead = this.hashTable[bucket];

         for (LinkedHashMultimap.ValueEntry<K, V> entry = rowHead; entry != null; entry = entry.nextInValueBucket) {
            if (entry.matchesValue(value, smearedHash)) {
               return false;
            }
         }

         LinkedHashMultimap.ValueEntry<K, V> newEntry = new LinkedHashMultimap.ValueEntry<>((K)this.key, value, smearedHash, rowHead);
         LinkedHashMultimap.succeedsInValueSet(this.lastEntry, newEntry);
         LinkedHashMultimap.succeedsInValueSet(newEntry, this);
         LinkedHashMultimap.succeedsInMultimap(LinkedHashMultimap.this.multimapHeaderEntry.getPredecessorInMultimap(), newEntry);
         LinkedHashMultimap.succeedsInMultimap(newEntry, LinkedHashMultimap.this.multimapHeaderEntry);
         this.hashTable[bucket] = newEntry;
         this.size++;
         this.modCount++;
         this.rehashIfNecessary();
         return true;
      }

      private void rehashIfNecessary() {
         if (Hashing.needsResizing(this.size, this.hashTable.length, 1.0)) {
            LinkedHashMultimap.ValueEntry<K, V>[] hashTable = new LinkedHashMultimap.ValueEntry[this.hashTable.length * 2];
            this.hashTable = hashTable;
            int mask = hashTable.length - 1;

            for (LinkedHashMultimap.ValueSetLink<K, V> entry = this.firstEntry; entry != this; entry = entry.getSuccessorInValueSet()) {
               LinkedHashMultimap.ValueEntry<K, V> valueEntry = (LinkedHashMultimap.ValueEntry<K, V>)entry;
               int bucket = valueEntry.smearedValueHash & mask;
               valueEntry.nextInValueBucket = hashTable[bucket];
               hashTable[bucket] = valueEntry;
            }
         }
      }

      @CanIgnoreReturnValue
      @Override
      public boolean remove(@Nullable Object o) {
         int smearedHash = Hashing.smearedHash(o);
         int bucket = smearedHash & this.mask();
         LinkedHashMultimap.ValueEntry<K, V> prev = null;

         for (LinkedHashMultimap.ValueEntry<K, V> entry = this.hashTable[bucket]; entry != null; entry = entry.nextInValueBucket) {
            if (entry.matchesValue(o, smearedHash)) {
               if (prev == null) {
                  this.hashTable[bucket] = entry.nextInValueBucket;
               } else {
                  prev.nextInValueBucket = entry.nextInValueBucket;
               }

               LinkedHashMultimap.deleteFromValueSet(entry);
               LinkedHashMultimap.deleteFromMultimap(entry);
               this.size--;
               this.modCount++;
               return true;
            }

            prev = entry;
         }

         return false;
      }

      @Override
      public void clear() {
         Arrays.fill(this.hashTable, null);
         this.size = 0;

         for (LinkedHashMultimap.ValueSetLink<K, V> entry = this.firstEntry; entry != this; entry = entry.getSuccessorInValueSet()) {
            LinkedHashMultimap.ValueEntry<K, V> valueEntry = (LinkedHashMultimap.ValueEntry<K, V>)entry;
            LinkedHashMultimap.deleteFromMultimap(valueEntry);
         }

         LinkedHashMultimap.succeedsInValueSet(this, this);
         this.modCount++;
      }
   }

   private interface ValueSetLink<K, V> {
      LinkedHashMultimap.ValueSetLink<K, V> getPredecessorInValueSet();

      LinkedHashMultimap.ValueSetLink<K, V> getSuccessorInValueSet();

      void setPredecessorInValueSet(LinkedHashMultimap.ValueSetLink<K, V> entry);

      void setSuccessorInValueSet(LinkedHashMultimap.ValueSetLink<K, V> entry);
   }
}
