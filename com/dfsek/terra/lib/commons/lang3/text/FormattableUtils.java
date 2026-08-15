package com.dfsek.terra.lib.commons.lang3.text;

import com.dfsek.terra.lib.commons.lang3.ObjectUtils;
import com.dfsek.terra.lib.commons.lang3.Validate;
import java.util.Formattable;
import java.util.Formatter;

@Deprecated
public class FormattableUtils {
   private static final String SIMPLEST_FORMAT = "%s";

   public static Formatter append(CharSequence seq, Formatter formatter, int flags, int width, int precision) {
      return append(seq, formatter, flags, width, precision, ' ', null);
   }

   public static Formatter append(CharSequence seq, Formatter formatter, int flags, int width, int precision, char padChar) {
      return append(seq, formatter, flags, width, precision, padChar, null);
   }

   public static Formatter append(CharSequence seq, Formatter formatter, int flags, int width, int precision, char padChar, CharSequence ellipsis) {
      Validate.isTrue(
         ellipsis == null || precision < 0 || ellipsis.length() <= precision, "Specified ellipsis '%1$s' exceeds precision of %2$s", ellipsis, precision
      );
      StringBuilder buf = new StringBuilder(seq);
      if (precision >= 0 && precision < seq.length()) {
         CharSequence actualEllipsis = ObjectUtils.defaultIfNull(ellipsis, "");
         buf.replace(precision - actualEllipsis.length(), seq.length(), actualEllipsis.toString());
      }

      boolean leftJustify = (flags & 1) == 1;

      for (int i = buf.length(); i < width; i++) {
         buf.insert(leftJustify ? i : 0, padChar);
      }

      formatter.format(buf.toString());
      return formatter;
   }

   public static Formatter append(CharSequence seq, Formatter formatter, int flags, int width, int precision, CharSequence ellipsis) {
      return append(seq, formatter, flags, width, precision, ' ', ellipsis);
   }

   public static String toString(Formattable formattable) {
      return String.format("%s", formattable);
   }
}
