package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotCall;
import com.google.errorprone.annotations.DoNotMock;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;

@DoNotMock("Use ImmutableList.of or another implementation")
@GwtCompatible(emulated = true)
public abstract class ImmutableCollection<E> extends AbstractCollection<E> implements Serializable {
   static final int SPLITERATOR_CHARACTERISTICS = 1296;
   private static final Object[] EMPTY_ARRAY = new Object[0];
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = -889275714L;

   ImmutableCollection() {
   }

   public abstract UnmodifiableIterator<E> iterator();

   @Override
   public Spliterator<E> spliterator() {
      return Spliterators.spliterator(this, 1296);
   }

   @J2ktIncompatible
   @Override
   public final Object[] toArray() {
      return this.toArray(EMPTY_ARRAY);
   }

   @CanIgnoreReturnValue
   @Override
   public final <T> T[] toArray(T[] other) {
      Preconditions.checkNotNull((T)other);
      int size = this.size();
      if (other.length < size) {
         Object[] internal = this.internalArray();
         if (internal != null) {
            return (T[])Platform.copy(internal, this.internalArrayStart(), this.internalArrayEnd(), other);
         }

         other = (T[])ObjectArrays.newArray(other, size);
      } else if (other.length > size) {
         other[size] = null;
      }

      this.copyIntoArray(other, 0);
      return other;
   }

   Object @Nullable [] internalArray() {
      return null;
   }

   int internalArrayStart() {
      throw new UnsupportedOperationException();
   }

   int internalArrayEnd() {
      throw new UnsupportedOperationException();
   }

   @Override
   public abstract boolean contains(@Nullable Object object);

   @Deprecated
   @CanIgnoreReturnValue
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final boolean add(E e) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final boolean remove(@Nullable Object object) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final boolean addAll(Collection<? extends E> newElements) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final boolean removeAll(Collection<?> oldElements) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final boolean removeIf(Predicate<? super E> filter) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final boolean retainAll(Collection<?> elementsToKeep) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final void clear() {
      throw new UnsupportedOperationException();
   }

   public ImmutableList<E> asList() {
      switch (this.size()) {
         case 0:
            return ImmutableList.of();
         case 1:
            return ImmutableList.of(this.iterator().next());
         default:
            return new RegularImmutableAsList<>(this, this.toArray());
      }
   }

   abstract boolean isPartialView();

   @CanIgnoreReturnValue
   int copyIntoArray(@Nullable Object[] dst, int offset) {
      for (E e : this) {
         dst[offset++] = e;
      }

      return offset;
   }

   @J2ktIncompatible
   @GwtIncompatible
   Object writeReplace() {
      return new ImmutableList.SerializedForm(this.toArray());
   }

   @J2ktIncompatible
   private void readObject(ObjectInputStream stream) throws InvalidObjectException {
      throw new InvalidObjectException("Use SerializedForm");
   }

   @DoNotMock
   public abstract static class Builder<E> {
      static final int DEFAULT_INITIAL_CAPACITY = 4;

      static int expandedCapacity(int oldCapacity, int minCapacity) {
         if (minCapacity < 0) {
            throw new IllegalArgumentException("cannot store more than Integer.MAX_VALUE elements");
         }

         if (minCapacity <= oldCapacity) {
            return oldCapacity;
         }

         int newCapacity = oldCapacity + (oldCapacity >> 1) + 1;
         if (newCapacity < minCapacity) {
            newCapacity = Integer.highestOneBit(minCapacity - 1) << 1;
         }

         if (newCapacity < 0) {
            newCapacity = Integer.MAX_VALUE;
         }

         return newCapacity;
      }

      Builder() {
      }

      @CanIgnoreReturnValue
      public abstract ImmutableCollection.Builder<E> add(E element);

      @CanIgnoreReturnValue
      public ImmutableCollection.Builder<E> add(E... elements) {
         for (E element : elements) {
            this.add(element);
         }

         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableCollection.Builder<E> addAll(Iterable<? extends E> elements) {
         for (E element : elements) {
            this.add(element);
         }

         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableCollection.Builder<E> addAll(Iterator<? extends E> elements) {
         while (elements.hasNext()) {
            this.add((E)elements.next());
         }

         return this;
      }

      public abstract ImmutableCollection<E> build();
   }
}
