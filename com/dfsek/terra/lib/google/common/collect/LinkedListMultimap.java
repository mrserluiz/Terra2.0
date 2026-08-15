package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

@GwtCompatible(serializable = true, emulated = true)
public class LinkedListMultimap<K, V> extends AbstractMultimap<K, V> implements ListMultimap<K, V>, Serializable {
   private transient LinkedListMultimap.@Nullable Node<K, V> head;
   private transient LinkedListMultimap.@Nullable Node<K, V> tail;
   private transient Map<K, LinkedListMultimap.KeyList<K, V>> keyToKeyList;
   private transient int size;
   private transient int modCount;
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 0L;

   public static <K, V> LinkedListMultimap<K, V> create() {
      return new LinkedListMultimap<>();
   }

   public static <K, V> LinkedListMultimap<K, V> create(int expectedKeys) {
      return new LinkedListMultimap<>(expectedKeys);
   }

   public static <K, V> LinkedListMultimap<K, V> create(Multimap<? extends K, ? extends V> multimap) {
      return new LinkedListMultimap<>(multimap);
   }

   LinkedListMultimap() {
      this(12);
   }

   private LinkedListMultimap(int expectedKeys) {
      this.keyToKeyList = Platform.newHashMapWithExpectedSize(expectedKeys);
   }

   private LinkedListMultimap(Multimap<? extends K, ? extends V> multimap) {
      this(multimap.keySet().size());
      this.putAll(multimap);
   }

   @CanIgnoreReturnValue
   private LinkedListMultimap.Node<K, V> addNode(@ParametricNullness K key, @ParametricNullness V value, LinkedListMultimap.@Nullable Node<K, V> nextSibling) {
      LinkedListMultimap.Node<K, V> node = new LinkedListMultimap.Node<>(key, value);
      if (this.head == null) {
         this.head = this.tail = node;
         this.keyToKeyList.put(key, new LinkedListMultimap.KeyList<>(node));
         this.modCount++;
      } else if (nextSibling == null) {
         Objects.requireNonNull(this.tail).next = node;
         node.previous = this.tail;
         this.tail = node;
         LinkedListMultimap.KeyList<K, V> keyList = this.keyToKeyList.get(key);
         if (keyList == null) {
            this.keyToKeyList.put(key, new LinkedListMultimap.KeyList<>(node));
            this.modCount++;
         } else {
            keyList.count++;
            LinkedListMultimap.Node<K, V> keyTail = keyList.tail;
            keyTail.nextSibling = node;
            node.previousSibling = keyTail;
            keyList.tail = node;
         }
      } else {
         LinkedListMultimap.KeyList<K, V> keyList = Objects.requireNonNull(this.keyToKeyList.get(key));
         keyList.count++;
         node.previous = nextSibling.previous;
         node.previousSibling = nextSibling.previousSibling;
         node.next = nextSibling;
         node.nextSibling = nextSibling;
         if (nextSibling.previousSibling == null) {
            keyList.head = node;
         } else {
            nextSibling.previousSibling.nextSibling = node;
         }

         if (nextSibling.previous == null) {
            this.head = node;
         } else {
            nextSibling.previous.next = node;
         }

         nextSibling.previous = node;
         nextSibling.previousSibling = node;
      }

      this.size++;
      return node;
   }

   private void removeNode(LinkedListMultimap.Node<K, V> node) {
      if (node.previous != null) {
         node.previous.next = node.next;
      } else {
         this.head = node.next;
      }

      if (node.next != null) {
         node.next.previous = node.previous;
      } else {
         this.tail = node.previous;
      }

      if (node.previousSibling == null && node.nextSibling == null) {
         LinkedListMultimap.KeyList<K, V> keyList = Objects.requireNonNull(this.keyToKeyList.remove(node.key));
         keyList.count = 0;
         this.modCount++;
      } else {
         LinkedListMultimap.KeyList<K, V> keyList = Objects.requireNonNull(this.keyToKeyList.get(node.key));
         keyList.count--;
         if (node.previousSibling == null) {
            keyList.head = Objects.requireNonNull(node.nextSibling);
         } else {
            node.previousSibling.nextSibling = node.nextSibling;
         }

         if (node.nextSibling == null) {
            keyList.tail = Objects.requireNonNull(node.previousSibling);
         } else {
            node.nextSibling.previousSibling = node.previousSibling;
         }
      }

      this.size--;
   }

   private void removeAllNodes(@ParametricNullness K key) {
      Iterators.clear(new LinkedListMultimap.ValueForKeyIterator(key));
   }

   @Override
   public int size() {
      return this.size;
   }

   @Override
   public boolean isEmpty() {
      return this.head == null;
   }

   @Override
   public boolean containsKey(@Nullable Object key) {
      return this.keyToKeyList.containsKey(key);
   }

   @Override
   public boolean containsValue(@Nullable Object value) {
      return this.values().contains(value);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean put(@ParametricNullness K key, @ParametricNullness V value) {
      this.addNode(key, value, null);
      return true;
   }

   @CanIgnoreReturnValue
   @Override
   public List<V> replaceValues(@ParametricNullness K key, Iterable<? extends V> values) {
      List<V> oldValues = this.getCopy(key);
      ListIterator<V> keyValues = new LinkedListMultimap.ValueForKeyIterator(key);
      Iterator<? extends V> newValues = values.iterator();

      while (keyValues.hasNext() && newValues.hasNext()) {
         keyValues.next();
         keyValues.set((V)newValues.next());
      }

      while (keyValues.hasNext()) {
         keyValues.next();
         keyValues.remove();
      }

      while (newValues.hasNext()) {
         keyValues.add((V)newValues.next());
      }

      return oldValues;
   }

   private List<V> getCopy(@ParametricNullness K key) {
      return Collections.unmodifiableList(Lists.newArrayList(new LinkedListMultimap.ValueForKeyIterator(key)));
   }

   @CanIgnoreReturnValue
   @Override
   public List<V> removeAll(@Nullable Object key) {
      K castKey = (K)key;
      List<V> oldValues = this.getCopy(castKey);
      this.removeAllNodes(castKey);
      return oldValues;
   }

   @Override
   public void clear() {
      this.head = null;
      this.tail = null;
      this.keyToKeyList.clear();
      this.size = 0;
      this.modCount++;
   }

   @Override
   public List<V> get(@ParametricNullness K key) {
      return new AbstractSequentialList<V>() {
         @Override
         public int size() {
            LinkedListMultimap.KeyList<K, V> keyList = LinkedListMultimap.this.keyToKeyList.get(key);
            return keyList == null ? 0 : keyList.count;
         }

         @Override
         public ListIterator<V> listIterator(int index) {
            return LinkedListMultimap.this.new ValueForKeyIterator(key, index);
         }
      };
   }

   @Override
   Set<K> createKeySet() {
      class KeySetImpl extends Sets.ImprovedAbstractSet<K> {
         @Override
         public int size() {
            return LinkedListMultimap.this.keyToKeyList.size();
         }

         @Override
         public Iterator<K> iterator() {
            return LinkedListMultimap.this.new DistinctKeyIterator();
         }

         @Override
         public boolean contains(@Nullable Object key) {
            return LinkedListMultimap.this.containsKey(key);
         }

         @Override
         public boolean remove(@Nullable Object o) {
            return !LinkedListMultimap.this.removeAll(o).isEmpty();
         }
      }

      return new KeySetImpl();
   }

   @Override
   Multiset<K> createKeys() {
      return new Multimaps.Keys<>(this);
   }

   public List<V> values() {
      return (List<V>)super.values();
   }

   List<V> createValues() {
      class ValuesImpl extends AbstractSequentialList<V> {
         @Override
         public int size() {
            return LinkedListMultimap.this.size;
         }

         @Override
         public ListIterator<V> listIterator(int index) {
            final LinkedListMultimap<K, V>.NodeIterator nodeItr = LinkedListMultimap.this.new NodeIterator(index);
            return new TransformedListIterator<Entry<K, V>, V>(nodeItr) {
               @ParametricNullness
               V transform(Entry<K, V> entry) {
                  return entry.getValue();
               }

               @Override
               public void set(@ParametricNullness V value) {
                  nodeItr.setValue(value);
               }
            };
         }
      }

      return new ValuesImpl();
   }

   public List<Entry<K, V>> entries() {
      return (List<Entry<K, V>>)super.entries();
   }

   List<Entry<K, V>> createEntries() {
      class EntriesImpl extends AbstractSequentialList<Entry<K, V>> {
         @Override
         public int size() {
            return LinkedListMultimap.this.size;
         }

         @Override
         public ListIterator<Entry<K, V>> listIterator(int index) {
            return LinkedListMultimap.this.new NodeIterator(index);
         }

         @Override
         public void forEach(Consumer<? super Entry<K, V>> action) {
            Preconditions.checkNotNull(action);

            for (LinkedListMultimap.Node<K, V> node = LinkedListMultimap.this.head; node != null; node = node.next) {
               action.accept(node);
            }
         }
      }

      return new EntriesImpl();
   }

   @Override
   Iterator<Entry<K, V>> entryIterator() {
      throw new AssertionError("should never be called");
   }

   @Override
   Map<K, Collection<V>> createAsMap() {
      return new Multimaps.AsMap<>(this);
   }

   @GwtIncompatible
   @J2ktIncompatible
   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
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
      this.keyToKeyList = Maps.newLinkedHashMap();
      int size = stream.readInt();

      for (int i = 0; i < size; i++) {
         K key = (K)stream.readObject();
         V value = (V)stream.readObject();
         this.put(key, value);
      }
   }

   private class DistinctKeyIterator implements Iterator<K> {
      final Set<K> seenKeys = Sets.newHashSetWithExpectedSize(LinkedListMultimap.this.keySet().size());
      LinkedListMultimap.@Nullable Node<K, V> next = LinkedListMultimap.this.head;
      LinkedListMultimap.@Nullable Node<K, V> current;
      int expectedModCount = LinkedListMultimap.this.modCount;

      private DistinctKeyIterator() {
      }

      private void checkForConcurrentModification() {
         if (LinkedListMultimap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         }
      }

      @Override
      public boolean hasNext() {
         this.checkForConcurrentModification();
         return this.next != null;
      }

      @ParametricNullness
      @Override
      public K next() {
         this.checkForConcurrentModification();
         if (this.next == null) {
            throw new NoSuchElementException();
         }

         this.current = this.next;
         this.seenKeys.add(this.current.key);

         do {
            this.next = this.next.next;
         } while (this.next != null && !this.seenKeys.add(this.next.key));

         return this.current.key;
      }

      @Override
      public void remove() {
         this.checkForConcurrentModification();
         Preconditions.checkState(this.current != null, "no calls to next() since the last call to remove()");
         LinkedListMultimap.this.removeAllNodes(this.current.key);
         this.current = null;
         this.expectedModCount = LinkedListMultimap.this.modCount;
      }
   }

   private static class KeyList<K, V> {
      LinkedListMultimap.Node<K, V> head;
      LinkedListMultimap.Node<K, V> tail;
      int count;

      KeyList(LinkedListMultimap.Node<K, V> firstNode) {
         this.head = firstNode;
         this.tail = firstNode;
         firstNode.previousSibling = null;
         firstNode.nextSibling = null;
         this.count = 1;
      }
   }

   static final class Node<K, V> extends AbstractMapEntry<K, V> {
      @ParametricNullness
      final K key;
      @ParametricNullness
      V value;
      LinkedListMultimap.@Nullable Node<K, V> next;
      LinkedListMultimap.@Nullable Node<K, V> previous;
      LinkedListMultimap.@Nullable Node<K, V> nextSibling;
      LinkedListMultimap.@Nullable Node<K, V> previousSibling;

      Node(@ParametricNullness K key, @ParametricNullness V value) {
         this.key = key;
         this.value = value;
      }

      @ParametricNullness
      @Override
      public K getKey() {
         return this.key;
      }

      @ParametricNullness
      @Override
      public V getValue() {
         return this.value;
      }

      @ParametricNullness
      @Override
      public V setValue(@ParametricNullness V newValue) {
         V result = this.value;
         this.value = newValue;
         return result;
      }
   }

   private class NodeIterator implements ListIterator<Entry<K, V>> {
      int nextIndex;
      LinkedListMultimap.@Nullable Node<K, V> next;
      LinkedListMultimap.@Nullable Node<K, V> current;
      LinkedListMultimap.@Nullable Node<K, V> previous;
      int expectedModCount = LinkedListMultimap.this.modCount;

      NodeIterator(int index) {
         int size = LinkedListMultimap.this.size();
         Preconditions.checkPositionIndex(index, size);
         if (index >= size / 2) {
            this.previous = LinkedListMultimap.this.tail;
            this.nextIndex = size;

            while (index++ < size) {
               this.previous();
            }
         } else {
            this.next = LinkedListMultimap.this.head;

            while (index-- > 0) {
               this.next();
            }
         }

         this.current = null;
      }

      private void checkForConcurrentModification() {
         if (LinkedListMultimap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         }
      }

      @Override
      public boolean hasNext() {
         this.checkForConcurrentModification();
         return this.next != null;
      }

      @CanIgnoreReturnValue
      public LinkedListMultimap.Node<K, V> next() {
         this.checkForConcurrentModification();
         if (this.next == null) {
            throw new NoSuchElementException();
         }

         this.previous = this.current = this.next;
         this.next = this.next.next;
         this.nextIndex++;
         return this.current;
      }

      @Override
      public void remove() {
         this.checkForConcurrentModification();
         Preconditions.checkState(this.current != null, "no calls to next() since the last call to remove()");
         if (this.current != this.next) {
            this.previous = this.current.previous;
            this.nextIndex--;
         } else {
            this.next = this.current.next;
         }

         LinkedListMultimap.this.removeNode(this.current);
         this.current = null;
         this.expectedModCount = LinkedListMultimap.this.modCount;
      }

      @Override
      public boolean hasPrevious() {
         this.checkForConcurrentModification();
         return this.previous != null;
      }

      @CanIgnoreReturnValue
      public LinkedListMultimap.Node<K, V> previous() {
         this.checkForConcurrentModification();
         if (this.previous == null) {
            throw new NoSuchElementException();
         }

         this.next = this.current = this.previous;
         this.previous = this.previous.previous;
         this.nextIndex--;
         return this.current;
      }

      @Override
      public int nextIndex() {
         return this.nextIndex;
      }

      @Override
      public int previousIndex() {
         return this.nextIndex - 1;
      }

      public void set(Entry<K, V> e) {
         throw new UnsupportedOperationException();
      }

      public void add(Entry<K, V> e) {
         throw new UnsupportedOperationException();
      }

      void setValue(@ParametricNullness V value) {
         Preconditions.checkState(this.current != null);
         this.current.value = value;
      }
   }

   private class ValueForKeyIterator implements ListIterator<V> {
      @ParametricNullness
      final Object key;
      int nextIndex;
      LinkedListMultimap.@Nullable Node<K, V> next;
      LinkedListMultimap.@Nullable Node<K, V> current;
      LinkedListMultimap.@Nullable Node<K, V> previous;

      ValueForKeyIterator(@ParametricNullness K key) {
         this.key = key;
         LinkedListMultimap.KeyList<K, V> keyList = LinkedListMultimap.this.keyToKeyList.get(key);
         this.next = keyList == null ? null : keyList.head;
      }

      public ValueForKeyIterator(@ParametricNullness K key, int index) {
         LinkedListMultimap.KeyList<K, V> keyList = LinkedListMultimap.this.keyToKeyList.get(key);
         int size = keyList == null ? 0 : keyList.count;
         Preconditions.checkPositionIndex(index, size);
         if (index >= size / 2) {
            this.previous = keyList == null ? null : keyList.tail;
            this.nextIndex = size;

            while (index++ < size) {
               this.previous();
            }
         } else {
            this.next = keyList == null ? null : keyList.head;

            while (index-- > 0) {
               this.next();
            }
         }

         this.key = key;
         this.current = null;
      }

      @Override
      public boolean hasNext() {
         return this.next != null;
      }

      @CanIgnoreReturnValue
      @ParametricNullness
      @Override
      public V next() {
         if (this.next == null) {
            throw new NoSuchElementException();
         }

         this.previous = this.current = this.next;
         this.next = this.next.nextSibling;
         this.nextIndex++;
         return this.current.value;
      }

      @Override
      public boolean hasPrevious() {
         return this.previous != null;
      }

      @CanIgnoreReturnValue
      @ParametricNullness
      @Override
      public V previous() {
         if (this.previous == null) {
            throw new NoSuchElementException();
         }

         this.next = this.current = this.previous;
         this.previous = this.previous.previousSibling;
         this.nextIndex--;
         return this.current.value;
      }

      @Override
      public int nextIndex() {
         return this.nextIndex;
      }

      @Override
      public int previousIndex() {
         return this.nextIndex - 1;
      }

      @Override
      public void remove() {
         Preconditions.checkState(this.current != null, "no calls to next() since the last call to remove()");
         if (this.current != this.next) {
            this.previous = this.current.previousSibling;
            this.nextIndex--;
         } else {
            this.next = this.current.nextSibling;
         }

         LinkedListMultimap.this.removeNode(this.current);
         this.current = null;
      }

      @Override
      public void set(@ParametricNullness V value) {
         Preconditions.checkState(this.current != null);
         this.current.value = value;
      }

      @Override
      public void add(@ParametricNullness V value) {
         this.previous = LinkedListMultimap.this.addNode((K)this.key, value, this.next);
         this.nextIndex++;
         this.current = null;
      }
   }
}
