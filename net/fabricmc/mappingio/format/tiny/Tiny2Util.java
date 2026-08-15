package net.fabricmc.mappingio.format.tiny;

import java.io.IOException;
import java.io.Writer;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class Tiny2Util {
   private static final String toEscape = "\\\n\r\u0000\t";
   private static final String escaped = "\\nr0t";
   static final String escapedNamesProperty = "escaped-names";

   private Tiny2Util() {
   }

   public static boolean needEscape(String s) {
      int pos = 0;

      for (int len = s.length(); pos < len; pos++) {
         char c = s.charAt(pos);
         if ("\\\n\r\u0000\t".indexOf(c) >= 0) {
            return true;
         }
      }

      return false;
   }

   public static void writeEscaped(String s, Writer out) throws IOException {
      int len = s.length();
      int start = 0;

      for (int pos = 0; pos < len; pos++) {
         char c = s.charAt(pos);
         int idx = "\\\n\r\u0000\t".indexOf(c);
         if (idx >= 0) {
            out.write(s, start, pos - start);
            out.write(92);
            out.write("\\nr0t".charAt(idx));
            start = pos + 1;
         }
      }

      out.write(s, start, len - start);
   }

   public static String unescape(String str) {
      int pos = str.indexOf(92);
      if (pos < 0) {
         return str;
      }

      StringBuilder ret = new StringBuilder(str.length() - 1);
      int start = 0;

      do {
         ret.append(str, start, pos);
         if (++pos >= str.length()) {
            throw new RuntimeException("incomplete escape sequence at the end");
         }

         int type;
         if ((type = "\\nr0t".indexOf(str.charAt(pos))) < 0) {
            throw new RuntimeException("invalid escape character: \\" + str.charAt(pos));
         }

         ret.append("\\\n\r\u0000\t".charAt(type));
         start = pos + 1;
      } while ((pos = str.indexOf(92, start)) >= 0);

      ret.append(str, start, str.length());
      return ret.toString();
   }
}
