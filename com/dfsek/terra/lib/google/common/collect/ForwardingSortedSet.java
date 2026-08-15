package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class ForwardingSortedSet<E> extends ForwardingSet<E> implements SortedSet<E> {
   protected ForwardingSortedSet() {
   }

   protected abstract SortedSet<E> delegate();

   @Override
   public @Nullable Comparator<? super E> comparator() {
      return this.delegate().comparator();
   }

   @ParametricNullness
   @Override
   public E first() {
      return this.delegate().first();
   }

   @Override
   public SortedSet<E> headSet(@ParametricNullness E toElement) {
      return this.delegate().headSet(toElement);
   }

   @ParametricNullness
   @Override
   public E last() {
      return this.delegate().last();
   }

   @Override
   public SortedSet<E> subSet(@ParametricNullness E fromElement, @ParametricNullness E toElement) {
      return this.delegate().subSet(fromElement, toElement);
   }

   @Override
   public SortedSet<E> tailSet(@ParametricNullness E fromElement) {
      return this.delegate().tailSet(fromElement);
   }

   @Override
   protected boolean standardContains(Object object) {
      try {
         SortedSet<Object> self = this;
         Object ceiling = self.tailSet(object).first();
         return ForwardingSortedMap.unsafeCompare(this.comparator(), ceiling, object) == 0;
      } catch (ClassCastException | NoSuchElementException | NullPointerException e) {
         return false;
      }
   }

   @Override
   protected boolean standardRemove(Object object) {
      try {
         SortedSet<Object> self = this;
         Iterator<?> iterator = self.tailSet(object).iterator();
         if (iterator.hasNext()) {
            Object ceiling = iterator.next();
            if (ForwardingSortedMap.unsafeCompare(this.comparator(), ceiling, object) == 0) {
               iterator.remove();
               return true;
            }
         }

         return false;
      } catch (ClassCastException | NullPointerException e) {
         return false;
      }
   }

   protected SortedSet<E> standardSubSet(@ParametricNullness E fromElement, @ParametricNullness E toElement) {
      return this.tailSet(fromElement).headSet(toElement);
   }
}
