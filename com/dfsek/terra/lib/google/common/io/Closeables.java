package com.dfsek.terra.lib.google.common.io;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
public final class Closeables {
   @VisibleForTesting
   static final Logger logger = Logger.getLogger(Closeables.class.getName());

   private Closeables() {
   }

   public static void close(@Nullable Closeable closeable, boolean swallowIOException) throws IOException {
      if (closeable != null) {
         try {
            closeable.close();
         } catch (IOException e) {
            if (!swallowIOException) {
               throw e;
            }

            logger.log(Level.WARNING, "IOException thrown while closing Closeable.", e);
         }
      }
   }

   public static void closeQuietly(@Nullable InputStream inputStream) {
      try {
         close(inputStream, true);
      } catch (IOException impossible) {
         throw new AssertionError(impossible);
      }
   }

   public static void closeQuietly(@Nullable Reader reader) {
      try {
         close(reader, true);
      } catch (IOException impossible) {
         throw new AssertionError(impossible);
      }
   }
}
