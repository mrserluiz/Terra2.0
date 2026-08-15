package com.dfsek.terra.lib.commons.text.lookup;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

final class BiFunctionStringLookup<P, R> implements BiStringLookup<P> {
   private final BiFunction<String, P, R> biFunction;

   static <U, T> BiFunctionStringLookup<U, T> on(BiFunction<String, U, T> biFunction) {
      return new BiFunctionStringLookup<>(biFunction);
   }

   static <U, T> BiFunctionStringLookup<U, T> on(Map<String, T> map) {
      return on((key, u) -> map.get(key));
   }

   private BiFunctionStringLookup(BiFunction<String, P, R> biFunction) {
      this.biFunction = biFunction;
   }

   @Override
   public String lookup(String key) {
      return this.lookup(key, null);
   }

   @Override
   public String lookup(String key, P object) {
      if (this.biFunction == null) {
         return null;
      }

      R obj;
      try {
         obj = this.biFunction.apply(key, object);
      } catch (SecurityException | NullPointerException | IllegalArgumentException e) {
         return null;
      }

      return Objects.toString(obj, null);
   }

   @Override
   public String toString() {
      return super.toString() + " [function=" + this.biFunction + "]";
   }
}
