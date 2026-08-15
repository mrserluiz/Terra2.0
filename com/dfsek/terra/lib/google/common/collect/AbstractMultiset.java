package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import org.jspecify.annotations.Nullable;

@GwtCompatible
abstract class AbstractMultiset<E> extends AbstractCollection<E> implements Multiset<E> {
   @LazyInit
   private transient @Nullable Set<E> elementSet;
   @LazyInit
   private transient @Nullable Set<Multiset.Entry<E>> entrySet;

   @Override
   public boolean isEmpty() {
      return this.entrySet().isEmpty();
   }

   @Override
   public boolean contains(@Nullable Object element) {
      return this.count(element) > 0;
   }

   @CanIgnoreReturnValue
   @Override
   public final boolean add(@ParametricNullness E element) {
      this.add(element, 1);
      return true;
   }

   @CanIgnoreReturnValue
   @Override
   public int add(@ParametricNullness E element, int occurrences) {
      throw new UnsupportedOperationException();
   }

   @CanIgnoreReturnValue
   @Override
   public final boolean remove(@Nullable Object element) {
      return this.remove(element, 1) > 0;
   }

   @CanIgnoreReturnValue
   @Override
   public int remove(@Nullable Object element, int occurrences) {
      throw new UnsupportedOperationException();
   }

   @CanIgnoreReturnValue
   @Override
   public int setCount(@ParametricNullness E element, int count) {
      return Multisets.setCountImpl(this, element, count);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean setCount(@ParametricNullness E element, int oldCount, int newCount) {
      return Multisets.setCountImpl(this, element, oldCount, newCount);
   }

   @CanIgnoreReturnValue
   @Override
   public final boolean addAll(Collection<? extends E> elementsToAdd) {
      return Multisets.addAllImpl(this, elementsToAdd);
   }

   @CanIgnoreReturnValue
   @Override
   public final boolean removeAll(Collection<?> elementsToRemove) {
      return Multisets.removeAllImpl(this, elementsToRemove);
   }

   @CanIgnoreReturnValue
   @Override
   public final boolean retainAll(Collection<?> elementsToRetain) {
      return Multisets.retainAllImpl(this, elementsToRetain);
   }

   @Override
   public abstract void clear();

   @Override
   public Set<E> elementSet() {
      Set<E> result = this.elementSet;
      if (result == null) {
         this.elementSet = result = this.createElementSet();
      }

      return result;
   }

   Set<E> createElementSet() {
      return new AbstractMultiset.ElementSet();
   }

   abstract Iterator<E> elementIterator();

   @Override
   public Set<Multiset.Entry<E>> entrySet() {
      Set<Multiset.Entry<E>> result = this.entrySet;
      if (result == null) {
         this.entrySet = result = this.createEntrySet();
      }

      return result;
   }

   Set<Multiset.Entry<E>> createEntrySet() {
      return new AbstractMultiset.EntrySet();
   }

   abstract Iterator<Multiset.Entry<E>> entryIterator();

   abstract int distinctElements();

   @Override
   public final boolean equals(@Nullable Object object) {
      return Multisets.equalsImpl(this, object);
   }

   @Override
   public final int hashCode() {
      return this.entrySet().hashCode();
   }

   @Override
   public final String toString() {
      return this.entrySet().toString();
   }

   class ElementSet extends Multisets.ElementSet<E> {
      @Override
      Multiset<E> multiset() {
         return AbstractMultiset.this;
      }

      @Override
      public Iterator<E> iterator() {
         return AbstractMultiset.this.elementIterator();
      }
   }

   class EntrySet extends Multisets.EntrySet<E> {
      @Override
      Multiset<E> multiset() {
         return AbstractMultiset.this;
      }

      @Override
      public Iterator<Multiset.Entry<E>> iterator() {
         return AbstractMultiset.this.entryIterator();
      }

      @Override
      public int size() {
         return AbstractMultiset.this.distinctElements();
      }
   }
}
