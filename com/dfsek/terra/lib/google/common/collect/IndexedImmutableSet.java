package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import java.util.Spliterator;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
abstract class IndexedImmutableSet<E> extends ImmutableSet.CachingAsList<E> {
   abstract E get(int index);

   @Override
   public UnmodifiableIterator<E> iterator() {
      return this.asList().iterator();
   }

   @Override
   public Spliterator<E> spliterator() {
      return CollectSpliterators.indexed(this.size(), 1297, this::get);
   }

   @Override
   public void forEach(Consumer<? super E> consumer) {
      Preconditions.checkNotNull(consumer);
      int n = this.size();

      for (int i = 0; i < n; i++) {
         consumer.accept(this.get(i));
      }
   }

   @GwtIncompatible
   @Override
   int copyIntoArray(@Nullable Object[] dst, int offset) {
      return this.asList().copyIntoArray(dst, offset);
   }

   @Override
   ImmutableList<E> createAsList() {
      return new ImmutableAsList<E>() {
         @Override
         public E get(int index) {
            return (E)IndexedImmutableSet.this.get(index);
         }

         @Override
         boolean isPartialView() {
            return IndexedImmutableSet.this.isPartialView();
         }

         @Override
         public int size() {
            return IndexedImmutableSet.this.size();
         }

         @Override
         ImmutableCollection<E> delegateCollection() {
            return IndexedImmutableSet.this;
         }

         @J2ktIncompatible
         @GwtIncompatible
         @Override
         Object writeReplace() {
            return super.writeReplace();
         }
      };
   }

   @J2ktIncompatible
   @GwtIncompatible
   @Override
   Object writeReplace() {
      return super.writeReplace();
   }
}
