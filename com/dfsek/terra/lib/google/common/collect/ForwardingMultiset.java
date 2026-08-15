package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Objects;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class ForwardingMultiset<E> extends ForwardingCollection<E> implements Multiset<E> {
   protected ForwardingMultiset() {
   }

   protected abstract Multiset<E> delegate();

   @Override
   public int count(@Nullable Object element) {
      return this.delegate().count(element);
   }

   @CanIgnoreReturnValue
   @Override
   public int add(@ParametricNullness E element, int occurrences) {
      return this.delegate().add(element, occurrences);
   }

   @CanIgnoreReturnValue
   @Override
   public int remove(@Nullable Object element, int occurrences) {
      return this.delegate().remove(element, occurrences);
   }

   @Override
   public Set<E> elementSet() {
      return this.delegate().elementSet();
   }

   @Override
   public Set<Multiset.Entry<E>> entrySet() {
      return this.delegate().entrySet();
   }

   @Override
   public boolean equals(@Nullable Object object) {
      return object == this || this.delegate().equals(object);
   }

   @Override
   public int hashCode() {
      return this.delegate().hashCode();
   }

   @CanIgnoreReturnValue
   @Override
   public int setCount(@ParametricNullness E element, int count) {
      return this.delegate().setCount(element, count);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean setCount(@ParametricNullness E element, int oldCount, int newCount) {
      return this.delegate().setCount(element, oldCount, newCount);
   }

   @Override
   protected boolean standardContains(@Nullable Object object) {
      return this.count(object) > 0;
   }

   @Override
   protected void standardClear() {
      Iterators.clear(this.entrySet().iterator());
   }

   protected int standardCount(@Nullable Object object) {
      for (Multiset.Entry<?> entry : this.entrySet()) {
         if (Objects.equal(entry.getElement(), object)) {
            return entry.getCount();
         }
      }

      return 0;
   }

   protected boolean standardAdd(@ParametricNullness E element) {
      this.add(element, 1);
      return true;
   }

   @Override
   protected boolean standardAddAll(Collection<? extends E> elementsToAdd) {
      return Multisets.addAllImpl(this, elementsToAdd);
   }

   @Override
   protected boolean standardRemove(@Nullable Object element) {
      return this.remove(element, 1) > 0;
   }

   @Override
   protected boolean standardRemoveAll(Collection<?> elementsToRemove) {
      return Multisets.removeAllImpl(this, elementsToRemove);
   }

   @Override
   protected boolean standardRetainAll(Collection<?> elementsToRetain) {
      return Multisets.retainAllImpl(this, elementsToRetain);
   }

   protected int standardSetCount(@ParametricNullness E element, int count) {
      return Multisets.setCountImpl(this, element, count);
   }

   protected boolean standardSetCount(@ParametricNullness E element, int oldCount, int newCount) {
      return Multisets.setCountImpl(this, element, oldCount, newCount);
   }

   protected Iterator<E> standardIterator() {
      return Multisets.iteratorImpl(this);
   }

   protected int standardSize() {
      return Multisets.linearTimeSizeImpl(this);
   }

   protected boolean standardEquals(@Nullable Object object) {
      return Multisets.equalsImpl(this, object);
   }

   protected int standardHashCode() {
      return this.entrySet().hashCode();
   }

   @Override
   protected String standardToString() {
      return this.entrySet().toString();
   }

   protected class StandardElementSet extends Multisets.ElementSet<E> {
      public StandardElementSet() {
      }

      @Override
      Multiset<E> multiset() {
         return ForwardingMultiset.this;
      }

      @Override
      public Iterator<E> iterator() {
         return Multisets.elementIterator(this.multiset().entrySet().iterator());
      }
   }
}
