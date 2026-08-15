package com.github.benmanes.caffeine.cache;

import org.jspecify.annotations.Nullable;

final class AccessOrderDeque<E extends AccessOrderDeque.AccessOrder<E>> extends AbstractLinkedDeque<E> {
   @Override
   public boolean contains(Object o) {
      return o instanceof AccessOrderDeque.AccessOrder && this.contains((AccessOrderDeque.AccessOrder<?>)o);
   }

   boolean contains(AccessOrderDeque.AccessOrder<?> e) {
      return e.getPreviousInAccessOrder() != null || e.getNextInAccessOrder() != null || e == this.first;
   }

   @Override
   public boolean remove(Object o) {
      return o instanceof AccessOrderDeque.AccessOrder && this.remove((E)o);
   }

   boolean remove(E e) {
      if (this.contains(e)) {
         this.unlink(e);
         return true;
      } else {
         return false;
      }
   }

   public @Nullable E getPrevious(E e) {
      return e.getPreviousInAccessOrder();
   }

   public void setPrevious(E e, @Nullable E prev) {
      e.setPreviousInAccessOrder(prev);
   }

   public @Nullable E getNext(E e) {
      return e.getNextInAccessOrder();
   }

   public void setNext(E e, @Nullable E next) {
      e.setNextInAccessOrder(next);
   }

   interface AccessOrder<T extends AccessOrderDeque.AccessOrder<T>> {
      @Nullable T getPreviousInAccessOrder();

      void setPreviousInAccessOrder(@Nullable T prev);

      @Nullable T getNextInAccessOrder();

      void setNextInAccessOrder(@Nullable T next);
   }
}
