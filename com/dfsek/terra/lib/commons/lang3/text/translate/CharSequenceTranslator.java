package com.dfsek.terra.lib.commons.lang3.text.translate;

import com.dfsek.terra.lib.commons.lang3.ArrayUtils;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Locale;
import java.util.Objects;

@Deprecated
public abstract class CharSequenceTranslator {
   static final char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

   public static String hex(int codePoint) {
      return Integer.toHexString(codePoint).toUpperCase(Locale.ENGLISH);
   }

   public final String translate(CharSequence input) {
      if (input == null) {
         return null;
      }

      try {
         StringWriter writer = new StringWriter(input.length() * 2);
         this.translate(input, writer);
         return writer.toString();
      } catch (IOException ioe) {
         throw new UncheckedIOException(ioe);
      }
   }

   public abstract int translate(CharSequence var1, int var2, Writer var3) throws IOException;

   public final void translate(CharSequence input, Writer writer) throws IOException {
      Objects.requireNonNull(writer, "writer");
      if (input != null) {
         int pos = 0;
         int len = input.length();

         while (pos < len) {
            int consumed = this.translate(input, pos, writer);
            if (consumed == 0) {
               char c1 = input.charAt(pos);
               writer.write(c1);
               if (Character.isHighSurrogate(c1) && ++pos < len) {
                  char c2 = input.charAt(pos);
                  if (Character.isLowSurrogate(c2)) {
                     writer.write(c2);
                     pos++;
                  }
               }
            } else {
               for (int pt = 0; pt < consumed; pt++) {
                  pos += Character.charCount(Character.codePointAt(input, pos));
               }
            }
         }
      }
   }

   public final CharSequenceTranslator with(CharSequenceTranslator... translators) {
      CharSequenceTranslator[] newArray = new CharSequenceTranslator[translators.length + 1];
      newArray[0] = this;
      return new AggregateTranslator(ArrayUtils.arraycopy(translators, 0, newArray, 1, translators.length));
   }
}
