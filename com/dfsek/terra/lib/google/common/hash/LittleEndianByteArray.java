package com.dfsek.terra.lib.google.common.hash;

import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import com.dfsek.terra.lib.google.common.primitives.Longs;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.nio.ByteOrder;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import sun.misc.Unsafe;

final class LittleEndianByteArray {
   private static final LittleEndianByteArray.LittleEndianBytes byteArray = makeGetter();

   static long load64(byte[] input, int offset) {
      assert input.length >= offset + 8;
      return byteArray.getLongLittleEndian(input, offset);
   }

   static long load64Safely(byte[] input, int offset, int length) {
      long result = 0L;
      int limit = Math.min(length, 8);

      for (int i = 0; i < limit; i++) {
         result |= (input[offset + i] & 255L) << i * 8;
      }

      return result;
   }

   static void store64(byte[] sink, int offset, long value) {
      assert offset >= 0 && offset + 8 <= sink.length;
      byteArray.putLongLittleEndian(sink, offset, value);
   }

   static int load32(byte[] source, int offset) {
      return source[offset] & 0xFF | (source[offset + 1] & 0xFF) << 8 | (source[offset + 2] & 0xFF) << 16 | (source[offset + 3] & 0xFF) << 24;
   }

   static boolean usingFastPath() {
      return byteArray.usesFastPath();
   }

   static LittleEndianByteArray.LittleEndianBytes makeGetter() {
      LittleEndianByteArray.LittleEndianBytes usingVarHandle = LittleEndianByteArray.VarHandleLittleEndianBytesMaker.INSTANCE
         .tryMakeVarHandleLittleEndianBytes();
      if (usingVarHandle != null) {
         return usingVarHandle;
      }

      try {
         String arch = System.getProperty("os.arch");
         if (Objects.equals(arch, "amd64") || Objects.equals(arch, "aarch64")) {
            return ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)
               ? LittleEndianByteArray.UnsafeByteArray.UNSAFE_LITTLE_ENDIAN
               : LittleEndianByteArray.UnsafeByteArray.UNSAFE_BIG_ENDIAN;
         }
      } catch (Throwable var2) {
      }

      return LittleEndianByteArray.JavaLittleEndianBytes.INSTANCE;
   }

   private LittleEndianByteArray() {
   }

   private enum JavaLittleEndianBytes implements LittleEndianByteArray.LittleEndianBytes {
      INSTANCE {
         @Override
         public long getLongLittleEndian(byte[] source, int offset) {
            return Longs.fromBytes(
               source[offset + 7],
               source[offset + 6],
               source[offset + 5],
               source[offset + 4],
               source[offset + 3],
               source[offset + 2],
               source[offset + 1],
               source[offset]
            );
         }

         @Override
         public void putLongLittleEndian(byte[] sink, int offset, long value) {
            long mask = 255L;

            for (int i = 0; i < 8; i++) {
               sink[offset + i] = (byte)((value & mask) >> i * 8);
               mask <<= 8;
            }
         }

         @Override
         public boolean usesFastPath() {
            return false;
         }
      };

      JavaLittleEndianBytes() {
      }
   }

   private interface LittleEndianBytes {
      long getLongLittleEndian(byte[] array, int offset);

      void putLongLittleEndian(byte[] array, int offset, long value);

      boolean usesFastPath();
   }

   @VisibleForTesting
   enum UnsafeByteArray implements LittleEndianByteArray.LittleEndianBytes {
      UNSAFE_LITTLE_ENDIAN {
         @Override
         public long getLongLittleEndian(byte[] array, int offset) {
            return LittleEndianByteArray.UnsafeByteArray.theUnsafe.getLong(array, (long)offset + LittleEndianByteArray.UnsafeByteArray.BYTE_ARRAY_BASE_OFFSET);
         }

         @Override
         public void putLongLittleEndian(byte[] array, int offset, long value) {
            LittleEndianByteArray.UnsafeByteArray.theUnsafe.putLong(array, (long)offset + LittleEndianByteArray.UnsafeByteArray.BYTE_ARRAY_BASE_OFFSET, value);
         }
      },
      UNSAFE_BIG_ENDIAN {
         @Override
         public long getLongLittleEndian(byte[] array, int offset) {
            long bigEndian = LittleEndianByteArray.UnsafeByteArray.theUnsafe
               .getLong(array, (long)offset + LittleEndianByteArray.UnsafeByteArray.BYTE_ARRAY_BASE_OFFSET);
            return Long.reverseBytes(bigEndian);
         }

         @Override
         public void putLongLittleEndian(byte[] array, int offset, long value) {
            long littleEndianValue = Long.reverseBytes(value);
            LittleEndianByteArray.UnsafeByteArray.theUnsafe
               .putLong(array, (long)offset + LittleEndianByteArray.UnsafeByteArray.BYTE_ARRAY_BASE_OFFSET, littleEndianValue);
         }
      };

      private static final Unsafe theUnsafe = getUnsafe();
      private static final int BYTE_ARRAY_BASE_OFFSET = theUnsafe.arrayBaseOffset(byte[].class);

      UnsafeByteArray() {
      }

      @Override
      public boolean usesFastPath() {
         return true;
      }

      private static Unsafe getUnsafe() {
         try {
            return Unsafe.getUnsafe();
         } catch (SecurityException var2) {
            try {
               return AccessController.doPrivileged(() -> {
                  Class<Unsafe> k = Unsafe.class;

                  for (Field f : k.getDeclaredFields()) {
                     f.setAccessible(true);
                     Object x = f.get(null);
                     if (k.isInstance(x)) {
                        return k.cast(x);
                     }
                  }

                  throw new NoSuchFieldError("the Unsafe");
               });
            } catch (PrivilegedActionException e) {
               throw new RuntimeException("Could not initialize intrinsics", e.getCause());
            }
         }
      }

      static {
         if (theUnsafe.arrayIndexScale(byte[].class) != 1) {
            throw new AssertionError();
         }
      }
   }

   @IgnoreJRERequirement
   private enum VarHandleLittleEndianBytes implements LittleEndianByteArray.LittleEndianBytes {
      INSTANCE {
         @Override
         public long getLongLittleEndian(byte[] array, int offset) {
            return (long)HANDLE.get((byte[])array, (int)offset);
         }

         @Override
         public void putLongLittleEndian(byte[] array, int offset, long value) {
            HANDLE.set((byte[])array, (int)offset, (long)value);
         }
      };

      static final VarHandle HANDLE = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN);

      VarHandleLittleEndianBytes() {
      }

      @Override
      public boolean usesFastPath() {
         return true;
      }
   }

   private enum VarHandleLittleEndianBytesMaker {
      INSTANCE {
         @Override
         LittleEndianByteArray.@Nullable LittleEndianBytes tryMakeVarHandleLittleEndianBytes() {
            try {
               Class.forName("java.lang.invoke.VarHandle");
            } catch (ClassNotFoundException beforeJava9) {
               return null;
            }

            return LittleEndianByteArray.VarHandleLittleEndianBytes.INSTANCE;
         }
      };

      VarHandleLittleEndianBytesMaker() {
      }

      LittleEndianByteArray.@Nullable LittleEndianBytes tryMakeVarHandleLittleEndianBytes() {
         return null;
      }
   }
}
