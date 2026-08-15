package com.github.benmanes.caffeine.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class StrongInterner<E> implements Interner<E> {
   final ConcurrentMap<E, E> map = new ConcurrentHashMap<>();

   @Override
   public E intern(E sample) {
      E canonical = this.map.get(sample);
      if (canonical != null) {
         return canonical;
      }

      E value = this.map.putIfAbsent(sample, sample);
      return value == null ? sample : value;
   }
}
