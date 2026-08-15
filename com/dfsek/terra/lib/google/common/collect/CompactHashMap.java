package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import com.dfsek.terra.lib.google.common.base.Objects;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.primitives.Ints;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

@GwtIncompatible
class CompactHashMap<K, V> extends AbstractMap<K, V> implements Serializable {
   private static final Object NOT_FOUND = new Object();
   @VisibleForTesting
   static final double HASH_FLOODING_FPP = 0.001;
   private static final int MAX_HASH_BUCKET_LENGTH = 9;
   private transient @Nullable Object table;
   @VisibleForTesting
   transient int @Nullable [] entries;
   @VisibleForTesting
   transient @Nullable Object @Nullable [] keys;
   @VisibleForTesting
   transient @Nullable Object @Nullable [] values;
   private transient int metadata;
   private transient int size;
   @LazyInit
   private transient @Nullable Set<K> keySetView;
   @LazyInit
   private transient @Nullable Set<Entry<K, V>> entrySetView;
   @LazyInit
   private transient @Nullable Collection<V> valuesView;

   public static <K, V> CompactHashMap<K, V> create() {
      return new CompactHashMap<>();
   }

   public static <K, V> CompactHashMap<K, V> createWithExpectedSize(int expectedSize) {
      return new CompactHashMap<>(expectedSize);
   }

   CompactHashMap() {
      this.init(3);
   }

   CompactHashMap(int expectedSize) {
      this.init(expectedSize);
   }

   void init(int expectedSize) {
      Preconditions.checkArgument(expectedSize >= 0, "Expected size must be >= 0");
      this.metadata = Ints.constrainToRange(expectedSize, 1, 1073741823);
   }

   boolean needsAllocArrays() {
      return this.table == null;
   }

   @CanIgnoreReturnValue
   int allocArrays() {
      Preconditions.checkState(this.needsAllocArrays(), "Arrays already allocated");
      int expectedSize = this.metadata;
      int buckets = CompactHashing.tableSize(expectedSize);
      this.table = CompactHashing.createTable(buckets);
      this.setHashTableMask(buckets - 1);
      this.entries = new int[expectedSize];
      this.keys = new Object[expectedSize];
      this.values = new Object[expectedSize];
      return expectedSize;
   }

   @VisibleForTesting
   @Nullable Map<K, V> delegateOrNull() {
      return this.table instanceof Map ? (Map)this.table : null;
   }

   Map<K, V> createHashFloodingResistantDelegate(int tableSize) {
      return new LinkedHashMap<>(tableSize, 1.0F);
   }

   @CanIgnoreReturnValue
   Map<K, V> convertToHashFloodingResistantImplementation() {
      Map<K, V> newDelegate = this.createHashFloodingResistantDelegate(this.hashTableMask() + 1);

      for (int i = this.firstEntryIndex(); i >= 0; i = this.getSuccessor(i)) {
         newDelegate.put(this.key(i), this.value(i));
      }

      this.table = newDelegate;
      this.entries = null;
      this.keys = null;
      this.values = null;
      this.incrementModCount();
      return newDelegate;
   }

   private void setHashTableMask(int mask) {
      int hashTableBits = 32 - Integer.numberOfLeadingZeros(mask);
      this.metadata = CompactHashing.maskCombine(this.metadata, hashTableBits, 31);
   }

   private int hashTableMask() {
      return (1 << (this.metadata & 31)) - 1;
   }

   void incrementModCount() {
      this.metadata += 32;
   }

   void accessEntry(int index) {
   }

   @CanIgnoreReturnValue
   @Override
   public V put(@ParametricNullness K key, @ParametricNullness V value) {
      if (this.needsAllocArrays()) {
         this.allocArrays();
      }

      Map<K, V> delegate = this.delegateOrNull();
      if (delegate != null) {
         return delegate.put(key, value);
      }

      int[] entries = this.requireEntries();
      Object[] keys = this.requireKeys();
      Object[] values = this.requireValues();
      int newEntryIndex = this.size;
      int newSize = newEntryIndex + 1;
      int hash = Hashing.smearedHash(key);
      int mask = this.hashTableMask();
      int tableIndex = hash & mask;
      int next = CompactHashing.tableGet(this.requireTable(), tableIndex);
      if (next == 0) {
         if (newSize > mask) {
            mask = this.resizeTable(mask, CompactHashing.newCapacity(mask), hash, newEntryIndex);
         } else {
            CompactHashing.tableSet(this.requireTable(), tableIndex, newEntryIndex + 1);
         }
      } else {
         int hashPrefix = CompactHashing.getHashPrefix(hash, mask);
         int bucketLength = 0;

         int entryIndex;
         int entry;
         do {
            entryIndex = next - 1;
            entry = entries[entryIndex];
            if (CompactHashing.getHashPrefix(entry, mask) == hashPrefix && Objects.equal(key, keys[entryIndex])) {
               V oldValue = (V)values[entryIndex];
               values[entryIndex] = value;
               this.accessEntry(entryIndex);
               return oldValue;
            }

            next = CompactHashing.getNext(entry, mask);
            bucketLength++;
         } while (next != 0);

         if (bucketLength >= 9) {
            return this.convertToHashFloodingResistantImplementation().put(key, value);
         }

         if (newSize > mask) {
            mask = this.resizeTable(mask, CompactHashing.newCapacity(mask), hash, newEntryIndex);
         } else {
            entries[entryIndex] = CompactHashing.maskCombine(entry, newEntryIndex + 1, mask);
         }
      }

      this.resizeMeMaybe(newSize);
      this.insertEntry(newEntryIndex, key, value, hash, mask);
      this.size = newSize;
      this.incrementModCount();
      return null;
   }

   void insertEntry(int entryIndex, @ParametricNullness K key, @ParametricNullness V value, int hash, int mask) {
      this.setEntry(entryIndex, CompactHashing.maskCombine(hash, 0, mask));
      this.setKey(entryIndex, key);
      this.setValue(entryIndex, value);
   }

   private void resizeMeMaybe(int newSize) {
      int entriesSize = this.requireEntries().length;
      if (newSize > entriesSize) {
         int newCapacity = Math.min(1073741823, entriesSize + Math.max(1, entriesSize >>> 1) | 1);
         if (newCapacity != entriesSize) {
            this.resizeEntries(newCapacity);
         }
      }
   }

   void resizeEntries(int newCapacity) {
      this.entries = Arrays.copyOf(this.requireEntries(), newCapacity);
      this.keys = Arrays.copyOf(this.requireKeys(), newCapacity);
      this.values = Arrays.copyOf(this.requireValues(), newCapacity);
   }

   @CanIgnoreReturnValue
   private int resizeTable(int oldMask, int newCapacity, int targetHash, int targetEntryIndex) {
      Object newTable = CompactHashing.createTable(newCapacity);
      int newMask = newCapacity - 1;
      if (targetEntryIndex != 0) {
         CompactHashing.tableSet(newTable, targetHash & newMask, targetEntryIndex + 1);
      }

      Object oldTable = this.requireTable();
      int[] entries = this.requireEntries();

      for (int oldTableIndex = 0; oldTableIndex <= oldMask; oldTableIndex++) {
         int oldNext = CompactHashing.tableGet(oldTable, oldTableIndex);

         while (oldNext != 0) {
            int entryIndex = oldNext - 1;
            int oldEntry = entries[entryIndex];
            int hash = CompactHashing.getHashPrefix(oldEntry, oldMask) | oldTableIndex;
            int newTableIndex = hash & newMask;
            int newNext = CompactHashing.tableGet(newTable, newTableIndex);
            CompactHashing.tableSet(newTable, newTableIndex, oldNext);
            entries[entryIndex] = CompactHashing.maskCombine(hash, newNext, newMask);
            oldNext = CompactHashing.getNext(oldEntry, oldMask);
         }
      }

      this.table = newTable;
      this.setHashTableMask(newMask);
      return newMask;
   }

   private int indexOf(@Nullable Object key) {
      if (this.needsAllocArrays()) {
         return -1;
      }

      int hash = Hashing.smearedHash(key);
      int mask = this.hashTableMask();
      int next = CompactHashing.tableGet(this.requireTable(), hash & mask);
      if (next == 0) {
         return -1;
      }

      int hashPrefix = CompactHashing.getHashPrefix(hash, mask);

      do {
         int entryIndex = next - 1;
         int entry = this.entry(entryIndex);
         if (CompactHashing.getHashPrefix(entry, mask) == hashPrefix && Objects.equal(key, this.key(entryIndex))) {
            return entryIndex;
         }

         next = CompactHashing.getNext(entry, mask);
      } while (next != 0);

      return -1;
   }

   @Override
   public boolean containsKey(@Nullable Object key) {
      Map<K, V> delegate = this.delegateOrNull();
      return delegate != null ? delegate.containsKey(key) : this.indexOf(key) != -1;
   }

   @Override
   public @Nullable V get(@Nullable Object key) {
      Map<K, V> delegate = this.delegateOrNull();
      if (delegate != null) {
         return delegate.get(key);
      }

      int index = this.indexOf(key);
      if (index == -1) {
         return null;
      }

      this.accessEntry(index);
      return this.value(index);
   }

   @CanIgnoreReturnValue
   @Override
   public @Nullable V remove(@Nullable Object key) {
      Map<K, V> delegate = this.delegateOrNull();
      if (delegate != null) {
         return delegate.remove(key);
      }

      Object oldValue = this.removeHelper(key);
      return (V)(oldValue == NOT_FOUND ? null : oldValue);
   }

   private @Nullable Object removeHelper(@Nullable Object key) {
      if (this.needsAllocArrays()) {
         return NOT_FOUND;
      }

      int mask = this.hashTableMask();
      int index = CompactHashing.remove(key, null, mask, this.requireTable(), this.requireEntries(), this.requireKeys(), null);
      if (index == -1) {
         return NOT_FOUND;
      }

      Object oldValue = this.value(index);
      this.moveLastEntry(index, mask);
      this.size--;
      this.incrementModCount();
      return oldValue;
   }

   void moveLastEntry(int dstIndex, int mask) {
      Object table = this.requireTable();
      int[] entries = this.requireEntries();
      Object[] keys = this.requireKeys();
      Object[] values = this.requireValues();
      int srcIndex = this.size() - 1;
      if (dstIndex < srcIndex) {
         Object key = keys[srcIndex];
         keys[dstIndex] = key;
         values[dstIndex] = values[srcIndex];
         keys[srcIndex] = null;
         values[srcIndex] = null;
         entries[dstIndex] = entries[srcIndex];
         entries[srcIndex] = 0;
         int tableIndex = Hashing.smearedHash(key) & mask;
         int next = CompactHashing.tableGet(table, tableIndex);
         int srcNext = srcIndex + 1;
         if (next == srcNext) {
            CompactHashing.tableSet(table, tableIndex, dstIndex + 1);
         } else {
            int entryIndex;
            int entry;
            do {
               entryIndex = next - 1;
               entry = entries[entryIndex];
               next = CompactHashing.getNext(entry, mask);
            } while (next != srcNext);

            entries[entryIndex] = CompactHashing.maskCombine(entry, dstIndex + 1, mask);
         }
      } else {
         keys[dstIndex] = null;
         values[dstIndex] = null;
         entries[dstIndex] = 0;
      }
   }

   int firstEntryIndex() {
      return this.isEmpty() ? -1 : 0;
   }

   int getSuccessor(int entryIndex) {
      return entryIndex + 1 < this.size ? entryIndex + 1 : -1;
   }

   int adjustAfterRemove(int indexBeforeRemove, int indexRemoved) {
      return indexBeforeRemove - 1;
   }

   @Override
   public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
      Preconditions.checkNotNull(function);
      Map<K, V> delegate = this.delegateOrNull();
      if (delegate != null) {
         delegate.replaceAll(function);
      } else {
         for (int i = 0; i < this.size; i++) {
            this.setValue(i, (V)function.apply(this.key(i), this.value(i)));
         }
      }
   }

   @Override
   public Set<K> keySet() {
      return this.keySetView == null ? (this.keySetView = this.createKeySet()) : this.keySetView;
   }

   Set<K> createKeySet() {
      return new CompactHashMap.KeySetView();
   }

   Iterator<K> keySetIterator() {
      Map<K, V> delegate = this.delegateOrNull();
      return delegate != null ? delegate.keySet().iterator() : new CompactHashMap<K, V>.Itr<K>() {
         @ParametricNullness
         @Override
         K getOutput(int entry) {
            return (K)CompactHashMap.this.key(entry);
         }
      };
   }

   @Override
   public void forEach(BiConsumer<? super K, ? super V> action) {
      Preconditions.checkNotNull(action);
      Map<K, V> delegate = this.delegateOrNull();
      if (delegate != null) {
         delegate.forEach(action);
      } else {
         for (int i = this.firstEntryIndex(); i >= 0; i = this.getSuccessor(i)) {
            action.accept(this.key(i), this.value(i));
         }
      }
   }

   @Override
   public Set<Entry<K, V>> entrySet() {
      return this.entrySetView == null ? (this.entrySetView = this.createEntrySet()) : this.entrySetView;
   }

   Set<Entry<K, V>> createEntrySet() {
      return new CompactHashMap.EntrySetView();
   }

   Iterator<Entry<K, V>> entrySetIterator() {
      Map<K, V> delegate = this.delegateOrNull();
      return delegate != null ? delegate.entrySet().iterator() : new CompactHashMap<K, V>.Itr<Entry<K, V>>() {
         Entry<K, V> getOutput(int entry) {
            return CompactHashMap.this.new MapEntry(entry);
         }
      };
   }

   @Override
   public int size() {
      Map<K, V> delegate = this.delegateOrNull();
      return delegate != null ? delegate.size() : this.size;
   }

   @Override
   public boolean isEmpty() {
      return this.size() == 0;
   }

   @Override
   public boolean containsValue(@Nullable Object value) {
      Map<K, V> delegate = this.delegateOrNull();
      if (delegate != null) {
         return delegate.containsValue(value);
      }

      for (int i = 0; i < this.size; i++) {
         if (Objects.equal(value, this.value(i))) {
            return true;
         }
      }

      return false;
   }

   @Override
   public Collection<V> values() {
      return this.valuesView == null ? (this.valuesView = this.createValues()) : this.valuesView;
   }

   Collection<V> createValues() {
      return new CompactHashMap.ValuesView();
   }

   Iterator<V> valuesIterator() {
      Map<K, V> delegate = this.delegateOrNull();
      return delegate != null ? delegate.values().iterator() : new CompactHashMap<K, V>.Itr<V>() {
         @ParametricNullness
         @Override
         V getOutput(int entry) {
            return (V)CompactHashMap.this.value(entry);
         }
      };
   }

   public void trimToSize() {
      if (!this.needsAllocArrays()) {
         Map<K, V> delegate = this.delegateOrNull();
         if (delegate != null) {
            Map<K, V> newDelegate = this.createHashFloodingResistantDelegate(this.size());
            newDelegate.putAll(delegate);
            this.table = newDelegate;
         } else {
            int size = this.size;
            if (size < this.requireEntries().length) {
               this.resizeEntries(size);
            }

            int minimumTableSize = CompactHashing.tableSize(size);
            int mask = this.hashTableMask();
            if (minimumTableSize < mask) {
               this.resizeTable(mask, minimumTableSize, 0, 0);
            }
         }
      }
   }

   @Override
   public void clear() {
      if (!this.needsAllocArrays()) {
         this.incrementModCount();
         Map<K, V> delegate = this.delegateOrNull();
         if (delegate != null) {
            this.metadata = Ints.constrainToRange(this.size(), 3, 1073741823);
            delegate.clear();
            this.table = null;
            this.size = 0;
         } else {
            Arrays.fill(this.requireKeys(), 0, this.size, null);
            Arrays.fill(this.requireValues(), 0, this.size, null);
            CompactHashing.tableClear(this.requireTable());
            Arrays.fill(this.requireEntries(), 0, this.size, 0);
            this.size = 0;
         }
      }
   }

   @J2ktIncompatible
   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      stream.writeInt(this.size());
      Iterator<Entry<K, V>> entryIterator = this.entrySetIterator();

      while (entryIterator.hasNext()) {
         Entry<K, V> e = entryIterator.next();
         stream.writeObject(e.getKey());
         stream.writeObject(e.getValue());
      }
   }

   @J2ktIncompatible
   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      int elementCount = stream.readInt();
      if (elementCount < 0) {
         throw new InvalidObjectException("Invalid size: " + elementCount);
      }

      this.init(elementCount);

      for (int i = 0; i < elementCount; i++) {
         K key = (K)stream.readObject();
         V value = (V)stream.readObject();
         this.put(key, value);
      }
   }

   private Object requireTable() {
      return java.util.Objects.requireNonNull(this.table);
   }

   private int[] requireEntries() {
      return java.util.Objects.requireNonNull(this.entries);
   }

   private @Nullable Object[] requireKeys() {
      return java.util.Objects.requireNonNull(this.keys);
   }

   private @Nullable Object[] requireValues() {
      return java.util.Objects.requireNonNull(this.values);
   }

   private K key(int i) {
      return (K)this.requireKeys()[i];
   }

   private V value(int i) {
      return (V)this.requireValues()[i];
   }

   private int entry(int i) {
      return this.requireEntries()[i];
   }

   private void setKey(int i, K key) {
      this.requireKeys()[i] = key;
   }

   private void setValue(int i, V value) {
      this.requireValues()[i] = value;
   }

   private void setEntry(int i, int value) {
      this.requireEntries()[i] = value;
   }

   class EntrySetView extends Maps.EntrySet<K, V> {
      @Override
      Map<K, V> map() {
         return CompactHashMap.this;
      }

      @Override
      public Iterator<Entry<K, V>> iterator() {
         return CompactHashMap.this.entrySetIterator();
      }

      @Override
      public Spliterator<Entry<K, V>> spliterator() {
         Map<K, V> delegate = CompactHashMap.this.delegateOrNull();
         return delegate != null
            ? delegate.entrySet().spliterator()
            : CollectSpliterators.indexed(CompactHashMap.this.size, 17, x$0 -> CompactHashMap.this.new MapEntry(x$0));
      }

      @Override
      public boolean contains(@Nullable Object o) {
         Map<K, V> delegate = CompactHashMap.this.delegateOrNull();
         if (delegate != null) {
            return delegate.entrySet().contains(o);
         }

         if (!(o instanceof Entry)) {
            return false;
         }

         Entry<?, ?> entry = (Entry<?, ?>)o;
         int index = CompactHashMap.this.indexOf(entry.getKey());
         return index != -1 && Objects.equal(CompactHashMap.this.value(index), entry.getValue());
      }

      @Override
      public boolean remove(@Nullable Object o) {
         Map<K, V> delegate = CompactHashMap.this.delegateOrNull();
         if (delegate != null) {
            return delegate.entrySet().remove(o);
         }

         if (o instanceof Entry) {
            Entry<?, ?> entry = (Entry<?, ?>)o;
            if (CompactHashMap.this.needsAllocArrays()) {
               return false;
            }

            int mask = CompactHashMap.this.hashTableMask();
            int index = CompactHashing.remove(
               entry.getKey(),
               entry.getValue(),
               mask,
               CompactHashMap.this.requireTable(),
               CompactHashMap.this.requireEntries(),
               CompactHashMap.this.requireKeys(),
               CompactHashMap.this.requireValues()
            );
            if (index == -1) {
               return false;
            }

            CompactHashMap.this.moveLastEntry(index, mask);
            CompactHashMap.this.size--;
            CompactHashMap.this.incrementModCount();
            return true;
         } else {
            return false;
         }
      }
   }

   private abstract class Itr<T> implements Iterator<T> {
      int expectedMetadata = CompactHashMap.this.metadata;
      int currentIndex = CompactHashMap.this.firstEntryIndex();
      int indexToRemove = -1;

      private Itr() {
      }

      @Override
      public boolean hasNext() {
         return this.currentIndex >= 0;
      }

      @ParametricNullness
      abstract T getOutput(int entry);

      @ParametricNullness
      @Override
      public T next() {
         this.checkForConcurrentModification();
         if (!this.hasNext()) {
            throw new NoSuchElementException();
         }

         this.indexToRemove = this.currentIndex;
         T result = this.getOutput(this.currentIndex);
         this.currentIndex = CompactHashMap.this.getSuccessor(this.currentIndex);
         return result;
      }

      @Override
      public void remove() {
         this.checkForConcurrentModification();
         CollectPreconditions.checkRemove(this.indexToRemove >= 0);
         this.incrementExpectedModCount();
         CompactHashMap.this.remove(CompactHashMap.this.key(this.indexToRemove));
         this.currentIndex = CompactHashMap.this.adjustAfterRemove(this.currentIndex, this.indexToRemove);
         this.indexToRemove = -1;
      }

      void incrementExpectedModCount() {
         this.expectedMetadata += 32;
      }

      private void checkForConcurrentModification() {
         if (CompactHashMap.this.metadata != this.expectedMetadata) {
            throw new ConcurrentModificationException();
         }
      }
   }

   class KeySetView extends Maps.KeySet<K, V> {
      KeySetView() {
         super(CompactHashMap.this);
      }

      @Override
      public @Nullable Object[] toArray() {
         if (CompactHashMap.this.needsAllocArrays()) {
            return new Object[0];
         }

         Map<K, V> delegate = CompactHashMap.this.delegateOrNull();
         return delegate != null ? delegate.keySet().toArray() : ObjectArrays.copyAsObjectArray(CompactHashMap.this.requireKeys(), 0, CompactHashMap.this.size);
      }

      @Override
      public <T> T[] toArray(T[] a) {
         if (CompactHashMap.this.needsAllocArrays()) {
            if (a.length > 0) {
               Object[] unsoundlyCovariantArray = a;
               unsoundlyCovariantArray[0] = null;
            }

            return a;
         } else {
            Map<K, V> delegate = CompactHashMap.this.delegateOrNull();
            return (T[])(delegate != null
               ? delegate.keySet().toArray(a)
               : ObjectArrays.toArrayImpl(CompactHashMap.this.requireKeys(), 0, CompactHashMap.this.size, a));
         }
      }

      @Override
      public boolean remove(@Nullable Object o) {
         Map<K, V> delegate = CompactHashMap.this.delegateOrNull();
         return delegate != null ? delegate.keySet().remove(o) : CompactHashMap.this.removeHelper(o) != CompactHashMap.NOT_FOUND;
      }

      @Override
      public Iterator<K> iterator() {
         return CompactHashMap.this.keySetIterator();
      }

      @Override
      public Spliterator<K> spliterator() {
         if (CompactHashMap.this.needsAllocArrays()) {
            return Spliterators.spliterator(new Object[0], 17);
         }

         Map<K, V> delegate = CompactHashMap.this.delegateOrNull();
         return delegate != null
            ? delegate.keySet().spliterator()
            : Spliterators.spliterator(CompactHashMap.this.requireKeys(), 0, CompactHashMap.this.size, 17);
      }

      @Override
      public void forEach(Consumer<? super K> action) {
         Preconditions.checkNotNull(action);
         Map<K, V> delegate = CompactHashMap.this.delegateOrNull();
         if (delegate != null) {
            delegate.keySet().forEach(action);
         } else {
            for (int i = CompactHashMap.this.firstEntryIndex(); i >= 0; i = CompactHashMap.this.getSuccessor(i)) {
               action.accept(CompactHashMap.this.key(i));
            }
         }
      }
   }

   final class MapEntry extends AbstractMapEntry<K, V> {
      @ParametricNullness
      private final Object key;
      private int lastKnownIndex;

      MapEntry(int index) {
         this.key = CompactHashMap.this.key(index);
         this.lastKnownIndex = index;
      }

      @ParametricNullness
      @Override
      public K getKey() {
         return (K)this.key;
      }

      private void updateLastKnownIndex() {
         if (this.lastKnownIndex == -1
            || this.lastKnownIndex >= CompactHashMap.this.size()
            || !Objects.equal(this.key, CompactHashMap.this.key(this.lastKnownIndex))) {
            this.lastKnownIndex = CompactHashMap.this.indexOf(this.key);
         }
      }

      @ParametricNullness
      @Override
      public V getValue() {
         Map<K, V> delegate = CompactHashMap.this.delegateOrNull();
         if (delegate != null) {
            return NullnessCasts.uncheckedCastNullableTToT(delegate.get(this.key));
         }

         this.updateLastKnownIndex();
         return this.lastKnownIndex == -1 ? NullnessCasts.unsafeNull() : CompactHashMap.this.value(this.lastKnownIndex);
      }

      @ParametricNullness
      @Override
      public V setValue(@ParametricNullness V value) {
         Map<K, V> delegate = CompactHashMap.this.delegateOrNull();
         if (delegate != null) {
            return NullnessCasts.uncheckedCastNullableTToT(delegate.put((K)this.key, value));
         } else {
            this.updateLastKnownIndex();
            if (this.lastKnownIndex == -1) {
               CompactHashMap.this.put((K)this.key, value);
               return NullnessCasts.unsafeNull();
            } else {
               V old = CompactHashMap.this.value(this.lastKnownIndex);
               CompactHashMap.this.setValue(this.lastKnownIndex, value);
               return old;
            }
         }
      }
   }

   class ValuesView extends Maps.Values<K, V> {
      ValuesView() {
         super(CompactHashMap.this);
      }

      @Override
      public Iterator<V> iterator() {
         return CompactHashMap.this.valuesIterator();
      }

      @Override
      public void forEach(Consumer<? super V> action) {
         Preconditions.checkNotNull(action);
         Map<K, V> delegate = CompactHashMap.this.delegateOrNull();
         if (delegate != null) {
            delegate.values().forEach(action);
         } else {
            for (int i = CompactHashMap.this.firstEntryIndex(); i >= 0; i = CompactHashMap.this.getSuccessor(i)) {
               action.accept(CompactHashMap.this.value(i));
            }
         }
      }

      @Override
      public Spliterator<V> spliterator() {
         if (CompactHashMap.this.needsAllocArrays()) {
            return Spliterators.spliterator(new Object[0], 16);
         }

         Map<K, V> delegate = CompactHashMap.this.delegateOrNull();
         return delegate != null
            ? delegate.values().spliterator()
            : Spliterators.spliterator(CompactHashMap.this.requireValues(), 0, CompactHashMap.this.size, 16);
      }

      @Override
      public @Nullable Object[] toArray() {
         if (CompactHashMap.this.needsAllocArrays()) {
            return new Object[0];
         }

         Map<K, V> delegate = CompactHashMap.this.delegateOrNull();
         return delegate != null
            ? delegate.values().toArray()
            : ObjectArrays.copyAsObjectArray(CompactHashMap.this.requireValues(), 0, CompactHashMap.this.size);
      }

      @Override
      public <T> T[] toArray(T[] a) {
         if (CompactHashMap.this.needsAllocArrays()) {
            if (a.length > 0) {
               Object[] unsoundlyCovariantArray = a;
               unsoundlyCovariantArray[0] = null;
            }

            return a;
         } else {
            Map<K, V> delegate = CompactHashMap.this.delegateOrNull();
            return (T[])(delegate != null
               ? delegate.values().toArray(a)
               : ObjectArrays.toArrayImpl(CompactHashMap.this.requireValues(), 0, CompactHashMap.this.size, a));
         }
      }
   }
}
