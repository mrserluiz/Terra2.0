package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import java.io.Serializable;

@GwtCompatible(serializable = true)
final class UsingToStringOrdering extends Ordering<Object> implements Serializable {
   static final UsingToStringOrdering INSTANCE = new UsingToStringOrdering();
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 0L;

   @Override
   public int compare(Object left, Object right) {
      return left.toString().compareTo(right.toString());
   }

   private Object readResolve() {
      return INSTANCE;
   }

   @Override
   public String toString() {
      return "Ordering.usingToString()";
   }

   private UsingToStringOrdering() {
   }
}
