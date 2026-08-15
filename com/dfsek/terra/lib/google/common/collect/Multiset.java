package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CompatibleWith;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public interface Multiset<E> extends Collection<E> {
   @Override
   int size();

   int count(@CompatibleWith("E") @Nullable Object element);

   @CanIgnoreReturnValue
   int add(@ParametricNullness E element, int occurrences);

   @CanIgnoreReturnValue
   @Override
   boolean add(@ParametricNullness E element);

   @CanIgnoreReturnValue
   int remove(@CompatibleWith("E") @Nullable Object element, int occurrences);

   @CanIgnoreReturnValue
   @Override
   boolean remove(@Nullable Object element);

   @CanIgnoreReturnValue
   int setCount(@ParametricNullness E element, int count);

   @CanIgnoreReturnValue
   boolean setCount(@ParametricNullness E element, int oldCount, int newCount);

   Set<E> elementSet();

   Set<Multiset.Entry<E>> entrySet();

   default void forEachEntry(ObjIntConsumer<? super E> action) {
      Preconditions.checkNotNull(action);
      this.entrySet().forEach(entry -> action.accept(entry.getElement(), entry.getCount()));
   }

   @Override
   boolean equals(@Nullable Object object);

   @Override
   int hashCode();

   @Override
   String toString();

   @Override
   Iterator<E> iterator();

   @Override
   boolean contains(@Nullable Object element);

   @Override
   boolean containsAll(Collection<?> elements);

   @CanIgnoreReturnValue
   @Override
   boolean removeAll(Collection<?> c);

   @CanIgnoreReturnValue
   @Override
   boolean retainAll(Collection<?> c);

   @Override
   default void forEach(Consumer<? super E> action) {
      Preconditions.checkNotNull(action);
      this.entrySet().forEach(entry -> {
         E elem = entry.getElement();
         int count = entry.getCount();

         for (int i = 0; i < count; i++) {
            action.accept(elem);
         }
      });
   }

   @Override
   default Spliterator<E> spliterator() {
      return Multisets.spliteratorImpl(this);
   }

   interface Entry<E> {
      @ParametricNullness
      E getElement();

      int getCount();

      @Override
      boolean equals(@Nullable Object o);

      @Override
      int hashCode();

      @Override
      String toString();
   }
}
