package com.dfsek.terra.lib.commons.lang3.mutable;

import com.dfsek.terra.lib.commons.lang3.BooleanUtils;
import java.io.Serializable;

public class MutableBoolean implements Mutable<Boolean>, Serializable, Comparable<MutableBoolean> {
   private static final long serialVersionUID = -4830728138360036487L;
   private boolean value;

   public MutableBoolean() {
   }

   public MutableBoolean(boolean value) {
      this.value = value;
   }

   public MutableBoolean(Boolean value) {
      this.value = value;
   }

   public boolean booleanValue() {
      return this.value;
   }

   public int compareTo(MutableBoolean other) {
      return BooleanUtils.compare(this.value, other.value);
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof MutableBoolean ? this.value == ((MutableBoolean)obj).booleanValue() : false;
   }

   public Boolean getValue() {
      return this.value;
   }

   @Override
   public int hashCode() {
      return this.value ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode();
   }

   public boolean isFalse() {
      return !this.value;
   }

   public boolean isTrue() {
      return this.value;
   }

   public void setFalse() {
      this.value = false;
   }

   public void setTrue() {
      this.value = true;
   }

   public void setValue(boolean value) {
      this.value = value;
   }

   public void setValue(Boolean value) {
      this.value = value;
   }

   public Boolean toBoolean() {
      return this.booleanValue();
   }

   @Override
   public String toString() {
      return String.valueOf(this.value);
   }
}
