package com.dfsek.terra.lib.commons.io;

import com.dfsek.terra.lib.commons.io.output.CloseShieldOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Objects;

public class HexDump {
   @Deprecated
   public static final String EOL = System.lineSeparator();
   private static final char[] HEX_CODES = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
   private static final int[] SHIFTS = new int[]{28, 24, 20, 16, 12, 8, 4, 0};

   public static void dump(byte[] data, Appendable appendable) throws IOException {
      dump(data, 0L, appendable, 0, data.length);
   }

   public static void dump(byte[] data, long offset, Appendable appendable, int index, int length) throws IOException, ArrayIndexOutOfBoundsException {
      Objects.requireNonNull(appendable, "appendable");
      if (index >= 0 && index < data.length) {
         long display_offset = offset + index;
         StringBuilder buffer = new StringBuilder(74);
         if (length >= 0 && index + length <= data.length) {
            int endIndex = index + length;

            for (int j = index; j < endIndex; j += 16) {
               int chars_read = endIndex - j;
               if (chars_read > 16) {
                  chars_read = 16;
               }

               dump(buffer, display_offset).append(' ');

               for (int k = 0; k < 16; k++) {
                  if (k < chars_read) {
                     dump(buffer, data[k + j]);
                  } else {
                     buffer.append("  ");
                  }

                  buffer.append(' ');
               }

               for (int k = 0; k < chars_read; k++) {
                  if (data[k + j] >= 32 && data[k + j] < 127) {
                     buffer.append((char)data[k + j]);
                  } else {
                     buffer.append('.');
                  }
               }

               buffer.append(System.lineSeparator());
               appendable.append(buffer);
               buffer.setLength(0);
               display_offset += chars_read;
            }
         } else {
            throw new ArrayIndexOutOfBoundsException(String.format("Range [%s, %<s + %s) out of bounds for length %s", index, length, data.length));
         }
      } else {
         throw new ArrayIndexOutOfBoundsException("illegal index: " + index + " into array of length " + data.length);
      }
   }

   public static void dump(byte[] data, long offset, OutputStream stream, int index) throws IOException, ArrayIndexOutOfBoundsException {
      Objects.requireNonNull(stream, "stream");
      OutputStreamWriter out = new OutputStreamWriter(CloseShieldOutputStream.wrap(stream), Charset.defaultCharset());

      try {
         dump(data, offset, out, index, data.length - index);
      } catch (Throwable var9) {
         try {
            out.close();
         } catch (Throwable var8) {
            var9.addSuppressed(var8);
         }

         throw var9;
      }

      out.close();
   }

   private static StringBuilder dump(StringBuilder builder, byte value) {
      for (int j = 0; j < 2; j++) {
         builder.append(HEX_CODES[value >> SHIFTS[j + 6] & 15]);
      }

      return builder;
   }

   private static StringBuilder dump(StringBuilder builder, long value) {
      for (int j = 0; j < 8; j++) {
         builder.append(HEX_CODES[(int)(value >> SHIFTS[j]) & 15]);
      }

      return builder;
   }
}
