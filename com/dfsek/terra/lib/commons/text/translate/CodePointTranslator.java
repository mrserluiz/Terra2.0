package com.dfsek.terra.lib.commons.text.translate;

import java.io.IOException;
import java.io.Writer;

public abstract class CodePointTranslator extends CharSequenceTranslator {
   @Override
   public final int translate(CharSequence input, int index, Writer writer) throws IOException {
      int codePoint = Character.codePointAt(input, index);
      boolean consumed = this.translate(codePoint, writer);
      return consumed ? 1 : 0;
   }

   public abstract boolean translate(int var1, Writer var2) throws IOException;
}
