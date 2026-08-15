package com.dfsek.terra.lib.google.common.io;

import com.dfsek.terra.lib.google.common.annotations.Beta;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import java.io.Flushable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@J2ktIncompatible
@GwtIncompatible
public final class Flushables {
   private static final Logger logger = Logger.getLogger(Flushables.class.getName());

   private Flushables() {
   }

   public static void flush(Flushable flushable, boolean swallowIOException) throws IOException {
      try {
         flushable.flush();
      } catch (IOException e) {
         if (!swallowIOException) {
            throw e;
         }

         logger.log(Level.WARNING, "IOException thrown while flushing Flushable.", e);
      }
   }

   @Beta
   public static void flushQuietly(Flushable flushable) {
      try {
         flush(flushable, true);
      } catch (IOException e) {
         logger.log(Level.SEVERE, "IOException should not have been thrown.", e);
      }
   }
}
