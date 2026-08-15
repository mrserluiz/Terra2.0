package com.dfsek.terra.lib.commons.lang3.text.translate;

import java.io.IOException;
import java.io.Writer;

@Deprecated
public abstract class CodePointTranslator extends CharSequenceTranslator {
   @Override
   public final int translate(CharSequence input, int index, Writer out) throws IOException {
      int codePoint = Character.codePointAt(input, index);
      boolean consumed = this.translate(codePoint, out);
      return consumed ? 1 : 0;
   }

   public abstract boolean translate(int var1, Writer var2) throws IOException;
}
