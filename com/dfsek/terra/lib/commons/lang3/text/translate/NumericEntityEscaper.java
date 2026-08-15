package com.dfsek.terra.lib.commons.lang3.text.translate;

import java.io.IOException;
import java.io.Writer;

@Deprecated
public class NumericEntityEscaper extends CodePointTranslator {
   private final int below;
   private final int above;
   private final boolean between;

   public static NumericEntityEscaper above(int codePoint) {
      return outsideOf(0, codePoint);
   }

   public static NumericEntityEscaper below(int codePoint) {
      return outsideOf(codePoint, Integer.MAX_VALUE);
   }

   public static NumericEntityEscaper between(int codePointLow, int codePointHigh) {
      return new NumericEntityEscaper(codePointLow, codePointHigh, true);
   }

   public static NumericEntityEscaper outsideOf(int codePointLow, int codePointHigh) {
      return new NumericEntityEscaper(codePointLow, codePointHigh, false);
   }

   public NumericEntityEscaper() {
      this(0, Integer.MAX_VALUE, true);
   }

   private NumericEntityEscaper(int below, int above, boolean between) {
      this.below = below;
      this.above = above;
      this.between = between;
   }

   @Override
   public boolean translate(int codePoint, Writer out) throws IOException {
      if (this.between) {
         if (codePoint < this.below || codePoint > this.above) {
            return false;
         }
      } else if (codePoint >= this.below && codePoint <= this.above) {
         return false;
      }

      out.write("&#");
      out.write(Integer.toString(codePoint, 10));
      out.write(59);
      return true;
   }
}
