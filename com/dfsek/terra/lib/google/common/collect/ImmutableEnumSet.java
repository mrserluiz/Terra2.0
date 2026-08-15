package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Spliterator;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

@GwtCompatible(serializable = true, emulated = true)
final class ImmutableEnumSet<E extends Enum<E>> extends ImmutableSet<E> {
   private final transient EnumSet<E> delegate;
   @LazyInit
   private transient int hashCode;

   static <E extends Enum<E>> ImmutableSet<E> asImmutable(EnumSet<E> set) {
      switch (set.size()) {
         case 0:
            return ImmutableSet.of();
         case 1:
            return ImmutableSet.of(Iterables.getOnlyElement(set));
         default:
            return new ImmutableEnumSet<>(set);
      }
   }

   private ImmutableEnumSet(EnumSet<E> delegate) {
      this.delegate = delegate;
   }

   @Override
   boolean isPartialView() {
      return false;
   }

   @Override
   public UnmodifiableIterator<E> iterator() {
      return Iterators.unmodifiableIterator(this.delegate.iterator());
   }

   @Override
   public Spliterator<E> spliterator() {
      return this.delegate.spliterator();
   }

   @Override
   public void forEach(Consumer<? super E> action) {
      this.delegate.forEach(action);
   }

   @Override
   public int size() {
      return this.delegate.size();
   }

   @Override
   public boolean contains(@Nullable Object object) {
      return this.delegate.contains(object);
   }

   @Override
   public boolean containsAll(Collection<?> collection) {
      if (collection instanceof ImmutableEnumSet) {
         collection = ((ImmutableEnumSet)collection).delegate;
      }

      return this.delegate.containsAll(collection);
   }

   @Override
   public boolean isEmpty() {
      return this.delegate.isEmpty();
   }

   @Override
   public boolean equals(@Nullable Object object) {
      if (object == this) {
         return true;
      }

      if (object instanceof ImmutableEnumSet) {
         object = ((ImmutableEnumSet)object).delegate;
      }

      return this.delegate.equals(object);
   }

   @Override
   boolean isHashCodeFast() {
      return true;
   }

   @Override
   public int hashCode() {
      int result = this.hashCode;
      return result == 0 ? (this.hashCode = this.delegate.hashCode()) : result;
   }

   @Override
   public String toString() {
      return this.delegate.toString();
   }

   @J2ktIncompatible
   @Override
   Object writeReplace() {
      return new ImmutableEnumSet.EnumSerializedForm<>(this.delegate);
   }

   @J2ktIncompatible
   private void readObject(ObjectInputStream stream) throws InvalidObjectException {
      throw new InvalidObjectException("Use SerializedForm");
   }

   @J2ktIncompatible
   private static class EnumSerializedForm<E extends Enum<E>> implements Serializable {
      final EnumSet<E> delegate;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      EnumSerializedForm(EnumSet<E> delegate) {
         this.delegate = delegate;
      }

      Object readResolve() {
         return new ImmutableEnumSet(this.delegate.clone());
      }
   }
}
