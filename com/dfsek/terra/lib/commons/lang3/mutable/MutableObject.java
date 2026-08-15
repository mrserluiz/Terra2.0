package com.dfsek.terra.lib.commons.lang3.mutable;

import java.io.Serializable;
import java.util.Objects;

public class MutableObject<T> implements Mutable<T>, Serializable {
   private static final long serialVersionUID = 86241875189L;
   private T value;

   public MutableObject() {
   }

   public MutableObject(T value) {
      this.value = value;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (this == obj) {
         return true;
      } else if (this.getClass() == obj.getClass()) {
         MutableObject<?> that = (MutableObject<?>)obj;
         return Objects.equals(this.value, that.value);
      } else {
         return false;
      }
   }

   @Override
   public T getValue() {
      return this.value;
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.value);
   }

   @Override
   public void setValue(T value) {
      this.value = value;
   }

   @Override
   public String toString() {
      return Objects.toString(this.value);
   }
}
