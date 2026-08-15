package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

@GwtCompatible(serializable = true, emulated = true)
final class SingletonImmutableSet<E> extends ImmutableSet<E> {
   final transient E element;

   SingletonImmutableSet(E element) {
      this.element = Preconditions.checkNotNull(element);
   }

   @Override
   public int size() {
      return 1;
   }

   @Override
   public boolean contains(@Nullable Object target) {
      return this.element.equals(target);
   }

   @Override
   public UnmodifiableIterator<E> iterator() {
      return Iterators.singletonIterator(this.element);
   }

   @Override
   public ImmutableList<E> asList() {
      return ImmutableList.of(this.element);
   }

   @Override
   boolean isPartialView() {
      return false;
   }

   @Override
   int copyIntoArray(@Nullable Object[] dst, int offset) {
      dst[offset] = this.element;
      return offset + 1;
   }

   @Override
   public final int hashCode() {
      return this.element.hashCode();
   }

   @Override
   public String toString() {
      return '[' + this.element.toString() + ']';
   }

   @J2ktIncompatible
   @GwtIncompatible
   @Override
   Object writeReplace() {
      return super.writeReplace();
   }
}
