package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class ForwardingList<E> extends ForwardingCollection<E> implements List<E> {
   protected ForwardingList() {
   }

   protected abstract List<E> delegate();

   @Override
   public void add(int index, @ParametricNullness E element) {
      this.delegate().add(index, element);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean addAll(int index, Collection<? extends E> elements) {
      return this.delegate().addAll(index, elements);
   }

   @ParametricNullness
   @Override
   public E get(int index) {
      return this.delegate().get(index);
   }

   @Override
   public int indexOf(@Nullable Object element) {
      return this.delegate().indexOf(element);
   }

   @Override
   public int lastIndexOf(@Nullable Object element) {
      return this.delegate().lastIndexOf(element);
   }

   @Override
   public ListIterator<E> listIterator() {
      return this.delegate().listIterator();
   }

   @Override
   public ListIterator<E> listIterator(int index) {
      return this.delegate().listIterator(index);
   }

   @CanIgnoreReturnValue
   @ParametricNullness
   @Override
   public E remove(int index) {
      return this.delegate().remove(index);
   }

   @CanIgnoreReturnValue
   @ParametricNullness
   @Override
   public E set(int index, @ParametricNullness E element) {
      return this.delegate().set(index, element);
   }

   @Override
   public List<E> subList(int fromIndex, int toIndex) {
      return this.delegate().subList(fromIndex, toIndex);
   }

   @Override
   public boolean equals(@Nullable Object object) {
      return object == this || this.delegate().equals(object);
   }

   @Override
   public int hashCode() {
      return this.delegate().hashCode();
   }

   protected boolean standardAdd(@ParametricNullness E element) {
      this.add(this.size(), element);
      return true;
   }

   protected boolean standardAddAll(int index, Iterable<? extends E> elements) {
      return Lists.addAllImpl(this, index, elements);
   }

   protected int standardIndexOf(@Nullable Object element) {
      return Lists.indexOfImpl(this, element);
   }

   protected int standardLastIndexOf(@Nullable Object element) {
      return Lists.lastIndexOfImpl(this, element);
   }

   protected Iterator<E> standardIterator() {
      return this.listIterator();
   }

   protected ListIterator<E> standardListIterator() {
      return this.listIterator(0);
   }

   protected ListIterator<E> standardListIterator(int start) {
      return Lists.listIteratorImpl(this, start);
   }

   protected List<E> standardSubList(int fromIndex, int toIndex) {
      return Lists.subListImpl(this, fromIndex, toIndex);
   }

   protected boolean standardEquals(@Nullable Object object) {
      return Lists.equalsImpl(this, object);
   }

   protected int standardHashCode() {
      return Lists.hashCodeImpl(this);
   }
}
