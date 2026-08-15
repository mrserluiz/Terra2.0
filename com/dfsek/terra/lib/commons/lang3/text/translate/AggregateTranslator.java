package com.dfsek.terra.lib.commons.lang3.text.translate;

import com.dfsek.terra.lib.commons.lang3.ArrayUtils;
import java.io.IOException;
import java.io.Writer;

@Deprecated
public class AggregateTranslator extends CharSequenceTranslator {
   private final CharSequenceTranslator[] translators;

   public AggregateTranslator(CharSequenceTranslator... translators) {
      this.translators = ArrayUtils.clone(translators);
   }

   @Override
   public int translate(CharSequence input, int index, Writer out) throws IOException {
      for (CharSequenceTranslator translator : this.translators) {
         int consumed = translator.translate(input, index, out);
         if (consumed != 0) {
            return consumed;
         }
      }

      return 0;
   }
}
