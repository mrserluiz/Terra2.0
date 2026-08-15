package com.dfsek.terra.lib.google.common.net;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.escape.UnicodeEscaper;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public final class PercentEscaper extends UnicodeEscaper {
   private static final char[] plusSign = new char[]{'+'};
   private static final char[] upperHexDigits = "0123456789ABCDEF".toCharArray();
   private final boolean plusForSpace;
   private final boolean[] safeOctets;

   public PercentEscaper(String safeChars, boolean plusForSpace) {
      Preconditions.checkNotNull(safeChars);
      if (safeChars.matches(".*[0-9A-Za-z].*")) {
         throw new IllegalArgumentException("Alphanumeric characters are always 'safe' and should not be explicitly specified");
      }

      safeChars = safeChars + "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
      if (plusForSpace && safeChars.contains(" ")) {
         throw new IllegalArgumentException("plusForSpace cannot be specified when space is a 'safe' character");
      }

      this.plusForSpace = plusForSpace;
      this.safeOctets = createSafeOctets(safeChars);
   }

   private static boolean[] createSafeOctets(String safeChars) {
      int maxChar = -1;
      char[] safeCharArray = safeChars.toCharArray();

      for (char c : safeCharArray) {
         maxChar = Math.max(c, maxChar);
      }

      boolean[] octets = new boolean[maxChar + 1];

      for (char c : safeCharArray) {
         octets[c] = true;
      }

      return octets;
   }

   @Override
   protected int nextEscapeIndex(CharSequence csq, int index, int end) {
      Preconditions.checkNotNull(csq);

      while (index < end) {
         char c = csq.charAt(index);
         if (c >= this.safeOctets.length || !this.safeOctets[c]) {
            break;
         }

         index++;
      }

      return index;
   }

   @Override
   public String escape(String s) {
      Preconditions.checkNotNull(s);
      int slen = s.length();

      for (int index = 0; index < slen; index++) {
         char c = s.charAt(index);
         if (c >= this.safeOctets.length || !this.safeOctets[c]) {
            return this.escapeSlow(s, index);
         }
      }

      return s;
   }

   @Override
   protected char @Nullable [] escape(int cp) {
      if (cp < this.safeOctets.length && this.safeOctets[cp]) {
         return null;
      } else if (cp == 32 && this.plusForSpace) {
         return plusSign;
      } else if (cp <= 127) {
         char[] dest = new char[]{'%', '\u0000', upperHexDigits[cp & 15]};
         dest[1] = upperHexDigits[cp >>> 4];
         return dest;
      } else if (cp <= 2047) {
         char[] dest = new char[]{'%', '\u0000', '\u0000', '%', '\u0000', upperHexDigits[cp & 15]};
         cp >>>= 4;
         dest[4] = upperHexDigits[8 | cp & 3];
         cp >>>= 2;
         dest[2] = upperHexDigits[cp & 15];
         cp >>>= 4;
         dest[1] = upperHexDigits[12 | cp];
         return dest;
      } else if (cp <= 65535) {
         char[] dest = new char[9];
         dest[0] = '%';
         dest[1] = 'E';
         dest[3] = '%';
         dest[6] = '%';
         dest[8] = upperHexDigits[cp & 15];
         cp >>>= 4;
         dest[7] = upperHexDigits[8 | cp & 3];
         cp >>>= 2;
         dest[5] = upperHexDigits[cp & 15];
         cp >>>= 4;
         dest[4] = upperHexDigits[8 | cp & 3];
         cp >>>= 2;
         dest[2] = upperHexDigits[cp];
         return dest;
      } else if (cp <= 1114111) {
         char[] dest = new char[12];
         dest[0] = '%';
         dest[1] = 'F';
         dest[3] = '%';
         dest[6] = '%';
         dest[9] = '%';
         dest[11] = upperHexDigits[cp & 15];
         cp >>>= 4;
         dest[10] = upperHexDigits[8 | cp & 3];
         cp >>>= 2;
         dest[8] = upperHexDigits[cp & 15];
         cp >>>= 4;
         dest[7] = upperHexDigits[8 | cp & 3];
         cp >>>= 2;
         dest[5] = upperHexDigits[cp & 15];
         cp >>>= 4;
         dest[4] = upperHexDigits[8 | cp & 3];
         cp >>>= 2;
         dest[2] = upperHexDigits[cp & 7];
         return dest;
      } else {
         throw new IllegalArgumentException("Invalid unicode character value " + cp);
      }
   }
}
