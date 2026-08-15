package com.dfsek.terra.lib.commons.text.lookup;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

final class FunctionStringLookup<V> extends AbstractStringLookup {
   private final Function<String, V> function;

   static <R> FunctionStringLookup<R> on(Function<String, R> function) {
      return new FunctionStringLookup<>(function);
   }

   static <V> FunctionStringLookup<V> on(Map<String, V> map) {
      return on(StringLookupFactory.toMap(map)::get);
   }

   private FunctionStringLookup(Function<String, V> function) {
      this.function = function;
   }

   @Override
   public String lookup(String key) {
      if (this.function == null) {
         return null;
      }

      V obj;
      try {
         obj = this.function.apply(key);
      } catch (SecurityException | NullPointerException | IllegalArgumentException e) {
         return null;
      }

      return Objects.toString(obj, null);
   }

   @Override
   public String toString() {
      return super.toString() + " [function=" + this.function + "]";
   }
}
