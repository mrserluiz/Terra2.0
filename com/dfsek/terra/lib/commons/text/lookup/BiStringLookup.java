package com.dfsek.terra.lib.commons.text.lookup;

@FunctionalInterface
public interface BiStringLookup<U> extends StringLookup {
   default String lookup(String key, U object) {
      return this.lookup(key);
   }
}
