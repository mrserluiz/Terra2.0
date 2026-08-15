package com.dfsek.paralithic.util;

import java.util.function.Supplier;

public class Lazy<T> {
   private final Supplier<T> supplier;
   private T value;
   private boolean computed = false;

   private Lazy(Supplier<T> supplier) {
      this.supplier = supplier;
   }

   public T get() {
      if (!this.computed) {
         this.computed = true;
         this.value = this.supplier.get();
      }

      return this.value;
   }

   public void invalidate() {
      this.computed = false;
   }

   public static <T> Lazy<T> of(Supplier<T> supplier) {
      return new Lazy<>(supplier);
   }
}
