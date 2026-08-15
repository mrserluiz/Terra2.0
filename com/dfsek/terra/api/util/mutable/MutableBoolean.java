package com.dfsek.terra.api.util.mutable;

import org.jetbrains.annotations.NotNull;

public class MutableBoolean implements MutablePrimitive<Boolean> {
   private boolean value;

   public MutableBoolean() {
      this.value = false;
   }

   public MutableBoolean(boolean value) {
      this.value = value;
   }

   public Boolean get() {
      return this.value;
   }

   public void set(Boolean value) {
      this.value = value;
   }

   public boolean invert() {
      this.value = !this.value;
      return this.value;
   }

   public int compareTo(@NotNull Boolean o) {
      return Boolean.compare(this.value, o);
   }
}
