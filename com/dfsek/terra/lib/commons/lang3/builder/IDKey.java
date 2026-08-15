package com.dfsek.terra.lib.commons.lang3.builder;

final class IDKey {
   private final Object value;
   private final int id;

   IDKey(Object value) {
      this.id = System.identityHashCode(value);
      this.value = value;
   }

   @Override
   public boolean equals(Object other) {
      if (!(other instanceof IDKey)) {
         return false;
      }

      IDKey idKey = (IDKey)other;
      return this.id != idKey.id ? false : this.value == idKey.value;
   }

   @Override
   public int hashCode() {
      return this.id;
   }
}
