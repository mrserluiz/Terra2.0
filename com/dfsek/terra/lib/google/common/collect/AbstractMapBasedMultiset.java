package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.primitives.Ints;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.ObjIntConsumer;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
abstract class AbstractMapBasedMultiset<E> extends AbstractMultiset<E> implements Serializable {
   private transient Map<E, Count> backingMap;
   private transient long size;
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = -2250766705698539974L;

   protected AbstractMapBasedMultiset(Map<E, Count> backingMap) {
      Preconditions.checkArgument(backingMap.isEmpty());
      this.backingMap = backingMap;
   }

   void setBackingMap(Map<E, Count> backingMap) {
      this.backingMap = backingMap;
   }

   @Override
   public Set<Multiset.Entry<E>> entrySet() {
      return super.entrySet();
   }

   @Override
   Iterator<E> elementIterator() {
      final Iterator<Map.Entry<E, Count>> backingEntries = this.backingMap.entrySet().iterator();
      return new Iterator<E>() {
         @Nullable Entry<E, Count> toRemove;

         @Override
         public boolean hasNext() {
            return backingEntries.hasNext();
         }

         @ParametricNullness
         @Override
         public E next() {
            Map.Entry<E, Count> mapEntry = backingEntries.next();
            this.toRemove = mapEntry;
            return mapEntry.getKey();
         }

         @Override
         public void remove() {
            Preconditions.checkState(this.toRemove != null, "no calls to next() since the last call to remove()");
            AbstractMapBasedMultiset.this.size -= this.toRemove.getValue().getAndSet(0);
            backingEntries.remove();
            this.toRemove = null;
         }
      };
   }

   @Override
   Iterator<Multiset.Entry<E>> entryIterator() {
      final Iterator<Map.Entry<E, Count>> backingEntries = this.backingMap.entrySet().iterator();
      return new Iterator<Multiset.Entry<E>>() {
         @Nullable Entry<E, Count> toRemove;

         @Override
         public boolean hasNext() {
            return backingEntries.hasNext();
         }

         public Multiset.Entry<E> next() {
            final Map.Entry<E, Count> mapEntry = backingEntries.next();
            this.toRemove = mapEntry;
            return new Multisets.AbstractEntry<E>() {
               @ParametricNullness
               @Override
               public E getElement() {
                  return mapEntry.getKey();
               }

               @Override
               public int getCount() {
                  Count count = mapEntry.getValue();
                  if (count == null || count.get() == 0) {
                     Count frequency = AbstractMapBasedMultiset.this.backingMap.get(this.getElement());
                     if (frequency != null) {
                        return frequency.get();
                     }
                  }

                  return count == null ? 0 : count.get();
               }
            };
         }

         @Override
         public void remove() {
            Preconditions.checkState(this.toRemove != null, "no calls to next() since the last call to remove()");
            AbstractMapBasedMultiset.this.size -= this.toRemove.getValue().getAndSet(0);
            backingEntries.remove();
            this.toRemove = null;
         }
      };
   }

   @Override
   public void forEachEntry(ObjIntConsumer<? super E> action) {
      Preconditions.checkNotNull(action);
      this.backingMap.forEach((element, count) -> action.accept(element, count.get()));
   }

   @Override
   public void clear() {
      for (Count frequency : this.backingMap.values()) {
         frequency.set(0);
      }

      this.backingMap.clear();
      this.size = 0L;
   }

   @Override
   int distinctElements() {
      return this.backingMap.size();
   }

   @Override
   public int size() {
      return Ints.saturatedCast(this.size);
   }

   @Override
   public Iterator<E> iterator() {
      return new AbstractMapBasedMultiset.MapBasedMultisetIterator();
   }

   @Override
   public int count(@Nullable Object element) {
      Count frequency = Maps.safeGet(this.backingMap, element);
      return frequency == null ? 0 : frequency.get();
   }

   @CanIgnoreReturnValue
   @Override
   public int add(@ParametricNullness E element, int occurrences) {
      if (occurrences == 0) {
         return this.count(element);
      }

      Preconditions.checkArgument(occurrences > 0, "occurrences cannot be negative: %s", occurrences);
      Count frequency = this.backingMap.get(element);
      int oldCount;
      if (frequency == null) {
         oldCount = 0;
         this.backingMap.put(element, new Count(occurrences));
      } else {
         oldCount = frequency.get();
         long newCount = (long)oldCount + occurrences;
         Preconditions.checkArgument(newCount <= 2147483647L, "too many occurrences: %s", newCount);
         frequency.add(occurrences);
      }

      this.size += occurrences;
      return oldCount;
   }

   @CanIgnoreReturnValue
   @Override
   public int remove(@Nullable Object element, int occurrences) {
      if (occurrences == 0) {
         return this.count(element);
      }

      Preconditions.checkArgument(occurrences > 0, "occurrences cannot be negative: %s", occurrences);
      Count frequency = this.backingMap.get(element);
      if (frequency == null) {
         return 0;
      }

      int oldCount = frequency.get();
      int numberRemoved;
      if (oldCount > occurrences) {
         numberRemoved = occurrences;
      } else {
         numberRemoved = oldCount;
         this.backingMap.remove(element);
      }

      frequency.add(-numberRemoved);
      this.size -= numberRemoved;
      return oldCount;
   }

   @CanIgnoreReturnValue
   @Override
   public int setCount(@ParametricNullness E element, int count) {
      CollectPreconditions.checkNonnegative(count, "count");
      int oldCount;
      if (count == 0) {
         Count existingCounter = this.backingMap.remove(element);
         oldCount = getAndSet(existingCounter, count);
      } else {
         Count existingCounter = this.backingMap.get(element);
         oldCount = getAndSet(existingCounter, count);
         if (existingCounter == null) {
            this.backingMap.put(element, new Count(count));
         }
      }

      this.size += count - oldCount;
      return oldCount;
   }

   private static int getAndSet(@Nullable Count i, int count) {
      return i == null ? 0 : i.getAndSet(count);
   }

   @GwtIncompatible
   @J2ktIncompatible
   private void readObjectNoData() throws ObjectStreamException {
      throw new InvalidObjectException("Stream data required");
   }

   private class MapBasedMultisetIterator implements Iterator<E> {
      final Iterator<Map.Entry<E, Count>> entryIterator = AbstractMapBasedMultiset.this.backingMap.entrySet().iterator();
      @Nullable Entry<E, Count> currentEntry;
      int occurrencesLeft;
      boolean canRemove;

      MapBasedMultisetIterator() {
      }

      @Override
      public boolean hasNext() {
         return this.occurrencesLeft > 0 || this.entryIterator.hasNext();
      }

      @ParametricNullness
      @Override
      public E next() {
         if (this.occurrencesLeft == 0) {
            this.currentEntry = this.entryIterator.next();
            this.occurrencesLeft = this.currentEntry.getValue().get();
         }

         this.occurrencesLeft--;
         this.canRemove = true;
         return Objects.requireNonNull(this.currentEntry).getKey();
      }

      @Override
      public void remove() {
         CollectPreconditions.checkRemove(this.canRemove);
         int frequency = Objects.requireNonNull(this.currentEntry).getValue().get();
         if (frequency <= 0) {
            throw new ConcurrentModificationException();
         }

         if (this.currentEntry.getValue().addAndGet(-1) == 0) {
            this.entryIterator.remove();
         }

         AbstractMapBasedMultiset.this.size--;
         this.canRemove = false;
      }
   }
}
