package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;

@GwtCompatible
public enum BoundType {
   OPEN(false),
   CLOSED(true);

   final boolean inclusive;

   BoundType(boolean inclusive) {
      this.inclusive = inclusive;
   }

   static BoundType forBoolean(boolean inclusive) {
      return inclusive ? CLOSED : OPEN;
   }
}
