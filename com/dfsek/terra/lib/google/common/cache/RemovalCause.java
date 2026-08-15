package com.dfsek.terra.lib.google.common.cache;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;

@GwtCompatible
public enum RemovalCause {
   EXPLICIT {
      @Override
      boolean wasEvicted() {
         return false;
      }
   },
   REPLACED {
      @Override
      boolean wasEvicted() {
         return false;
      }
   },
   COLLECTED {
      @Override
      boolean wasEvicted() {
         return true;
      }
   },
   EXPIRED {
      @Override
      boolean wasEvicted() {
         return true;
      }
   },
   SIZE {
      @Override
      boolean wasEvicted() {
         return true;
      }
   };

   RemovalCause() {
   }

   abstract boolean wasEvicted();
}
