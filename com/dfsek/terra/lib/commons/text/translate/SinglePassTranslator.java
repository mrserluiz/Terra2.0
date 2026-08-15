package com.dfsek.terra.lib.commons.text.translate;

import java.io.IOException;
import java.io.Writer;

abstract class SinglePassTranslator extends CharSequenceTranslator {
   private String getClassName() {
      Class<? extends SinglePassTranslator> clazz = (Class<? extends SinglePassTranslator>)this.getClass();
      return clazz.isAnonymousClass() ? clazz.getName() : clazz.getSimpleName();
   }

   @Override
   public int translate(CharSequence input, int index, Writer writer) throws IOException {
      if (index != 0) {
         throw new IllegalArgumentException(
            this.getClassName() + ".translate(final CharSequence input, final int index, final Writer out) cannot handle a non-zero index."
         );
      }

      this.translateWhole(input, writer);
      return Character.codePointCount(input, index, input.length());
   }

   abstract void translateWhole(CharSequence var1, Writer var2) throws IOException;
}
