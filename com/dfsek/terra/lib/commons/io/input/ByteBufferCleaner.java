package com.dfsek.terra.lib.commons.io.input;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

final class ByteBufferCleaner {
   private static final ByteBufferCleaner.Cleaner INSTANCE = getCleaner();

   static void clean(ByteBuffer buffer) {
      try {
         INSTANCE.clean(buffer);
      } catch (Exception e) {
         throw new IllegalStateException("Failed to clean direct buffer.", e);
      }
   }

   private static ByteBufferCleaner.Cleaner getCleaner() {
      try {
         return new ByteBufferCleaner.Java8Cleaner();
      } catch (Exception e) {
         try {
            return new ByteBufferCleaner.Java9Cleaner();
         } catch (Exception e1) {
            throw new IllegalStateException("Failed to initialize a Cleaner.", e);
         }
      }
   }

   static boolean isSupported() {
      return INSTANCE != null;
   }

   private interface Cleaner {
      void clean(ByteBuffer var1) throws ReflectiveOperationException;
   }

   private static final class Java8Cleaner implements ByteBufferCleaner.Cleaner {
      private final Method cleanerMethod;
      private final Method cleanMethod = Class.forName("sun.misc.Cleaner").getMethod("clean");

      private Java8Cleaner() throws ReflectiveOperationException, SecurityException {
         this.cleanerMethod = Class.forName("sun.nio.ch.DirectBuffer").getMethod("cleaner");
      }

      @Override
      public void clean(ByteBuffer buffer) throws ReflectiveOperationException {
         Object cleaner = this.cleanerMethod.invoke(buffer);
         if (cleaner != null) {
            this.cleanMethod.invoke(cleaner);
         }
      }
   }

   private static final class Java9Cleaner implements ByteBufferCleaner.Cleaner {
      private final Object theUnsafe;
      private final Method invokeCleaner;

      private Java9Cleaner() throws ReflectiveOperationException, SecurityException {
         Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
         Field field = unsafeClass.getDeclaredField("theUnsafe");
         field.setAccessible(true);
         this.theUnsafe = field.get(null);
         this.invokeCleaner = unsafeClass.getMethod("invokeCleaner", ByteBuffer.class);
      }

      @Override
      public void clean(ByteBuffer buffer) throws ReflectiveOperationException {
         this.invokeCleaner.invoke(this.theUnsafe, buffer);
      }
   }
}
